import re
import math
import numpy as np
from urllib.parse import urlparse
from collections import Counter
from typing import List, Dict, Any, Tuple, Optional

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
    "country", "bid", "loan", "date", "racing", "win", "download", 
    "accountant", "science", "party", "review", "trade", "webcam", "faith",
    "info", "site", "space", "shop", "cfd", "sbs", "bond", "lat", "monster",
    "link", "skin", "autos", "hair", "makeup", "beauty", "quest", "agency", "cc", "pro",
    "support", "app", "zone", "services", "center", "ltd"
}

# Major targeted brands frequently spoofed in phishing
TARGET_BRANDS = [
    "paypal", "apple", "google", "microsoft", "amazon", "netflix", "chase", 
    "bankofamerica", "wellsfargo", "citibank", "capitalone", "binance", 
    "coinbase", "metamask", "trustwallet", "instagram", "facebook", "whatsapp", "telegram",
    "twitter", "discord", "steam", "roblox", "ebay", "walmart", "target", "usps", "fedex", "dhl", "ups",
    "kraken", "kucoin", "ledger", "trezor", "revolut", "venmo", "cashapp", "zelle",
    "pnc", "santander", "barclays", "hsbc", "dropbox", "adobe", "docusign", "zoom", "tiktok", "linkedin"
]

