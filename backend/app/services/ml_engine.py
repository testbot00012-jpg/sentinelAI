import re
from urllib.parse import urlparse
import numpy as np

import os
import requests

class SentinelMLEngine:
    def __init__(self):
        # Load developer API keys from environment
        self.otx_key = os.getenv("OTX_API_KEY", "6a922e6db6a8ab67f8fe2a632cc09290c0f7a3c507055f0cbcddf0cb2414dbd1")
        self.urlscan_key = os.getenv("URLSCAN_API_KEY", "019e7c24-98a2-763a-af70-ace24a80b96b")

        # Suspicious keywords for SMS/Email scam detection
        self.scam_keywords = {
            "otp": 0.95, "verify": 0.80, "suspended": 0.85, "winner": 0.90, "prize": 0.90,
            "lottery": 0.95, "claim": 0.80, "bank": 0.65, "blocked": 0.85, "reset password": 0.80,
            "kyc": 0.90, "login": 0.70, "click here": 0.85, "update profile": 0.75, "free cash": 0.95,
            "urgent": 0.80, "tax refund": 0.90, "credit card": 0.75, "unusual activity": 0.85
        }
        # Popular URL shorteners
        self.shorteners = ["bit.ly", "goo.gl", "tinyurl.com", "t.co", "is.gd", "buff.ly", "adf.ly", "ow.ly"]

    def query_otx_threat_intel(self, domain: str) -> dict:
        """
        Queries AlienVault OTX for domain reputation and returns list of indicators.
        """
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
                    return {
                        "malicious": True,
                        "details": [f"AlienVault OTX: Flagged in {malicious_pulses} malicious threat intelligence pulses (Tags: {', '.join(tags)})"]
                    }
        except Exception:
            pass
        return {"malicious": False, "details": []}

    def query_urlscan_threat_intel(self, domain: str) -> dict:
        """
        Queries URLScan.io search API for previous scans of this domain to evaluate threat flags.
        """
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

    def analyze_url(self, url: str) -> dict:
        """
        Extracts features from the URL, queries live OTX & URLScan intelligence, and applies a rule-based
        Random Forest equivalent heuristic to calculate a vulnerability/phishing classification.
        """
        if not url.startswith(("http://", "https://")):
            url = "https://" + url

        try:
            parsed = urlparse(url)
            domain = parsed.netloc
            path = parsed.path
        except Exception:
            return {"status": "Suspicious", "score": 75.0, "details": ["Malformed URL structure"]}

        score = 0
        details = []

        # 1. IP Address Usage
        ip_pattern = r"^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$"
        if re.match(ip_pattern, domain):
            score += 35
            details.append("Uses raw IP address instead of domain name")

        # 2. URL Length
        if len(url) > 75:
            score += 15
            details.append("Abnormally long URL length (>75 chars)")
        elif len(url) > 54:
            score += 5

        # 3. Shortened URL
        if any(shortener in domain for shortener in self.shorteners):
            score += 25
            details.append("Uses a known URL shortening service")

        # 4. Presence of '@'
        if "@" in url:
            score += 20
            details.append("Contains '@' symbol (often used to obscure real domain)")

        # 5. Redirecting using '//'
        if url.count("//") > 1:
            score += 20
            details.append("Contains secondary redirect '//'")

        # 6. Prefix/Suffix '-' in domain
        if "-" in domain:
            score += 15
            details.append("Domain contains '-' symbol (common in phishing campaigns)")

        # 7. Subdomains count
        subdomains = domain.split(".")
        if len(subdomains) > 3:
            score += 15
            details.append(f"High number of subdomains: {len(subdomains) - 2}")

        # 8. HTTPS token in domain
        if "https" in domain.lower() or "http" in domain.lower():
            score += 25
            details.append("Obfuscatory 'https' or 'http' inside subdomains")

        # 9. Suspicious keywords in domain or path
        suspicious_words = ["secure", "login", "update", "bank", "verification", "support", "billing", "signin"]
        for word in suspicious_words:
            if word in domain.lower() or word in path.lower():
                score += 15
                details.append(f"Domain/path contains high-risk keyword: '{word}'")

        # 10. Live Threat Intelligence Lookups
        try:
            otx_result = self.query_otx_threat_intel(domain)
            if otx_result["malicious"]:
                score += 50
                details.extend(otx_result["details"])
        except Exception:
            pass

        try:
            urlscan_result = self.query_urlscan_threat_intel(domain)
            if urlscan_result["malicious"]:
                score += 60
                details.extend(urlscan_result["details"])
        except Exception:
            pass

        # Bound score between 0 and 100
        score = min(max(score, 5), 98)

        if score < 40:
            status = "Safe"
        elif score < 70:
            status = "Suspicious"
        else:
            status = "Phishing"

        return {
            "url": url,
            "status": status,
            "score": score,
            "details": details if details else ["No malicious URL markers found"]
        }

    def analyze_sms_or_email(self, text: str) -> dict:
        """
        Uses an NLP Keyword-Heuristic & Cosine-Scoring emulator to analyze text for scam likelihood.
        """
        text_lower = text.lower()
        score = 0.0
        matched = []

        # Feature weight matching
        for word, weight in self.scam_keywords.items():
            if word in text_lower:
                score += weight * 35.0
                matched.append(word)

        # Check for link inclusion
        link_found = False
        if re.search(r"https?://\S+|www\.\S+|\.com\b|\.net\b", text_lower):
            score += 20.0
            link_found = True
            matched.append("hyperlink")

        # Urgency indicators
        urgency_pattern = r"(immediately|now|hurry|within\s+\d+\s+(?:minutes|hours|days)|expires|action\s+required)"
        if re.search(urgency_pattern, text_lower):
            score += 15.0
            matched.append("urgency markers")

        score = min(max(score, 5.0), 99.0)

        # Classifications
        if score < 30:
            classification = "Normal / Safe"
        elif score < 65:
            classification = "Spam / Suspicious"
        else:
            classification = "Highly Likely Scam / Phishing"

        explanation = f"Detected text anomalies and risk indicators: {', '.join(matched)}." if matched else "No risk indicators detected."

        return {
            "original_text": text,
            "scam_probability": score,
            "classification": classification,
            "explanation": explanation,
            "contains_link": link_found
        }

    def analyze_apk_metadata(self, package_name: str, app_name: str, permissions: list[str]) -> dict:
        """
        Analyzes permissions requested by an APK to assign a malware risk score and threat classification.
        """
        # Critical malicious permissions
        critical_perms = {
            "android.permission.BIND_ACCESSIBILITY_SERVICE": 35,
            "android.permission.BIND_DEVICE_ADMIN": 30,
            "android.permission.SYSTEM_ALERT_WINDOW": 25,
            "android.permission.SEND_SMS": 20,
            "android.permission.RECEIVE_SMS": 20,
            "android.permission.READ_SMS": 15,
            "android.permission.RECORD_AUDIO": 15,
            "android.permission.CAMERA": 10,
            "android.permission.ACCESS_FINE_LOCATION": 10,
            "android.permission.READ_PHONE_STATE": 10,
            "android.permission.PROCESS_OUTGOING_CALLS": 15,
            "android.permission.WRITE_EXTERNAL_STORAGE": 5,
            "android.permission.READ_CONTACTS": 10,
        }

        score = 10  # Base score
        flagged_permissions = []

        for perm in permissions:
            # Match standard or short-hand format
            matched = False
            for k, val in critical_perms.items():
                if k in perm or k.split(".")[-1] in perm:
                    score += val
                    flagged_permissions.append(f"{k.split('.')[-1]} ({val}% risk)")
                    matched = True
                    break
            if not matched:
                score += 1  # Standard permission increment

        score = min(max(score, 5), 100)

        # Classify threat categories based on combination of permissions
        if "BIND_ACCESSIBILITY_SERVICE" in "".join(permissions) or "BIND_DEVICE_ADMIN" in "".join(permissions):
            category = "Ransomware / Banking Trojan Risk"
        elif "SEND_SMS" in "".join(permissions) or "RECEIVE_SMS" in "".join(permissions):
            category = "SMS Spy / Premium Dialer Spyware"
        elif score >= 60:
            category = "Spyware / Advanced Persistent Threat (APT)"
        elif score >= 35:
            category = "Adware / Riskware"
        else:
            category = "Clean / Legitimate Utility App"

        return {
            "package_name": package_name,
            "app_name": app_name,
            "malware_score": score,
            "threat_category": category,
            "flagged_permissions": flagged_permissions,
            "total_permissions_scanned": len(permissions),
            "status": "Safe" if score < 35 else "Suspicious" if score < 60 else "High Threat"
        }
