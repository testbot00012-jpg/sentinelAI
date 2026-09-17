import re
import math
import numpy as np
from urllib.parse import urlparse
from collections import Counter
from typing import List, Dict, Any, Tuple

# Popular URL shortening domains
SHORTENER_DOMAINS = {
    "bit.ly", "goo.gl", "tinyurl.com", "t.co", "is.gd", "buff.ly", "adf.ly", 
    "ow.ly", "rebrand.ly", "cutt.ly", "tiny.cc", "soo.gd", "s2r.co", "clicky.me"
}

# High-risk / suspicious top-level domains commonly abused in phishing campaigns
SUSPICIOUS_TLDS = {
    "xyz", "top", "tk", "ml", "ga", "cf", "gq", "buzz", "club", "fit", "work", 
    "rest", "live", "guru", "online", "stream", "cam", "icu", "click", "vip", "country"
}

# Suspicious keywords in URLs
SUSPICIOUS_URL_KEYWORDS = [
    "secure", "login", "update", "bank", "verification", "support", "billing", 
    "signin", "auth", "account", "confirm", "service", "password", "wallet", 
    "recover", "ebayisapi", "webscr", "paypai", "appie", "micros0ft", "security"
]

# High-risk SMS keywords and urgency patterns
SCAM_SMS_KEYWORDS = {
    "otp": 0.95, "verify": 0.80, "suspended": 0.85, "winner": 0.90, "prize": 0.90,
    "lottery": 0.95, "claim": 0.80, "bank": 0.65, "blocked": 0.85, "reset password": 0.80,
    "kyc": 0.90, "login": 0.70, "click here": 0.85, "update profile": 0.75, "free cash": 0.95,
    "urgent": 0.80, "tax refund": 0.90, "credit card": 0.75, "unusual activity": 0.85,
    "wire transfer": 0.85, "crypto": 0.75, "bitcoin": 0.80, "action required": 0.85,
    "congratulations": 0.80, "locked": 0.80, "deactivated": 0.85, "reward": 0.80,
    "gift card": 0.85, "deposit": 0.70, "cash prize": 0.90
}

# Standard & Critical Android permissions registry
KNOWN_ANDROID_PERMISSIONS = [
    "android.permission.BIND_ACCESSIBILITY_SERVICE",
    "android.permission.BIND_DEVICE_ADMIN",
    "android.permission.SYSTEM_ALERT_WINDOW",
    "android.permission.SEND_SMS",
    "android.permission.RECEIVE_SMS",
    "android.permission.READ_SMS",
    "android.permission.RECORD_AUDIO",
    "android.permission.CAMERA",
    "android.permission.ACCESS_FINE_LOCATION",
    "android.permission.ACCESS_COARSE_LOCATION",
    "android.permission.READ_PHONE_STATE",
    "android.permission.PROCESS_OUTGOING_CALLS",
    "android.permission.CALL_PHONE",
    "android.permission.READ_CALL_LOG",
    "android.permission.WRITE_CALL_LOG",
    "android.permission.WRITE_EXTERNAL_STORAGE",
    "android.permission.READ_EXTERNAL_STORAGE",
    "android.permission.READ_CONTACTS",
    "android.permission.WRITE_CONTACTS",
    "android.permission.GET_ACCOUNTS",
    "android.permission.RECEIVE_BOOT_COMPLETED",
    "android.permission.REQUEST_INSTALL_PACKAGES",
    "android.permission.INTERNET",
    "android.permission.ACCESS_NETWORK_STATE",
    "android.permission.ACCESS_WIFI_STATE",
    "android.permission.CHANGE_WIFI_STATE",
    "android.permission.FOREGROUND_SERVICE",
    "android.permission.WAKE_LOCK",
    "android.permission.VIBRATE",
    "android.permission.KILL_BACKGROUND_PROCESSES",
    "android.permission.USE_BIOMETRIC",
    "android.permission.USE_FINGERPRINT",
    "android.permission.NFC",
    "android.permission.BLUETOOTH",
    "android.permission.BLUETOOTH_ADMIN"
]


def calculate_entropy(text: str) -> float:
    """Calculates Shannon entropy for lexical randomness."""
    if not text:
        return 0.0
    counter = Counter(text)
    length = len(text)
    return -sum((count / length) * math.log2(count / length) for count in counter.values())


# ============================================================================
# 1. URL FEATURE EXTRACTION
# ============================================================================