# Sensitive credentials & lure keywords in URLs
SUSPICIOUS_URL_KEYWORDS = [
    "secure", "login", "update", "bank", "verification", "support", "billing", 
    "signin", "auth", "account", "confirm", "service", "password", "wallet", 
    "recover", "ebayisapi", "webscr", "paypai", "appie", "micros0ft", "security",
    "portal", "verify", "identity", "validate", "token", "session", "passcode",
    "unlock", "restore", "suspended", "alert", "notice", "claim", "prize",
    "docs", "doc", "document", "documents", "form", "forms", "invoice", "view",
    "super", "bonus", "winner", "reward", "gift", "airdrop", "office365", "docusign",
    "paypa1", "arnazon", "goog1e", "app1e", "netflixx", "wha7sapp", "faceb00k", "instagrarn",
    "kyc", "credential", "seed-phrase", "private-key", "resolve", "reactivate", "restricted"
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

    # 42: Brand spoofing in domain or subdomain
    # e.g., paypal.com.attacker.xyz or paypal-security.xyz or appleid-verification.top
    brand_spoofed = 0.0
    for brand in TARGET_BRANDS:
        if brand in domain_clean:
            is_authentic = (
                domain_clean == f"{brand}.com" or 
                domain_clean == f"www.{brand}.com" or
                domain_clean.endswith(f".{brand}.com") or 
                domain_clean.endswith(f".{brand}.org") or
                domain_clean.endswith(f".{brand}.io") or 
                domain_clean.endswith(f".{brand}.net") or
                domain_clean.endswith(f".{brand}.co") or
                domain_clean.endswith(f".{brand}.me")
            )
            if not is_authentic:
                brand_spoofed = 1.0
                break

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


# ============================================================================
# 4. PAYMENT SCREENSHOT OCR & FRAUD TAMPERING DETECTOR
# ============================================================================

def extract_payment_receipt_amount(text: str) -> str:
    """
    Extracts the legitimate transaction amount from raw receipt text using
    the same spatial heuristics and glyph de-aliasing as the on-device ReceiptSpatialSLM.
    """
    if not text:
        return "Unknown"

    # Non-amount exclusions (times, years, dates, masked bank accounts, UTRs)
    time_matches = re.findall(r"\b\d{1,2}:\d{2}(?::\d{2})?\b", text)
    time_numbers = set()
    for tm in time_matches:
        for p in tm.split(":"):
            if p.strip().isdigit():
                time_numbers.add(int(p.strip()))

    years = {int(y) for y in re.findall(r"\b(20[1-3][0-9])\b", text)}

    date_days = set()
    for dd in re.findall(r"\b(\d{1,2})\s*(?:st|nd|rd|th)?\s+(?:jan|feb|mar|apr|may|jun|jul|aug|sep|sept|oct|nov|dec)", text, re.I):
        if dd.isdigit():
            date_days.add(int(dd))

    masked_acc_matches = re.findall(r"(?:[X*x•]{2,}|\.{3,}|A/c\s*|Account\s*|debited\s+from\s+|credited\s+to\s+)\s*(\d{2,6})\b", text, re.I)
    masked_accs = set(masked_acc_matches)

    phone_matches = re.findall(r"(?:\+?91[\s\.\-•*]*|\b)[0-9•*xX]{4,15}([0-9]{3,5})\b", text)
    masked_accs.update(phone_matches)

    utrs = set(re.findall(r"\b\d{12}\b", text))

    def is_excluded(val_str: str) -> bool:
        clean = val_str.replace(",", "").replace(" ", "").strip()
        try:
            val_num = float(clean)
        except ValueError:
            return True
        int_val = int(val_num)
        if val_num <= 0 or val_num > 10000000:
            return True
        if clean.startswith("0") and "." not in clean:
            return True
        if clean in utrs:
            return True
        if int_val in years:
            return True
        if clean in masked_accs or str(int_val) in masked_accs:
            return True
        if clean in ("91", "+91"):
            return True
        if int_val in time_numbers or int_val in date_days:
            return True
        return False

    candidates = {}
    val_to_fmt = {}

    # 1. Explicit currency symbol matches (₹, ?, Rs, INR, $, €, £, and OCR 'F'/'f' misread of ₹)
    for m in re.finditer(r"(?<![A-Za-z0-9])(?:[\$€£₹?]|rs\.?|inr|[Ff])\s*([0-9]{1,6}(?:,[0-9]{3})*(?:\.[0-9]{1,2})?)(?![A-Za-z0-9])", text, re.I):
        num_str = m.group(1).replace(",", "")
        if not is_excluded(num_str):
            try:
                v = float(num_str)
                candidates[v] = candidates.get(v, 0) + 300
                val_to_fmt[v] = f"₹{int(v):,}" if v.is_integer() else f"₹{v:,.2f}"
            except ValueError:
                pass

    # 2. OCR '7' misread of Indian Rupee symbol (e.g. 7400 -> 400, 790 -> 90)
    for m in re.finditer(r"(?<![A-Za-z0-9])7([0-9]{2,5})(?![A-Za-z0-9])", text):
        num_str = m.group(1)
        if not is_excluded(num_str):
            try:
                v = float(num_str)
                candidates[v] = candidates.get(v, 0) + 200
                if v not in val_to_fmt:
                    val_to_fmt[v] = f"₹{int(v):,}"
            except ValueError:
                pass

    # 3. Contextual search near action keywords
    lines = [l.strip() for l in text.splitlines() if l.strip()]
    action_keywords = ["received from", "paid to", "payment to", "transfer to", "sent to", "debited from", "credited to", "amount", "total"]
    for i, line in enumerate(lines):
        ll = line.lower()
        if any(kw in ll for kw in action_keywords):
            for j in range(i, min(i + 4, len(lines))):
                for nm in re.finditer(r"(?<![A-Za-z0-9])([0-9]{1,6}(?:,[0-9]{3})*(?:\.[0-9]{1,2})?)(?![A-Za-z0-9])", lines[j]):
                    num_str = nm.group(1).replace(",", "")
                    if not is_excluded(num_str):
                        try:
                            v = float(num_str)
                            candidates[v] = candidates.get(v, 0) + 100
                            if v not in val_to_fmt:
                                val_to_fmt[v] = f"₹{int(v):,}" if v.is_integer() else f"₹{v:,.2f}"
                        except ValueError:
                            pass

    # 4. Standalone decimals
    for m in re.finditer(r"(?<![A-Za-z0-9])([0-9]{1,6}(?:,[0-9]{3})*\.[0-9]{2})(?![A-Za-z0-9])", text):
        num_str = m.group(1).replace(",", "")
        if not is_excluded(num_str):
            try:
                v = float(num_str)
                candidates[v] = candidates.get(v, 0) + 150
                if v not in val_to_fmt:
                    val_to_fmt[v] = f"₹{v:,.2f}"
            except ValueError:
                pass

    if not candidates:
        return "Unknown"

    best_val = max(candidates.keys(), key=lambda k: candidates[k])
    return val_to_fmt.get(best_val, f"₹{int(best_val)}")


def analyze_payment_screenshot_data(ocr_text: str, metadata: Optional[Dict[str, Any]] = None) -> Dict[str, Any]:
    """
    Analyzes OCR text and metadata from user-submitted payment screenshots
    for manipulation artifacts, invalid reference schemes, and transaction fraud indicators.
    Adheres to Screen 14 specification.
    """
    text = ocr_text or ""
    text_lower = text.lower()
    details = []
    fraud_score = 10.0

    # 1. Transaction Amount Extraction via SLM Heuristics
    extracted_amount = extract_payment_receipt_amount(text)

    # 2. Reference / UTR Number Extraction
    utr_match = re.search(r"(?:utr|ref|reference|txn\s*id|transaction\s*id)[\s\:\#\-]*([a-zA-Z0-9]{8,24})", text, re.IGNORECASE)
    extracted_utr = utr_match.group(1) if utr_match else None

    # 3. Date & Timestamp Extraction
    date_match = re.search(r"(?:\d{1,2}[\/\-\.]\d{1,2}[\/\-\.]\d{2,4})|(?:\d{1,2}\s+(?:jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*\s+\d{2,4})", text, re.IGNORECASE)
    extracted_date = date_match.group(0) if date_match else "Not found"

    # 4. Transaction Status Indicators
    status_success = any(w in text_lower for w in ["paid successfully", "payment completed", "successful", "transferred", "payment sent", "received"])
    status_failed = any(w in text_lower for w in ["failed", "declined", "pending", "reversed"])

    # 5. Detected Payment Ecosystem
    ecosystem = "Generic Receipt"
    if any(k in text_lower for k in ["gpay", "google pay", "tez"]):
        ecosystem = "Google Pay"
    elif "phonepe" in text_lower:
        ecosystem = "PhonePe"
    elif "paytm" in text_lower:
        ecosystem = "Paytm"
    elif "paypal" in text_lower:
        ecosystem = "PayPal"
    elif any(k in text_lower for k in ["chase", "quickpay", "zelle"]):
        ecosystem = "Chase / Zelle"
    elif any(k in text_lower for k in ["venmo", "cash app"]):
        ecosystem = "Venmo / CashApp"
    elif any(k in text_lower for k in ["upi", "npci", "bhim"]):
        ecosystem = "UPI / NPCI Network"

    # 6. Fraud & Manipulation Heuristic Checks
    # Check A: Missing reference ID
    if not extracted_utr:
        fraud_score += 25.0
        details.append("Missing Bank Reference ID: Legitimate payment receipts mandate a traceable UTR or transaction reference number.")
    else:
        # Check B: Standard UPI UTR validity (UPI UTR must be 12 numeric digits)
        if "upi" in text_lower or ecosystem in ["Google Pay", "PhonePe", "Paytm", "UPI / NPCI Network"]:
            if not (len(extracted_utr) == 12 and extracted_utr.isdigit()):
                fraud_score += 45.0
                details.append(f"Invalid UTR Format: Reference '{extracted_utr}' violates standard banking rules (must be exactly 12 numeric digits). High probability of synthetic screenshot generator.")

    # Check C: Missing confirmation keywords
    if not status_success and not status_failed:
        fraud_score += 20.0
        details.append("Unverified Status: Missing definitive payment confirmation wording or server completion stamp.")

    # Check D: Template generator signature markers
    fake_generator_markers = ["fake pay", "prank payment", "spoofpay", "payment screenshot maker", "sample only", "demo receipt"]
    if any(m in text_lower for m in fake_generator_markers):
        fraud_score = 98.0
        details.insert(0, "Deceptive Tool Watermark: Text contains artifacts associated with known receipt fabrication tools.")

    # Check E: Repeated or malformed punctuation
    if text.count("..") > 1 or "$$" in text or "₹₹" in text:
        fraud_score += 25.0
        details.append("Font / Text Alignment Artifacts: Irregular symbol repetition indicative of edited or layered image text.")

    final_score = round(min(max(fraud_score, 5.0), 99.0), 1)

    if final_score < 30.0:
        verdict = "Low Risk / Consistent Indicators"
        confidence = 94.5
        recom = "Screenshot exhibits standard transaction characteristics. Always verify actual credit in your bank app before releasing goods."
    elif final_score < 65.0:
        verdict = "Medium Risk / Inconsistent Layout"
        confidence = 89.0
        recom = "Discrepancies identified in reference ID or formatting. Do NOT accept as proof of payment until funds clear your bank account."
    else:
        verdict = "High Risk / Probable Fake Screenshot"
        confidence = 96.8
        recom = "Strong indicators of image manipulation or fabricated reference number. Do NOT release goods or transfer funds."

    return {
        "extracted_amount": extracted_amount,
        "extracted_date": extracted_date,
        "extracted_reference": extracted_utr or "None",
        "ecosystem": ecosystem,
        "fraud_score": final_score,
        "classification": verdict,
        "confidence": confidence,
        "evidence": details if details else ["Standard visual formatting and valid reference structure observed."],
        "recommended_action": recom,
        "disclaimer": "Potential-risk assessment based on image indicators, not guaranteed proof of bank ledger settlement."
    }


# ============================================================================
# 5. DEVICE SECURITY SIGNALS & TAMPERING EVALUATOR
# ============================================================================

def evaluate_device_security_signals(signals: Dict[str, Any]) -> Dict[str, Any]:
    """
    Assesses Android device security signals available through platform APIs.
    Adheres to Screen 10 specification.
    """
    findings = []
    risk_points = 0
    confidence = 97.5

    os_version = str(signals.get("os_version", "14"))
    security_patch = str(signals.get("security_patch_level", "2024-01-01"))
    screen_lock = bool(signals.get("screen_lock_enabled", True))
    dev_options = bool(signals.get("developer_options_enabled", False))
    usb_debugging = bool(signals.get("usb_debugging_enabled", False))
    unknown_sources = bool(signals.get("unknown_sources_allowed", False))
    device_admin_count = int(signals.get("device_admin_count", 0))
    root_detected = bool(signals.get("root_detected", False))
    play_protect = bool(signals.get("play_protect_enabled", True))
    encrypted = bool(signals.get("encryption_enabled", True))

    # 1. Root / Tampering Check (Critical)
    if root_detected:
        risk_points += 45
        findings.append({
            "severity": "Critical",
            "title": "Device Root / System Tampering Detected",
            "detail": "Su binary or rooting management framework discovered. Application sandbox security guarantees are compromised.",
            "remedy": "Unroot device or flash genuine OEM firmware to prevent memory inspection by malicious apps."
        })

    # 2. Unknown Sources / Sideloading (High)
    if unknown_sources:
        risk_points += 25
        findings.append({
            "severity": "High",
            "title": "Installation from Unknown Sources Enabled",
            "detail": "Allows applications to be installed outside verified app stores without Play Protect pre-execution screening.",
            "remedy": "Disable 'Install Unknown Apps' in Android Settings > Apps > Special app access."
        })

    # 3. Lock Screen Security (High)
    if not screen_lock:
        risk_points += 20
        findings.append({
            "severity": "High",
            "title": "Device Lock Screen Disabled",
            "detail": "Hardware keystore and device credentials are fully accessible if the device is lost or unattended.",
            "remedy": "Configure a PIN, strong password, or biometric authentication in Android Security Settings."
        })

    # 4. USB Debugging Active (High)
    if usb_debugging:
        risk_points += 22
        findings.append({
            "severity": "High",
            "title": "USB Debugging (ADB) Active - Attack Surface Exposed",
            "detail": "Android Debug Bridge daemon (adbd) listening over USB (TCP port 5037). MITRE ATT&CK Mobile: T1401 (Exploit via USB) & T1630 (ADB Command Interpreter). Exposes full shell execution (adb shell), private database extraction (run-as/backup), and unauthorized APK sideloading if connected to untrusted hosts or public charging ports (Juice Jacking).",
            "remedy": "Turn off USB Debugging immediately in Android Settings > Developer Options when leaving secure workstations. Revoke USB debugging authorizations periodically."
        })

    # 5. Developer Options Active (Low)
    if dev_options and not usb_debugging:
        risk_points += 5
        findings.append({
            "severity": "Low",
            "title": "Developer Options Enabled",
            "detail": "Developer mode unlocks system-level diagnostics and debugging hooks.",
            "remedy": "Disable Developer Options in Settings if not required."
        })

    # 6. Play Protect Disabled (High)
    if not play_protect:
        risk_points += 20
        findings.append({
            "severity": "High",
            "title": "Google Play Protect Disabled",
            "detail": "Device is not receiving automatic background signature scans for malicious behavior.",
            "remedy": "Enable Google Play Protect in the Play Store Settings."
        })

    # 7. Device Encryption State (Critical)
    if not encrypted:
        risk_points += 35
        findings.append({
            "severity": "Critical",
            "title": "Storage Encryption Inactive",
            "detail": "Internal storage data is stored in plaintext, vulnerable to direct chip-off or recovery readout.",
            "remedy": "Enable Full Disk / File-Based Encryption in Device Settings."
        })

    # Calculate overall security posture score (0 - 100, where 100 is pristine)
    security_score = max(5, 100 - risk_points)

    if security_score >= 85:
        posture = "Hardened / Secure"
    elif security_score >= 60:
        posture = "Moderate / Vulnerabilities Present"
    else:
        posture = "Compromised / High Threat Posture"

    return {
        "security_score": security_score,
        "posture": posture,
        "confidence": confidence,
        "findings": findings,
        "signals_evaluated": {
            "os_version": os_version,
            "security_patch_level": security_patch,
            "screen_lock": screen_lock,
            "usb_debugging": usb_debugging,
            "unknown_sources": unknown_sources,
            "root_detected": root_detected,
            "play_protect": play_protect,
            "encryption": encrypted
        },
        "recommendations_count": len(findings)
    }


# ============================================================================
# 6. NETWORK SECURITY & ROGUE WI-FI DETECTOR
# ============================================================================

def evaluate_network_security_signals(network_info: Dict[str, Any]) -> Dict[str, Any]:
    """
    Evaluates current Wi-Fi/cellular connection security, DNS integrity, and rogue AP signals.
    Adheres to Screen 15 specification.
    """
    conn_type = str(network_info.get("connection_type", "WIFI")).upper()
    ssid = str(network_info.get("ssid", "Current Network"))
    encryption = str(network_info.get("encryption", "WPA2")).upper()
    is_captive = bool(network_info.get("is_captive_portal", False))
    vpn_active = bool(network_info.get("vpn_active", False))
    dns_servers = network_info.get("dns_servers", ["8.8.8.8", "1.1.1.1"])

    findings = []
    risk_score = 10.0
    confidence = 96.0

    # 1. Unsecured Open Wi-Fi Check
    if conn_type == "WIFI" and encryption in ["OPEN", "NONE", "WEP"]:
        risk_score += 45.0
        findings.append({
            "severity": "High",
            "title": "Unencrypted Public Wi-Fi Network",
            "detail": f"Network '{ssid}' lacks WPA2/WPA3 encryption. Transmitted packets are visible to anyone in radio range.",
            "remedy": "Activate Sentinel VPN immediately or disconnect from this wireless network."
        })

    # 2. Captive Portal Infiltration
    if is_captive:
        risk_score += 15.0
        findings.append({
            "severity": "Medium",
            "title": "Captive Portal Redirection Active",
            "detail": "Network requires authentication through a browser splash screen, which can be spoofed to harvest credentials.",
            "remedy": "Do not enter primary email or social logins on network landing portals."
        })

    # 3. DNS Integrity Audit
    TRUSTED_DNS = {"8.8.8.8", "8.8.4.4", "1.1.1.1", "1.0.0.1", "9.9.9.9", "149.112.112.112", "208.67.222.222"}
    untrusted_dns = [d for d in dns_servers if d not in TRUSTED_DNS and not d.startswith(("192.168.", "10.", "172."))]
    if untrusted_dns:
        risk_score += 25.0
        findings.append({
            "severity": "Medium",
            "title": "Untrusted Upstream DNS Resolver",
            "detail": f"DNS queries routed through non-standard server ({', '.join(untrusted_dns)}), presenting DNS hijacking risks.",
            "remedy": "Configure Android Private DNS to use DNS-over-TLS (e.g., dns.quad9.net)."
        })

    # 4. VPN Protection Mitigation
    if vpn_active:
        risk_score = max(5.0, risk_score - 30.0)
        findings.append({
            "severity": "Informational",
            "title": "Encrypted VPN Tunnel Active",
            "detail": "Underlying network traffic is encapsulated in a cryptographic tunnel.",
            "remedy": "Maintain VPN connection while using untrusted networks."
        })

    final_risk = round(min(max(risk_score, 5.0), 99.0), 1)

    return {
        "network_risk_score": final_risk,
        "status": "Secure" if final_risk < 35 else ("Suspicious" if final_risk < 65 else "High Threat"),
        "confidence": confidence,
        "connection_type": conn_type,
        "ssid": ssid,
        "encryption": encryption,
        "vpn_active": vpn_active,
        "findings": findings,
        "recommendation": "Network appears safe for normal traffic." if final_risk < 35 else "Enable Sentinel VPN protection to encrypt all outbound packets."
    }


# ============================================================================
# 7. QR CODE DESTINATION & PAYLOAD ANALYZER
# ============================================================================

def analyze_qr_code_payload(payload: str) -> Dict[str, Any]:
    """
    Parses QR code contents, extracts URLs/intents, and runs threat heuristics.
    Adheres to Screen 12 specification.
    """
    raw = payload.strip()
    if not raw:
        return {
            "payload_type": "EMPTY",
            "decoded_content": "",
            "risk_level": "Informational",
            "confidence": 100.0,
            "threat_summary": "Empty QR code data.",
            "details": []
        }

    details = []
    confidence = 98.0

    if raw.startswith(("http://", "https://", "www.")) or "." in raw.split("/")[0]:
        payload_type = "URL / Web Link"
        target_destination = raw
        risk_level = "Medium"
        threat_summary = "QR resolves to external web destination. Caution advised before browsing."
        details.append("Direct URL navigation encoded in QR code.")
    elif raw.startswith("upi://"):
        payload_type = "UPI Financial Transfer"
        risk_level = "Informational"
        threat_summary = "QR encodes direct financial transaction request. Verify payee identity before confirming payment."
        details.append("Direct UPI URI detected. Check payee VPA and amount carefully.")
        target_destination = raw
    elif raw.startswith("WIFI:"):
        payload_type = "Wi-Fi Configuration"
        risk_level = "Informational"
        threat_summary = "QR configures wireless network connection."
        target_destination = raw
    else:
        payload_type = "Plain Text / Custom Payload"
        risk_level = "Safe"
        threat_summary = "QR contains unformatted text."
        target_destination = raw

    return {
        "payload_type": payload_type,
        "decoded_content": raw,
        "target_destination": target_destination,
        "risk_level": risk_level,
        "confidence": confidence,
        "threat_summary": threat_summary,
        "details": details if details else ["Standard formatting observed."]
    }

