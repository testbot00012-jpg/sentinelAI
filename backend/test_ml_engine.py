import time
import sys
from app.services.ml_engine import SentinelMLEngine

def run_tests():
    print("=========================================================")
    print("       Sentinel AI Machine Learning Validation Suite      ")
    print("=========================================================\n")

    engine = SentinelMLEngine()

    # -------------------------------------------------------------------------
    # 1. URL PHISHING DETECTOR VALIDATION
    # -------------------------------------------------------------------------
    print("[1] Testing URL Phishing Classifier...")
    url_test_cases = [
        ("https://google.com", "Safe"),
        ("https://github.com/torvalds/linux", "Safe"),
        ("https://en.wikipedia.org/wiki/Computer_security", "Safe"),
        ("http://192.168.1.100/chase-bank/login.php?verify=1", "Phishing"),
        ("http://paypal.com.verify-user-account-security.xyz/login", "Phishing"),
        ("http://appleid.apple.com.manage-account.ga/signin", "Phishing"),
        ("", "Safe") # Edge case
    ]

    t0 = time.perf_counter()
    url_passes = 0
    for url, expected in url_test_cases:
        res = engine.analyze_url(url)
        is_pass = (res["status"] == expected) or (expected == "Phishing" and res["status"] in ["Suspicious", "Phishing"])
        if is_pass:
            url_passes += 1
            mark = "PASS"
        else:
            mark = "FAIL"
        print(f"    [{mark}] URL: {url[:50]:<50} -> Score: {res['score']:<5} | Status: {res['status']}")
    t1 = time.perf_counter()
    avg_url_lat = ((t1 - t0) / len(url_test_cases)) * 1000.0
    print(f"    --> URL Tests Passed: {url_passes}/{len(url_test_cases)} | Avg Latency: {avg_url_lat:.2f} ms\n")

    # -------------------------------------------------------------------------
    # 2. SMS / EMAIL SCAM NLP VALIDATION
    # -------------------------------------------------------------------------
    print("[2] Testing SMS & Email Fraud NLP Engine...")
    sms_test_cases = [
        ("Hey, are you free for lunch today at 1 pm?", "Normal / Safe"),
        ("Your Amazon package has been delivered to your front door. Have a great day!", "Normal / Safe"),
        ("URGENT: Your Chase bank account has been suspended due to unusual activity. Click here to verify: http://chase-secure-verify.top/login", "Highly Likely Scam / Phishing"),
        ("Congratulations! You are the winner of a $5,000 Walmart Gift Card! Claim your cash prize immediately: http://giftcard-winner.vip/claim", "Highly Likely Scam / Phishing"),
        ("IRS Notice: Unpaid tax refund of $1,420 waiting for deposit. Enter your SSN and bank details now: http://irs-refund-claim.buzz", "Highly Likely Scam / Phishing"),
        ("", "Normal / Safe") # Edge case
    ]

    t0 = time.perf_counter()
    sms_passes = 0
    for text, expected in sms_test_cases:
        res = engine.analyze_sms_or_email(text)
        is_pass = (res["classification"] == expected) or (expected == "Highly Likely Scam / Phishing" and res["classification"] in ["Spam / Suspicious", "Highly Likely Scam / Phishing"])
        if is_pass:
            sms_passes += 1
            mark = "PASS"
        else:
            mark = "FAIL"
        print(f"    [{mark}] Text: {text[:45]:<45} -> Prob: {res['scam_probability']:<5}% | Class: {res['classification']}")
    t1 = time.perf_counter()
    avg_sms_lat = ((t1 - t0) / len(sms_test_cases)) * 1000.0
    print(f"    --> SMS Tests Passed: {sms_passes}/{len(sms_test_cases)} | Avg Latency: {avg_sms_lat:.2f} ms\n")

    # -------------------------------------------------------------------------
    # 3. ANDROID APK MALWARE VALIDATION
    # -------------------------------------------------------------------------
    print("[3] Testing Android APK Malware Classifier...")
    apk_test_cases = [
        ("com.sec.calculator", "Simple Calculator", ["android.permission.VIBRATE"], "Safe"),
        ("com.clean.notes", "Color Notepad", ["android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"], "Safe"),
        ("com.bank.fakeoverlay", "Flash Player Update", [
            "android.permission.BIND_ACCESSIBILITY_SERVICE",
            "android.permission.SYSTEM_ALERT_WINDOW",
            "android.permission.INTERNET",
            "android.permission.READ_PHONE_STATE"
        ], "High Threat"),
        ("com.locker.ransomware", "System Device Fix", [
            "android.permission.BIND_DEVICE_ADMIN",
            "android.permission.SYSTEM_ALERT_WINDOW",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.INTERNET"
        ], "High Threat"),
        ("com.empty.app", "Zero Perms", [], "Safe") # Edge case
    ]

    t0 = time.perf_counter()
    apk_passes = 0
    for pkg, name, perms, expected in apk_test_cases:
        res = engine.analyze_apk_metadata(pkg, name, perms)
        is_pass = (res["status"] == expected) or (expected == "High Threat" and res["status"] in ["Suspicious", "High Threat", "Critical"])
        if is_pass:
            apk_passes += 1
            mark = "PASS"
        else:
            mark = "FAIL"
        print(f"    [{mark}] App: {name:<22} -> Risk: {res['malware_score']:<5}% | Status: {res['status']:<11} | Category: {res['threat_category']}")
    t1 = time.perf_counter()
    avg_apk_lat = ((t1 - t0) / len(apk_test_cases)) * 1000.0
    print(f"    --> APK Tests Passed: {apk_passes}/{len(apk_test_cases)} | Avg Latency: {avg_apk_lat:.2f} ms\n")

    total_tests = len(url_test_cases) + len(sms_test_cases) + len(apk_test_cases)
    total_passes = url_passes + sms_passes + apk_passes
    print("=========================================================")
    print(f"  TOTAL RESULT: {total_passes}/{total_tests} Tests Passed ({(total_passes/total_tests)*100:.1f}%)")
    print("=========================================================")

    if total_passes == total_tests:
        print("[SUCCESS] All AI ML Models and Edge Cases Validated Successfully!")
        return 0
    else:
        print("[ERROR] Some tests failed!")
        return 1

if __name__ == "__main__":
    sys.exit(run_tests())
