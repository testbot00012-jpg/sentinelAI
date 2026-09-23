import datetime
from typing import List, Optional, Dict, Any
from fastapi import APIRouter, Depends, HTTPException, Header
from pydantic import BaseModel
from app.db.database import get_db

router = APIRouter(prefix="/api/incidents", tags=["Security Incidents & Operations"])

# Standard Incidents Store
SAMPLE_INCIDENTS = [
    {
        "incident_id": "INC-2026-001",
        "title": "Suspected Banking Trojan Infiltration via Malicious APK",
        "severity": "Critical",
        "confidence": 98.2,
        "affected_asset": "Samsung Galaxy S24 (Android 14)",
        "status": "In Progress",
        "timeline": [
            {
                "timestamp": "2026-09-18T10:14:22Z",
                "source": "App Security Auditor",
                "event": "Detected app 'Quick Flashlight Pro' requesting BIND_ACCESSIBILITY_SERVICE and SYSTEM_ALERT_WINDOW.",
                "severity": "High"
            },
            {
                "timestamp": "2026-09-18T10:14:35Z",
                "source": "Network Threat Engine",
                "event": "Outbound connection attempted to known C2 server domain 'http://89.208.107.123:8080'.",
                "severity": "Critical"
            },
            {
                "timestamp": "2026-09-18T10:15:00Z",
                "source": "Sentinel Local Neural AI",
                "event": "Automated containment advisory generated. User alerted to disable Accessibility service.",
                "severity": "Informational"
            }
        ],
        "evidence": [
            "Combinatorial synergy: BIND_ACCESSIBILITY_SERVICE + SYSTEM_ALERT_WINDOW",
            "C2 domain matching known SharkBot threat signature",
            "Sideloaded from unknown browser download package"
        ],
        "recommended_actions": [
            "Revoke Accessibility and Device Admin permissions immediately in Android Settings.",
            "Boot into Android Safe Mode and uninstall 'Quick Flashlight Pro'.",
            "Freeze online banking cards on a clean device."
        ]
    },
    {
        "incident_id": "INC-2026-002",
        "title": "Credential Harvester Smishing Campaign Intercepted",
        "severity": "High",
        "confidence": 99.1,
        "affected_asset": "SMS Messaging Gateway",
        "status": "Resolved",
        "timeline": [
            {
                "timestamp": "2026-09-18T14:20:10Z",
                "source": "Scam Message NLP Pipeline",
                "event": "Inbound SMS detected: 'URGENT: Chase account suspended. Verify at http://chase-secure.top/auth'.",
                "severity": "High"
            },
            {
                "timestamp": "2026-09-18T14:20:11Z",
                "source": "URL Phishing Classifier",
                "event": "Embedded URL 'http://chase-secure.top' scored 96.0% phishing probability.",
                "severity": "Critical"
            }
        ],
        "evidence": [
            "High-abuse .top TLD paired with brand impersonation ('chase')",
            "Urgency psycholinguistic marker ('suspended within 2 hours')",
            "Sender spoofed as alphanumeric shortcode"
        ],
        "recommended_actions": [
            "Block sender and report to 7726 (SPAM).",
            "Do not follow or open link.",
            "Mark alert as resolved."
        ]
    }
]

SAMPLE_ALERTS = [
    {
        "alert_id": "ALT-101",
        "severity": "Critical",
        "title": "Rogue Device Administrator Detected",
        "summary": "Application 'Device Battery Saver' requested BIND_DEVICE_ADMIN privileges.",
        "action_required": "Revoke permission in Settings > Security > Device Admin.",
        "read": False,
        "timestamp": datetime.datetime.utcnow().isoformat()
    },
    {
        "alert_id": "ALT-102",
        "severity": "High",
        "title": "Unencrypted Public Wi-Fi Active",
        "summary": "Connected to 'CoffeeShop_Free_WiFi' with open encryption and no WPA protection.",
        "action_required": "Enable Sentinel VPN or disconnect.",
        "read": False,
        "timestamp": datetime.datetime.utcnow().isoformat()
    },
    {
        "alert_id": "ALT-103",
        "severity": "Medium",
        "title": "USB Debugging Enabled",
        "summary": "ADB bridge is active on device, exposing shell access over USB.",
        "action_required": "Turn off USB Debugging in Developer Options.",
        "read": True,
        "timestamp": datetime.datetime.utcnow().isoformat()
    },
    {
        "alert_id": "ALT-104",
        "severity": "Low",
        "title": "Security Patch 60 Days Old",
        "summary": "Your device security patch is from 2 months ago.",
        "action_required": "Check for system software updates.",
        "read": True,
        "timestamp": datetime.datetime.utcnow().isoformat()
    }
]

