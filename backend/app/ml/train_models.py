import os
import json
import joblib
import numpy as np
from datetime import datetime, timezone
from sklearn.model_selection import train_test_split, StratifiedKFold, cross_val_score
from sklearn.ensemble import RandomForestClassifier
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.pipeline import Pipeline, FeatureUnion
from sklearn.preprocessing import FunctionTransformer, StandardScaler
from sklearn.calibration import CalibratedClassifierCV
from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score, roc_auc_score

from app.ml.feature_extractors import (
    extract_url_features,
    extract_sms_heuristic_features,
    batch_extract_sms_heuristics,
    extract_apk_permission_vector
)

MODELS_DIR = os.path.join(os.path.dirname(__file__), "saved_models")
os.makedirs(MODELS_DIR, exist_ok=True)


# ============================================================================
# 1. ADVANCED URL PHISHING DATASET & TRAINING
# ============================================================================

def generate_expanded_url_dataset():
    """Builds a rich, high-diversity dataset of benign and phishing URLs."""
    benign_base = [
        "https://google.com", "https://www.google.com/search?q=sentinel+ai",
        "https://github.com/torvalds/linux", "https://github.com/features/security",
        "https://en.wikipedia.org/wiki/Computer_security", "https://wikipedia.org/wiki/Neural_network",
        "https://stackoverflow.com/questions/tagged/python", "https://stackoverflow.com/users/signup",
        "https://microsoft.com/en-us/windows", "https://azure.microsoft.com/en-us/products/storage",
        "https://amazon.com/dp/B08N5WRWNW", "https://aws.amazon.com/free",
        "https://apple.com/iphone-16-pro", "https://developer.apple.com/documentation/security",
        "https://netflix.com/browse", "https://netflix.com/login",
        "https://nytimes.com/section/technology", "https://bbc.com/news/world",
        "https://cnn.com/business", "https://reddit.com/r/Cybersecurity",
        "https://medium.com/@infosec/phishing-heuristics", "https://hub.docker.com/_/redis",
        "https://linkedin.com/in/security-researcher", "https://chase.com/personal/banking",
        "https://bankofamerica.com/online-banking", "https://wellsfargo.com",
        "https://paypal.com/signin", "https://paypal.com/us/home",
        "https://stripe.com/docs/api", "https://stripe.com/payments",
        "https://cloudflare.com/learning/security/what-is-phishing",
        "https://docs.python.org/3/library/urllib.parse.html", "https://pypi.org/project/scikit-learn",
        "https://huggingface.co/models", "https://kaggle.com/datasets",
        "https://weather.com/weather/today", "https://espn.com/nba/scores",
        "https://spotify.com/us/premium", "https://dropbox.com/home",
        "https://slack.com/workspace", "https://zoom.us/join",
        "https://salesforce.com/products", "https://adobe.com/creativecloud.html",
        "https://coursera.org/learn/machine-learning", "https://edx.org/course/cs50",
        "https://quora.com/topic/Information-Security", "https://gitlab.com/explore",
        "https://bitbucket.org/product", "https://spring.io/projects/spring-boot",
        "https://flutter.dev/docs", "https://developer.android.com/reference",
        "https://oracle.com/java", "https://mongodb.com/atlas/database",
        "https://firebase.google.com/docs/auth", "https://fastapi.tiangolo.com/tutorial",
        "https://scikit-learn.org/stable/modules/ensemble.html", "https://target.com/deals",
        "https://walmart.com/store", "https://bestbuy.com/site",
        "https://ebay.com/itm/electronic-sensor", "https://irs.gov/individuals",
        "https://usps.com/manage", "https://fedex.com/tracking",
        "https://dhl.com/global-en/home/tracking.html", "https://ups.com/track",
        "https://coinbase.com/learn", "https://binance.com/en/trade",
        "https://ethereum.org/en/developers", "https://whatsapp.com/download",
        "https://telegram.org/apps", "https://discord.com/channels/@me"
    ]

    phishing_base = [
        # Raw IP Attacks
        "http://192.168.1.100/chase-bank/login.php?verify=1",
        "http://45.33.32.156/paypal/signin/verification.html",
        "http://217.182.195.12/secure/banking/login",
        "http://104.244.42.1/webscr?cmd=_login-run&dispatch=5885d8011",
        "http://89.208.107.123:8080/bankofamerica/secure-login",
        # Brand-in-Subdomain Attacks
        "http://paypal.com.verify-user-account-security.xyz/login",
        "http://chase.com-secure-online-login.top/auth/index.php",
        "http://appleid.apple.com.manage-account.ga/signin",
        "http://wellsfargo.com.update-security-profile.cf/login.html",
        "http://netflix.account-billing-update.icu/payment",
        "http://microsoft.secure-auth-login.buzz/verify",
        "http://amazon.shipping-order-confirm.vip/tracking",
        "http://google.account-security-alert.tk/recover",
        "http://secure-login-bankofamerica.top/portal",
        "http://login.chase.com.security-department-update.ml/index",
        "http://bank-of-america-verify.com.suspended-account.fit",
        "http://binance.security-unlock-device.xyz/login",
        "http://coinbase.wallet-verify-funds.buzz/restore",
        "http://metamask.wallet-seed-recovery.icu/phrase",
        "http://trustwallet.auth-connect-node.click/import",
        # Punycode / Homograph Attacks
        "http://xn--pple-43d.com/secure-login",
        "http://xn--microsft-57a.com/verify-account",
        "http://xn--paypa-r4a.com/signin",
        # Abused TLD Attacks
        "http://fedex.tracking-package-address-fee.top/delivery",
        "http://dhl.reschedule-parcel-clearance.club/confirm",
        "http://usps.redelivery-schedule-fee.xyz/index.html",
        "http://walmart.rewards-winner-claim.vip/cards",
        "http://target.giftcard-balance-update.work/auth",
        "http://att.bill-payment-overdue.online/pay",
        "http://verizon.account-locked-restore.guru/support",
        "http://citibank.online-banking-token.country/signin.php",
        "http://pncbank.verification-center.top/login.aspx",
        "http://capitalone.security-alert-review.rest/account",
        "http://instagram.copyright-infringement-appeal.cam",
        "http://facebook.security-checkpoint-appeal.live/login",
        "http://whatsapp.web-qr-code-login.fit/scan",
        "http://telegram.official-gift-premium.buzz/claim",
        "http://discord.nitro-free-generator.xyz/redeem",
        "http://twitter.badge-verification-portal.top/apply",
        "http://roblox.free-robux-promo.vip/redeem",
        # Shorteners with malicious intent
        "https://bit.ly/3xPh1shSecureBankUpdate",
        "https://tinyurl.com/free-crypto-giveaway-claim",
        "https://cutt.ly/bank-security-kyc-verify",
        "https://is.gd/chase_urgent_action",
        # Credential Obfuscation & Keyword stuffing
        "http://user@verify-paypal-accounts-portal.click/signin",
        "http://sign-in.ebayisapi.dll-secure.top/ebay3d",
        "http://secure-irs-tax-refund.top/claim-now.php?id=928",
        "http://microsoft-online-365-login.gq/owa/auth",
        "http://sharepoint.document-access-verification.xyz",
        "http://adobe-pdf-cloud-sign-document.top/preview.php",
        "http://docu-sign-secure-envelope.buzz/view-document",
        "http://zoom-meeting-invitation-secure.icu/join",
        "http://webmail.company-portal-upgrade.work/roundcube",
        "http://cpanel.hosting-account-suspended.rest/login",
        "http://godaddy.ssl-certificate-expiration-notice.vip/renew"
    ]

    X_urls = []
    y_urls = []

    # Augment Benign URLs
    for u in benign_base:
        X_urls.append(u)
        y_urls.append(0)
        X_urls.append(u + ("" if "?" in u else "?lang=en&ref=home"))
        y_urls.append(0)
        X_urls.append(u + ("" if "?" in u else "/about/security-policy"))
        y_urls.append(0)
        X_urls.append(u + ("" if "?" in u else "/docs/v2/getting-started"))
        y_urls.append(0)
        X_urls.append(u + ("" if "?" in u else "/help/center/faq"))
        y_urls.append(0)

    # Augment Phishing URLs
    for u in phishing_base:
        X_urls.append(u)
        y_urls.append(1)
        X_urls.append(u + ("&session=expired_token" if "?" in u else "?session=expired_token"))
        y_urls.append(1)
        X_urls.append(u + ("&redirect_url=http://victim.com" if "?" in u else "?redirect_url=http://victim.com"))
        y_urls.append(1)
        X_urls.append(u + ("&auth_step=verification_mfa" if "?" in u else "?auth_step=verification_mfa"))
        y_urls.append(1)
        X_urls.append(u + ("&error=invalid_credentials" if "?" in u else "?error=invalid_credentials"))
        y_urls.append(1)

    return X_urls, y_urls


