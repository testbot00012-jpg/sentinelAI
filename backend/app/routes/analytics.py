from fastapi import APIRouter, Depends, HTTPException
from app.db.database import get_db
from app.schemas.schemas import DeviceTelemetryRequest
from app.core.security import get_current_user
import datetime
from typing import Dict, Any

router = APIRouter(prefix="/api/analytics", tags=["Device Diagnostics & Analytics"])

@router.post("/telemetry")
async def update_telemetry(req: DeviceTelemetryRequest, current_user: dict = Depends(get_current_user), db = Depends(get_db)):
    # Find existing device stats or create new
    stat = await db["device_stats"].find_one({"user_id": current_user["_id"]})
    if not stat:
        stat = {
            "user_id": current_user["_id"],
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
                "device_model": req.device_model or stat.get("device_model"),
                "os_version": req.os_version or stat.get("os_version"),
                "security_score": req.security_score,
                "battery_health": req.battery_health or stat.get("battery_health"),
                "ram_usage_percent": req.ram_usage_percent or stat.get("ram_usage_percent"),
                "storage_usage_percent": req.storage_usage_percent or stat.get("storage_usage_percent"),
                "updated_at": datetime.datetime.utcnow()
            }}
        )

    # Log minor warnings for low metrics
    if req.security_score < 70:
        warning_exist = await db["threat_logs"].find_one({
            "user_id": current_user["_id"],
            "threat_type": "Low Security Score",
            "resolved": False
        })
        if not warning_exist:
            threat = {
                "user_id": current_user["_id"],
                "threat_type": "Low Security Score",
                "severity": "Medium",
                "source": "System Daemon",
                "description": f"Device security score dropped below threshold: {req.security_score}%",
                "resolved": False,
                "detected_at": datetime.datetime.utcnow()
            }
            await db["threat_logs"].insert_one(threat)

    return {"message": "Device diagnostics telemetry compiled successfully"}


@router.get("/metrics")
async def get_user_metrics(current_user: dict = Depends(get_current_user), db = Depends(get_db)):
    # Retrieve all metrics with resilient fallback
    try:
        url_scans_count = await db["url_scans"].count_documents({"user_id": current_user["_id"]})
        fraud_scans_count = await db["fraud_scans"].count_documents({"user_id": current_user["_id"]})
        device = await db["device_stats"].find_one({"user_id": current_user["_id"]})
        threats_blocked = await db["threat_logs"].count_documents({"user_id": current_user["_id"]})
        phishing_count = await db["url_scans"].count_documents({"user_id": current_user["_id"], "status": "Phishing"})
        scam_count = await db["fraud_scans"].count_documents({"user_id": current_user["_id"], "classification": {"$regex": "Scam", "$options": "i"}})
        malware_count = await db["threat_logs"].count_documents({"user_id": current_user["_id"], "threat_type": "Malicious APK"})
    except Exception as db_err:
        print(f"[Warning] Metrics DB query failed: {db_err}")
        url_scans_count = 0
        fraud_scans_count = 0
        device = None
        threats_blocked = 0
        phishing_count = 0
        scam_count = 0
        malware_count = 0

    security_score = device.get("security_score", 95) if device else 95

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
        {"name": "Malware APks", "value": malware_count}
    ]

    return {
        "summary": {
            "security_score": security_score,
            "threats_blocked": threats_blocked,
            "total_scans": url_scans_count + fraud_scans_count,
            "device_health": {
                "battery": device.get("battery_health", 88) if device else 88,
                "ram": device.get("ram_usage_percent", 45.5) if device else 45.5,
                "storage": device.get("storage_usage_percent", 62.0) if device else 62.0,
                "model": device.get("device_model", "Web Browser Client") if device else "Web Browser Client"
            }
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
