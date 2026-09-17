import os
import re
import logging
from urllib.parse import urlparse
from typing import List, Dict, Any, Optional
import requests
import joblib
import numpy as np

from app.ml.feature_extractors import (
    extract_url_features,
    explain_url_features,
    extract_sms_heuristic_features,
    explain_sms_features,
    extract_apk_permission_vector,
    explain_apk_permissions,
    SHORTENER_DOMAINS,
    SUSPICIOUS_TLDS,
    SCAM_SMS_KEYWORDS
)

logger = logging.getLogger("sentinel.ml_engine")
MODELS_DIR = os.path.join(os.path.dirname(os.path.dirname(__file__)), "ml", "saved_models")


class SentinelMLEngine:
    """
    Production-grade AI Threat Intelligence Engine for Sentinel AI.
    Combines trained Scikit-Learn machine learning pipelines with live threat intelligence feeds
    (AlienVault OTX & URLScan.io) and fail-safe heuristic safeguards.
    """

    def __init__(self):
        # Load API keys from environment
        self.otx_key = os.getenv("OTX_API_KEY", "6a922e6db6a8ab67f8fe2a632cc09290c0f7a3c507055f0cbcddf0cb2414dbd1")
        self.urlscan_key = os.getenv("URLSCAN_API_KEY", "019e7c24-98a2-763a-af70-ace24a80b96b")

        # Models storage
        self.url_model = None
        self.sms_model = None
        self.apk_model = None

        self._load_models()

    def _load_models(self):
        """Loads serialized Scikit-Learn models from saved_models directory with safe fallbacks."""
        # 1. URL Phishing Detector
        url_path = os.path.join(MODELS_DIR, "url_phishing_model.joblib")
        if os.path.exists(url_path):
            try:
                self.url_model = joblib.load(url_path)
                logger.info("[ML Engine] URL Phishing Random Forest model loaded.")
            except Exception as e:
                logger.warning(f"[ML Engine] Failed to load URL model ({e}), using heuristic fallback.")

        # 2. SMS Fraud NLP Classifier
        sms_path = os.path.join(MODELS_DIR, "sms_fraud_model.joblib")
        if os.path.exists(sms_path):
            try:
                self.sms_model = joblib.load(sms_path)
                logger.info("[ML Engine] SMS Fraud NLP pipeline loaded.")
            except Exception as e:
                logger.warning(f"[ML Engine] Failed to load SMS model ({e}), using heuristic fallback.")

        # 3. Android APK Malware Analyzer
        apk_path = os.path.join(MODELS_DIR, "apk_malware_model.joblib")
        if os.path.exists(apk_path):
            try:
                self.apk_model = joblib.load(apk_path)
                logger.info("[ML Engine] APK Malware Classifier model loaded.")
            except Exception as e:
                logger.warning(f"[ML Engine] Failed to load APK model ({e}), using heuristic fallback.")

    # ========================================================================
    # Threat Intelligence Feeds (OTX & URLScan)
    # ========================================================================

    def query_otx_threat_intel(self, domain: str) -> Dict[str, Any]:
        """Queries AlienVault OTX for domain reputation indicators."""
        if not self.otx_key:
            return {"malicious": False, "details": []}

        try:
            url = f"https://otx.alienvault.com/api/v1/indicators/domain/{domain}/general"
            headers = {"X-OTX-API-KEY": self.otx_key}
            response = requests.get(url, headers=headers, timeout=2.5)
            if response.status_code == 200:
                data = response.json()
                pulse_info = data.get("pulse_info", {})
                pulses = pulse_info.get("pulses", [])
                if pulses:
                    malicious_pulses = len(pulses)
                    tags = []
                    for p in pulses[:3]:
                        tags.extend(p.get("tags", []))
                    tags = list(set(tags))[:5]
                    tag_str = f" (Tags: {', '.join(tags)})" if tags else ""
                    return {
                        "malicious": True,
                        "details": [f"AlienVault OTX: Flagged in {malicious_pulses} malicious threat pulses{tag_str}"]
                    }
        except Exception:
            pass
        return {"malicious": False, "details": []}

    def query_urlscan_threat_intel(self, domain: str) -> Dict[str, Any]:
        """Queries URLScan.io search API for previous historical scans of the domain."""
        if not self.urlscan_key:
            return {"malicious": False, "details": []}

        try:
            url = f"https://urlscan.io/api/v1/search/?q=domain:{domain}"
            headers = {"API-Key": self.urlscan_key}
            response = requests.get(url, headers=headers, timeout=2.5)
            if response.status_code == 200:
                data = response.json()
                results = data.get("results", [])
                if results:
                    malicious_count = 0
                    for r in results[:5]:
                        verdicts = r.get("verdicts", {})
                        overall = verdicts.get("overall", {})
                        if overall.get("malicious") or overall.get("score", 0) > 60:
                            malicious_count += 1

                    if malicious_count > 0:
                        return {
                            "malicious": True,
                            "details": [f"URLScan.io: Identified in {malicious_count} historically malicious threat records"]
                        }
        except Exception:
            pass
        return {"malicious": False, "details": []}

    # ========================================================================
    # 1. URL PHISHING ANALYSIS
    # ========================================================================

    def analyze_url(self, url: str) -> Dict[str, Any]:
        """
        Analyzes a URL using the trained Scikit-Learn Random Forest Classifier,
        combines with live AlienVault OTX & URLScan.io intelligence, and returns risk breakdown.
        """
        if not url:
            return {"url": "", "status": "Safe", "score": 5.0, "details": ["Empty URL string"]}

        if not url.startswith(("http://", "https://")):
            url_norm = "https://" + url
        else:
            url_norm = url

        try:
            parsed = urlparse(url_norm)
            domain = parsed.netloc.split(":")[0] if ":" in parsed.netloc else parsed.netloc
        except Exception:
            domain = ""

        domain_lower = domain.lower()
        url_lower = url_norm.lower()

        # Whitelist of trusted high-reputation domains (instant sub-millisecond return)
        SAFE_DOMAINS = {
            "google.com", "www.google.com", "github.com", "www.github.com",
            "wikipedia.org", "en.wikipedia.org", "microsoft.com", "www.microsoft.com",
            "apple.com", "www.apple.com", "amazon.com", "www.amazon.com",
            "paypal.com", "www.paypal.com", "chase.com", "www.chase.com",
            "netflix.com", "www.netflix.com", "youtube.com", "www.youtube.com",
            "twitter.com", "x.com", "linkedin.com", "facebook.com", "instagram.com",
            "reddit.com", "railway.app", "railway.com", "firebase.google.com"
        }
        if domain_lower in SAFE_DOMAINS and "@" not in url_norm and "//" not in parsed.path:
            return {
                "url": url,
                "status": "Safe",
                "score": 5.0,
                "details": ["Verified legitimate global authority domain name"]
            }

        # Extract features and explanations
        features = extract_url_features(url_norm)
        details = explain_url_features(url_norm)

        # ML Model Inference
        ml_score = None
        if self.url_model is not None:
            try:
                feature_arr = np.array([features], dtype=np.float64)
                probs = self.url_model.predict_proba(feature_arr)[0]
                phishing_prob = probs[1] * 100.0
                ml_score = phishing_prob
            except Exception as e:
                logger.error(f"[ML Engine] URL model inference error: {e}")

        # Fallback heuristic scoring if model not loaded or error
        if ml_score is None:
            ml_score = 15.0
            if len(url_norm) > 75:
                ml_score += 20.0
            if any(s in domain for s in SHORTENER_DOMAINS):
                ml_score += 25.0
            if "@" in url_norm:
                ml_score += 25.0
            if url_norm.count("//") > 1:
                ml_score += 20.0
            if "-" in domain:
                ml_score += 15.0

        # Query Live Threat Intelligence
        if domain:
            try:
                otx_result = self.query_otx_threat_intel(domain)
                if otx_result["malicious"]:
                    ml_score = max(ml_score, 80.0)
                    details.extend(otx_result["details"])
            except Exception:
                pass

            try:
                urlscan_result = self.query_urlscan_threat_intel(domain)
                if urlscan_result["malicious"]:
                    ml_score = max(ml_score, 85.0)
                    details.extend(urlscan_result["details"])
            except Exception:
                pass

        # Deterministic Phishing Signatures & High-Precision Heuristics
        domain_lower = domain.lower()
        url_lower = url_norm.lower()

        # 1. Whitelist of trusted high-reputation domains (prevents false positives)
        SAFE_DOMAINS = {
            "google.com", "www.google.com", "github.com", "www.github.com",
            "wikipedia.org", "en.wikipedia.org", "microsoft.com", "www.microsoft.com",
            "apple.com", "www.apple.com", "amazon.com", "www.amazon.com",
            "paypal.com", "www.paypal.com", "chase.com", "www.chase.com",
            "netflix.com", "www.netflix.com", "youtube.com", "www.youtube.com",
            "twitter.com", "x.com", "linkedin.com", "facebook.com", "instagram.com",
            "reddit.com", "railway.app", "railway.com", "firebase.google.com"
        }
        is_whitelisted = domain_lower in SAFE_DOMAINS and "@" not in url_norm and "//" not in parsed.path

        # 2. Targeted Brands Spoofing & Impersonation
        BRAND_OFFICIAL_MAP = {
            "paypal": ["paypal.com"],
            "apple": ["apple.com", "icloud.com"],
            "chase": ["chase.com"],
            "netflix": ["netflix.com"],
            "wellsfargo": ["wellsfargo.com"],
            "bankofamerica": ["bankofamerica.com"],
            "binance": ["binance.com"],
            "coinbase": ["coinbase.com"],
            "metamask": ["metamask.io"],
            "steam": ["steampowered.com", "steamcommunity.com"],
            "amazon": ["amazon.com"],
            "microsoft": ["microsoft.com", "live.com", "office.com"],
            "google": ["google.com", "youtube.com"]
        }

        brand_spoofed = False
        for brand, official_domains in BRAND_OFFICIAL_MAP.items():
            if brand in url_lower:
                is_official = any(domain_lower == od or domain_lower.endswith("." + od) for od in official_domains)
                if not is_official:
                    brand_spoofed = True
                    ml_score = max(ml_score, 96.0)
                    details.insert(0, f"Brand Impersonation: Detected spoofed brand '{brand.capitalize()}' on domain '{domain}'")
                    break

        # 3. Numeric IPv4 / IPv6 host
        ip_pattern = r"^(?:\d{1,3}\.){3}\d{1,3}$|^(?:[0-9a-fA-F]{1,4}:){1,7}[0-9a-fA-F]{1,4}$"
        if re.match(ip_pattern, domain):
            ml_score = max(ml_score, 93.5)
            details.insert(0, f"Bare IP Address Host: URL uses direct IP '{domain}' instead of a registered domain name")

        # 4. IDN Homograph Punycode attack
        if "xn--" in domain_lower:
            ml_score = max(ml_score, 95.0)
            details.insert(0, "Punycode Homograph Attack: Host contains encoded 'xn--' characters spoofing brand lookalikes")

        # 5. High-Abuse TLD with Phishing Lures
        tld = domain_lower.split(".")[-1] if "." in domain_lower else ""
        if tld in SUSPICIOUS_TLDS:
            has_lure = any(kw in url_lower for kw in ["login", "signin", "verify", "secure", "account", "update", "bank", "wallet", "claim", "prize", "auth", "kyc", "alert"])
            if has_lure:
                ml_score = max(ml_score, 94.0)
                details.insert(0, f"Suspicious TLD Abuse: High-abuse '.{tld}' domain paired with authentication/credential lure keywords")

        # 6. Ephemeral Free Tunneling / Forwarding Abuse
        TUNNEL_SERVICES = ["ngrok-free.app", "ngrok.io", "loca.lt", "trycloudflare.com", "glitch.me", "pagekite.me"]
        if any(ts in domain_lower for ts in TUNNEL_SERVICES):
            has_lure = any(kw in url_lower for kw in ["login", "signin", "verify", "secure", "bank", "account", "update"])
            if has_lure:
                ml_score = max(ml_score, 92.5)
                details.insert(0, f"Abused Cloud Tunnel: Ephemeral tunnel provider '{domain}' hosting credential phishing interface")

        # 7. Subdomain Stacking with Credential Keywords
        if domain_lower.count(".") >= 3 and any(kw in url_lower for kw in ["login", "signin", "verify", "bank"]):
            ml_score = max(ml_score, 88.0)
            details.append("Excessive Subdomains: Host contains 4+ domain levels attempting visual authority masking")

        # Apply whitelist suppression
        if is_whitelisted and not brand_spoofed:
            final_score = 5.0
            status = "Safe"
            details = ["Verified legitimate global authority domain name"]
        else:
            final_score = round(min(max(ml_score, 5.0), 99.0), 1)
            if final_score < 40.0:
                status = "Safe"
            elif final_score < 70.0:
                status = "Suspicious"
            else:
                status = "Phishing"

        return {
            "url": url,
            "status": status,
            "score": final_score,
            "details": details if details else ["URL exhibits benign baseline characteristics"]
        }

    # ========================================================================
    # 2. SMS / EMAIL SCAM NLP ANALYSIS
    # ========================================================================

    def analyze_sms_or_email(self, text: str) -> Dict[str, Any]:
        """
        Analyzes SMS or email text for scam likelihood using the trained NLP pipeline
        (TF-IDF word/char n-grams + dense heuristic triggers) and returns probability & trigger explanation.
        """
        if not text or not text.strip():
            return {
                "original_text": text or "",
                "scam_probability": 5.0,
                "classification": "Normal / Safe",
                "explanation": "No text provided for analysis.",
                "contains_link": False
            }

        reasons, has_link = explain_sms_features(text)
        scam_prob = None

        # ML Model Inference
        if self.sms_model is not None:
            try:
                probs = self.sms_model.predict_proba([text])[0]
                scam_prob = probs[1] * 100.0
            except Exception as e:
                logger.error(f"[ML Engine] SMS NLP inference error: {e}")

        # Fallback heuristic calculation
        if scam_prob is None:
            text_lower = text.lower()
            heuristic_score = 5.0
            for kw, weight in SCAM_SMS_KEYWORDS.items():
                if kw in text_lower:
                    heuristic_score += weight * 30.0
            if has_link:
                heuristic_score += 20.0
            scam_prob = heuristic_score

        final_prob = round(min(max(scam_prob, 5.0), 99.0), 1)

        if final_prob < 35.0:
            classification = "Normal / Safe"
        elif final_prob < 70.0:
            classification = "Spam / Suspicious"
        else:
            classification = "Highly Likely Scam / Phishing"

        explanation = f"Detected risk factors: {', '.join(reasons)}." if reasons else "No risk indicators detected."

        return {
            "original_text": text,
            "scam_probability": final_prob,
            "classification": classification,
            "explanation": explanation,
            "contains_link": has_link
        }

    # ========================================================================
    # 3. ANDROID APK PERMISSION MALWARE ANALYSIS
    # ========================================================================

    def analyze_apk_metadata(self, package_name: str, app_name: str, permissions: List[str]) -> Dict[str, Any]:
        """
        Evaluates Android APK permissions using the trained combinatorial Random Forest
        malware classifier and synergy threat pattern analysis.
        """
        if not permissions:
            return {
                "package_name": package_name,
                "app_name": app_name,
                "malware_score": 5.0,
                "threat_category": "Clean / Legitimate Utility App",
                "flagged_permissions": [],
                "total_permissions_scanned": 0,
                "status": "Safe"
            }

        flagged_perms, detected_category = explain_apk_permissions(permissions)
        ml_score = None

        # ML Model Inference
        if self.apk_model is not None:
            try:
                feat_vec = extract_apk_permission_vector(permissions)
                probs = self.apk_model.predict_proba([feat_vec])[0]
                ml_score = probs[1] * 100.0
            except Exception as e:
                logger.error(f"[ML Engine] APK model inference error: {e}")

        # Fallback calculation
        if ml_score is None:
            base_score = 10.0 + (len(flagged_perms) * 20.0)
            ml_score = base_score

        final_score = round(min(max(ml_score, 5.0), 99.0), 1)

        if final_score < 35.0:
            status = "Safe"
        elif final_score < 65.0:
            status = "Suspicious"
        else:
            status = "High Threat"

        return {
            "package_name": package_name,
            "app_name": app_name,
            "malware_score": final_score,
            "threat_category": detected_category,
            "flagged_permissions": flagged_perms,
            "total_permissions_scanned": len(permissions),
            "status": status
        }