def extract_url_features(url: str) -> List[float]:
    """
    Extracts numerical feature vector (22 features) for URL classification.
    """
    if not url.startswith(("http://", "https://")):
        url_to_parse = "https://" + url
    else:
        url_to_parse = url

    try:
        parsed = urlparse(url_to_parse)
        domain = parsed.netloc.lower()
        path = parsed.path.lower()
    except Exception:
        domain = ""
        path = ""

    # Clean domain if port exists
    domain_clean = domain.split(":")[0] if ":" in domain else domain

    url_len = float(len(url))
    domain_len = float(len(domain_clean))
    path_len = float(len(path))

    count_dots = float(url.count("."))
    count_hyphens = float(domain_clean.count("-"))
    count_underscores = float(url.count("_"))
    count_slashes = float(url.count("/"))
    count_questions = float(url.count("?"))
    count_equals = float(url.count("="))
    count_at = float(url.count("@"))
    count_ampersands = float(url.count("&"))
    count_percent = float(url.count("%"))

    # Digit calculations
    digits_in_domain = sum(c.isdigit() for c in domain_clean)
    count_digits = float(digits_in_domain)
    digit_ratio = float(digits_in_domain / domain_len) if domain_len > 0 else 0.0

    # Shannon Entropy
    domain_entropy = float(calculate_entropy(domain_clean))

    # IP address check
    ip_pattern = r"^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$"
    is_ip = 1.0 if re.match(ip_pattern, domain_clean) else 0.0

    # Subdomains count
    parts = domain_clean.split(".")
    subdomain_count = float(max(0, len(parts) - 2))

    # Shortener check
    is_shortener = 1.0 if domain_clean in SHORTENER_DOMAINS else 0.0

    # High-risk TLD check
    tld = parts[-1] if len(parts) > 1 else ""
    is_suspicious_tld = 1.0 if tld in SUSPICIOUS_TLDS else 0.0

    # Keyword count
    url_lower = url.lower()
    keyword_matches = sum(1.0 for kw in SUSPICIOUS_URL_KEYWORDS if kw in url_lower)

    # Double http/https token
    multiple_http = 1.0 if (url_lower.count("http://") + url_lower.count("https://")) > 1 or "http" in path else 0.0

    # Non-standard port
    has_port = 1.0 if (":" in domain and not (domain.endswith(":80") or domain.endswith(":443"))) else 0.0

    return [
        url_len,
        domain_len,
        path_len,
        count_dots,
        count_hyphens,
        count_underscores,
        count_slashes,
        count_questions,
        count_equals,
        count_at,
        count_ampersands,
        count_percent,
        count_digits,
        digit_ratio,
        domain_entropy,
        is_ip,
        subdomain_count,
        is_shortener,
        is_suspicious_tld,
        keyword_matches,
        multiple_http,
        has_port
    ]


def explain_url_features(url: str) -> List[str]:
    """Generates human-readable explanations for URL anomalies."""
    if not url.startswith(("http://", "https://")):
        url_to_parse = "https://" + url
    else:
        url_to_parse = url

    try:
        parsed = urlparse(url_to_parse)
        domain = parsed.netloc.lower()
        path = parsed.path.lower()
    except Exception:
        return ["Malformed URL structure"]

    domain_clean = domain.split(":")[0] if ":" in domain else domain
    details = []

    # IP address
    if re.match(r"^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$", domain_clean):
        details.append("Uses direct IP address instead of registered domain name")

    # Length
    if len(url) > 75:
        details.append(f"Abnormally long URL string ({len(url)} characters)")

    # Shorteners
    if domain_clean in SHORTENER_DOMAINS:
        details.append(f"Employs known URL redirection shortener ({domain_clean})")

    # Obfuscating symbols
    if "@" in url:
        details.append("Contains '@' symbol used to obscure genuine host destination")

    if url.count("//") > 1:
        details.append("Contains secondary redirect protocol slashes '//'")

    if "-" in domain_clean:
        details.append("Domain contains hyphenated brand-spoofing markers")

    # Subdomains
    subdomains = domain_clean.split(".")
    if len(subdomains) > 3:
        details.append(f"High subdomain depth ({len(subdomains) - 2} levels)")

    # TLD
    tld = subdomains[-1] if len(subdomains) > 1 else ""
    if tld in SUSPICIOUS_TLDS:
        details.append(f"Uses high-abuse top-level domain extension (.{tld})")

    # Keywords
    matched_kws = [kw for kw in SUSPICIOUS_URL_KEYWORDS if kw in url.lower()]
    if matched_kws:
        details.append(f"Contains security-sensitive target tokens: {', '.join(matched_kws[:3])}")

    # Entropy
    if calculate_entropy(domain_clean) > 3.8 and len(domain_clean) > 12:
        details.append("Unusually high character entropy (possible DGA/randomized host)")

    return details if details else ["URL structure exhibits benign characteristics"]


