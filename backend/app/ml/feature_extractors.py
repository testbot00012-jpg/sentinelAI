import re
import math
import numpy as np
from urllib.parse import urlparse
from collections import Counter
from typing import List, Dict, Any, Tuple

# Comprehensive URL shortening domains (30+ providers)
SHORTENER_DOMAINS = {
    "bit.ly", "goo.gl", "tinyurl.com", "t.co", "is.gd", "buff.ly", "adf.ly", 
    "ow.ly", "rebrand.ly", "cutt.ly", "tiny.cc", "soo.gd", "s2r.co", "clicky.me",
    "shorturl.at", "bl.ink", "trib.al", "qr.ae", "v.gd", "clck.ru", "t.ly",
    "rb.gy", "hyperurl.co", "rotf.lol", "snip.ly", "short.io", "linktr.ee"
}

# High-risk / suspicious top-level domains heavily abused in phishing
SUSPICIOUS_TLDS = {
    "xyz", "top", "tk", "ml", "ga", "cf", "gq", "buzz", "club", "fit", "work", 
    "rest", "live", "guru", "online", "stream", "cam", "icu", "click", "vip", 
    "country", "bid", "loan", "date", "racing", "win", "stream", "download", 
    "accountant", "science", "party", "review", "trade", "webcam", "faith"
}

# Major targeted brands frequently spoofed in phishing
TARGET_BRANDS = [
    "paypal", "apple", "google", "microsoft", "amazon", "netflix", "chase", 
    "bankofamerica", "wellsfargo", "citibank", "capitalone", "binance", 
    "coinbase", "metamask", "instagram", "facebook", "whatsapp", "telegram",
    "twitter", "discord", "steam", "walmart", "usps", "fedex", "dhl", "ups"
]

# Sensitive credentials & lure keywords in URLs
SUSPICIOUS_URL_KEYWORDS = [
    "secure", "login", "update", "bank", "verification", "support", "billing", 
    "signin", "auth", "account", "confirm", "service", "password", "wallet", 
    "recover", "ebayisapi", "webscr", "paypai", "appie", "micros0ft", "security",
    "portal", "verify", "identity", "validate", "token", "session", "passcode",
    "unlock", "restore", "suspended", "alert", "notice", "claim", "prize"
]

# High-risk SMS psycholinguistic dictionaries
URGENCY_KEYWORDS = {
    "immediately": 1.0, "urgent": 0.95, "now": 0.8, "within 24 hours": 0.95,
    "within 2 hours": 1.0, "action required": 0.9, "expires": 0.85, "expire today": 0.95,
    "permanently blocked": 1.0, "suspended": 0.9, "final notice": 0.95, "asap": 0.85,
    "deactivated": 0.9, "locked": 0.85, "restricted": 0.85, "last warning": 1.0
}

FINANCIAL_LURE_KEYWORDS = {
    "lottery": 1.0, "winner": 0.95, "prize": 0.95, "cash prize": 1.0, "won": 0.9,
    "refund": 0.9, "tax refund": 0.95, "grant": 0.85, "bonus": 0.85, "free cash": 1.0,
    "wire transfer": 0.9, "deposit": 0.8, "crypto": 0.85, "bitcoin": 0.85, "gift card": 0.9,
    "reward points": 0.8, "unclaimed money": 1.0, "payout": 0.85, "credit card": 0.8
}

SECURITY_IMPERSONATION_KEYWORDS = {
    "otp": 1.0, "passcode": 0.95, "verification code": 0.95, "kyc": 0.95, "bank alert": 0.9,
    "fraud alert": 0.95, "unusual activity": 0.9, "security alert": 0.9, "reset password": 0.85,
    "verify identity": 0.9, "account blocked": 0.95, "unauthorized login": 0.95
}

ACTION_KEYWORDS = {
    "click here": 0.9, "tap here": 0.9, "click link": 0.95, "tap link": 0.95,
    "call immediately": 0.9, "call now": 0.85, "verify now": 0.9, "claim now": 0.95
}

SCAM_SMS_KEYWORDS = {
    **URGENCY_KEYWORDS,
    **FINANCIAL_LURE_KEYWORDS,
    **SECURITY_IMPERSONATION_KEYWORDS,
    **ACTION_KEYWORDS
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
    "android.permission.BLUETOOTH_ADMIN",
    "android.permission.MODIFY_AUDIO_SETTINGS",
    "android.permission.RECORD_VIDEO",
    "android.permission.ACCESS_BACKGROUND_LOCATION",
    "android.permission.ACTIVITY_RECOGNITION"
]


