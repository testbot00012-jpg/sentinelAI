from fastapi import APIRouter, Depends, HTTPException, Header
from typing import Optional, List, Dict, Any
from app.db.database import get_db
from app.schemas.schemas import (
    URLScanRequest, URLScanResponse,
    FraudScanRequest, FraudScanResponse,
    APKScanRequest, APKScanResponse,
    PaymentScanRequest, PaymentScanResponse,
    QRScanRequest, QRScanResponse,
    DeviceScanRequest, DeviceScanResponse,
    NetworkScanRequest, NetworkScanResponse,
    QuickScanRequest, QuickScanResponse,
    FullScanRequest, FullScanResponse,
    ModelStatusResponse,
    SecurityHistoryRecordRequest, SecurityHistoryItemResponse, SecurityHistoryListResponse
)
from app.core.security import get_current_user, verify_firebase_token
from app.services.ml_engine import SentinelMLEngine
from bson import ObjectId
import datetime
import re

router = APIRouter(prefix="/api/scan", tags=["Security Intelligent Scans"])
ml_engine = SentinelMLEngine()

# ============================================================================
# 1. URL PHISHING SCANNER (Screen 11)
# ============================================================================

@router.post("/url", response_model=URLScanResponse)
async def scan_url(
    req: URLScanRequest,
    authorization: Optional[str] = Header(None),
    x_user_email: Optional[str] = Header(None, alias="X-User-Email"),
    db = Depends(get_db)
):
    result = ml_engine.analyze_url(req.url)
    
    user_id = None
    user_email = (x_user_email or "").strip().lower() or None
    if authorization:
        try:
            token = authorization.replace("Bearer ", "").strip()
            claims = verify_firebase_token(token)
            user_id = str(claims.get("uid", ""))
            if not user_email and claims.get("email"):
                user_email = claims.get("email", "").strip().lower()
        except Exception as e:
            pass

    if not user_id and user_email:
        user_id = f"user_{user_email}"

    scan_id = str(ObjectId())
    db_scan = {
        "_id": ObjectId(scan_id),
        "user_id": user_id or "anonymous",
        "user_email": user_email or "",
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
                    "scan_id": scan_id,
                    "user_id": user_id or "anonymous",
                    "user_email": user_email or "",
                    "threat_type": "Phishing URL",
                    "severity": "Medium" if result["status"] == "Suspicious" else "High",
                    "source": "URL Scanner",
                    "description": f"User scanned a {result['status'].lower()} URL: {result['url']}",
                    "resolved": False,
                    "detected_at": datetime.datetime.utcnow()
                }
                await db["threat_logs"].insert_one(threat)
        except Exception:
            pass
    
    return {
        "url": db_scan["url"],
        "status": db_scan["status"],
        "score": db_scan["score"],
        "details": db_scan["details"],
        "scanned_at": db_scan["scanned_at"]
    }

# ============================================================================
# 2. SMS / EMAIL SCAM ANALYZER (Screen 13)
# ============================================================================

@router.post("/fraud", response_model=FraudScanResponse)
async def scan_fraud(
    req: FraudScanRequest,
    authorization: Optional[str] = Header(None),
    x_user_email: Optional[str] = Header(None, alias="X-User-Email"),
    db = Depends(get_db)
):
    result = ml_engine.analyze_sms_or_email(req.content)
    
    user_id = None
    user_email = (x_user_email or "").strip().lower() or None
    if authorization:
        try:
            token = authorization.replace("Bearer ", "").strip()
            claims = verify_firebase_token(token)
            user_id = str(claims.get("uid", ""))
            if not user_email and claims.get("email"):
                user_email = claims.get("email", "").strip().lower()
        except Exception:
            pass

    if not user_id and user_email:
        user_id = f"user_{user_email}"

    scan_id = str(ObjectId())
    db_scan = {
        "_id": ObjectId(scan_id),
        "user_id": user_id or "anonymous",
        "user_email": user_email or "",
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
                    "scan_id": scan_id,
                    "user_id": user_id or "anonymous",
                    "user_email": user_email or "",
                    "threat_type": "Scam Message" if req.scan_type == "SMS" else "Email Phishing",
                    "severity": "High",
                    "source": "Scam Analyzer",
                    "description": f"Highly probable scam detected with score {result['scam_probability']}%.",
                    "resolved": False,
                    "detected_at": datetime.datetime.utcnow()
                }
                await db["threat_logs"].insert_one(threat)
        except Exception:
            pass

    return {
        "scan_type": db_scan["scan_type"],
        "original_text": db_scan["content"],
        "scam_probability": db_scan["scam_probability"],
        "classification": db_scan["classification"],
        "explanation": db_scan["explanation"],
        "contains_link": result["contains_link"],
        "scanned_at": db_scan["scanned_at"]
    }