# ============================================================================
# 2. SMS / EMAIL SCAM LINGUISTIC EXTRACTION
# ============================================================================

def extract_sms_heuristic_features(text: str) -> List[float]:
    """
    Extracts dense linguistic and risk features for SMS / text fraud detection.
    """
    text_lower = text.lower()
    length = float(len(text))

    # Keyword risk weight accumulation
    matched_score = sum(weight for kw, weight in SCAM_SMS_KEYWORDS.items() if kw in text_lower)

    # Link presence
    has_link = 1.0 if re.search(r"https?://\S+|www\.\S+|\.[a-z]{2,4}/\S+", text_lower) else 0.0

    # Urgency indicators
    urgency_pattern = r"(immediately|now|urgent|within\s+\d+\s+(?:minutes|hours|days)|expires|action\s+required|asap|right\s+away)"
    has_urgency = 1.0 if re.search(urgency_pattern, text_lower) else 0.0

    # Phone / account / monetary numbers
    has_phone = 1.0 if re.search(r"\b(?:\+?\d{1,3}[-.\s]?)?\(?\d{3}\)?[-.\s]?\d{3}[-.\s]?\d{4}\b", text) else 0.0
    has_currency = 1.0 if re.search(r"[\$€£₹]|rs\b|inr|usd", text_lower) else 0.0

    # Capitalization ratio
    caps_count = sum(1 for c in text if c.isupper())
    caps_ratio = float(caps_count / max(1, len(text)))

    # Exclamation count
    excl_count = float(text.count("!"))

    return [
        length,
        matched_score,
        has_link,
        has_urgency,
        has_phone,
        has_currency,
        caps_ratio,
        excl_count
    ]


def batch_extract_sms_heuristics(texts: List[str]) -> np.ndarray:
    """Batch extraction of dense heuristic features for SMS messages."""
    return np.array([extract_sms_heuristic_features(t) for t in texts], dtype=np.float64)


def explain_sms_features(text: str) -> Tuple[List[str], bool]:
    """Generates detailed threat indicators and detects if message has links."""
    text_lower = text.lower()
    reasons = []

    matched_keywords = [kw for kw in SCAM_SMS_KEYWORDS.keys() if kw in text_lower]
    if matched_keywords:
        reasons.append(f"Matched high-risk financial/credential keywords: {', '.join(matched_keywords[:4])}")

    has_link = bool(re.search(r"https?://\S+|www\.\S+|\.[a-z]{2,4}/\S+", text_lower))
    if has_link:
        reasons.append("Contains suspicious unverified hyperlink")

    urgency_pattern = r"(immediately|now|urgent|within\s+\d+\s+(?:minutes|hours|days)|expires|action\s+required|asap)"
    if re.search(urgency_pattern, text_lower):
        reasons.append("Displays aggressive urgency/coercion language")

    if re.search(r"[\$€£₹]|rs\b|usd|inr|\b(?:won|prize|refund|cash)\b", text_lower):
        reasons.append("Mentions monetary reward, prize claim, or financial lure")

    caps_count = sum(1 for c in text if c.isupper())
    if caps_count > 12 and (caps_count / max(1, len(text))) > 0.25:
        reasons.append("Abnormally high uppercase lettering (shouting / alarmist tone)")

    return (reasons if reasons else ["No high-risk scam triggers identified"], has_link)


# ============================================================================
# 3. APK PERMISSION EXTRACTION
# ============================================================================

