from fastapi import APIRouter, Depends, HTTPException, status
from app.db.database import get_db
from app.core.security import verify_firebase_token
from pydantic import BaseModel
import datetime

router = APIRouter(prefix="/api/auth", tags=["Firebase Identity Authentication"])

class FirebaseTokenRequest(BaseModel):
    id_token: str

@router.post("/verify")
async def verify_firebase_login(req: FirebaseTokenRequest, db = Depends(get_db)):
    """
    Validates Firebase ID token dynamically against Google public certificate authorities,
    automatically provisions local MongoDB user profiles, and logs activity triggers.
    """
    try:
        claims = verify_firebase_token(req.id_token)
        email = claims.get("email")
        if not email:
            raise HTTPException(status_code=401, detail="Invalid identity token payload")
    except Exception as e:
        raise HTTPException(status_code=401, detail=f"Authentication check failed: {str(e)}")

    # Check for existing user or provision on the fly
    user = await db["users"].find_one({"email": email})
    if user is None:
        count = await db["users"].count_documents({})
        user = {
            "email": email,
            "hashed_password": "SSO_MANAGED_PASSWORD_STUB",
            "full_name": claims.get("name", "Firebase User"),
            "role": "admin" if count == 0 else "user",
            "created_at": datetime.datetime.utcnow()
        }
        res = await db["users"].insert_one(user)
        user["_id"] = res.inserted_id

    # Log action in unified DB Audit Trails
    log = {
        "user_id": user["_id"],
        "action": "Firebase Login Sync",
        "timestamp": datetime.datetime.utcnow()
    }
    await db["activity_logs"].insert_one(log)

    return {
        "access_token": req.id_token,
        "token_type": "bearer",
        "email": user["email"],
        "role": user["role"],
        "status": "synchronized"
    }
