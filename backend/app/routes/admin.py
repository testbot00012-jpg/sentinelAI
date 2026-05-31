from fastapi import APIRouter, Depends, HTTPException
from app.db.database import get_db
from app.core.security import get_admin_user
import datetime
from bson.objectid import ObjectId

router = APIRouter(prefix="/api/admin", tags=["Enterprise Admin Control Suite"])

@router.get("/overview")
async def get_admin_overview(admin_user: dict = Depends(get_admin_user), db = Depends(get_db)):
    total_users = await db["users"].count_documents({})
    total_threats = await db["threat_logs"].count_documents({})
    active_devices = await db["device_stats"].count_documents({})
    resolved_threats = await db["threat_logs"].count_documents({"resolved": True})
    unresolved_threats = await db["threat_logs"].count_documents({"resolved": False})

    # Get recent 10 activity logs
    cursor_logs = db["activity_logs"].find().sort("timestamp", -1).limit(10)
    recent_logs = await cursor_logs.to_list(length=10)

    # Get recent 5 threats
    cursor_threats = db["threat_logs"].find().sort("detected_at", -1).limit(5)
    recent_threats = await cursor_threats.to_list(length=5)

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
                "id": str(log["_id"]),
                "action": log["action"],
                "timestamp": log["timestamp"],
                "user_id": str(log["user_id"])
            } for log in recent_logs
        ],
        "recent_threats": [
            {
                "id": str(threat["_id"]),
                "threat_type": threat["threat_type"],
                "severity": threat["severity"],
                "source": threat["source"],
                "description": threat["description"],
                "detected_at": threat["detected_at"],
                "resolved": threat.get("resolved", False)
            } for threat in recent_threats
        ]
    }


@router.post("/threats/{threat_id}/resolve")
async def resolve_threat(threat_id: str, admin_user: dict = Depends(get_admin_user), db = Depends(get_db)):
    try:
        obj_id = ObjectId(threat_id)
    except:
        raise HTTPException(status_code=400, detail="Invalid Threat ID format")

    threat = await db["threat_logs"].find_one({"_id": obj_id})
    if not threat:
        raise HTTPException(status_code=404, detail="Threat log item not found")

    await db["threat_logs"].update_one({"_id": obj_id}, {"$set": {"resolved": True}})
    
    return {"message": f"Threat classification #{threat_id} resolved and archived."}


@router.get("/users")
async def list_system_users(admin_user: dict = Depends(get_admin_user), db = Depends(get_db)):
    cursor = db["users"].find()
    users = await cursor.to_list(length=500)
    
    return [
        {
            "id": str(u["_id"]),
            "email": u["email"],
            "full_name": u.get("full_name"),
            "role": u.get("role", "user"),
            "created_at": u.get("created_at"),
            "is_active": u.get("is_active", True)
        } for u in users
    ]