def calculate_entropy(text: str) -> float:
    """Calculates Shannon entropy for lexical randomness."""
    if not text:
        return 0.0
    counter = Counter(text)
    length = len(text)
    return -sum((count / length) * math.log2(count / length) for count in counter.values())


# ============================================================================
# 1. ADVANCED URL FEATURE EXTRACTION (42 HIGH-DIMENSIONAL FEATURES)
# ============================================================================

def extract_url_features(url: str) -> List[float]:
    """
    Extracts high-dimensional numerical feature vector (42 features) for URL classification.
    """
    if not url:
        return [0.0] * 42

    if not url.startswith(("http://", "https://")):
        url_to_parse = "https://" + url
    else:
        url_to_parse = url

    try:
        parsed = urlparse(url_to_parse)
        domain = parsed.netloc.lower()
        path = parsed.path.lower()
        query = parsed.query.lower()
    except Exception:
        domain = ""
        path = ""
        query = ""

    domain_clean = domain.split(":")[0] if ":" in domain else domain

    # 1-4: Length metrics
    url_len = float(len(url))
    domain_len = float(len(domain_clean))
    path_len = float(len(path))
    query_len = float(len(query))

    # 5-16: Symbol frequencies
    count_dots = float(url.count("."))
    count_hyphens_domain = float(domain_clean.count("-"))
    count_hyphens_url = float(url.count("-"))
    count_underscores = float(url.count("_"))
    count_slashes = float(url.count("/"))
    count_questions = float(url.count("?"))
    count_equals = float(url.count("="))
    count_at = float(url.count("@"))
    count_ampersands = float(url.count("&"))
    count_percent = float(url.count("%"))
    count_exclamation = float(url.count("!"))
    count_tildes = float(url.count("~"))

    # 17-21: Digit metrics
    digits_in_domain = sum(c.isdigit() for c in domain_clean)
    digits_in_url = sum(c.isdigit() for c in url)
    count_digits_domain = float(digits_in_domain)
    count_digits_url = float(digits_in_url)
    digit_ratio_domain = float(digits_in_domain / max(1, len(domain_clean)))
    digit_ratio_url = float(digits_in_url / max(1, len(url)))

    # 22: Vowel-to-letter ratio in domain
    letters_in_domain = [c for c in domain_clean if c.isalpha()]
    vowels_in_domain = sum(1 for c in letters_in_domain if c in "aeiou")
    vowel_ratio = float(vowels_in_domain / max(1, len(letters_in_domain)))

    # 23-25: Shannon Entropies
    domain_entropy = float(calculate_entropy(domain_clean))
    path_entropy = float(calculate_entropy(path))
    url_entropy = float(calculate_entropy(url))

    # 26-27: IP address detection (standard IPv4, hex/octal representations)
    ip_pattern = r"^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$"
    is_ip = 1.0 if re.match(ip_pattern, domain_clean) else 0.0
    is_hex_oct_ip = 1.0 if re.match(r"^0x[0-9a-fA-F]+", domain_clean) or re.match(r"^[0-9]+$", domain_clean) else 0.0

    # 28-29: Subdomains
    parts = domain_clean.split(".")
    subdomain_count = float(max(0, len(parts) - 2))
    longest_subdomain = float(max((len(p) for p in parts[:-2]), default=0))

    # 30: Shortener check
    is_shortener = 1.0 if domain_clean in SHORTENER_DOMAINS else 0.0

    # 31: Suspicious TLD check
    tld = parts[-1] if len(parts) > 1 else ""
    is_suspicious_tld = 1.0 if tld in SUSPICIOUS_TLDS else 0.0

    # 32-34: Keyword occurrences
    url_lower = url.lower()
    kws_in_domain = sum(1.0 for kw in SUSPICIOUS_URL_KEYWORDS if kw in domain_clean)
    kws_in_path = sum(1.0 for kw in SUSPICIOUS_URL_KEYWORDS if kw in path)
    total_kws = kws_in_domain + kws_in_path

    # 35: Protocol obfuscation
    multiple_http = 1.0 if (url_lower.count("http://") + url_lower.count("https://")) > 1 or "http" in path else 0.0

    # 36: Non-standard port
    has_port = 1.0 if (":" in domain and not (domain.endswith(":80") or domain.endswith(":443"))) else 0.0

    # 37: Punycode / IDN Homograph attack check
    has_punycode = 1.0 if "xn--" in domain_clean else 0.0

    # 38-39: Consecutive symbols
    consecutive_hyphens = 1.0 if "--" in domain_clean else 0.0
    consecutive_dots = 1.0 if ".." in url else 0.0

    # 40: Path depth
    path_depth = float(len([p for p in path.split("/") if p]))

    # 41: Client credential lure token
    has_client_spoof = 1.0 if any(t in url_lower for t in ["signin", "login", "verify", "password", "wallet"]) else 0.0

    # 42: Brand-in-subdomain spoofing
    # e.g., paypal.com.attacker.xyz or apple-id.verification.top
    brand_spoofed = 0.0
    if len(parts) > 2:
        subdomain_str = ".".join(parts[:-2])
        if any(brand in subdomain_str for brand in TARGET_BRANDS):
            brand_spoofed = 1.0

    return [
        url_len, domain_len, path_len, query_len,
        count_dots, count_hyphens_domain, count_hyphens_url, count_underscores,
        count_slashes, count_questions, count_equals, count_at, count_ampersands,
        count_percent, count_exclamation, count_tildes,
        count_digits_domain, count_digits_url, digit_ratio_domain, digit_ratio_url,
        vowel_ratio, domain_entropy, path_entropy, url_entropy,
        is_ip, is_hex_oct_ip, subdomain_count, longest_subdomain,
        is_shortener, is_suspicious_tld,
        kws_in_domain, kws_in_path, total_kws,
        multiple_http, has_port, has_punycode,
        consecutive_hyphens, consecutive_dots, path_depth,
        has_client_spoof, brand_spoofed,
        1.0 if (domain_len > 35 or url_len > 85) else 0.0
    ]