# ============================================================================
# 3. ANDROID APK MALWARE & RISK DETAILS (Screens 07, 08)
# ============================================================================

@router.post("/apk", response_model=APKScanResponse)
async def scan_apk(
    req: APKScanRequest,
    authorization: Optional[str] = Header(None),
    x_user_email: Optional[str] = Header(None, alias="X-User-Email"),
    db = Depends(get_db)
):
    result = ml_engine.analyze_apk_metadata(
        package_name=req.package_name,
        app_name=req.app_name,
        permissions=req.permissions,
        installer=req.installer,
        is_system=req.is_system,
        version=req.version
    )

    user_id = None
    user_email = (x_user_email or "").strip().lower() or None
    if authorization:
        try:
            token = authorization.replace("Bearer ", "").strip()
            claims = verify_firebase_token(token)
            user_id = str(claims.get("uid", ""))
            if not user_email and claims.get("email"):
                user_email = claims.get("email", "").strip().lower()
        except Exception:
            pass

    if not user_id and user_email:
        user_id = f"user_{user_email}"

    scan_id = str(ObjectId())
    if db is not None:
        try:
            db_scan = {
                "_id": ObjectId(scan_id),
                "user_id": user_id or "anonymous",
                "user_email": user_email or "",
                "app_name": req.app_name,
                "package_name": req.package_name,
                "malware_score": result["malware_score"],
                "threat_category": result["threat_category"],
                "flagged_permissions": result["flagged_permissions"],
                "total_permissions_scanned": result["total_permissions_scanned"],
                "status": result["status"],
                "origin": result.get("origin", "Google Play Store"),
                "scanned_at": datetime.datetime.utcnow()
            }
            await db["apk_scans"].insert_one(db_scan)

            if result["malware_score"] >= 50:
                threat = {
                    "scan_id": scan_id,
                    "user_id": user_id or "anonymous",
                    "user_email": user_email or "",
                    "threat_type": "Malicious APK",
                    "severity": "Medium" if result["malware_score"] < 60 else "High" if result["malware_score"] < 80 else "Critical",
                    "source": "App Security Auditor",
                    "description": f"App {req.app_name} ({req.package_name}) flagged as {result['threat_category']} with risk score of {result['malware_score']}%.",
                    "resolved": False,
                    "detected_at": datetime.datetime.utcnow()
                }
                await db["threat_logs"].insert_one(threat)
        except Exception:
            pass

    return result



# ============================================================================
# 4. PAYMENT SCREENSHOT ANALYZER (Screen 14)
# ============================================================================

