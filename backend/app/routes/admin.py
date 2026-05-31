from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from app.db.database import get_db
from app.models.models import User, ThreatLog, ActivityLog, DeviceStat
from app.core.security import get_admin_user
import datetime

router = APIRouter(prefix="/api/admin", tags=["Enterprise Admin Control Suite"])

@router.get("/overview")
def get_admin_overview(admin_user: User = Depends(get_admin_user), db: Session = Depends(get_db)):
    total_users = db.query(User).count()
    total_threats = db.query(ThreatLog).count()
    active_devices = db.query(DeviceStat).count()
    resolved_threats = db.query(ThreatLog).filter(ThreatLog.resolved == True).count()
    unresolved_threats = db.query(ThreatLog).filter(ThreatLog.resolved == False).count()

    recent_logs = db.query(ActivityLog).order_by(ActivityLog.timestamp.desc()).limit(10).all()
    recent_threats = db.query(ThreatLog).order_by(ThreatLog.detected_at.desc()).limit(5).all()

    # Formulate security statistics
    return {
        "counters": {
            "total_users": total_users,
            "total_threats_logged": total_threats,
            "active_devices": active_devices,
            "resolved_threats": resolved_threats,
            "unresolved_threats": unresolved_threats
        },
        "recent_activity": [
            {
                "id": log.id,
                "action": log.action,
                "timestamp": log.timestamp,
                "user_id": log.user_id
            } for log in recent_logs
        ],
        "recent_threats": [
            {
                "id": threat.id,
                "threat_type": threat.threat_type,
                "severity": threat.severity,
                "source": threat.source,
                "description": threat.description,
                "detected_at": threat.detected_at,
                "resolved": threat.resolved
            } for threat in recent_threats
        ]
    }


@router.post("/threats/{threat_id}/resolve")
def resolve_threat(threat_id: int, admin_user: User = Depends(get_admin_user), db: Session = Depends(get_db)):
    threat = db.query(ThreatLog).filter(ThreatLog.id == threat_id).first()
    if not threat:
        raise HTTPException(status_code=404, detail="Threat log item not found")

    threat.resolved = True
    db.commit()
    return {"message": f"Threat classification #{threat_id} resolved and archived."}


@router.get("/users")
def list_system_users(admin_user: User = Depends(get_admin_user), db: Session = Depends(get_db)):
    users = db.query(User).all()
    return [
        {
            "id": u.id,
            "email": u.email,
            "full_name": u.full_name,
            "role": u.role,
            "created_at": u.created_at,
            "is_active": u.is_active
        } for u in users
    ]
