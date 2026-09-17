from fastapi import APIRouter, Depends, HTTPException
from app.db.database import get_db
from app.schemas.schemas import DeviceTelemetryRequest
from app.core.security import get_current_user
import datetime
from typing import Dict, Any

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
async def get_user_metrics(current_user: dict = Depends(get_current_user), db = Depends(get_db)):
    user_id = current_user.get("_id")
    user_email = current_user.get("email")
    match_conditions = []
    if user_id:
        match_conditions.append({"user_id": user_id})
        match_conditions.append({"user_id": str(user_id)})
    if user_email:
        match_conditions.append({"user_email": user_email})
        match_conditions.append({"user_id": user_email})
        match_conditions.append({"user_id": f"user_{user_email}"})

    user_filter = {"$or": match_conditions} if match_conditions else {}

    # Retrieve all metrics dynamically from database
    try:
        url_scans_count = await db["url_scans"].count_documents(user_filter)
        fraud_scans_count = await db["fraud_scans"].count_documents(user_filter)
        device = await db["device_stats"].find_one(user_filter, sort=[("updated_at", -1)])
        threats_blocked = await db["threat_logs"].count_documents(user_filter)
        phishing_count = await db["url_scans"].count_documents({"$and": [user_filter, {"status": {"$in": ["Phishing", "Suspicious"]}}]})
        scam_count = await db["fraud_scans"].count_documents({"$and": [user_filter, {"classification": {"$regex": "Scam", "$options": "i"}}]})
        malware_count = await db["threat_logs"].count_documents({"$and": [user_filter, {"threat_type": {"$regex": "Malware|APK", "$options": "i"}}]})
    except Exception as db_err:
        print(f"[Warning] Metrics DB query error: {db_err}")
        url_scans_count = 0
        fraud_scans_count = 0
        device = None
        threats_blocked = 0
        phishing_count = 0
        scam_count = 0
        malware_count = 0

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
            "total_scans": url_scans_count + fraud_scans_count,
            "device_health": device_health
        },
        "trends": security_trends,
        "threat_distribution": threat_distribution
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