@router.post("/payment", response_model=PaymentScanResponse)
async def scan_payment_screenshot(
    req: PaymentScanRequest,
    authorization: Optional[str] = Header(None),
    x_user_email: Optional[str] = Header(None, alias="X-User-Email"),
    db = Depends(get_db)
):
    result = ml_engine.analyze_payment_screenshot(req.ocr_text, req.metadata)

    user_id = None
    user_email = (x_user_email or "").strip().lower() or None
    if authorization:
        try:
            token = authorization.replace("Bearer ", "").strip()
            claims = verify_firebase_token(token)
            user_id = str(claims.get("uid", ""))
            if not user_email and claims.get("email"):
                user_email = claims.get("email", "").strip().lower()
        except Exception:
            pass

    if not user_id and user_email:
        user_id = f"user_{user_email}"

    scan_id = str(ObjectId())
    if db is not None:
        try:
            db_record = {
                "_id": ObjectId(scan_id),
                "user_id": user_id or "anonymous",
                "user_email": user_email or "",
                "extracted_amount": result["extracted_amount"],
                "extracted_reference": result["extracted_reference"],
                "ecosystem": result["ecosystem"],
                "fraud_score": result["fraud_score"],
                "classification": result["classification"],
                "scanned_at": datetime.datetime.utcnow()
            }
            await db["payment_scans"].insert_one(db_record)

            if result["fraud_score"] >= 60:
                threat = {
                    "scan_id": scan_id,
                    "user_id": user_id or "anonymous",
                    "user_email": user_email or "",
                    "threat_type": "Fake Payment Receipt",
                    "severity": "High" if result["fraud_score"] < 80 else "Critical",
                    "source": "Payment Shield",
                    "description": f"Potential fake screenshot detected for {result['extracted_amount']} ({result['ecosystem']}).",
                    "resolved": False,
                    "detected_at": datetime.datetime.utcnow()
                }
                await db["threat_logs"].insert_one(threat)
        except Exception:
            pass

    return result

# ============================================================================
# 5. QR CODE SCANNER (Screen 12)
# ============================================================================

@router.post("/qr", response_model=QRScanResponse)
async def scan_qr_code(
    req: QRScanRequest,
    authorization: Optional[str] = Header(None),
    db = Depends(get_db)
):
    result = ml_engine.analyze_qr(req.payload)
    return result

# ============================================================================
# 6. DEVICE SECURITY SIGNALS AUDITOR (Screen 10)
# ============================================================================

@router.post("/device", response_model=DeviceScanResponse)
async def scan_device_security(
    req: DeviceScanRequest,
    authorization: Optional[str] = Header(None),
    db = Depends(get_db)
):
    signals = req.dict()
    result = ml_engine.analyze_device_security(signals)
    return result

# ============================================================================
# 7. NETWORK SECURITY CHECKER (Screen 15)
# ============================================================================

@router.post("/network", response_model=NetworkScanResponse)
async def scan_network_security(
    req: NetworkScanRequest,
    authorization: Optional[str] = Header(None),
    db = Depends(get_db)
):
    info = req.dict()
    result = ml_engine.analyze_network_security(info)
    return result

# ============================================================================
# 8. QUICK SCAN (Screen 05)
# ============================================================================

@router.post("/quick", response_model=QuickScanResponse)
async def run_quick_scan(
    req: QuickScanRequest,
    authorization: Optional[str] = Header(None),
    db = Depends(get_db)
):
    result = ml_engine.perform_quick_scan(req.device_signals or {}, req.apps_sample or [])
    return result

# ============================================================================
# 9. FULL SECURITY SCAN (Screen 06)
# ============================================================================

@router.post("/full", response_model=FullScanResponse)
async def run_full_scan(
    req: FullScanRequest,
    authorization: Optional[str] = Header(None),
    db = Depends(get_db)
):
    result = ml_engine.perform_full_scan(
        device_signals=req.device_signals or {},
        installed_apps=req.installed_apps or [],
        network_info=req.network_info or {},
        urls_history=req.urls_history or []
    )
    return result

# ============================================================================
# 10. MODEL & AI STATUS (Screen 26)
# ============================================================================

@router.get("/models/status", response_model=ModelStatusResponse)
async def get_models_status():
    """Returns telemetry, versioning, integrity, and offline readiness of all Sentinel AI models."""
    return ml_engine.get_models_telemetry()

# ============================================================================
# 11. UNIFIED SECURITY SCAN HISTORY (SHARED ACROSS APP & WEB)
# ============================================================================

def _build_user_query(user_email: Optional[str], user_id: Optional[str]) -> Optional[Dict[str, Any]]:
    match_conditions = []
    if user_email:
        clean = user_email.strip().lower()
        match_conditions.append({"user_email": clean})
        match_conditions.append({"user_email": {"$regex": f"^{re.escape(clean)}$", "$options": "i"}})
        match_conditions.append({"user_id": clean})
        match_conditions.append({"user_id": f"user_{clean}"})
    if user_id:
        match_conditions.append({"user_id": str(user_id)})
    return {"$or": match_conditions} if match_conditions else None