def train_url_phishing_model():
    """Trains high-dimensional calibrated Random Forest classifier with 5-fold cross validation."""
    print("[1] Training High-Accuracy URL Phishing Classifier...")
    X_raw, y_raw = generate_expanded_url_dataset()

    X = np.array([extract_url_features(u) for u in X_raw], dtype=np.float64)
    y = np.array(y_raw, dtype=np.int32)

    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.20, random_state=42, stratify=y
    )

    # High-capacity Random Forest with balanced subsampling
    base_rf = RandomForestClassifier(
        n_estimators=200,
        max_depth=16,
        min_samples_split=2,
        min_samples_leaf=1,
        class_weight="balanced_subsample",
        random_state=42,
        n_jobs=-1
    )

    # 5-fold cross-validation score
    cv = StratifiedKFold(n_splits=5, shuffle=True, random_state=42)
    cv_scores = cross_val_score(base_rf, X_train, y_train, cv=cv, scoring="accuracy")
    print(f"    -> 5-Fold Stratified CV Accuracy: {cv_scores.mean() * 100:.2f}% (std: {cv_scores.std():.4f})")

    base_rf.fit(X_train, y_train)

    preds = base_rf.predict(X_test)
    probs = base_rf.predict_proba(X_test)[:, 1]

    acc = accuracy_score(y_test, preds)
    prec = precision_score(y_test, preds, zero_division=0)
    rec = recall_score(y_test, preds, zero_division=0)
    f1 = f1_score(y_test, preds, zero_division=0)
    auc = roc_auc_score(y_test, probs)

    print(f"    -> Test Accuracy:  {acc * 100:.2f}%")
    print(f"    -> Test Precision: {prec:.4f} | Recall: {rec:.4f} | F1: {f1:.4f} | ROC-AUC: {auc:.4f}")

    model_path = os.path.join(MODELS_DIR, "url_phishing_model.joblib")
    joblib.dump(base_rf, model_path, compress=3)

    return {
        "accuracy": float(acc),
        "precision": float(prec),
        "recall": float(rec),
        "f1_score": float(f1),
        "roc_auc": float(auc),
        "cv_5fold_mean": float(cv_scores.mean())
    }


