import asyncio
import sys
from bson import ObjectId
from app.db.database import get_db
from app.routes.scan import scan_url, scan_fraud, record_scan_history_event, get_scan_history, delete_scan_history_item, clear_all_scan_history
from app.routes.analytics import get_user_metrics
from app.schemas.schemas import URLScanRequest, FraudScanRequest, SecurityHistoryRecordRequest

TEST_USER_EMAIL = "sync_test_agent@sentinel.ai"

async def run_sync_verification():
    print("=" * 65)
    print("   Sentinel AI Web & Android Realtime Sync Verification Suite   ")
    print("=" * 65)

    db = await get_db()
    if db is None:
        print("[FAIL] Database is not connected.")
        return 1

    # Step 0: Clean slate for test user
    print("\n[Step 0] Cleaning test slate for user:", TEST_USER_EMAIL)
    clear_res = await clear_all_scan_history(authorization=None, x_user_email=TEST_USER_EMAIL, db=db)
    print(f"    --> Initial cleared records: {clear_res.get('deleted_count', 0)}")

    metrics_0 = await get_user_metrics(authorization=None, x_user_email=TEST_USER_EMAIL, db=db)
    initial_total = metrics_0["summary"]["total_scans"]
    print(f"    --> Baseline Total Scans: {initial_total}")
    assert initial_total == 0, f"Expected 0 scans, got {initial_total}"

    # Step 1: Web Scans a Suspicious URL
    print("\n[Step 1] Simulating Web user scanning a URL...")
    url_req = URLScanRequest(url="http://chase-bank-verify-account.top/login")
    url_res = await scan_url(req=url_req, authorization=None, x_user_email=TEST_USER_EMAIL, db=db)
    print(f"    [PASS] URL scanned: {url_res['url']} -> Verdict: {url_res['status']}")

    # Step 2: Check Realtime Sync on Android / Backend History & Metrics
    print("\n[Step 2] Checking Realtime Sync on Android / Backend Metrics...")
    metrics_1 = await get_user_metrics(authorization=None, x_user_email=TEST_USER_EMAIL.upper(), db=db)
    total_1 = metrics_1["summary"]["total_scans"]
    print(f"    --> Total Scans after Web URL Scan (queried with uppercase email): {total_1}")
    assert total_1 == 1, f"Expected 1 scan, got {total_1}"

    hist_1 = await get_scan_history(authorization=None, x_user_email=TEST_USER_EMAIL, db=db)
    print(f"    --> History items count: {len(hist_1['history'])}, Total reported: {hist_1['total']}")
    assert hist_1["total"] == 1, f"Expected total 1, got {hist_1['total']}"
    assert len(hist_1["history"]) == 1, "Expected 1 history item"
    web_scan_id = hist_1["history"][0]["id"]
    print(f"    [PASS] Web scan synced with ID: {web_scan_id}")

    # Step 3: Android Scans an SMS / Fraud Message
    print("\n[Step 3] Simulating Android App user scanning an SMS message...")
    sms_req = FraudScanRequest(content="URGENT: Your bank account is locked! Click http://phish.xyz", scan_type="SMS")
    sms_res = await scan_fraud(req=sms_req, authorization=None, x_user_email=TEST_USER_EMAIL, db=db)
    print(f"    [PASS] SMS scanned -> Classification: {sms_res['classification']}")

    # Step 4: Android Quick Scan recorded via SecurityHistoryManager
    print("\n[Step 4] Simulating Android App recording Quick Scan in SecurityHistoryManager...")
    hist_req = SecurityHistoryRecordRequest(
        scan_type="Quick Security Scan",
        target="Device Posture & Settings",
        verdict="Healthy",
        score=95,
        severity="Safe"
    )
    hist_res = await record_scan_history_event(req=hist_req, authorization=None, x_user_email=TEST_USER_EMAIL, db=db)
    mobile_scan_id = hist_res["id"]
    print(f"    [PASS] Mobile Quick Scan recorded with ID: {mobile_scan_id}")

    # Step 5: Verify Total Scans & History reflect both Web and App
    print("\n[Step 5] Verifying unified count and history reflect all Web & Mobile scans...")
    metrics_2 = await get_user_metrics(authorization=None, x_user_email=TEST_USER_EMAIL, db=db)
    total_2 = metrics_2["summary"]["total_scans"]
    print(f"    --> Total Scans across Web & Mobile: {total_2}")
    assert total_2 == 3, f"Expected 3 scans, got {total_2}"

    hist_2 = await get_scan_history(authorization=None, x_user_email=TEST_USER_EMAIL, db=db)
    print(f"    --> History records found: {len(hist_2['history'])}, Total reported: {hist_2['total']}")
    assert hist_2["total"] == 3, f"Expected 3, got {hist_2['total']}"
    assert len(hist_2["history"]) == 3, f"Expected 3 items, got {len(hist_2['history'])}"

    # Step 6: Delete an item on Android / Web and verify instant reflection
    print(f"\n[Step 6] Deleting Web scan ({web_scan_id}) from history...")
    del_res = await delete_scan_history_item(item_id=web_scan_id, authorization=None, x_user_email=TEST_USER_EMAIL, db=db)
    print(f"    --> Delete response: {del_res}")
    assert del_res["deleted"], f"Expected item {web_scan_id} to be deleted"

    # Step 7: Verify count decrements immediately and item is gone across both Web & Mobile...")
    metrics_3 = await get_user_metrics(authorization=None, x_user_email=TEST_USER_EMAIL, db=db)
    total_3 = metrics_3["summary"]["total_scans"]
    print(f"    --> New Total Scans after deletion: {total_3}")
    assert total_3 == 2, f"Expected 2 scans, got {total_3}"

    hist_3 = await get_scan_history(authorization=None, x_user_email=TEST_USER_EMAIL, db=db)
    remaining_ids = [item["id"] for item in hist_3["history"]]
    print(f"    --> Remaining IDs: {remaining_ids}")
    assert web_scan_id not in remaining_ids, f"Deleted ID {web_scan_id} should not be in history"
    assert hist_3["total"] == 2, f"Expected total 2, got {hist_3['total']}"
    print("    [PASS] Item cleanly removed and total count decremented in real-time.")

    # Step 8: Clear all history and verify both sides become 0
    print("\n[Step 8] Clearing all scan history...")
    clear_final = await clear_all_scan_history(authorization=None, x_user_email=TEST_USER_EMAIL, db=db)
    print(f"    --> Cleared items count: {clear_final.get('deleted_count', 0)}")

    metrics_final = await get_user_metrics(authorization=None, x_user_email=TEST_USER_EMAIL, db=db)
    total_final = metrics_final["summary"]["total_scans"]
    print(f"    --> Final Total Scans after clear-all: {total_final}")
    assert total_final == 0, f"Expected 0 scans, got {total_final}"

    hist_final = await get_scan_history(authorization=None, x_user_email=TEST_USER_EMAIL, db=db)
    assert len(hist_final["history"]) == 0, "Expected empty history"
    assert hist_final["total"] == 0, "Expected total 0"
    print("    [PASS] Both Web and Mobile return 0 scans and empty history.")

    print("\n" + "=" * 65)
    print("   ALL REALTIME SYNC TESTS PASSED SUCCESSFULLY! (100.0%)   ")
    print("=" * 65)
    return 0

if __name__ == "__main__":
    sys.exit(asyncio.run(run_sync_verification()))
