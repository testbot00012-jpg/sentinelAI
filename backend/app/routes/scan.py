from fastapi import APIRouter, Depends, HTTPException, Header
from typing import Optional
from app.db.database import get_db
from app.schemas.schemas import (
    URLScanRequest, URLScanResponse,
    FraudScanRequest, FraudScanResponse,
    APKScanRequest, APKScanResponse
)
from app.core.security import get_current_user, verify_firebase_token
from app.services.ml_engine import SentinelMLEngine
import datetime

router = APIRouter(prefix="/api/scan", tags=["Security Intelligent Scans"])
ml_engine = SentinelMLEngine()

@router.post("/url", response_model=URLScanResponse)
async def scan_url(
    req: URLScanRequest,
    authorization: Optional[str] = Header(None),
    x_user_email: Optional[str] = Header(None, alias="X-User-Email"),
    db = Depends(get_db)
):
    result = ml_engine.analyze_url(req.url)
    
    user_id = None
    user_email = x_user_email or None
    if authorization:
        try:
            token = authorization.replace("Bearer ", "").strip()
            claims = verify_firebase_token(token)
            user_id = str(claims.get("uid", ""))
            if not user_email:
                user_email = claims.get("email", "")
        except Exception as e:
            print(f"[Warning] scan_url token decode skipped: {e}")

    if not user_id and user_email:
        user_id = f"user_{user_email}"

    # Save to scan history in MongoDB
    db_scan = {
        "user_id": user_id or "anonymous",
        "user_email": user_email or "",
        "firebase_uid": user_id or "",
        "url": result["url"],
        "status": result["status"],
        "score": result["score"],
        "details": result["details"],
        "scanned_at": datetime.datetime.utcnow()
    }
    if db is not None:
        try:
            await db["url_scans"].insert_one(db_scan)
            if result["status"] in ["Suspicious", "Phishing"]:
                threat = {
                    "user_id": user_id or "anonymous",
                    "user_email": user_email or "",
                    "firebase_uid": user_id or "",
                    "threat_type": "Phishing URL",
                    "severity": "Medium" if result["status"] == "Suspicious" else "High",
                    "source": "Web Scanner",
                    "description": f"User scanned a {result['status'].lower()} URL: {result['url']}",
                    "resolved": False,
                    "detected_at": datetime.datetime.utcnow()
                }
                await db["threat_logs"].insert_one(threat)
        except Exception as db_err:
            print(f"[Warning] scan_url DB persist error: {db_err}")
    
    return {
        "url": db_scan["url"],
        "status": db_scan["status"],
        "score": db_scan["score"],
        "details": db_scan["details"],
        "scanned_at": db_scan["scanned_at"]
    }


@router.post("/fraud", response_model=FraudScanResponse)
async def scan_fraud(
    req: FraudScanRequest,
    authorization: Optional[str] = Header(None),
    x_user_email: Optional[str] = Header(None, alias="X-User-Email"),
    db = Depends(get_db)
):
    result = ml_engine.analyze_sms_or_email(req.content)
    
    user_id = None
    user_email = x_user_email or None
    if authorization:
        try:
            token = authorization.replace("Bearer ", "").strip()
            claims = verify_firebase_token(token)
            user_id = str(claims.get("uid", ""))
            if not user_email:
                user_email = claims.get("email", "")
        except Exception as e:
            print(f"[Warning] scan_fraud token decode skipped: {e}")

    if not user_id and user_email:
        user_id = f"user_{user_email}"

    # Save to history in MongoDB
    db_scan = {
        "user_id": user_id or "anonymous",
        "user_email": user_email or "",
        "firebase_uid": user_id or "",
        "scan_type": req.scan_type,
        "content": req.content,
        "scam_probability": result["scam_probability"],
        "classification": result["classification"],
        "explanation": result["explanation"],
        "scanned_at": datetime.datetime.utcnow()
    }
    if db is not None:
        try:
            await db["fraud_scans"].insert_one(db_scan)
            if result["scam_probability"] >= 65:
                threat = {
                    "user_id": user_id or "anonymous",
                    "user_email": user_email or "",
                    "firebase_uid": user_id or "",
                    "threat_type": "Scam Message" if req.scan_type == "SMS" else "Email Phishing",
                    "severity": "High",
                    "source": "Mobile Agent" if req.scan_type == "SMS" else "Web Scanner",
                    "description": f"Highly probable scam content detected with score {result['scam_probability']}%.",
                    "resolved": False,
                    "detected_at": datetime.datetime.utcnow()
                }
                await db["threat_logs"].insert_one(threat)
        except Exception as db_err:
            print(f"[Warning] scan_fraud DB persist error: {db_err}")

    return {
        "scan_type": db_scan["scan_type"],
        "original_text": db_scan["content"],
        "scam_probability": db_scan["scam_probability"],
        "classification": db_scan["classification"],
        "explanation": db_scan["explanation"],
        "contains_link": result["contains_link"],
        "scanned_at": db_scan["scanned_at"]
    }


@router.post("/apk", response_model=APKScanResponse)
async def scan_apk(req: APKScanRequest, current_user: dict = Depends(get_current_user), db = Depends(get_db)):
    result = ml_engine.analyze_apk_metadata(req.package_name, req.app_name, req.permissions)

    user_id = current_user.get("_id")
    user_email = current_user.get("email")
    firebase_uid = current_user.get("firebase_uid", "")

    # Save to apk scan history and log threat if APK represents medium-to-high malware risk
    if db is not None:
        try:
            db_scan = {
                "user_id": str(user_id) if user_id else "anonymous",
                "user_email": user_email or "",
                "firebase_uid": firebase_uid or "",
                "app_name": req.app_name,
                "package_name": req.package_name,
                "malware_score": result["malware_score"],
                "threat_category": result["threat_category"],
                "flagged_permissions": result["flagged_permissions"],
                "total_permissions_scanned": result["total_permissions_scanned"],
                "status": result["status"],
                "scanned_at": datetime.datetime.utcnow()
            }
            await db["apk_scans"].insert_one(db_scan)

            if result["malware_score"] >= 35:
                threat = {
                    "user_id": str(user_id) if user_id else "anonymous",
                    "user_email": user_email or "",
                    "firebase_uid": firebase_uid or "",
                    "threat_type": "Malicious APK",
                    "severity": "Medium" if result["malware_score"] < 60 else "High" if result["malware_score"] < 80 else "Critical",
                    "source": "Mobile Agent",
                    "description": f"App {req.app_name} ({req.package_name}) flagged as {result['threat_category']} with risk score of {result['malware_score']}%.",
                    "resolved": False,
                    "detected_at": datetime.datetime.utcnow()
                }
                await db["threat_logs"].insert_one(threat)
        except Exception as db_err:
            print(f"[Warning] scan_apk DB persist error: {db_err}")

    return {
        "app_name": result["app_name"],
        "package_name": result["package_name"],
        "malware_score": result["malware_score"],
        "threat_category": result["threat_category"],
        "flagged_permissions": result["flagged_permissions"],
        "total_permissions_scanned": result["total_permissions_scanned"],
        "status": result["status"]
    }