# ============================================================================
# 2. ADVANCED SMS / EMAIL SCAM NLP PIPELINE & TRAINING
# ============================================================================

def generate_expanded_sms_dataset():
    """Builds an expanded, multi-category smishing/phishing vs benign text dataset."""
    benign_base = [
        "Hey, are you free for lunch today at 1 pm?",
        "Your Amazon package has been delivered to your front door. Have a great day!",
        "Hi Mom, I'm heading home right now. See you in about 20 minutes.",
        "Your appointment with Dr. Smith is confirmed for Thursday at 10:00 AM.",
        "Your Uber driver is arriving in 3 minutes in a Silver Toyota Camry.",
        "Reminder: The team standup meeting starts in 15 minutes on Google Meet.",
        "Your flight DL 1422 to Atlanta is on schedule, departing from Gate B12.",
        "Thanks for dining with us! Here is your e-receipt from Starbucks for $4.75.",
        "Can you please send me the latest PDF report when you finish it?",
        "Happy Birthday! Hope you have an amazing day filled with joy and laughter!",
        "Your order #92842 has shipped. Track via your official account dashboard.",
        "The library books you reserved are ready for pickup at the main desk.",
        "Great job on the presentation today, everyone was very impressed.",
        "Did you remember to turn off the lights before leaving the office?",
        "Here is the recipe we talked about: 2 cups flour, 1 cup sugar, 2 eggs.",
        "The weather forecast says it might rain this afternoon, take an umbrella.",
        "Your Netflix subscription has been renewed successfully for $15.49.",
        "Good morning! Let me know when you are available to review the pull request.",
        "Don't forget grocery shopping: milk, eggs, bread, and apples.",
        "We have received your payment of $45.00 for the monthly gym membership.",
        "The gym is closing early today for maintenance at 8 PM.",
        "Let's reschedule our coffee meetup to Friday if that works better for you.",
        "Your car service maintenance is complete and ready for pickup at Honda.",
        "Thank you for your feedback! We are always working to improve our service.",
        "See you tomorrow at soccer practice at 5 PM.",
        "Your dental cleaning appointment is scheduled for next Monday at 2 PM.",
        "Your package is out for delivery with USPS driver #4.",
        "Can you pick up dinner on your way back from work?",
        "The project deadline has been extended to next Friday.",
        "Here is the Zoom link for our family call tonight at 7."
    ]

    scam_base = [
        # Banking & Account Takeover
        "URGENT: Your Chase bank account has been suspended due to unusual activity. Click here to verify your identity: http://chase-secure-verify.top/login",
        "Wells Fargo Alert: A wire transfer of $1,250 was attempted. If this wasn't you, call immediately or verify at: http://wellsfargo-freeze.xyz",
        "Bank Alert: Your debit card ending in 4921 has been restricted. Reset your online password now: http://bank-secure-reset.icu",
        "Action Required: Your PayPal account has been limited. Confirm your profile information within 24 hours: http://paypal-resolution.ml",
        "Citibank Alert: Account access suspended. Reactivate your debit credentials now: http://citi-reactivate-login.online",
        "Bank of America Notice: Unauthorized login from IP 185.220.101.5. Lock your account immediately: http://boa-lock-auth.top",
        "PNC Security: Suspicious charge of $450.00 at Apple Store. Reject this charge by logging in here: http://pnc-charge-cancel.buzz",
        "Security Alert: Multiple failed login attempts detected. Verify your OTP passcode 882910 at: http://auth-login-verify.top",
        "Dear customer, your KYC verification is incomplete. Your account will be frozen within 2 hours. Update now: http://kyc-bank-portal.top",
        # Lottery & Prize Lures
        "Congratulations! You are the winner of a $5,000 Walmart Gift Card! Claim your cash prize immediately: http://giftcard-winner.vip/claim",
        "CONGRATS! Your phone number won 1st prize in our international lottery! Send your bank details and claim code 9920 to claim cash!",
        "You have been selected for an exclusive $1,000 Amazon shopping spree! Tap link now before it expires: http://amazon-spree.buzz",
        "Congratulations! You have been selected for an iPhone 16 giveaway! Claim free prize now: http://apple-gift-promo.vip",
        "Special Reward: You have 15,000 unredeemed loyalty points worth $150. Claim in cash today: http://points-reward-payout.fit",
        # Government & Tax Refunds
        "IRS Final Notice: You have an unpaid tax refund of $1,420. Submit your SSN and bank details now to receive deposit: http://irs-refund-claim.buzz",
        "Free Govt Relief Grant of $2,500 is available for you! Enter your personal information to receive instant wire: http://govt-grant.live",
        "State Treasury Notice: Unclaimed funds of $890 under your name. Verify identity to release check: http://state-unclaimed-funds.vip",
        # Shipping & Delivery Imposter
        "USPS Notice: Your parcel cannot be delivered due to an incomplete address. Pay $1.99 redelivery fee now: http://usps-parcel-tracking.xyz",
        "DHL: Customs fee of $2.50 is required before clearing your shipment. Pay now to avoid return to sender: http://dhl-customs-fee.club",
        "FedEx delivery notification: Courier missed you. Reschedule your package immediately: http://fedex-schedule-parcel.top/delivery",
        "UPS Delivery: Incomplete house number. Click to update delivery details within 12 hours: http://ups-parcel-verify.online",
        # Subscription & Threat Scams
        "Your Netflix account is blocked due to billing failure. Update payment immediately to avoid permanent deactivation: http://netflix-update.cf",
        "Apple Security Warning: Your Apple ID was accessed from Russia. Reset your iCloud password immediately: http://apple-id-protect.work",
        "Final Reminder: Your electricity service will be disconnected in 30 minutes due to unpaid bill. Pay immediately at: http://utility-pay.xyz",
        "Your Instagram account will be deleted for copyright infringement. Appeal now before it's too late: http://insta-appeal-form.cam",
        # Crypto & Job Lures
        "You have received 0.5 BTC into your crypto wallet! Click the secure link now to accept your Bitcoin deposit: http://crypto-free-claim.buzz",
        "Exclusive offer! Earn $500/day working from home 20 mins a day! No experience needed. Deposit $50 starter fee to begin: http://easy-money.click",
        "Binance Security: Withdrawal of 2.1 ETH requested. If not requested by you, cancel transaction immediately: http://binance-cancel-withdraw.xyz"
    ]

    X = []
    y = []

    # Augment Benign
    for msg in benign_base:
        X.append(msg)
        y.append(0)
        X.append(msg.lower())
        y.append(0)
        X.append(msg + " Thanks!")
        y.append(0)
        X.append("Hi! " + msg)
        y.append(0)
        X.append(msg + " Let me know.")
        y.append(0)

    # Augment Scams
    for msg in scam_base:
        X.append(msg)
        y.append(1)
        X.append(msg.upper())
        y.append(1)
        X.append("URGENT NOTIFICATION: " + msg)
        y.append(1)
        X.append("ALERT: " + msg)
        y.append(1)
        X.append(msg + " Action required now.")
        y.append(1)

    return X, y


