from fastapi import APIRouter, Depends, HTTPException, Header
from app.db.database import get_db
from app.schemas.schemas import DeviceTelemetryRequest
from app.core.security import get_current_user, verify_firebase_token
import datetime
import re
from typing import Dict, Any, Optional

router = APIRouter(prefix="/api/analytics", tags=["Device Diagnostics & Analytics"])

@router.post("/telemetry")
async def update_telemetry(req: DeviceTelemetryRequest, current_user: dict = Depends(get_current_user), db = Depends(get_db)):
    user_id = current_user.get("_id")
    user_email = current_user.get("email")
    match_conditions = []
    if user_id:
        match_conditions.append({"user_id": user_id})
        match_conditions.append({"user_id": str(user_id)})
    if user_email:
        match_conditions.append({"user_email": user_email})
        match_conditions.append({"user_id": user_email})

    user_query = {"$or": match_conditions} if match_conditions else {"user_email": user_email}

    # Find existing device stats or create new
    stat = await db["device_stats"].find_one(user_query)
    if not stat:
        stat = {
            "user_id": str(user_id),
            "user_email": user_email,
            "device_model": req.device_model,
            "os_version": req.os_version,
            "security_score": req.security_score,
            "battery_health": req.battery_health,
            "ram_usage_percent": req.ram_usage_percent,
            "storage_usage_percent": req.storage_usage_percent,
            "updated_at": datetime.datetime.utcnow()
        }
        await db["device_stats"].insert_one(stat)
    else:
        await db["device_stats"].update_one(
            {"_id": stat["_id"]},
            {"$set": {
                "user_email": user_email or stat.get("user_email"),
                "device_model": req.device_model or stat.get("device_model"),
                "os_version": req.os_version or stat.get("os_version"),
                "security_score": req.security_score,
                "battery_health": req.battery_health if req.battery_health is not None else stat.get("battery_health"),
                "ram_usage_percent": req.ram_usage_percent if req.ram_usage_percent is not None else stat.get("ram_usage_percent"),
                "storage_usage_percent": req.storage_usage_percent if req.storage_usage_percent is not None else stat.get("storage_usage_percent"),
                "updated_at": datetime.datetime.utcnow()
            }}
        )

    return {"message": "Device diagnostics telemetry compiled successfully"}