def explain_url_features(url: str) -> List[str]:
    """Generates detailed, human-readable explanations for URL anomalies."""
    if not url:
        return ["Empty URL string"]

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
        details.append("Uses raw IP address instead of registered domain name (Bypasses DNS reputation)")

    # Punycode / Homograph attack
    if "xn--" in domain_clean:
        details.append("Punycode detected: Potential IDN Homograph domain spoofing attack")

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

    # Subdomains & Brand Spoofing
    subdomains = domain_clean.split(".")
    if len(subdomains) > 3:
        details.append(f"High subdomain depth ({len(subdomains) - 2} levels)")

    if len(subdomains) > 2:
        sub_str = ".".join(subdomains[:-2])
        matched_brand = [b for b in TARGET_BRANDS if b in sub_str]
        if matched_brand:
            details.append(f"Brand Impersonation: '{matched_brand[0]}' spoofed inside subdomain prefix")

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
        details.append("Unusually high character entropy (possible DGA algorithmic host)")

    return details if details else ["URL structure exhibits benign characteristics"]


# ============================================================================
# 2. SMS / EMAIL PSYCHOLINGUISTIC FEATURE EXTRACTION (16 FEATURES)
# ============================================================================

def extract_sms_heuristic_features(text: str) -> List[float]:
    """
    Extracts dense psycholinguistic, behavioral, and risk features for SMS/text fraud.
    """
    if not text:
        return [0.0] * 16

    text_lower = text.lower()
    length = float(len(text))
    words = text.split()
    word_count = float(len(words))
    avg_word_len = float(length / max(1, word_count))

    # 4-7: Psycholinguistic scores
    urgency_score = sum(weight for kw, weight in URGENCY_KEYWORDS.items() if kw in text_lower)
    financial_score = sum(weight for kw, weight in FINANCIAL_LURE_KEYWORDS.items() if kw in text_lower)
    security_score = sum(weight for kw, weight in SECURITY_IMPERSONATION_KEYWORDS.items() if kw in text_lower)
    action_score = sum(weight for kw, weight in ACTION_KEYWORDS.items() if kw in text_lower)

    # 8-9: Link presence
    has_link = 1.0 if re.search(r"https?://\S+|www\.\S+|\.[a-z]{2,4}/\S+", text_lower) else 0.0
    has_shortener = 1.0 if any(s in text_lower for s in SHORTENER_DOMAINS) else 0.0

    # 10: Phone / shortcode pattern
    has_phone = 1.0 if re.search(r"\b(?:\+?\d{1,3}[-.\s]?)?\(?\d{3}\)?[-.\s]?\d{3}[-.\s]?\d{4}\b|\b\d{5,6}\b", text) else 0.0

    # 11: Currency / monetary indicator
    has_currency = 1.0 if re.search(r"[\$€£₹]|rs\b|inr|usd|\b(?:dollars|bucks|cash)\b", text_lower) else 0.0

    # 12-14: Structural metrics
    caps_count = sum(1 for c in text if c.isupper())
    caps_ratio = float(caps_count / max(1, len(text)))
    excl_count = float(text.count("!"))
    quest_count = float(text.count("?"))

    # 15-16: Digit & special character ratios
    digit_count = sum(1 for c in text if c.isdigit())
    digit_ratio = float(digit_count / max(1, len(text)))
    special_char_count = sum(1 for c in text if not c.isalnum() and not c.isspace())
    special_ratio = float(special_char_count / max(1, len(text)))

    return [
        length, word_count, avg_word_len,
        urgency_score, financial_score, security_score, action_score,
        has_link, has_shortener, has_phone, has_currency,
        caps_ratio, excl_count, quest_count, digit_ratio, special_ratio
    ]


