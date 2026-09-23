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
    installer: Optional[str] = None
    is_system: Optional[bool] = False
    version: Optional[str] = "1.0"

class APKScanResponse(BaseModel):
    app_name: str
    package_name: str
    malware_score: float
    threat_category: str
    flagged_permissions: List[str]
    total_permissions_scanned: int
    status: str
    origin: Optional[str] = "Google Play Store"
    is_third_party: Optional[bool] = False
    is_system_app: Optional[bool] = False
    analysis_summary: Optional[str] = ""
    evidence: Optional[List[str]] = []
    recommended_action: Optional[str] = ""

# Payment Screenshot Analyzer (Screen 14)
class PaymentScanRequest(BaseModel):
    ocr_text: str
    image_base64: Optional[str] = None
    metadata: Optional[Dict[str, Any]] = None

class PaymentScanResponse(BaseModel):
    extracted_amount: str
    extracted_date: str
    extracted_reference: str
    ecosystem: str
    fraud_score: float
    classification: str
    confidence: float
    evidence: List[str]
    recommended_action: str
    disclaimer: str

# QR Scanner (Screen 12)
class QRScanRequest(BaseModel):
    payload: str

class QRScanResponse(BaseModel):
    payload_type: str
    decoded_content: str
    target_destination: str
    risk_level: str
    confidence: float
    threat_summary: str
    details: List[str]

# Device Security (Screen 10)
class DeviceScanRequest(BaseModel):
    os_version: Optional[str] = "14"
    security_patch_level: Optional[str] = "2024-01-01"
    screen_lock_enabled: Optional[bool] = True
    developer_options_enabled: Optional[bool] = False
    usb_debugging_enabled: Optional[bool] = False
    unknown_sources_allowed: Optional[bool] = False
    device_admin_count: Optional[int] = 0
    root_detected: Optional[bool] = False
    play_protect_enabled: Optional[bool] = True
    encryption_enabled: Optional[bool] = True

class DeviceScanResponse(BaseModel):
    security_score: int
    posture: str
    confidence: float
    findings: List[Dict[str, Any]]
    signals_evaluated: Dict[str, Any]
    recommendations_count: int

# Network Security (Screen 15)
class NetworkScanRequest(BaseModel):
    connection_type: Optional[str] = "WIFI"
    ssid: Optional[str] = "Current Network"
    encryption: Optional[str] = "WPA2"
    is_captive_portal: Optional[bool] = False
    vpn_active: Optional[bool] = False
    dns_servers: Optional[List[str]] = ["8.8.8.8", "1.1.1.1"]

class NetworkScanResponse(BaseModel):
    network_risk_score: float
    status: str
    confidence: float
    connection_type: str
    ssid: str
    encryption: str
    vpn_active: bool
    findings: List[Dict[str, Any]]
    recommendation: str

# Quick Scan (Screen 05)
class QuickScanRequest(BaseModel):
    device_signals: Optional[Dict[str, Any]] = {}
    apps_sample: Optional[List[Dict[str, Any]]] = []

class QuickScanResponse(BaseModel):
    scan_type: str
    overall_score: int
    status: str
    device_posture: str
    device_findings: List[Dict[str, Any]]
    apps_scanned_count: int
    threat_apps_count: int
    apps_results: List[Dict[str, Any]]
    total_findings_count: int
    timestamp: str

# Full Security Scan (Screen 06)
class FullScanRequest(BaseModel):
    device_signals: Optional[Dict[str, Any]] = {}
    installed_apps: Optional[List[Dict[str, Any]]] = []
    network_info: Optional[Dict[str, Any]] = {}
    urls_history: Optional[List[str]] = []

class FullScanResponse(BaseModel):
    scan_type: str
    composite_score: int
    posture: str
    device_summary: Dict[str, Any]
    network_summary: Dict[str, Any]
    total_apps_scanned: int
    flagged_apps_count: int
    apps_findings: List[Dict[str, Any]]
    incidents_created: List[Dict[str, Any]]
    recommendations: List[str]
    timestamp: str

# Incident & Alert Schemas (Screens 18, 19, 20)
class IncidentModel(BaseModel):
    incident_id: str
    title: str
    severity: str
    confidence: float
    affected_asset: str
    timeline: List[Dict[str, Any]]
    evidence: List[str]
    recommended_actions: List[str]
    status: str

class AlertModel(BaseModel):
    alert_id: str
    severity: str
    title: str
    summary: str
    action_required: str
    read: bool
    timestamp: str

# Model & AI Status (Screen 26)
class ModelStatusResponse(BaseModel):
    engine: str
    version: str
    inference_mode: str
    cloud_dependencies: str
    models: List[Dict[str, Any]]
    last_updated: str
    model_integrity: str

# Telemetry
class DeviceTelemetryRequest(BaseModel):
    device_model: Optional[str] = None
    os_version: Optional[str] = None
    security_score: int
    battery_health: Optional[int] = None
    ram_usage_percent: Optional[float] = None
    storage_usage_percent: Optional[float] = None

# Unified Security Scan History
class SecurityHistoryRecordRequest(BaseModel):
    scan_type: str
    target: str
    verdict: str
    score: int = 100
    severity: str = "Safe"
    timestamp: Optional[str] = None

class SecurityHistoryItemResponse(BaseModel):
    id: str
    scan_type: str
    target: str
    verdict: str
    score: int = 100
    severity: str = "Safe"
    timestamp: str
    created_at: Optional[str] = None

class SecurityHistoryListResponse(BaseModel):
    history: List[SecurityHistoryItemResponse]
    total: int