def extract_apk_permission_vector(permissions: List[str]) -> List[float]:
    """
    Encodes APK permissions into a binary and combination risk vector.
    """
    perm_set = set()
    for p in permissions:
        perm_clean = p.strip()
        perm_set.add(perm_clean)
        # also add short-hand version e.g. "SEND_SMS"
        if "." in perm_clean:
            perm_set.add(perm_clean.split(".")[-1])

    # 1. Binary presence for known permissions
    feature_vector = []
    for known_perm in KNOWN_ANDROID_PERMISSIONS:
        short_name = known_perm.split(".")[-1]
        feature_vector.append(1.0 if (known_perm in perm_set or short_name in perm_set) else 0.0)

    # 2. Total permissions count
    feature_vector.append(float(len(permissions)))

    # 3. Critical Permission Combinations (Synergy Vectors)
    # Accessibility + Device Admin or Overlay (Banking Trojan / Ransomware)
    acc = 1.0 if ("BIND_ACCESSIBILITY_SERVICE" in perm_set or "android.permission.BIND_ACCESSIBILITY_SERVICE" in perm_set) else 0.0
    admin = 1.0 if ("BIND_DEVICE_ADMIN" in perm_set or "android.permission.BIND_DEVICE_ADMIN" in perm_set) else 0.0
    overlay = 1.0 if ("SYSTEM_ALERT_WINDOW" in perm_set or "android.permission.SYSTEM_ALERT_WINDOW" in perm_set) else 0.0
    sms = 1.0 if any(s in perm_set for s in ["SEND_SMS", "RECEIVE_SMS", "READ_SMS"]) else 0.0
    net = 1.0 if ("INTERNET" in perm_set or "android.permission.INTERNET" in perm_set) else 0.0
    spy = 1.0 if any(s in perm_set for s in ["RECORD_AUDIO", "CAMERA", "ACCESS_FINE_LOCATION"]) else 0.0
    install = 1.0 if ("REQUEST_INSTALL_PACKAGES" in perm_set or "android.permission.REQUEST_INSTALL_PACKAGES" in perm_set) else 0.0

    combo_trojan = 1.0 if (acc and (overlay or admin)) else 0.0
    combo_sms_intercept = 1.0 if (sms and net) else 0.0
    combo_spyware = 1.0 if (spy and net) else 0.0
    combo_dropper = 1.0 if (install and net) else 0.0

    feature_vector.extend([combo_trojan, combo_sms_intercept, combo_spyware, combo_dropper])

    return feature_vector


def explain_apk_permissions(permissions: List[str]) -> Tuple[List[str], str]:
    """Flag critical permissions and evaluate composite threat category."""
    perm_set = set()
    for p in permissions:
        p_clean = p.strip()
        perm_set.add(p_clean)
        if "." in p_clean:
            perm_set.add(p_clean.split(".")[-1])

    critical_weights = {
        "BIND_ACCESSIBILITY_SERVICE": (35, "Full device UI inspection & keystroke capture"),
        "BIND_DEVICE_ADMIN": (30, "Remote wipe, pin lock, device takeover"),
        "SYSTEM_ALERT_WINDOW": (25, "Overlay attack spoofing banking/login screens"),
        "SEND_SMS": (20, "Silent background outbound premium SMS billing"),
        "RECEIVE_SMS": (20, "MFA 2FA SMS interception"),
        "READ_SMS": (15, "SMS conversation & OTP data exfiltration"),
        "RECORD_AUDIO": (15, "Ambient background microphone recording"),
        "CAMERA": (10, "Unauthorized covert photo & video capture"),
        "ACCESS_FINE_LOCATION": (10, "Continuous GPS movement tracking"),
        "READ_PHONE_STATE": (10, "IMEI, IMSI, and SIM carrier harvesting"),
        "REQUEST_INSTALL_PACKAGES": (20, "Silent payload dropper / unknown APK execution"),
        "PROCESS_OUTGOING_CALLS": (15, "Call interception & rerouting")
    }

    flagged = []
    for short_k, (weight, desc) in critical_weights.items():
        if short_k in perm_set or f"android.permission.{short_k}" in perm_set:
            flagged.append(f"{short_k} ({weight}% risk - {desc})")

    # Threat classification logic
    has_acc = "BIND_ACCESSIBILITY_SERVICE" in perm_set or "android.permission.BIND_ACCESSIBILITY_SERVICE" in perm_set
    has_admin = "BIND_DEVICE_ADMIN" in perm_set or "android.permission.BIND_DEVICE_ADMIN" in perm_set
    has_sms = any(s in perm_set for s in ["SEND_SMS", "RECEIVE_SMS", "READ_SMS"])
    has_overlay = "SYSTEM_ALERT_WINDOW" in perm_set or "android.permission.SYSTEM_ALERT_WINDOW" in perm_set

    if has_acc and has_overlay:
        category = "Banking Trojan / Credential Overlay Stealer"
    elif has_acc or has_admin:
        category = "Ransomware / Device Administrator Hijacker"
    elif has_sms:
        category = "SMS Spy / Premium Dialer Fraud"
    elif len(flagged) >= 4:
        category = "Advanced Persistent Threat (APT) / Spyware Suite"
    elif len(flagged) >= 2:
        category = "Adware / Suspicious Riskware Utility"
    else:
        category = "Clean / Legitimate Utility Application"

    return flagged, category