@router.get("/history", response_model=SecurityHistoryListResponse)
async def get_scan_history(
    authorization: Optional[str] = Header(None),
    x_user_email: Optional[str] = Header(None, alias="X-User-Email"),
    db = Depends(get_db)
):
    user_id = None
    user_email = (x_user_email or "").strip().lower() or None
    if authorization:
        try:
            token = authorization.replace("Bearer ", "").strip()
            claims = verify_firebase_token(token)
            user_id = str(claims.get("uid", ""))
            if not user_email and claims.get("email"):
                user_email = claims.get("email", "").strip().lower()
        except Exception:
            pass

    user_query = _build_user_query(user_email, user_id)
    if not user_query:
        return {
            "history": [],
            "total": 0
        }
    all_items = []

    if db is not None:
        try:
            # 1. Custom / Mobile scan_history entries
            cursor_hist = db["scan_history"].find(user_query).sort("scanned_at", -1).limit(50)
            for doc in await cursor_hist.to_list(50):
                sc_time = doc.get("scanned_at") or doc.get("created_at") or datetime.datetime.utcnow()
                time_str = sc_time.strftime("%d %b, %I:%M %p") if isinstance(sc_time, datetime.datetime) else str(sc_time)
                all_items.append({
                    "id": str(doc["_id"]),
                    "scan_type": doc.get("scan_type", "Security Scan"),
                    "target": doc.get("target", "Target System"),
                    "verdict": doc.get("verdict", "Safe"),
                    "score": int(doc.get("score", 100)),
                    "severity": doc.get("severity", "Safe"),
                    "timestamp": time_str,
                    "created_at": sc_time.isoformat() if isinstance(sc_time, datetime.datetime) else str(sc_time),
                    "_dt": sc_time if isinstance(sc_time, datetime.datetime) else datetime.datetime.min
                })

            # 2. URL scans
            cursor_urls = db["url_scans"].find(user_query).sort("scanned_at", -1).limit(50)
            for doc in await cursor_urls.to_list(50):
                sc_time = doc.get("scanned_at") or datetime.datetime.utcnow()
                time_str = sc_time.strftime("%d %b, %I:%M %p") if isinstance(sc_time, datetime.datetime) else str(sc_time)
                status = doc.get("status", "Safe")
                score_num = round(float(doc.get("score", 5)))
                all_items.append({
                    "id": str(doc["_id"]),
                    "scan_type": "URL Phishing Scanner",
                    "target": doc.get("url", ""),
                    "verdict": status,
                    "score": score_num,
                    "severity": "Critical" if status == "Phishing" else ("Medium" if status == "Suspicious" else "Safe"),
                    "timestamp": time_str,
                    "created_at": sc_time.isoformat() if isinstance(sc_time, datetime.datetime) else str(sc_time),
                    "_dt": sc_time if isinstance(sc_time, datetime.datetime) else datetime.datetime.min
                })

            # 3. Fraud / SMS scans
            cursor_fraud = db["fraud_scans"].find(user_query).sort("scanned_at", -1).limit(50)
            for doc in await cursor_fraud.to_list(50):
                sc_time = doc.get("scanned_at") or datetime.datetime.utcnow()
                time_str = sc_time.strftime("%d %b, %I:%M %p") if isinstance(sc_time, datetime.datetime) else str(sc_time)
                prob = round(float(doc.get("scam_probability", 5)))
                raw_text = doc.get("content", "")
                snippet = (raw_text[:45] + "...") if len(raw_text) > 45 else raw_text
                all_items.append({
                    "id": str(doc["_id"]),
                    "scan_type": f"{doc.get('scan_type', 'SMS')} Scam Analyzer",
                    "target": snippet,
                    "verdict": doc.get("classification", "Monitored"),
                    "score": prob,
                    "severity": "Critical" if prob >= 75 else ("High" if prob >= 50 else "Safe"),
                    "timestamp": time_str,
                    "created_at": sc_time.isoformat() if isinstance(sc_time, datetime.datetime) else str(sc_time),
                    "_dt": sc_time if isinstance(sc_time, datetime.datetime) else datetime.datetime.min
                })

            # 4. Payment scans
            cursor_pay = db["payment_scans"].find(user_query).sort("scanned_at", -1).limit(50)
            for doc in await cursor_pay.to_list(50):
                sc_time = doc.get("scanned_at") or datetime.datetime.utcnow()
                time_str = sc_time.strftime("%d %b, %I:%M %p") if isinstance(sc_time, datetime.datetime) else str(sc_time)
                amt = doc.get("extracted_amount", "Unknown")
                eco = doc.get("ecosystem", "UPI Receipt")
                f_score = round(float(doc.get("fraud_score", 10)))
                all_items.append({
                    "id": str(doc["_id"]),
                    "scan_type": "Payment Screenshot Shield",
                    "target": f"{amt} ({eco})",
                    "verdict": doc.get("classification", "Verified"),
                    "score": f_score,
                    "severity": "Critical" if f_score >= 70 else ("High" if f_score >= 40 else "Safe"),
                    "timestamp": time_str,
                    "created_at": sc_time.isoformat() if isinstance(sc_time, datetime.datetime) else str(sc_time),
                    "_dt": sc_time if isinstance(sc_time, datetime.datetime) else datetime.datetime.min
                })

            # 5. APK scans
            cursor_apk = db["apk_scans"].find(user_query).sort("scanned_at", -1).limit(50)
            for doc in await cursor_apk.to_list(50):
                sc_time = doc.get("scanned_at") or datetime.datetime.utcnow()
                time_str = sc_time.strftime("%d %b, %I:%M %p") if isinstance(sc_time, datetime.datetime) else str(sc_time)
                app_n = doc.get("app_name", "Application")
                pkg = doc.get("package_name", "")
                m_score = round(float(doc.get("malware_score", 10)))
                all_items.append({
                    "id": str(doc["_id"]),
                    "scan_type": "APK Malware Scan",
                    "target": f"{app_n} ({pkg})",
                    "verdict": doc.get("threat_category", "Safe"),
                    "score": m_score,
                    "severity": "Critical" if m_score >= 75 else ("High" if m_score >= 50 else "Safe"),
                    "timestamp": time_str,
                    "created_at": sc_time.isoformat() if isinstance(sc_time, datetime.datetime) else str(sc_time),
                    "_dt": sc_time if isinstance(sc_time, datetime.datetime) else datetime.datetime.min
                })
        except Exception as e:
            print(f"[Warning] scan_history query error: {e}")

    # Sort all entries by date descending
    all_items.sort(key=lambda x: x.get("_dt", datetime.datetime.min), reverse=True)
    results = [{k: v for k, v in item.items() if k != "_dt"} for item in all_items[:50]]

    total_count = len(results)
    if db is not None and user_query:
        try:
            total_count = (
                await db["scan_history"].count_documents(user_query) +
                await db["url_scans"].count_documents(user_query) +
                await db["fraud_scans"].count_documents(user_query) +
                await db["payment_scans"].count_documents(user_query) +
                await db["apk_scans"].count_documents(user_query)
            )
        except Exception:
            total_count = len(results)

    return {
        "history": results,
        "total": total_count
    }

