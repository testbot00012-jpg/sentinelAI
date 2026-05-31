from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from app.db.database import get_db
from app.models.models import User, ActivityLog
from app.core.security import verify_firebase_token
from pydantic import BaseModel

router = APIRouter(prefix="/api/auth", tags=["Firebase Identity Authentication"])

class FirebaseTokenRequest(BaseModel):
    id_token: str

@router.post("/verify")
def verify_firebase_login(req: FirebaseTokenRequest, db: Session = Depends(get_db)):
    """
    Validates Firebase ID token dynamically against Google public certificate authorities,
    automatically provisions local PostgreSQL user profiles, and logs activity triggers.
    """
    try:
        claims = verify_firebase_token(req.id_token)
        email = claims.get("email")
        if not email:
            raise HTTPException(status_code=401, detail="Invalid identity token payload")
    except Exception as e:
        raise HTTPException(status_code=401, detail=f"Authentication check failed: {str(e)}")

    # Check for existing user or provision on the fly
    user = db.query(User).filter(User.email == email).first()
    if user is None:
        user = User(
            email=email,
            hashed_password="SSO_MANAGED_PASSWORD_STUB",
            full_name=claims.get("name", "Firebase User"),
            role="admin" if db.query(User).count() == 0 else "user"
        )
        db.add(user)
        db.commit()
        db.refresh(user)

    # Log action in unified DB Audit Trails
    log = ActivityLog(user_id=user.id, action="Firebase Login Sync")
    db.add(log)
    db.commit()

    return {
        "access_token": req.id_token,
        "token_type": "bearer",
        "email": user.email,
        "role": user.role,
        "status": "synchronized"
    }

