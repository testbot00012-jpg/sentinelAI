from pydantic import BaseModel, EmailStr, Field
from typing import Optional, List, Dict, Any
from datetime import datetime

# Auth Schemas
class UserRegister(BaseModel):
    email: EmailStr
    password: str = Field(..., min_length=6)
    full_name: Optional[str] = None

class UserLogin(BaseModel):
    email: EmailStr
    password: str

class Token(BaseModel):
    access_token: str
    token_type: str
    role: str

# Scan Schemas
class URLScanRequest(BaseModel):
    url: str

class URLScanResponse(BaseModel):
    url: str
    status: str
    score: float
    details: List[str]
    scanned_at: datetime

class FraudScanRequest(BaseModel):
    content: str
    scan_type: str = "SMS" # SMS or Email

class FraudScanResponse(BaseModel):
    scan_type: str
    original_text: str
    scam_probability: float
    classification: str
    explanation: str
    contains_link: bool
    scanned_at: datetime

class APKScanRequest(BaseModel):
    app_name: str
    package_name: str
    permissions: List[str]

class APKScanResponse(BaseModel):
    app_name: str
    package_name: str
    malware_score: float
    threat_category: str
    flagged_permissions: List[str]
    total_permissions_scanned: int
    status: str

# Telemetry
class DeviceTelemetryRequest(BaseModel):
    device_model: Optional[str] = None
    os_version: Optional[str] = None
    security_score: int
    battery_health: Optional[int] = None
    ram_usage_percent: Optional[float] = None
    storage_usage_percent: Optional[float] = None