@router.post("/history", response_model=SecurityHistoryItemResponse)
async def record_scan_history_event(
    req: SecurityHistoryRecordRequest,
    authorization: Optional[str] = Header(None),
    x_user_email: Optional[str] = Header(None, alias="X-User-Email"),
    db = Depends(get_db)
):
    user_id = None
    user_email = (x_user_email or "").strip().lower() or None
    if authorization:
        try:
            token = authorization.replace("Bearer ", "").strip()
            claims = verify_firebase_token(token)
            user_id = str(claims.get("uid", ""))
            if not user_email and claims.get("email"):
                user_email = claims.get("email", "").strip().lower()
        except Exception:
            pass

    if not user_id and user_email:
        user_id = f"user_{user_email}"

    now = datetime.datetime.utcnow()
    time_str = req.timestamp or now.strftime("%d %b, %I:%M %p")

    scan_id = str(ObjectId())
    doc = {
        "_id": ObjectId(scan_id),
        "user_id": user_id or "anonymous",
        "user_email": user_email or "",
        "scan_type": req.scan_type,
        "target": req.target,
        "verdict": req.verdict,
        "score": req.score,
        "severity": req.severity,
        "timestamp": time_str,
        "scanned_at": now
    }

    if db is not None:
        try:
            await db["scan_history"].insert_one(doc)

            if req.severity in ["High", "Critical"]:
                threat = {
                    "scan_id": scan_id,
                    "user_id": user_id or "anonymous",
                    "user_email": user_email or "",
                    "threat_type": req.scan_type,
                    "severity": req.severity,
                    "source": req.target,
                    "description": f"{req.scan_type} flagged target '{req.target}' as {req.verdict}.",
                    "resolved": False,
                    "detected_at": now
                }
                await db["threat_logs"].insert_one(threat)
        except Exception as e:
            print(f"[Warning] record_scan_history insert error: {e}")

    return {
        "id": scan_id,
        "scan_type": doc["scan_type"],
        "target": doc["target"],
        "verdict": doc["verdict"],
        "score": doc["score"],
        "severity": doc["severity"],
        "timestamp": time_str,
        "created_at": now.isoformat()
    }