def batch_extract_sms_heuristics(texts: List[str]) -> np.ndarray:
    """Batch extraction of dense heuristic features for SMS messages."""
    return np.array([extract_sms_heuristic_features(t) for t in texts], dtype=np.float64)


def explain_sms_features(text: str) -> Tuple[List[str], bool]:
    """Generates detailed threat indicators and detects if message has links."""
    if not text:
        return (["No text provided for evaluation"], False)

    text_lower = text.lower()
    reasons = []

    # 1. Urgency & Coercion
    matched_urgency = [kw for kw in URGENCY_KEYWORDS if kw in text_lower]
    if matched_urgency:
        reasons.append(f"Urgency / Coercion trigger: '{', '.join(matched_urgency[:2])}'")

    # 2. Financial Lures
    matched_fin = [kw for kw in FINANCIAL_LURE_KEYWORDS if kw in text_lower]
    if matched_fin:
        reasons.append(f"Financial lure / reward promise: '{', '.join(matched_fin[:2])}'")

    # 3. Security Impersonation
    matched_sec = [kw for kw in SECURITY_IMPERSONATION_KEYWORDS if kw in text_lower]
    if matched_sec:
        reasons.append(f"Credential / Identity interception: '{', '.join(matched_sec[:2])}'")

    # 4. Links
    has_link = bool(re.search(r"https?://\S+|www\.\S+|\.[a-z]{2,4}/\S+", text_lower))
    if has_link:
        reasons.append("Contains unverified external hyperlink")

    if any(s in text_lower for s in SHORTENER_DOMAINS):
        reasons.append("Employs masked URL shortener link to evade spam gateways")

    # 5. Tone & Punctuation
    caps_count = sum(1 for c in text if c.isupper())
    if caps_count > 12 and (caps_count / max(1, len(text))) > 0.25:
        reasons.append("Alarmist high uppercase lettering (psychological pressure)")

    return (reasons if reasons else ["No high-risk scam triggers identified"], has_link)


# ============================================================================
# 3. ANDROID APK PERMISSION EXTRACTION & COMBINATION SYNERGIES
# ============================================================================