EMERGENCY_CHECKLIST = [
    {
        "step_number": 1,
        "title": "Air-Gap Device (Airplane Mode)",
        "description": "Disconnect immediately from Wi-Fi, Bluetooth, and cellular mobile data to stop ongoing data exfiltration and remote command execution.",
        "action_intent": "android.settings.AIRPLANE_MODE_SETTINGS",
        "criticality": "Immediate"
    },
    {
        "step_number": 2,
        "title": "Inspect & Revoke Accessibility Services",
        "description": "Malicious banking trojans abuse Accessibility to log keystrokes, read OTPs, and click screens automatically.",
        "action_intent": "android.settings.ACCESSIBILITY_SETTINGS",
        "criticality": "Critical"
    },
    {
        "step_number": 3,
        "title": "Review Device Administrator Apps",
        "description": "Ransomware and droppers lock administrator status to block user uninstallation.",
        "action_intent": "android.settings.SECURITY_SETTINGS",
        "criticality": "Critical"
    },
    {
        "step_number": 4,
        "title": "Freeze Online Banking & Payment Apps",
        "description": "Call your bank immediately from a secondary, clean phone to temporarily block credit/debit cards and UPI.",
        "action_intent": "ACTION_DIAL",
        "criticality": "Urgent"
    },
    {
        "step_number": 5,
        "title": "Audit Call & SMS Forwarding Rules",
        "description": "Dial *#21# or *#62# on your phone keypad to verify no conditional call/SMS forwarding is redirecting your 2FA OTPs.",
        "action_intent": "ACTION_DIAL_FORWARD_CHECK",
        "criticality": "High"
    },
    {
        "step_number": 6,
        "title": "Boot Android into Safe Mode",
        "description": "Safe Mode runs only essential system applications, allowing you to remove persistent trojan droppers.",
        "action_intent": "INSTRUCTION_SAFE_MODE",
        "criticality": "High"
    },
    {
        "step_number": 7,
        "title": "File Formal Cyber Crime Complaint",
        "description": "Preserve transaction IDs, APK names, and screenshots. In India call 1930 / cybercrime.gov.in; in USA visit ic3.gov.",
        "action_intent": "ACTION_REPORT_CYBERCRIME",
        "criticality": "Legal"
    }
]

# ============================================================================
# INCIDENT CENTER (Screen 19)
# ============================================================================

@router.get("")
async def get_incidents():
    """Returns grouped security incidents for Screen 19."""
    return {"incidents": SAMPLE_INCIDENTS, "total": len(SAMPLE_INCIDENTS)}

@router.get("/{incident_id}/timeline")
async def get_incident_timeline(incident_id: str):
    """Returns chronological timeline for Screen 20."""
    for inc in SAMPLE_INCIDENTS:
        if inc["incident_id"] == incident_id:
            return {
                "incident_id": inc["incident_id"],
                "title": inc["title"],
                "severity": inc["severity"],
                "confidence": inc["confidence"],
                "timeline": inc["timeline"],
                "evidence": inc["evidence"]
            }
    raise HTTPException(status_code=404, detail="Incident not found")

# ============================================================================
# SECURITY ALERTS INBOX (Screen 18)
# ============================================================================

@router.get("/alerts/inbox")
async def get_alerts_inbox():
    """Returns alerts inbox for Screen 18."""
    return {"alerts": SAMPLE_ALERTS, "total": len(SAMPLE_ALERTS)}

# ============================================================================
# EMERGENCY MODE PROTOCOL (Screen 22)
# ============================================================================

@router.get("/emergency/protocol")
async def get_emergency_protocol():
    """Returns 7-step interactive crisis response checklist for Screen 22."""
    return {
        "mode": "Emergency Device Compromise Protocol",
        "checklist": EMERGENCY_CHECKLIST,
        "disclaimer": "Step-by-step recovery guidance. The application does not guarantee automated removal of all rootkits; manual OEM recovery may be required."
    }

# ============================================================================
# EXECUTIVE SECURITY REPORT (Screen 23)
# ============================================================================

@router.get("/reports/latest")
async def get_latest_security_report():
    """Generates human-readable security report for Screen 23."""
    return {
        "report_id": f"RPT-{datetime.datetime.utcnow().strftime('%Y%m%d%H%M')}",
        "generated_at": datetime.datetime.utcnow().isoformat(),
        "security_score": 92,
        "device_summary": {
            "model": "Android Mobile Device",
            "os_version": "Android 14 (API 34)",
            "posture": "Hardened / Secure"
        },
        "findings_breakdown": {
            "critical": 0,
            "high": 1,
            "medium": 2,
            "low": 3,
            "informational": 5
        },
        "key_threats_blocked": [
            "Intercepted smishing URL on .top TLD",
            "Blocked overlay permission request from unknown utility APK",
            "Protected Wi-Fi connection via TLS verification"
        ],
        "recommendations": [
            "Revoke inactive SMS and Location permissions.",
            "Keep VPN active on public wireless connections.",
            "Run weekly Full Security Scans."
        ],
        "compliance": "NIST SP 800-124 Rev. 2 Aligned"
    }

# ============================================================================
# BUSINESS / ENTERPRISE ENROLLMENT (Screen 28 & Section 9)
# ============================================================================

@router.get("/enterprise/status")
async def get_enterprise_status():
    """Returns enterprise MDM and enrollment status for Screen 28."""
    return {
        "enrolled": True,
        "organization_name": "Sentinel Defense Labs (Enterprise)",
        "organization_id": "ORG-SEC-9920",
        "device_id": "DEV-AND-S24-9102",
        "mdm_policy_version": "v3.2.0-enforced",
        "last_policy_sync": datetime.datetime.utcnow().isoformat(),
        "compliance_status": "Compliant",
        "managed_features": [
            "Mandatory Screen Lock",
            "Prohibited Unknown Sources",
            "Encrypted Local Threat Storage",
            "Automatic Weekly Scan Scheduling"
        ]
    }