@router.delete("/history/{item_id}")
async def delete_scan_history_item(
    item_id: str,
    authorization: Optional[str] = Header(None),
    x_user_email: Optional[str] = Header(None, alias="X-User-Email"),
    db = Depends(get_db)
):
    if db is None:
        return {"message": "Database not configured", "deleted": False}

    id_queries = [{"_id": item_id}]
    try:
        id_queries.append({"_id": ObjectId(item_id)})
    except Exception:
        pass

    target_query = {"$or": id_queries}
    threat_query = {"$or": id_queries + [{"scan_id": item_id}]}

    deleted_count = 0
    collections = ["scan_history", "url_scans", "fraud_scans", "payment_scans", "apk_scans"]
    for col_name in collections:
        try:
            res = await db[col_name].delete_many(target_query)
            deleted_count += res.deleted_count
        except Exception:
            pass

    # Correlated deletion of any associated threat log
    try:
        await db["threat_logs"].delete_many(threat_query)
    except Exception:
        pass

    return {
        "message": f"Successfully deleted item {item_id}",
        "id": item_id,
        "deleted_count": deleted_count,
        "deleted": deleted_count > 0
    }

@router.delete("/history")
async def clear_all_scan_history(
    authorization: Optional[str] = Header(None),
    x_user_email: Optional[str] = Header(None, alias="X-User-Email"),
    db = Depends(get_db)
):
    if db is None:
        return {"message": "Database not configured", "deleted_count": 0}

    user_id = None
    user_email = (x_user_email or "").strip().lower() or None
    if authorization:
        try:
            token = authorization.replace("Bearer ", "").strip()
            claims = verify_firebase_token(token)
            user_id = str(claims.get("uid", ""))
            if not user_email and claims.get("email"):
                user_email = claims.get("email", "").strip().lower()
        except Exception:
            pass

    user_query = _build_user_query(user_email, user_id)
    if not user_query:
        user_query = {"$or": [
            {"user_id": "anonymous"},
            {"user_email": ""},
            {"user_email": "anonymous"},
            {"user_id": None},
            {"user_email": None}
        ]}

    total_deleted = 0
    collections = ["scan_history", "url_scans", "fraud_scans", "payment_scans", "apk_scans", "threat_logs"]
    for col_name in collections:
        try:
            res = await db[col_name].delete_many(user_query)
            total_deleted += res.deleted_count
        except Exception as e:
            print(f"[Warning] Failed to clear {col_name}: {e}")

    return {
        "message": "All scan history and threat logs successfully deleted across mobile & web",
        "deleted_count": total_deleted
    }