def train_sms_fraud_model():
    """Trains multi-channel NLP Pipeline with Word/Char N-Grams and Calibrated Classifier."""
    print("[2] Training High-Accuracy SMS/Email Scam NLP Engine...")
    X_raw, y_raw = generate_expanded_sms_dataset()

    X_train, X_test, y_train, y_test = train_test_split(
        X_raw, y_raw, test_size=0.20, random_state=42, stratify=y_raw
    )

    # Multi-granularity NLP representation: Word n-grams + Character n-grams + Dense heuristics
    word_tfidf = Pipeline([
        ('word_vec', TfidfVectorizer(
            ngram_range=(1, 2),
            max_features=3000,
            sublinear_tf=True,
            strip_accents='unicode'
        ))
    ])

    char_tfidf = Pipeline([
        ('char_vec', TfidfVectorizer(
            ngram_range=(3, 5),
            analyzer='char_wb',
            max_features=3000,
            sublinear_tf=True
        ))
    ])

    heuristic_pipe = Pipeline([
        ('extract_heuristics', FunctionTransformer(
            batch_extract_sms_heuristics,
            validate=False
        )),
        ('scaler', StandardScaler())
    ])

    feature_union = FeatureUnion([
        ('word_features', word_tfidf),
        ('char_features', char_tfidf),
        ('dense_heuristics', heuristic_pipe)
    ])

    base_clf = LogisticRegression(C=2.5, max_iter=1500, class_weight='balanced', random_state=42)

    calibrated_pipeline = Pipeline([
        ('features', feature_union),
        ('clf', CalibratedClassifierCV(estimator=base_clf, method='sigmoid', cv=5))
    ])

    calibrated_pipeline.fit(X_train, y_train)

    preds = calibrated_pipeline.predict(X_test)
    probs = calibrated_pipeline.predict_proba(X_test)[:, 1]

    acc = accuracy_score(y_test, preds)
    prec = precision_score(y_test, preds, zero_division=0)
    rec = recall_score(y_test, preds, zero_division=0)
    f1 = f1_score(y_test, preds, zero_division=0)
    auc = roc_auc_score(y_test, probs)

    print(f"    -> Test Accuracy:  {acc * 100:.2f}%")
    print(f"    -> Test Precision: {prec:.4f} | Recall: {rec:.4f} | F1: {f1:.4f} | ROC-AUC: {auc:.4f}")

    model_path = os.path.join(MODELS_DIR, "sms_fraud_model.joblib")
    joblib.dump(calibrated_pipeline, model_path, compress=3)

    return {
        "accuracy": float(acc),
        "precision": float(prec),
        "recall": float(rec),
        "f1_score": float(f1),
        "roc_auc": float(auc)
    }


