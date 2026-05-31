from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from app.db.database import get_db
from app.models.models import User, DeviceStat, ThreatLog, URLScan, FraudScan, SecurityReport
from app.schemas.schemas import DeviceTelemetryRequest
from app.core.security import get_current_user
import datetime
from typing import Dict, Any

router = APIRouter(prefix="/api/analytics", tags=["Device Diagnostics & Analytics"])

@router.post("/telemetry")
def update_telemetry(req: DeviceTelemetryRequest, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    # Find existing device stats or create new
    stat = db.query(DeviceStat).filter(DeviceStat.user_id == current_user.id).first()
    if not stat:
        stat = DeviceStat(user_id=current_user.id)
        db.add(stat)

    stat.device_model = req.device_model or stat.device_model
    stat.os_version = req.os_version or stat.os_version
    stat.security_score = req.security_score
    stat.battery_health = req.battery_health or stat.battery_health
    stat.ram_usage_percent = req.ram_usage_percent or stat.ram_usage_percent
    stat.storage_usage_percent = req.storage_usage_percent or stat.storage_usage_percent
    stat.updated_at = datetime.datetime.utcnow()

    # Log minor warnings for low metrics
    if req.security_score < 70:
        warning_exist = db.query(ThreatLog).filter(
            ThreatLog.user_id == current_user.id,
            ThreatLog.threat_type == "Low Security Score",
            ThreatLog.resolved == False
        ).first()
        if not warning_exist:
            threat = ThreatLog(
                user_id=current_user.id,
                threat_type="Low Security Score",
                severity="Medium",
                source="System Daemon",
                description=f"Device security score dropped below threshold: {req.security_score}%"
            )
            db.add(threat)

    db.commit()
    return {"message": "Device diagnostics telemetry compiled successfully"}


@router.get("/metrics")
def get_user_metrics(current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    # Retrieve all metrics for charts and dashboards
    url_scans_count = db.query(URLScan).filter(URLScan.user_id == current_user.id).count()
    fraud_scans_count = db.query(FraudScan).filter(FraudScan.user_id == current_user.id).count()
    unresolved_threats = db.query(ThreatLog).filter(ThreatLog.user_id == current_user.id, ThreatLog.resolved == False).count()
    device = db.query(DeviceStat).filter(DeviceStat.user_id == current_user.id).first()

    # Create dummy trends data for graphs
    security_trends = [
        {"day": "Mon", "score": 95},
        {"day": "Tue", "score": 92},
        {"day": "Wed", "score": 88},
        {"day": "Thu", "score": 90},
        {"day": "Fri", "score": 94},
        {"day": "Sat", "score": 98},
        {"day": "Sun", "score": device.security_score if device else 95}
    ]

    threat_distribution = [
        {"name": "Phishing URLs", "value": db.query(URLScan).filter(URLScan.user_id == current_user.id, URLScan.status == "Phishing").count()},
        {"name": "Scam SMS", "value": db.query(FraudScan).filter(FraudScan.user_id == current_user.id, FraudScan.classification.contains("Scam")).count()},
        {"name": "Malware APks", "value": db.query(ThreatLog).filter(ThreatLog.user_id == current_user.id, ThreatLog.threat_type == "Malicious APK").count()}
    ]

    return {
        "summary": {
            "security_score": device.security_score if device else 95,
            "threats_blocked": db.query(ThreatLog).filter(ThreatLog.user_id == current_user.id).count(),
            "total_scans": url_scans_count + fraud_scans_count,
            "device_health": {
                "battery": device.battery_health if device else 88,
                "ram": device.ram_usage_percent if device else 45.5,
                "storage": device.storage_usage_percent if device else 62.0,
                "model": device.device_model if device else "Web Browser Client"
            }
        },
        "trends": security_trends,
        "threat_distribution": threat_distribution
    }


@router.get("/reports")
def get_pdf_reports(current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    reports = db.query(SecurityReport).filter(SecurityReport.user_id == current_user.id).all()
    return reports