def extract_apk_permission_vector(permissions: List[str]) -> List[float]:
    """
    Encodes APK permissions into an extended binary and multi-combination synergy vector.
    """
    perm_set = set()
    for p in permissions:
        perm_clean = p.strip()
        perm_set.add(perm_clean)
        if "." in perm_clean:
            perm_set.add(perm_clean.split(".")[-1])

    # 1. Binary presence for known permissions
    feature_vector = []
    for known_perm in KNOWN_ANDROID_PERMISSIONS:
        short_name = known_perm.split(".")[-1]
        feature_vector.append(1.0 if (known_perm in perm_set or short_name in perm_set) else 0.0)

    # 2. Total permissions count & dangerous permissions count
    feature_vector.append(float(len(permissions)))

    # Critical individual indicators
    acc = 1.0 if ("BIND_ACCESSIBILITY_SERVICE" in perm_set or "android.permission.BIND_ACCESSIBILITY_SERVICE" in perm_set) else 0.0
    admin = 1.0 if ("BIND_DEVICE_ADMIN" in perm_set or "android.permission.BIND_DEVICE_ADMIN" in perm_set) else 0.0
    overlay = 1.0 if ("SYSTEM_ALERT_WINDOW" in perm_set or "android.permission.SYSTEM_ALERT_WINDOW" in perm_set) else 0.0
    sms = 1.0 if any(s in perm_set for s in ["SEND_SMS", "RECEIVE_SMS", "READ_SMS"]) else 0.0
    net = 1.0 if ("INTERNET" in perm_set or "android.permission.INTERNET" in perm_set) else 0.0
    spy = 1.0 if any(s in perm_set for s in ["RECORD_AUDIO", "CAMERA", "ACCESS_FINE_LOCATION"]) else 0.0
    install = 1.0 if ("REQUEST_INSTALL_PACKAGES" in perm_set or "android.permission.REQUEST_INSTALL_PACKAGES" in perm_set) else 0.0
    contacts = 1.0 if any(s in perm_set for s in ["READ_CONTACTS", "WRITE_CONTACTS", "GET_ACCOUNTS"]) else 0.0
    calls = 1.0 if any(s in perm_set for s in ["PROCESS_OUTGOING_CALLS", "CALL_PHONE", "READ_CALL_LOG"]) else 0.0
    storage = 1.0 if ("WRITE_EXTERNAL_STORAGE" in perm_set or "android.permission.WRITE_EXTERNAL_STORAGE" in perm_set) else 0.0

    # 3. Synergy Threat Vectors (Advanced Combinatorial Exploitation)
    combo_trojan = 1.0 if (acc and (overlay or admin)) else 0.0
    combo_ransomware = 1.0 if (admin and (storage or overlay)) else 0.0
    combo_sms_intercept = 1.0 if (sms and net) else 0.0
    combo_spyware = 1.0 if (spy and net and contacts) else 0.0
    combo_dropper = 1.0 if (install and net) else 0.0
    combo_call_hijack = 1.0 if (calls and net) else 0.0
    combo_keylogger = 1.0 if (acc and net) else 0.0

    feature_vector.extend([
        combo_trojan,
        combo_ransomware,
        combo_sms_intercept,
        combo_spyware,
        combo_dropper,
        combo_call_hijack,
        combo_keylogger
    ])

    return feature_vector


def explain_apk_permissions(permissions: List[str]) -> Tuple[List[str], str]:
    """Flag critical permissions and evaluate composite threat category."""
    if not permissions:
        return ([], "Clean / Legitimate Utility Application")

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
        "CAMERA": (10, "Covert unauthorized photo & video capture"),
        "ACCESS_FINE_LOCATION": (10, "Continuous GPS movement tracking"),
        "READ_PHONE_STATE": (10, "IMEI, IMSI, and SIM carrier harvesting"),
        "REQUEST_INSTALL_PACKAGES": (20, "Silent payload dropper / unknown APK execution"),
        "PROCESS_OUTGOING_CALLS": (15, "Call interception & rerouting")
    }

    flagged = []
    for short_k, (weight, desc) in critical_weights.items():
        if short_k in perm_set or f"android.permission.{short_k}" in perm_set:
            flagged.append(f"{short_k} ({weight}% risk - {desc})")

    # Composite Threat Classification
    has_acc = "BIND_ACCESSIBILITY_SERVICE" in perm_set or "android.permission.BIND_ACCESSIBILITY_SERVICE" in perm_set
    has_admin = "BIND_DEVICE_ADMIN" in perm_set or "android.permission.BIND_DEVICE_ADMIN" in perm_set
    has_sms = any(s in perm_set for s in ["SEND_SMS", "RECEIVE_SMS", "READ_SMS"])
    has_overlay = "SYSTEM_ALERT_WINDOW" in perm_set or "android.permission.SYSTEM_ALERT_WINDOW" in perm_set
    has_install = "REQUEST_INSTALL_PACKAGES" in perm_set or "android.permission.REQUEST_INSTALL_PACKAGES" in perm_set
    has_spy = any(s in perm_set for s in ["RECORD_AUDIO", "CAMERA", "ACCESS_FINE_LOCATION"])

    if has_acc and has_overlay:
        category = "Banking Trojan / Credential Overlay Stealer"
    elif has_admin and ("WRITE_EXTERNAL_STORAGE" in perm_set or has_overlay):
        category = "Ransomware / Device Administrator Locker"
    elif has_acc or has_admin:
        category = "Privileged Access Hijacker"
    elif has_sms and ("INTERNET" in perm_set or "android.permission.INTERNET" in perm_set):
        category = "SMS Spy / 2FA Interceptor"
    elif has_install:
        category = "Trojan Dropper / Silent Payload Installer"
    elif has_spy and len(flagged) >= 4:
        category = "Advanced Persistent Threat (APT) / Full Spy Suite"
    elif len(flagged) >= 2:
        category = "Adware / Suspicious Riskware Utility"
    else:
        category = "Clean / Legitimate Utility Application"

    return flagged, category