# ============================================================================
# 3. ADVANCED ANDROID APK MALWARE & COMBINATORIAL PERMISSION TRAINING
# ============================================================================

def generate_expanded_apk_dataset():
    """Builds a rich combinatorial permission dataset across diverse Android application types."""
    benign_profiles = [
        # Utilities & System
        ["android.permission.VIBRATE"],
        ["android.permission.CAMERA", "android.permission.FLASHLIGHT"],
        ["android.permission.INTERNET", "android.permission.ACCESS_NETWORK_STATE"],
        ["android.permission.READ_EXTERNAL_STORAGE", "android.permission.WAKE_LOCK", "android.permission.FOREGROUND_SERVICE"],
        ["android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"],
        ["android.permission.WAKE_LOCK", "android.permission.VIBRATE", "android.permission.RECEIVE_BOOT_COMPLETED"],
        ["android.permission.CAMERA", "android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"],
        # Navigation & Weather
        ["android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION", "android.permission.INTERNET"],
        ["android.permission.INTERNET", "android.permission.ACCESS_NETWORK_STATE", "android.permission.ACCESS_COARSE_LOCATION"],
        # Social & Communication (Legitimate)
        ["android.permission.INTERNET", "android.permission.ACCESS_NETWORK_STATE", "android.permission.CAMERA", "android.permission.RECORD_AUDIO", "android.permission.READ_CONTACTS"],
        # Fitness & Health
        ["android.permission.BODY_SENSORS", "android.permission.ACCESS_FINE_LOCATION", "android.permission.INTERNET", "android.permission.ACTIVITY_RECOGNITION"],
        # Media & Audio
        ["android.permission.MODIFY_AUDIO_SETTINGS", "android.permission.RECORD_AUDIO", "android.permission.RECORD_VIDEO", "android.permission.INTERNET"]
    ]

    malicious_profiles = [
        # Banking Trojans (Overlay Attack + Accessibility Hijack)
        ["android.permission.BIND_ACCESSIBILITY_SERVICE", "android.permission.SYSTEM_ALERT_WINDOW", "android.permission.INTERNET", "android.permission.READ_PHONE_STATE"],
        ["android.permission.BIND_ACCESSIBILITY_SERVICE", "android.permission.SYSTEM_ALERT_WINDOW", "android.permission.INTERNET", "android.permission.READ_CONTACTS"],
        # Ransomware Lockers
        ["android.permission.BIND_DEVICE_ADMIN", "android.permission.SYSTEM_ALERT_WINDOW", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.INTERNET"],
        ["android.permission.BIND_DEVICE_ADMIN", "android.permission.KILL_BACKGROUND_PROCESSES", "android.permission.WRITE_EXTERNAL_STORAGE"],
        # SMS 2FA Stealers
        ["android.permission.SEND_SMS", "android.permission.RECEIVE_SMS", "android.permission.READ_SMS", "android.permission.INTERNET", "android.permission.READ_PHONE_STATE"],
        ["android.permission.RECEIVE_SMS", "android.permission.READ_SMS", "android.permission.INTERNET", "android.permission.RECEIVE_BOOT_COMPLETED"],
        # Covert Spyware & Surveillance
        ["android.permission.RECORD_AUDIO", "android.permission.CAMERA", "android.permission.ACCESS_FINE_LOCATION", "android.permission.READ_CONTACTS", "android.permission.READ_CALL_LOG", "android.permission.INTERNET"],
        ["android.permission.RECORD_AUDIO", "android.permission.ACCESS_FINE_LOCATION", "android.permission.READ_SMS", "android.permission.INTERNET"],
        # Silent Droppers & Installers
        ["android.permission.REQUEST_INSTALL_PACKAGES", "android.permission.INTERNET", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.ACCESS_NETWORK_STATE"],
        ["android.permission.REQUEST_INSTALL_PACKAGES", "android.permission.INTERNET", "android.permission.RECEIVE_BOOT_COMPLETED"],
        # Premium Call / SMS Dialers
        ["android.permission.SEND_SMS", "android.permission.CALL_PHONE", "android.permission.PROCESS_OUTGOING_CALLS", "android.permission.INTERNET"],
        # Advanced Persistent Threat (APT) Multi-Vector
        ["android.permission.BIND_ACCESSIBILITY_SERVICE", "android.permission.BIND_DEVICE_ADMIN", "android.permission.RECORD_AUDIO", "android.permission.CAMERA", "android.permission.ACCESS_FINE_LOCATION", "android.permission.READ_SMS", "android.permission.SEND_SMS", "android.permission.INTERNET"]
    ]

    X = []
    y = []

    for p in benign_profiles:
        for _ in range(12):
            X.append(list(p))
            y.append(0)

    for p in malicious_profiles:
        for _ in range(12):
            X.append(list(p))
            y.append(1)

    return X, y


def train_apk_malware_model():
    """Trains Random Forest Classifier on APK permission synergy matrix with 5-fold CV."""
    print("[3] Training High-Accuracy Android APK Malware Classifier...")
    X_raw, y_raw = generate_expanded_apk_dataset()

    X = np.array([extract_apk_permission_vector(p) for p in X_raw], dtype=np.float64)
    y = np.array(y_raw, dtype=np.int32)

    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.20, random_state=42, stratify=y
    )

    clf = RandomForestClassifier(
        n_estimators=150,
        max_depth=12,
        min_samples_split=2,
        class_weight="balanced",
        random_state=42,
        n_jobs=-1
    )

    cv = StratifiedKFold(n_splits=5, shuffle=True, random_state=42)
    cv_scores = cross_val_score(clf, X_train, y_train, cv=cv, scoring="accuracy")
    print(f"    -> 5-Fold Stratified CV Accuracy: {cv_scores.mean() * 100:.2f}% (std: {cv_scores.std():.4f})")

    clf.fit(X_train, y_train)

    preds = clf.predict(X_test)
    probs = clf.predict_proba(X_test)[:, 1]

    acc = accuracy_score(y_test, preds)
    prec = precision_score(y_test, preds, zero_division=0)
    rec = recall_score(y_test, preds, zero_division=0)
    f1 = f1_score(y_test, preds, zero_division=0)
    auc = roc_auc_score(y_test, probs)

    print(f"    -> Test Accuracy:  {acc * 100:.2f}%")
    print(f"    -> Test Precision: {prec:.4f} | Recall: {rec:.4f} | F1: {f1:.4f} | ROC-AUC: {auc:.4f}")

    model_path = os.path.join(MODELS_DIR, "apk_malware_model.joblib")
    joblib.dump(clf, model_path, compress=3)

    return {
        "accuracy": float(acc),
        "precision": float(prec),
        "recall": float(rec),
        "f1_score": float(f1),
        "roc_auc": float(auc),
        "cv_5fold_mean": float(cv_scores.mean())
    }


# ============================================================================
# MASTER ORCHESTRATION PIPELINE
# ============================================================================

def train_all_models():
    """Trains all 3 state-of-the-art models and saves comprehensive performance metadata."""
    print("===================================================================")
    print("  Sentinel AI - Enterprise High-Accuracy Cyber Intelligence Training")
    print("===================================================================\n")

    url_metrics = train_url_phishing_model()
    print()
    sms_metrics = train_sms_fraud_model()
    print()
    apk_metrics = train_apk_malware_model()

    metadata = {
        "engine": "Sentinel AI Enterprise Cyber Threat Intelligence Suite",
        "version": "2.0.0-high-accuracy",
        "trained_at": datetime.now(timezone.utc).isoformat(),
        "models": {
            "url_phishing_detector": {
                "algorithm": "RandomForestClassifier (200 estimators, 42 features, balanced subsampling)",
                "features_count": 42,
                "metrics": url_metrics
            },
            "sms_fraud_nlp_detector": {
                "algorithm": "FeatureUnion(Word-Tfidf[1-2] + Char-Tfidf[3-5] + Heuristics[16]) -> CalibratedClassifierCV(LogisticRegression)",
                "features_count": 6016,
                "metrics": sms_metrics
            },
            "apk_malware_analyzer": {
                "algorithm": "RandomForestClassifier (Combinatorial Synergy Matrix, 150 estimators)",
                "features_count": 47,
                "metrics": apk_metrics
            }
        }
    }

    meta_path = os.path.join(MODELS_DIR, "model_metadata.json")
    with open(meta_path, "w", encoding="utf-8") as f:
        json.dump(metadata, f, indent=2)

    print("\n[OK] All enterprise models trained and serialized successfully into:", MODELS_DIR)
    print(f"[OK] Full audit metadata written to: {meta_path}\n")


if __name__ == "__main__":
    train_all_models()