@router.get("/metrics")
async def get_user_metrics(
    authorization: Optional[str] = Header(None),
    x_user_email: Optional[str] = Header(None, alias="X-User-Email"),
    db = Depends(get_db)
):
    user_id = None
    user_email = (x_user_email or "").strip().lower() or None
    firebase_uid = None

    if authorization:
        try:
            token = authorization.replace("Bearer ", "").strip()
            claims = verify_firebase_token(token)
            user_id = str(claims.get("uid", ""))
            firebase_uid = user_id
            if not user_email and claims.get("email"):
                user_email = claims.get("email", "").strip().lower()
        except Exception as e:
            print(f"[Warning] metrics token decode: {e}")

    match_conditions = []
    if user_email:
        clean = user_email.strip().lower()
        match_conditions.append({"user_email": clean})
        match_conditions.append({"user_email": {"$regex": f"^{re.escape(clean)}$", "$options": "i"}})
        match_conditions.append({"user_id": clean})
        match_conditions.append({"user_id": f"user_{clean}"})
    if user_id:
        match_conditions.append({"user_id": user_id})
        match_conditions.append({"user_id": str(user_id)})
    if firebase_uid:
        match_conditions.append({"user_id": str(firebase_uid)})
        match_conditions.append({"firebase_uid": str(firebase_uid)})

    user_filter = {"$or": match_conditions} if match_conditions else None

    # If no authenticated user, strictly return empty clean state (do not show default/other scans)
    if not user_filter:
        return {
            "summary": {
                "security_score": 100,
                "threats_blocked": 0,
                "total_scans": 0,
                "device_health": {
                    "battery": None,
                    "ram": None,
                    "storage": None,
                    "model": "Awaiting Mobile Sync",
                    "synced": False
                }
            },
            "trends": [],
            "threat_distribution": [
                {"name": "Phishing URLs", "value": 0},
                {"name": "Scam SMS", "value": 0},
                {"name": "Malware APKs", "value": 0}
            ],
            "recent_threats": [],
            "recent_scans": []
        }

    # Retrieve all metrics dynamically from database across all 5 scan collections
    try:
        url_scans_count = await db["url_scans"].count_documents(user_filter) if user_filter else 0
        fraud_scans_count = await db["fraud_scans"].count_documents(user_filter) if user_filter else 0
        apk_scans_count = await db["apk_scans"].count_documents(user_filter) if user_filter else 0
        pay_scans_count = await db["payment_scans"].count_documents(user_filter) if user_filter else 0
        hist_scans_count = await db["scan_history"].count_documents(user_filter) if user_filter else 0
        device = await db["device_stats"].find_one(user_filter, sort=[("updated_at", -1)]) if user_filter else None
        threats_blocked = await db["threat_logs"].count_documents(user_filter) if user_filter else 0
        phishing_count = await db["url_scans"].count_documents({"$and": [user_filter, {"status": {"$in": ["Phishing", "Suspicious"]}}]}) if user_filter else 0
        scam_count = await db["fraud_scans"].count_documents({"$and": [user_filter, {"classification": {"$regex": "Scam", "$options": "i"}}]}) if user_filter else 0
        malware_count = await db["threat_logs"].count_documents({"$and": [user_filter, {"threat_type": {"$regex": "Malware|APK", "$options": "i"}}]}) if user_filter else 0
    except Exception as db_err:
        print(f"[Warning] Metrics DB query error: {db_err}")
        url_scans_count = 0
        fraud_scans_count = 0
        apk_scans_count = 0
        pay_scans_count = 0
        hist_scans_count = 0
        device = None
        threats_blocked = 0
        phishing_count = 0
        scam_count = 0
        malware_count = 0

    # Retrieve real recent threats from MongoDB Atlas
    recent_threats = []
    if user_filter:
        try:
            threats_cursor = db["threat_logs"].find(user_filter).sort("detected_at", -1).limit(10)
            raw_threats = await threats_cursor.to_list(10)
            for t in raw_threats:
                det_time = t.get("detected_at")
                time_str = det_time.strftime("%d %b, %I:%M %p") if isinstance(det_time, datetime.datetime) else "Recent"
                desc = t.get("description", "")
                src = desc.replace("User scanned a phishing URL: ", "").replace("User scanned a suspicious URL: ", "") or t.get("source", "Web Scanner")
                recent_threats.append({
                    "id": str(t["_id"]),
                    "time": time_str,
                    "type": t.get("threat_type", "Phishing Threat"),
                    "source": src,
                    "score": "96%" if t.get("severity") == "High" else "75%",
                    "action": "Blocked",
                    "severity": "danger" if t.get("severity") in ["High", "Critical"] else "warning"
                })
        except Exception as th_err:
            print(f"[Warning] recent_threats DB query error: {th_err}")

    # Retrieve real recent scans from MongoDB Atlas (Unified across Web & Mobile)
    recent_scans = []
    if user_filter:
        try:
            unified_scans = []

            # 1. Custom / Mobile scan_history
            c_hist = db["scan_history"].find(user_filter).sort("scanned_at", -1).limit(10)
            for s in await c_hist.to_list(10):
                sc_time = s.get("scanned_at") or s.get("created_at") or datetime.datetime.utcnow()
                time_str = sc_time.strftime("%d %b, %I:%M %p") if isinstance(sc_time, datetime.datetime) else "Recent"
                status = s.get("verdict", "Safe")
                unified_scans.append({
                    "id": str(s["_id"]),
                    "time": time_str,
                    "scan_type": s.get("scan_type", "Security Scan"),
                    "url": s.get("target", "System Inspection"),
                    "status": status,
                    "score": f"{s.get('score', 100)}%",
                    "action": "Blocked" if s.get("severity") in ["High", "Critical"] else "Permitted",
                    "_dt": sc_time if isinstance(sc_time, datetime.datetime) else datetime.datetime.min
                })

            # 2. URL scans
            c_urls = db["url_scans"].find(user_filter).sort("scanned_at", -1).limit(10)
            for s in await c_urls.to_list(10):
                sc_time = s.get("scanned_at") or datetime.datetime.utcnow()
                time_str = sc_time.strftime("%d %b, %I:%M %p") if isinstance(sc_time, datetime.datetime) else "Recent"
                status = s.get("status", "Safe")
                unified_scans.append({
                    "id": str(s["_id"]),
                    "time": time_str,
                    "scan_type": "URL Phishing Scanner",
                    "url": s.get("url", ""),
                    "status": status,
                    "score": f"{round(float(s.get('score', 5)), 1)}%",
                    "action": "Blocked" if status in ["Phishing", "Suspicious"] else "Permitted",
                    "_dt": sc_time if isinstance(sc_time, datetime.datetime) else datetime.datetime.min
                })

            # 3. Fraud / SMS scans
            c_fraud = db["fraud_scans"].find(user_filter).sort("scanned_at", -1).limit(10)
            for s in await c_fraud.to_list(10):
                sc_time = s.get("scanned_at") or datetime.datetime.utcnow()
                time_str = sc_time.strftime("%d %b, %I:%M %p") if isinstance(sc_time, datetime.datetime) else "Recent"
                prob = round(float(s.get("scam_probability", 5)))
                raw_text = s.get("content", "")
                snippet = (raw_text[:40] + "...") if len(raw_text) > 40 else raw_text
                unified_scans.append({
                    "id": str(s["_id"]),
                    "time": time_str,
                    "scan_type": f"{s.get('scan_type', 'SMS')} Scam Analyzer",
                    "url": snippet,
                    "status": s.get("classification", "Scam"),
                    "score": f"{prob}%",
                    "action": "Blocked" if prob >= 65 else "Permitted",
                    "_dt": sc_time if isinstance(sc_time, datetime.datetime) else datetime.datetime.min
                })

            # 4. Payment scans
            c_pay = db["payment_scans"].find(user_filter).sort("scanned_at", -1).limit(10)
            for s in await c_pay.to_list(10):
                sc_time = s.get("scanned_at") or datetime.datetime.utcnow()
                time_str = sc_time.strftime("%d %b, %I:%M %p") if isinstance(sc_time, datetime.datetime) else "Recent"
                amt = s.get("extracted_amount", "Receipt")
                eco = s.get("ecosystem", "UPI")
                f_score = round(float(s.get("fraud_score", 10)))
                unified_scans.append({
                    "id": str(s["_id"]),
                    "time": time_str,
                    "scan_type": "Payment Screenshot Shield",
                    "url": f"{amt} • {eco}",
                    "status": s.get("classification", "Verified"),
                    "score": f"{f_score}%",
                    "action": "Blocked" if f_score >= 60 else "Permitted",
                    "_dt": sc_time if isinstance(sc_time, datetime.datetime) else datetime.datetime.min
                })

            # Sort by date descending
            unified_scans.sort(key=lambda x: x.get("_dt", datetime.datetime.min), reverse=True)
            recent_scans = [{k: v for k, v in item.items() if k != "_dt"} for item in unified_scans[:10]]
        except Exception as sc_err:
            print(f"[Warning] recent_scans DB query error: {sc_err}")

    security_score = device.get("security_score", 98) if device else 98

    # Create trends data for graphs
    security_trends = [
        {"day": "Mon", "score": 95},
        {"day": "Tue", "score": 92},
        {"day": "Wed", "score": 88},
        {"day": "Thu", "score": 90},
        {"day": "Fri", "score": 94},
        {"day": "Sat", "score": 98},
        {"day": "Sun", "score": security_score}
    ]

    threat_distribution = [
        {"name": "Phishing URLs", "value": phishing_count},
        {"name": "Scam SMS", "value": scam_count},
        {"name": "Malware APKs", "value": malware_count}
    ]

    device_health = {
        "battery": device.get("battery_health") if device else None,
        "ram": device.get("ram_usage_percent") if device else None,
        "storage": device.get("storage_usage_percent") if device else None,
        "model": device.get("device_model", "Android Smartphone") if device else "Awaiting Mobile Sync",
        "synced": device is not None
    }

    return {
        "summary": {
            "security_score": security_score,
            "threats_blocked": threats_blocked,
            "total_scans": url_scans_count + fraud_scans_count + apk_scans_count + pay_scans_count + hist_scans_count,
            "device_health": device_health
        },
        "trends": security_trends,
        "threat_distribution": threat_distribution,
        "recent_threats": recent_threats,
        "recent_scans": recent_scans
    }


@router.get("/reports")
async def get_pdf_reports(current_user: dict = Depends(get_current_user), db = Depends(get_db)):
    cursor = db["security_reports"].find({"user_id": current_user["_id"]})
    reports = await cursor.to_list(length=100)
    # Serialize ObjectId for FastAPI JSONResponse compatibility
    for report in reports:
        report["_id"] = str(report["_id"])
        report["user_id"] = str(report["user_id"])
    return reports
