from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from app.db.database import get_db
from app.models.models import User, URLScan, FraudScan, ThreatLog
from app.schemas.schemas import (
    URLScanRequest, URLScanResponse,
    FraudScanRequest, FraudScanResponse,
    APKScanRequest, APKScanResponse
)
from app.core.security import get_current_user
from app.services.ml_engine import SentinelMLEngine
import datetime

router = APIRouter(prefix="/api/scan", tags=["Security Intelligent Scans"])
ml_engine = SentinelMLEngine()

@router.post("/url", response_model=URLScanResponse)
def scan_url(req: URLScanRequest, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    result = ml_engine.analyze_url(req.url)
    
    # Save to scan history
    db_scan = URLScan(
        user_id=current_user.id,
        url=result["url"],
        status=result["status"],
        score=result["score"],
        details=result["details"]
    )
    db.add(db_scan)

    # Log threat if suspicious or phishing
    if result["status"] in ["Suspicious", "Phishing"]:
        threat = ThreatLog(
            user_id=current_user.id,
            threat_type="Phishing URL",
            severity="Medium" if result["status"] == "Suspicious" else "High",
            source="Web Scanner",
            description=f"User scanned a {result['status'].lower()} URL: {result['url']}"
        )
        db.add(threat)

    db.commit()
    db.refresh(db_scan)
    
    return {
        "url": db_scan.url,
        "status": db_scan.status,
        "score": db_scan.score,
        "details": db_scan.details,
        "scanned_at": db_scan.scanned_at
    }


@router.post("/fraud", response_model=FraudScanResponse)
def scan_fraud(req: FraudScanRequest, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    result = ml_engine.analyze_sms_or_email(req.content)
    
    # Save to history
    db_scan = FraudScan(
        user_id=current_user.id,
        scan_type=req.scan_type,
        content=req.content,
        scam_probability=result["scam_probability"],
        classification=result["classification"],
        explanation=result["explanation"]
    )
    db.add(db_scan)

    # Log threat if scam probability is high
    if result["scam_probability"] >= 65:
        threat = ThreatLog(
            user_id=current_user.id,
            threat_type="Scam Message" if req.scan_type == "SMS" else "Email Phishing",
            severity="High",
            source="Mobile Agent" if req.scan_type == "SMS" else "Web Scanner",
            description=f"Highly probable scam content detected with score {result['scam_probability']}%."
        )
        db.add(threat)

    db.commit()
    db.refresh(db_scan)

    return {
        "scan_type": db_scan.scan_type,
        "original_text": db_scan.content,
        "scam_probability": db_scan.scam_probability,
        "classification": db_scan.classification,
        "explanation": db_scan.explanation,
        "contains_link": result["contains_link"],
        "scanned_at": db_scan.scanned_at
    }


@router.post("/apk", response_model=APKScanResponse)
def scan_apk(req: APKScanRequest, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    result = ml_engine.analyze_apk_metadata(req.package_name, req.app_name, req.permissions)

    # Log threat if APK represents medium-to-high malware risk
    if result["malware_score"] >= 35:
        threat = ThreatLog(
            user_id=current_user.id,
            threat_type="Malicious APK",
            severity="Medium" if result["malware_score"] < 60 else "High" if result["malware_score"] < 80 else "Critical",
            source="Mobile Agent",
            description=f"App {req.app_name} ({req.package_name}) flagged as {result['threat_category']} with risk score of {result['malware_score']}%."
        )
        db.add(threat)

    db.commit()

    return {
        "app_name": result["app_name"],
        "package_name": result["package_name"],
        "malware_score": result["malware_score"],
        "threat_category": result["threat_category"],
        "flagged_permissions": result["flagged_permissions"],
        "total_permissions_scanned": result["total_permissions_scanned"],
        "status": result["status"]
    }
