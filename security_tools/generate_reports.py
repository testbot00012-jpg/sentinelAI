#!/usr/bin/env python3
"""
Sentinel AI Security Report & Deliverables Generator
Produces:
1. Vulnerability Test Results/security-review.md
2. Vulnerability Test Results/executive-summary.md
3. Vulnerability Test Results/dependency-report.md
4. Vulnerability Test Results/endpoint-inventory.xlsx
5. Vulnerability Test Results/findings.xlsx (4 Sheets: Security Findings, Endpoint Inventory, Dependency Vulnerabilities, Risk Summary)
"""

import os
import sys
import json
import datetime
from openpyxl import Workbook
from openpyxl.styles import Font, PatternFill, Alignment, Border, Side
from openpyxl.utils import get_column_letter

workspace_root = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
results_dir = os.path.join(workspace_root, "Vulnerability Test Results")
os.makedirs(results_dir, exist_ok=True)

# -----------------------------------------------------------------------------
# 1. CURATED SECURITY FINDINGS DATASET
# -----------------------------------------------------------------------------
FINDINGS = [
    {
        "id": "VULN-001",
        "title": "Hardcoded Master Secret & Mock Token Prefix Authentication Bypass",
        "severity": "Critical",
        "cvss": 9.8,
        "cwe": "CWE-287 / CWE-798",
        "type": "Authentication Bypass",
        "file_path": "backend/app/core/security.py",
        "lines": "68-73",
        "endpoint": "ALL Protected Endpoints (/api/admin/*, /api/analytics/*, /api/scan/apk)",
        "description": "The verify_firebase_token routine explicitly contains a hardcoded master token backdoor check (`if token.startswith('MOCK_') or token == 'SUPER_SECRET_NEON_SENTINEL_SHIELD_KEY_2026':`) which unconditionally grants valid agent credentials without checking cryptographic signatures or identity authorities.",
        "scenario": "An unauthenticated attacker sends an HTTP request to `/api/admin/users` or `/api/admin/overview` with `Authorization: Bearer SUPER_SECRET_NEON_SENTINEL_SHIELD_KEY_2026`. The backend bypasses Firebase verification, resolves the user, and grants administrative session access.",
        "impact": "Complete authentication and authorization circumvention across the entire API suite. Full compromise of confidentiality, integrity, and operational control.",
        "remediation": "Completely remove all hardcoded master token checks and 'MOCK_' prefix bypasses from production authentication dependencies. Require strictly signed and verified Google Firebase ID tokens."
    },
    {
        "id": "VULN-002",
        "title": "Unverified JWT Base64 Decoding Fallback Allowing Arbitrary Identity Forgery",
        "severity": "Critical",
        "cvss": 9.8,
        "cwe": "CWE-347 / CWE-287",
        "type": "Broken Cryptographic Verification",
        "file_path": "backend/app/core/security.py",
        "lines": "107-123",
        "endpoint": "ALL Authenticated Endpoints",
        "description": "When native Firebase Admin verification and Google certificate decoding fails, a tertiary fallback extracts the second segment of the JWT (`parts[1]`) via base64url decode without verifying any digital signature. Any user providing an arbitrary crafted JWT with `email: admin@sentinel.ai` is treated as authenticated.",
        "scenario": "An attacker creates an unsigned JWT header `{\"alg\": \"none\"}` and payload `{\"email\": \"admin@sentinel.ai\", \"sub\": \"forged_admin_id\"}`. The tertiary fallback accepts the claims, registers or retrieves the admin user, and returns privileged data.",
        "impact": "Total identity forgery and privilege escalation. Anyone can impersonate any user, administrator, or service account without possessing cryptographic keys.",
        "remediation": "Eliminate the unverified tertiary JWT decoding fallback. Strictly reject any token that fails cryptographic validation against official Google Firebase public keys."
    },
    {
        "id": "VULN-003",
        "title": "Firebase Admin SDK Private RSA Key Committed to Repository",
        "severity": "Critical",
        "cvss": 9.6,
        "cwe": "CWE-312 / CWE-798",
        "type": "Sensitive Data Exposure",
        "file_path": "backend/app/core/service_account.json",
        "lines": "1-14",
        "endpoint": "N/A (Repository & Filesystem)",
        "description": "The production Google Cloud Firebase service account credentials file containing the private RSA key (`-----BEGIN PRIVATE KEY-----...`) for project `sentinelai-9d573` is committed into the git repository and loaded directly in code.",
        "scenario": "An attacker cloning the git repository or inspecting public branches discovers the private RSA key and service account email `firebase-adminsdk-fbsvc@sentinelai-9d573.iam.gserviceaccount.com`, granting full administrative privileges to the Firebase project, Firestore database, and Cloud Storage.",
        "impact": "Complete compromise of the cloud project, data exfiltration of user identities, and unauthorized manipulation of cloud infrastructure.",
        "remediation": "Revoke and regenerate the compromised private key immediately in Google Cloud Console. Delete `service_account.json` from git history using `git filter-repo` or BFG, add to `.gitignore`, and supply credentials exclusively via environment variables (`FIREBASE_CREDENTIALS_JSON`)."
    },
    {
        "id": "VULN-004",
        "title": "Production MongoDB Atlas Connection URI with Plaintext Password Committed in Code & Configs",
        "severity": "Critical",
        "cvss": 9.8,
        "cwe": "CWE-798 / CWE-259",
        "type": "Hardcoded Credentials",
        "file_path": "backend/app/db/database.py, backend/.env, docker-compose.yml",
        "lines": "database.py:7-14, .env:8, docker-compose.yml:15",
        "endpoint": "Database Layer (MongoDB Atlas Cluster)",
        "description": "The live MongoDB Atlas production connection URI containing cleartext credentials (`mongodb+srv://testbot00012_db_user:ZWjW7Eidrlz3TwEn@cluster0.nwf6du7.mongodb.net/sentinel_db`) is hardcoded as fallback in `database.py`, tracked in `backend/.env`, and declared in `docker-compose.yml`.",
        "scenario": "An external adversary extracts the connection URI and connects directly to `cluster0.nwf6du7.mongodb.net` using standard Mongo CLI tools, bypassing the API layer entirely.",
        "impact": "Unrestricted read, write, and deletion access to all collections: users, threat_logs, url_scans, device_stats, and audit logs. Complete database breach.",
        "remediation": "Rotate the database password immediately in MongoDB Atlas. Remove hardcoded fallback strings from Python source code, add `.env` to `.gitignore`, and inject database secrets via environment variables in production."
    },
    {
        "id": "VULN-005",
        "title": "Broken Object Level Authorization (BOLA/IDOR) via Untrusted X-User-Email Header",
        "severity": "High",
        "cvss": 8.6,
        "cwe": "CWE-639 / CWE-284",
        "type": "Broken Authorization",
        "file_path": "backend/app/routes/analytics.py, backend/app/routes/scan.py",
        "lines": "analytics.py:58-90, scan.py:17-40",
        "endpoint": "GET /api/analytics/metrics, POST /api/scan/url, POST /api/scan/fraud",
        "description": "The `/api/analytics/metrics` endpoint retrieves confidential user telemetry, scanned URLs, and security threat logs filtered by an unvalidated client-supplied `X-User-Email` HTTP header without verifying authentication tokens.",
        "scenario": "An unauthenticated user issues `GET /api/analytics/metrics` with header `X-User-Email: victim@company.com`. The API returns the victim's device specs, battery health, RAM usage, scanned URLs, and recorded threats.",
        "impact": "Unauthorized surveillance and data exposure across all system users. An attacker can map all monitored mobile devices and read scan histories.",
        "remediation": "Deprecate the `X-User-Email` header. Derive user identity exclusively from cryptographically verified claims inside the validated JWT Bearer token."
    },
    {
        "id": "VULN-006",
        "title": "Insecure Permissive Wildcard CORS Configuration with Credentials Allowed",
        "severity": "High",
        "cvss": 8.1,
        "cwe": "CWE-942",
        "type": "Security Misconfiguration",
        "file_path": "backend/app/main.py",
        "lines": "15-21",
        "endpoint": "ALL Endpoints",
        "description": "The FastAPI application applies CORSMiddleware configured with `allow_origins=['*']` and `allow_credentials=True`. This violates the CORS standard and allows any arbitrary third-party origin to interact with the API.",
        "scenario": "A victim visits a malicious site `evil-attacker.com` while logged into Sentinel AI. The site initiates cross-origin requests to `http://localhost:8000/api/analytics/metrics` and extracts telemetry data.",
        "impact": "Cross-origin data exfiltration, token theft, and unauthorized background operations initiated from victim browsers.",
        "remediation": "Explicitly configure allowed origins with a whitelist of trusted frontend domains (e.g. `['https://sentinel-ai.com', 'http://localhost:3000']`). Disallow wildcard origins when credentials are enabled."
    },
    {
        "id": "VULN-007",
        "title": "Hardcoded Commercial Groq AI Inference API Key in chat.py",
        "severity": "High",
        "cvss": 7.5,
        "cwe": "CWE-798 / CWE-200",
        "type": "Hardcoded Secret",
        "file_path": "backend/app/routes/chat.py",
        "lines": "16-17",
        "endpoint": "POST /api/chat",
        "description": "A live commercial Groq LLM API key (`gsk_WUT36...[REDACTED_API_KEY]`) is hardcoded in `chat.py` by splitting the key into tuple components `_K_PARTS` to evade basic string scanning.",
        "scenario": "An attacker extracts the concatenated tuple string and uses the Groq API key directly to run unauthorized inference jobs or query models at the project owner's expense.",
        "impact": "Theft of API quotas, financial billing losses, and unauthorized commercial model access.",
        "remediation": "Revoke the exposed key in the Groq Cloud Console. Strictly source `GROQ_API_KEY` from environment variables without hardcoded fallbacks."
    },
    {
        "id": "VULN-008",
        "title": "Hardcoded AlienVault OTX and URLScan.io Threat Intelligence API Keys",
        "severity": "High",
        "cvss": 7.5,
        "cwe": "CWE-798",
        "type": "Hardcoded Secret",
        "file_path": "backend/app/services/ml_engine.py, backend/.env",
        "lines": "ml_engine.py:35-36, .env:4-5",
        "endpoint": "POST /api/scan/url",
        "description": "Live API credentials for AlienVault OTX (`6a922e6db...`) and URLScan.io (`019e7c24-...`) are embedded as default parameters in `ml_engine.py` and tracked in `backend/.env`.",
        "scenario": "Adversaries extract the keys from source or decompiled artifacts to abuse the threat intelligence APIs or deplete subscription quotas.",
        "impact": "Compromise of threat intelligence accounts and denial of threat classification service.",
        "remediation": "Rotate keys on AlienVault and URLScan platforms. Ensure keys are read strictly from secure environment variables."
    },
    {
        "id": "VULN-009",
        "title": "Unauthenticated Public AI Inference Endpoint Subject to DoS and Financial Drain",
        "severity": "High",
        "cvss": 7.5,
        "cwe": "CWE-306 / CWE-770",
        "type": "Unrestricted Resource Consumption",
        "file_path": "backend/app/routes/chat.py",
        "lines": "49-115",
        "endpoint": "POST /api/chat",
        "description": "The `/api/chat` route processes arbitrary text prompts and forwards them to commercial cloud LLMs (Groq) without requiring user authentication, token consumption quotas, or rate limiting.",
        "scenario": "A malicious script floods `/api/chat` with thousands of automated queries per minute, causing rapid exhaustion of API rate limits and generating large API bills.",
        "impact": "Service denial for legitimate users, API rate limit exhaustion, and uncontrolled cloud billing charges.",
        "remediation": "Enforce authentication on `/api/chat` using `Depends(get_current_user)` and apply rate limiting (e.g. 5 requests/minute per user) using Redis/SlowAPI."
    },
    {
        "id": "VULN-010",
        "title": "Race Condition & Privilege Escalation in First-User Auto-Admin Creation",
        "severity": "High",
        "cvss": 7.3,
        "cwe": "CWE-269 / CWE-362",
        "type": "Privilege Escalation",
        "file_path": "backend/app/core/security.py, backend/app/routes/auth.py",
        "lines": "security.py:154, auth.py:36",
        "endpoint": "POST /api/auth/verify",
        "description": "User provisioning logic checks `count = await db['users'].count_documents({})` and assigns `role = 'admin' if count == 0 else 'user'`. In new deployments or clean databases, the first user to submit a token automatically obtains system administrator privileges.",
        "scenario": "During application setup or database maintenance, an external attacker registers before legitimate administrators, securing permanent admin rights.",
        "impact": "Total administrative takeover of the platform by unauthorized initial users.",
        "remediation": "Do not grant administrative roles automatically based on database row counts. Seed administrative accounts via controlled CLI scripts or secure environment variables."
    },
    {
        "id": "VULN-011",
        "title": "Server-Side Request Forgery (SSRF) and Internal Network Probing in URL Scanner",
        "severity": "High",
        "cvss": 7.5,
        "cwe": "CWE-918",
        "type": "Server-Side Request Forgery",
        "file_path": "backend/app/routes/scan.py, backend/app/services/ml_engine.py",
        "lines": "scan.py:16-40, ml_engine.py:84, 112",
        "endpoint": "POST /api/scan/url",
        "description": "The URL scanning endpoint accepts arbitrary user URLs and parses domains directly into outbound query indicators. Internal loopback (`127.0.0.1`), cloud metadata (`169.254.169.254`), and internal LAN IP addresses are not validated or blocked.",
        "scenario": "An attacker submits `http://169.254.169.254/latest/meta-data/` or internal infrastructure addresses to probe network topology and extract metadata.",
        "impact": "Cloud metadata disclosure, internal port scanning, and reconnaissance of backend infrastructure.",
        "remediation": "Validate and sanitize scanned URLs using an IP address resolver that rejects private, loopback, link-local (169.254.0.0/16), and cloud metadata IP ranges before performing lookups."
    },
    {
        "id": "VULN-012",
        "title": "Insecure Deserialization of Machine Learning Models via Joblib/Pickle",
        "severity": "Medium",
        "cvss": 6.8,
        "cwe": "CWE-502",
        "type": "Insecure Deserialization",
        "file_path": "backend/app/services/ml_engine.py",
        "lines": "51, 60, 70",
        "endpoint": "N/A (Startup Initialization)",
        "description": "`joblib.load()` is used to deserialize serialized Python Scikit-Learn models from `backend/app/ml/saved_models/*.joblib`. Joblib relies on Python `pickle`, which can execute arbitrary bytecode during deserialization.",
        "scenario": "An adversary who achieves local write access, submits a pull request with a tampered model, or poisons the model artifact pipeline causes arbitrary code execution when the server starts.",
        "impact": "Remote Code Execution (RCE) on the backend host server if model files are tampered with.",
        "remediation": "Compute and verify SHA-256 cryptographic hashes of all `.joblib` model files before deserialization, or migrate to safer serialization formats such as ONNX or Safetensors."
    },
    {
        "id": "VULN-013",
        "title": "Missing Defense-in-Depth HTTP Security Headers Across All API Responses",
        "severity": "Medium",
        "cvss": 5.3,
        "cwe": "CWE-693",
        "type": "Security Misconfiguration",
        "file_path": "backend/app/main.py",
        "lines": "8-28",
        "endpoint": "ALL Endpoints",
        "description": "The FastAPI application does not set standard defensive security headers including `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, `Strict-Transport-Security`, `Content-Security-Policy`, and `Referrer-Policy`.",
        "scenario": "Browsers interacting with the API may perform MIME-type sniffing or allow embedding within malicious third-party iframes (clickjacking).",
        "impact": "Increased susceptibility to cross-site scripting, MIME confusion attacks, and clickjacking.",
        "remediation": "Add an ASGI middleware to set `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, `Strict-Transport-Security: max-age=31536000; includeSubDomains`, and a restrictive `Content-Security-Policy`."
    },
    {
        "id": "VULN-014",
        "title": "Verbose Internal Exception & Error Message Disclosure to Clients",
        "severity": "Medium",
        "cvss": 5.3,
        "cwe": "CWE-200 / CWE-209",
        "type": "Information Exposure",
        "file_path": "backend/app/routes/auth.py",
        "lines": "24",
        "endpoint": "POST /api/auth/verify",
        "description": "Exceptions caught during token verification are formatted into HTTP 401 response detail strings: `raise HTTPException(status_code=401, detail=f'Authentication check failed: {str(e)}')`.",
        "scenario": "When network errors, certificate parsing errors, or internal driver faults occur, raw system diagnostic messages and library exception text are returned directly to untrusted callers.",
        "impact": "Assists attackers in fingerprinting backend library versions, internal architectures, and error conditions.",
        "remediation": "Return generic client-facing messages (e.g., `'Invalid authentication credentials'`) while logging internal exception details securely to server-side logs."
    },
    {
        "id": "VULN-015",
        "title": "OpenAPI Documentation & Swagger UI Publicly Exposed Without Environment Control",
        "severity": "Medium",
        "cvss": 5.3,
        "cwe": "CWE-200 / CWE-489",
        "type": "Information Exposure",
        "file_path": "backend/app/main.py",
        "lines": "8-12",
        "endpoint": "/docs, /redoc, /openapi.json",
        "description": "FastAPI auto-generates `/docs` and `/openapi.json` by default. These routes are left fully enabled without verifying whether the server is running in production mode.",
        "scenario": "An external attacker visits `/docs` and downloads the complete OpenAPI schema, learning all hidden parameters, route schemas, and administrative endpoints.",
        "impact": "Complete reconnaissance of the application attack surface without needing fuzzing tools.",
        "remediation": "Disable OpenAPI documentation in production environments: `FastAPI(docs_url=None, redoc_url=None, openapi_url=None if os.getenv('ENV') == 'production' else '/openapi.json')`."
    },
    {
        "id": "VULN-016",
        "title": "Missing Rate Limiting and Brute-Force Throttling on Authentication and Scans",
        "severity": "Medium",
        "cvss": 5.3,
        "cwe": "CWE-306 / CWE-799",
        "type": "Missing Rate Limiting",
        "file_path": "backend/app/routes/auth.py, backend/app/routes/scan.py",
        "lines": "auth.py:12, scan.py:16, 79",
        "endpoint": "POST /api/auth/verify, POST /api/scan/url, POST /api/scan/fraud",
        "description": "None of the public endpoints enforce rate limiting. An attacker can repeatedly submit requests to brute-force tokens or overwhelm the ML classifier heuristics.",
        "scenario": "Automated bot attacks send 500 requests per second to `/api/scan/url`, exhausting server memory and CPU resources running Scikit-Learn pipelines.",
        "impact": "Denial of Service (DoS) and potential credential enumeration.",
        "remediation": "Implement rate limiting using `slowapi` or Redis-backed token bucket algorithms on all public endpoints."
    },
    {
        "id": "VULN-017",
        "title": "Vulnerable Third-Party Dependencies (python-multipart, Jinja2, python-jose)",
        "severity": "Medium",
        "cvss": 6.5,
        "cwe": "CWE-1395",
        "type": "Vulnerable Dependency",
        "file_path": "backend/requirements.txt",
        "lines": "7, 9, 14",
        "endpoint": "N/A (Dependency Management)",
        "description": "Pinned dependencies contain known vulnerabilities: `python-multipart==0.0.9` (CVE-2024-24762 ReDoS), `jinja2==3.1.3` (CVE-2024-34064 HTML autoescape bypass), and unmaintained `python-jose==3.3.0` (CVE-2024-33663 algorithm confusion).",
        "scenario": "An attacker sends crafted multipart boundaries or exploits Jinja2 template attribute filters to induce ReDoS or template manipulation.",
        "impact": "Denial of service and template injection vulnerabilities.",
        "remediation": "Upgrade `python-multipart` to `>=0.0.20`, `jinja2` to `>=3.1.5`, and replace `python-jose` with actively maintained `PyJWT>=2.10.0`."
    },
    {
        "id": "VULN-018",
        "title": "Server Bound to All Network Interfaces (0.0.0.0)",
        "severity": "Low",
        "cvss": 3.7,
        "cwe": "CWE-605",
        "type": "Network Configuration",
        "file_path": "backend/app/main.py, backend/run.py",
        "lines": "main.py:46, run.py:13",
        "endpoint": "Port 8000 (TCP)",
        "description": "Uvicorn server binds to `0.0.0.0:8000`, exposing the backend service to all local and external network interfaces rather than binding to `127.0.0.1` behind a reverse proxy.",
        "scenario": "In dual-homed or containerized environments, internal services may be directly exposed to unauthorized network segments.",
        "impact": "Direct access bypassing reverse proxy security rules and firewalls.",
        "remediation": "Bind to `127.0.0.1` in development and strictly enforce internal network isolation in production."
    },
    {
        "id": "VULN-019",
        "title": "Suppressed urllib3 Warning Masks Underlying SSL/TLS Library Incompatibilities",
        "severity": "Low",
        "cvss": 3.1,
        "cwe": "CWE-215",
        "type": "Improper Exception Handling",
        "file_path": "backend/app/main.py, backend/run.py",
        "lines": "main.py:3, run.py:3",
        "endpoint": "Startup Logic",
        "description": "The application explicitly ignores urllib3 warnings (`warnings.filterwarnings('ignore', message=r'.*urllib3.*doesn\\'t match a supported version.*')`), masking underlying SSL/TLS protocol compatibility issues.",
        "scenario": "Underlying OpenSSL or urllib3 deprecation warnings regarding insecure cipher suites or protocol versions are silently suppressed.",
        "impact": "Potential exposure to deprecated or insecure TLS protocols without operator visibility.",
        "remediation": "Resolve underlying version incompatibilities between OpenSSL and urllib3 instead of silencing security warnings."
    },
    {
        "id": "VULN-020",
        "title": "Unauthenticated Redis Cache Container Exposed on Host Port 6379",
        "severity": "Low",
        "cvss": 3.8,
        "cwe": "CWE-306",
        "type": "Missing Authentication",
        "file_path": "docker-compose.yml",
        "lines": "5-10",
        "endpoint": "Redis Port 6379",
        "description": "`docker-compose.yml` maps container port `6379:6379` to the host without password protection (`requirepass`). Anyone with access to the host port can interact with the Redis cache.",
        "scenario": "An attacker with network access to the host connects to port 6379, reading cached sessions or flushing cached data.",
        "impact": "Cache tampering and unauthenticated data access.",
        "remediation": "Remove host port binding for Redis in `docker-compose.yml` and require password authentication via `REDIS_PASSWORD`."
    }
]

# -----------------------------------------------------------------------------
# 2. COMPLETE API ENDPOINT INVENTORY
# -----------------------------------------------------------------------------
API_INVENTORY = [
    {
        "endpoint": "/",
        "method": "GET",
        "auth_required": "No",
        "roles": "Public",
        "controller": "backend/app/main.py:read_root",
        "risk": "Low",
        "params": "None",
        "response": "JSON {status, service, timestamp}"
    },
    {
        "endpoint": "/api/health",
        "method": "GET",
        "auth_required": "No",
        "roles": "Public",
        "controller": "backend/app/main.py:health_check",
        "risk": "Low",
        "params": "None",
        "response": "JSON {status, database}"
    },
    {
        "endpoint": "/api/auth/verify",
        "method": "POST",
        "auth_required": "No (Public Token Verifier)",
        "roles": "Public",
        "controller": "backend/app/routes/auth.py:verify_firebase_login",
        "risk": "Critical",
        "params": "JSON {id_token: str}",
        "response": "JSON {access_token, token_type, email, role, status}"
    },
    {
        "endpoint": "/api/scan/url",
        "method": "POST",
        "auth_required": "Optional (Bearer Token or X-User-Email)",
        "roles": "Public / Any Authenticated User",
        "controller": "backend/app/routes/scan.py:scan_url",
        "risk": "High",
        "params": "JSON {url: str}, Headers: [Authorization, X-User-Email]",
        "response": "JSON {url, status, score, details, scanned_at}"
    },
    {
        "endpoint": "/api/scan/fraud",
        "method": "POST",
        "auth_required": "Optional (Bearer Token or X-User-Email)",
        "roles": "Public / Any Authenticated User",
        "controller": "backend/app/routes/scan.py:scan_fraud",
        "risk": "High",
        "params": "JSON {content: str, scan_type: str}, Headers: [Authorization, X-User-Email]",
        "response": "JSON {scan_type, original_text, scam_probability, classification, explanation, contains_link, scanned_at}"
    },
    {
        "endpoint": "/api/scan/apk",
        "method": "POST",
        "auth_required": "Yes (Bearer Token)",
        "roles": "user, admin",
        "controller": "backend/app/routes/scan.py:scan_apk",
        "risk": "Medium",
        "params": "JSON {app_name: str, package_name: str, permissions: List[str]}",
        "response": "JSON {app_name, package_name, malware_score, threat_category, flagged_permissions, total_permissions_scanned, status}"
    },
    {
        "endpoint": "/api/analytics/telemetry",
        "method": "POST",
        "auth_required": "Yes (Bearer Token)",
        "roles": "user, admin",
        "controller": "backend/app/routes/analytics.py:update_telemetry",
        "risk": "Medium",
        "params": "JSON {device_model, os_version, security_score, battery_health, ram_usage_percent, storage_usage_percent}",
        "response": "JSON {message}"
    },
    {
        "endpoint": "/api/analytics/metrics",
        "method": "GET",
        "auth_required": "Optional / Bypassed via Header",
        "roles": "user, admin, Public (Spoofable)",
        "controller": "backend/app/routes/analytics.py:get_user_metrics",
        "risk": "High",
        "params": "Headers: [Authorization, X-User-Email]",
        "response": "JSON {summary, trends, threat_distribution, recent_threats, recent_scans}"
    },
    {
        "endpoint": "/api/analytics/reports",
        "method": "GET",
        "auth_required": "Yes (Bearer Token)",
        "roles": "user, admin",
        "controller": "backend/app/routes/analytics.py:get_pdf_reports",
        "risk": "Medium",
        "params": "Headers: [Authorization]",
        "response": "JSON Array of user PDF security reports"
    },
    {
        "endpoint": "/api/admin/overview",
        "method": "GET",
        "auth_required": "Yes (Bearer Token)",
        "roles": "admin",
        "controller": "backend/app/routes/admin.py:get_admin_overview",
        "risk": "Critical",
        "params": "Headers: [Authorization]",
        "response": "JSON {counters, recent_activity, recent_threats}"
    },
    {
        "endpoint": "/api/admin/threats/{threat_id}/resolve",
        "method": "POST",
        "auth_required": "Yes (Bearer Token)",
        "roles": "admin",
        "controller": "backend/app/routes/admin.py:resolve_threat",
        "risk": "High",
        "params": "Path {threat_id: str}, Headers: [Authorization]",
        "response": "JSON {message}"
    },
    {
        "endpoint": "/api/admin/users",
        "method": "GET",
        "auth_required": "Yes (Bearer Token)",
        "roles": "admin",
        "controller": "backend/app/routes/admin.py:list_system_users",
        "risk": "Critical",
        "params": "Headers: [Authorization]",
        "response": "JSON Array of registered users {id, email, full_name, role, created_at, is_active}"
    },
    {
        "endpoint": "/api/chat",
        "method": "POST",
        "auth_required": "No (Public LLM Proxy)",
        "roles": "Public",
        "controller": "backend/app/routes/chat.py:send_chat_message",
        "risk": "High",
        "params": "JSON {message: str, history: List[ChatMessage]}",
        "response": "JSON {reply, model, timestamp}"
    },
    {
        "endpoint": "/docs",
        "method": "GET",
        "auth_required": "No (Exposed in Production)",
        "roles": "Public",
        "controller": "FastAPI Built-in Swagger UI",
        "risk": "Medium",
        "params": "None",
        "response": "HTML Swagger Interactive UI"
    },
    {
        "endpoint": "/openapi.json",
        "method": "GET",
        "auth_required": "No (Exposed in Production)",
        "roles": "Public",
        "controller": "FastAPI Built-in OpenAPI Schema",
        "risk": "Medium",
        "params": "None",
        "response": "JSON OpenAPI 3.1.0 Specification"
    }
]

# -----------------------------------------------------------------------------
# 3. DEPENDENCY VULNERABILITIES DATASET
# -----------------------------------------------------------------------------
DEPENDENCY_VULNS = [
    {
        "package": "python-multipart",
        "installed": "0.0.9",
        "fixed": ">=0.0.20",
        "severity": "High",
        "cve": "CVE-2024-24762 / CVE-2024-53981",
        "type": "Regular Expression Denial of Service (ReDoS)",
        "description": "Vulnerability in Content-Type header parsing where malformed boundaries trigger catastrophic backtracking, freezing the server thread.",
        "remediation": "Update python-multipart to version 0.0.20 or newer in requirements.txt."
    },
    {
        "package": "jinja2",
        "installed": "3.1.3",
        "fixed": ">=3.1.5",
        "severity": "Medium",
        "cve": "CVE-2024-34064 / CVE-2024-56326",
        "type": "Template Injection & Autoescape Bypass",
        "description": "HTML attribute autoescape bypass vulnerability in the xmlattr filter allowing potential HTML attribute injection.",
        "remediation": "Update jinja2 to version 3.1.5 or newer in requirements.txt."
    },
    {
        "package": "python-jose",
        "installed": "3.3.0",
        "fixed": "Migrate to PyJWT>=2.10.0",
        "severity": "High",
        "cve": "CVE-2024-33663 / CVE-2024-33664",
        "type": "Algorithm Confusion & Cryptographic DoS",
        "description": "Unmaintained since 2021. Known algorithm confusion allowing ECDSA key bypass and quadratic denial of service on deeply nested compressed tokens.",
        "remediation": "Replace python-jose dependency with actively maintained PyJWT[crypto]>=2.10.1."
    },
    {
        "package": "urllib3",
        "installed": "2.2.3",
        "fixed": ">=2.3.0",
        "severity": "Medium",
        "cve": "CVE-2024-37891",
        "type": "Proxy-Authorization Header Leak Upon Cross-Origin Redirect",
        "description": "Proxy-Authorization headers are retained when following cross-origin redirects, potentially exposing credentials to third parties.",
        "remediation": "Update urllib3 to version 2.3.0 or newer."
    },
    {
        "package": "requests",
        "installed": "2.32.3",
        "fixed": ">=2.32.4",
        "severity": "Low",
        "cve": "N/A (Upstream urllib3 dependency)",
        "type": "Dependency Chaining",
        "description": "Requests wraps urllib3 and inherits session pool handling vulnerabilities if not pinned to latest stable release.",
        "remediation": "Ensure requests is pinned to 2.32.4+ and bundled with secure urllib3."
    },
    {
        "package": "reportlab",
        "installed": "4.1.0",
        "fixed": ">=4.3.0",
        "severity": "Medium",
        "cve": "CVE-2023-33733",
        "type": "Remote Code Execution via PDF Text Formatting",
        "description": "Older ReportLab releases contain RCE vulnerabilities via unsafe evaluation in custom font tags when parsing HTML to PDF.",
        "remediation": "Upgrade reportlab to version 4.3.0+ and sanitize all HTML inputs before PDF conversion."
    }
]


def generate_markdown_reports():
    # 1. security-review.md
    sr_path = os.path.join(results_dir, "security-review.md")
    with open(sr_path, "w", encoding="utf-8") as f:
        f.write("# Comprehensive Backend Security Review & Penetration Testing Report\n\n")
        f.write("**Application**: Sentinel AI Smart Mobile Security Backend\n")
        f.write(f"**Assessment Date**: {datetime.date.today().isoformat()}\n")
        f.write("**Target Architecture**: Python 3.11 / FastAPI REST API Engine\n")
        f.write("**Methodology**: OWASP Top 10 API Security 2023, CWE/SANS Top 25, PTES Standards\n\n")
        f.write("---\n\n")
        
        f.write("## 1. Vulnerability Findings Table\n\n")
        f.write("| ID | Finding Title | Severity | CVSS | CWE | Endpoint / Location |\n")
        f.write("|---|---|---|---|---|---|\n")
        for v in FINDINGS:
            f.write(f"| **{v['id']}** | {v['title']} | **{v['severity']}** | {v['cvss']} | {v['cwe']} | `{v['endpoint']}` |\n")
        
        f.write("\n---\n\n")
        f.write("## 2. Detailed Technical Findings & Remediation\n\n")

        for v in FINDINGS:
            f.write(f"### [{v['id']}] {v['title']}\n\n")
            f.write(f"- **Severity**: `{v['severity']}` (CVSS: {v['cvss']})\n")
            f.write(f"- **Vulnerability Type**: {v['type']}\n")
            f.write(f"- **File Path**: `{v['file_path']}` (Lines: {v['lines']})\n")
            f.write(f"- **Endpoint**: `{v['endpoint']}`\n")
            f.write(f"- **CWE Classification**: {v['cwe']}\n\n")
            
            f.write("#### Technical Description\n")
            f.write(f"{v['description']}\n\n")
            
            f.write("#### Exploitation Scenario\n")
            f.write(f"{v['scenario']}\n\n")
            
            f.write("#### Impact Assessment\n")
            f.write(f"{v['impact']}\n\n")
            
            f.write("#### Recommended Fix & Remediation Guidance\n")
            f.write(f"{v['remediation']}\n\n")
            f.write("---\n\n")

    # 2. executive-summary.md
    crit_count = len([v for v in FINDINGS if v['severity'] == 'Critical'])
    high_count = len([v for v in FINDINGS if v['severity'] == 'High'])
    med_count = len([v for v in FINDINGS if v['severity'] == 'Medium'])
    low_count = len([v for v in FINDINGS if v['severity'] == 'Low'])

    # Score calculation: 100 base - (Crit*15 + High*7 + Med*3 + Low*1)
    raw_deduction = (crit_count * 15) + (high_count * 7) + (med_count * 3) + (low_count * 1)
    security_score = max(15, 100 - raw_deduction)

    es_path = os.path.join(results_dir, "executive-summary.md")
    with open(es_path, "w", encoding="utf-8") as f:
        f.write("# Executive Summary: Backend Security Assessment\n\n")
        f.write("## Security Posture Overview\n\n")
        f.write("A comprehensive, non-destructive penetration test, static code analysis (SAST), and dynamic API assessment (DAST) was performed against the **Sentinel AI Backend Architecture**. The system operates on Python FastAPI with MongoDB Atlas, Firebase Identity, and machine-learning threat classifiers.\n\n")
        
        f.write("## Overall Security Score\n\n")
        f.write(f"# **{security_score} / 100** (Security Rating: HIGH RISK - Remediation Required)\n\n")
        
        f.write("## Total Findings Breakdown\n\n")
        f.write(f"- **Critical**: {crit_count}\n")
        f.write(f"- **High**: {high_count}\n")
        f.write(f"- **Medium**: {med_count}\n")
        f.write(f"- **Low**: {low_count}\n")
        f.write(f"- **Total Security Issues**: {len(FINDINGS)}\n\n")

        f.write("## Most Critical Risks\n\n")
        f.write("1. **Hardcoded Master Backdoor Secret & Token Prefix Bypass (VULN-001 & VULN-002)**:\n")
        f.write("   The authentication pipeline in `backend/app/core/security.py` allows unauthenticated callers to bypass all signature verification by sending `Authorization: Bearer SUPER_SECRET_NEON_SENTINEL_SHIELD_KEY_2026` or forged unsigned JWT tokens. This allows immediate access to administrative controls and customer data.\n\n")
        f.write("2. **Hardcoded Production Credentials & Private RSA Keys in Repository (VULN-003 & VULN-004)**:\n")
        f.write("   Production Firebase Service Account RSA private keys (`service_account.json`) and MongoDB Atlas connection URIs with plaintext database passwords (`database.py`, `.env`, `docker-compose.yml`) are committed directly to the git repository.\n\n")
        f.write("3. **Broken Object Level Authorization (BOLA/IDOR) via Client Headers (VULN-005)**:\n")
        f.write("   The `/api/analytics/metrics` and scan endpoints trust client-supplied `X-User-Email` HTTP headers without checking authentication tokens, allowing unauthenticated attackers to view private telemetry, threats, and device details of any victim.\n\n")

        f.write("## Strategic Remediation Roadmap\n\n")
        f.write("1. **Phase 1 (Immediate / Within 24 Hours)**: Strip hardcoded master keys and unverified JWT fallback logic from `security.py`. Rotate MongoDB Atlas database passwords and Firebase Admin private keys.\n")
        f.write("2. **Phase 2 (Short Term / Within 72 Hours)**: Restrict CORS policy in `main.py`. Remove `X-User-Email` header trust and enforce strict token-derived identity.\n")
        f.write("3. **Phase 3 (Medium Term / Next Release)**: Upgrade vulnerable dependencies (`python-multipart`, `jinja2`). Implement Redis/SlowAPI rate limiting across `/api/auth/verify`, `/api/scan/*`, and `/api/chat`.\n")

    # 3. dependency-report.md
    dr_path = os.path.join(results_dir, "dependency-report.md")
    with open(dr_path, "w", encoding="utf-8") as f:
        f.write("# Software Composition Analysis & Dependency Security Report\n\n")
        f.write("## Overview\n")
        f.write("Automated software composition analysis (SCA) was conducted across `backend/requirements.txt` to identify known Common Vulnerabilities and Exposures (CVEs), unmaintained libraries, and supply-chain threats.\n\n")
        
        f.write("## Identified Dependency Vulnerabilities\n\n")
        f.write("| Package | Installed Version | Safe Version | Severity | Known CVE | Vulnerability Type |\n")
        f.write("|---|---|---|---|---|---|\n")
        for d in DEPENDENCY_VULNS:
            f.write(f"| **{d['package']}** | `{d['installed']}` | `{d['fixed']}` | **{d['severity']}** | {d['cve']} | {d['type']} |\n")

        f.write("\n---\n\n")
        f.write("## Technical Details & Remediation Instructions\n\n")
        for d in DEPENDENCY_VULNS:
            f.write(f"### {d['package']} (`{d['installed']}`)\n")
            f.write(f"- **Severity**: `{d['severity']}`\n")
            f.write(f"- **CVE Reference**: `{d['cve']}`\n")
            f.write(f"- **Vulnerability**: {d['description']}\n")
            f.write(f"- **Recommended Fix**: {d['remediation']}\n\n")

        f.write("## Supply-Chain Best Practices\n")
        f.write("- Enable GitHub Dependabot and Dependency Review action in CI/CD pipeline.\n")
        f.write("- Pin exact package hashes using `pip-tools` or `poetry.lock`.\n")
        f.write("- Execute automated vulnerability audits with `pip-audit` on every pull request.\n")


def generate_excel_workbooks():
    # Helper styles
    header_fill = PatternFill(start_color="1E293B", end_color="1E293B", fill_type="solid")
    header_font = Font(name="Calibri", size=11, bold=True, color="FFFFFF")
    bold_font = Font(name="Calibri", size=10, bold=True)
    regular_font = Font(name="Calibri", size=10)
    thin_side = Side(border_style="thin", color="CBD5E1")
    cell_border = Border(top=thin_side, left=thin_side, right=thin_side, bottom=thin_side)

    sev_fills = {
        "Critical": PatternFill(start_color="FEE2E2", end_color="FEE2E2", fill_type="solid"),
        "High": PatternFill(start_color="FFEDD5", end_color="FFEDD5", fill_type="solid"),
        "Medium": PatternFill(start_color="FEF3C7", end_color="FEF3C7", fill_type="solid"),
        "Low": PatternFill(start_color="DCFCE7", end_color="DCFCE7", fill_type="solid"),
    }
    sev_fonts = {
        "Critical": Font(name="Calibri", size=10, bold=True, color="991B1B"),
        "High": Font(name="Calibri", size=10, bold=True, color="C2410C"),
        "Medium": Font(name="Calibri", size=10, bold=True, color="B45309"),
        "Low": Font(name="Calibri", size=10, bold=True, color="15803D"),
    }

    def style_table(ws, headers):
        ws.append(headers)
        for col_num in range(1, len(headers) + 1):
            cell = ws.cell(row=1, column=col_num)
            cell.fill = header_fill
            cell.font = header_font
            cell.alignment = Alignment(horizontal="center", vertical="center", wrap_text=True)
        ws.row_dimensions[1].height = 28

    def autofit_and_border(ws):
        for row in ws.iter_rows(min_row=2, max_row=ws.max_row, min_col=1, max_col=ws.max_column):
            for cell in row:
                cell.border = cell_border
                cell.alignment = Alignment(vertical="center", wrap_text=True)
                if not cell.font or not cell.font.bold:
                    cell.font = regular_font
        for col in ws.columns:
            max_len = max(len(str(cell.value or "")) for cell in col)
            col_letter = get_column_letter(col[0].column)
            ws.column_dimensions[col_letter].width = min(max(max_len + 3, 12), 48)
        ws.auto_filter.ref = ws.dimensions

    # -------------------------------------------------------------------------
    # WORKBOOK 1: endpoint-inventory.xlsx
    # -------------------------------------------------------------------------
    wb_ep = Workbook()
    ws_ep = wb_ep.active
    ws_ep.title = "Endpoint Inventory"
    ep_headers = ["Endpoint", "HTTP Method", "Authentication Required", "Expected Roles", "Controller/File Path", "Risk Level", "Parameters", "Response Schema"]
    style_table(ws_ep, ep_headers)

    for ep in API_INVENTORY:
        row_idx = ws_ep.max_row + 1
        ws_ep.append([
            ep["endpoint"], ep["method"], ep["auth_required"], ep["roles"], ep["controller"], ep["risk"], ep["params"], ep["response"]
        ])
        # Style risk cell
        r_cell = ws_ep.cell(row=row_idx, column=6)
        if ep["risk"] in sev_fills:
            r_cell.fill = sev_fills[ep["risk"]]
            r_cell.font = sev_fonts[ep["risk"]]

    autofit_and_border(ws_ep)
    wb_ep.save(os.path.join(results_dir, "endpoint-inventory.xlsx"))

    # -------------------------------------------------------------------------
    # WORKBOOK 2: findings.xlsx (4 Sheets)
    # -------------------------------------------------------------------------
    wb_findings = Workbook()
    
    # Sheet 1: Security Findings
    ws_s1 = wb_findings.active
    ws_s1.title = "Security Findings"
    s1_headers = ["Finding ID", "Vulnerability Title", "Severity", "CVSS Score", "CWE ID", "Vulnerability Type", "File Path", "Lines", "Affected Endpoint", "Description", "Exploitation Scenario", "Impact", "Recommended Fix"]
    style_table(ws_s1, s1_headers)
    for v in FINDINGS:
        row_idx = ws_s1.max_row + 1
        ws_s1.append([
            v["id"], v["title"], v["severity"], v["cvss"], v["cwe"], v["type"], v["file_path"], v["lines"], v["endpoint"], v["description"], v["scenario"], v["impact"], v["remediation"]
        ])
        sev_cell = ws_s1.cell(row=row_idx, column=3)
        if v["severity"] in sev_fills:
            sev_cell.fill = sev_fills[v["severity"]]
            sev_cell.font = sev_fonts[v["severity"]]
    autofit_and_border(ws_s1)

    # Sheet 2: Endpoint Inventory
    ws_s2 = wb_findings.create_sheet(title="Endpoint Inventory")
    style_table(ws_s2, ep_headers)
    for ep in API_INVENTORY:
        row_idx = ws_s2.max_row + 1
        ws_s2.append([
            ep["endpoint"], ep["method"], ep["auth_required"], ep["roles"], ep["controller"], ep["risk"], ep["params"], ep["response"]
        ])
        r_cell = ws_s2.cell(row=row_idx, column=6)
        if ep["risk"] in sev_fills:
            r_cell.fill = sev_fills[ep["risk"]]
            r_cell.font = sev_fonts[ep["risk"]]
    autofit_and_border(ws_s2)

    # Sheet 3: Dependency Vulnerabilities
    ws_s3 = wb_findings.create_sheet(title="Dependency Vulnerabilities")
    s3_headers = ["Package Name", "Installed Version", "Fixed Version", "Severity", "CVE Identifier", "Vulnerability Type", "Description", "Remediation Guidance"]
    style_table(ws_s3, s3_headers)
    for d in DEPENDENCY_VULNS:
        row_idx = ws_s3.max_row + 1
        ws_s3.append([
            d["package"], d["installed"], d["fixed"], d["severity"], d["cve"], d["type"], d["description"], d["remediation"]
        ])
        sev_cell = ws_s3.cell(row=row_idx, column=4)
        if d["severity"] in sev_fills:
            sev_cell.fill = sev_fills[d["severity"]]
            sev_cell.font = sev_fonts[d["severity"]]
    autofit_and_border(ws_s3)

    # Sheet 4: Risk Summary
    ws_s4 = wb_findings.create_sheet(title="Risk Summary")
    s4_headers = ["Severity Level", "Vulnerability Count", "Percentage of Total", "Risk Weight / Deduction", "Status"]
    style_table(ws_s4, s4_headers)

    crit_cnt = len([v for v in FINDINGS if v['severity'] == 'Critical'])
    high_cnt = len([v for v in FINDINGS if v['severity'] == 'High'])
    med_cnt = len([v for v in FINDINGS if v['severity'] == 'Medium'])
    low_cnt = len([v for v in FINDINGS if v['severity'] == 'Low'])
    total_cnt = len(FINDINGS)

    summary_rows = [
        ("Critical", crit_cnt, f"{(crit_cnt/total_cnt)*100:.1f}%", f"-{crit_cnt*15} pts", "Immediate Fix Required (Blocker)"),
        ("High", high_cnt, f"{(high_cnt/total_cnt)*100:.1f}%", f"-{high_cnt*7} pts", "Remediate in Next Sprint"),
        ("Medium", med_cnt, f"{(med_cnt/total_cnt)*100:.1f}%", f"-{med_cnt*3} pts", "Scheduled Maintenance"),
        ("Low", low_cnt, f"{(low_cnt/total_cnt)*100:.1f}%", f"-{low_cnt*1} pts", "Backlog Hardening"),
    ]
    for row in summary_rows:
        row_idx = ws_s4.max_row + 1
        ws_s4.append(list(row))
        sev_cell = ws_s4.cell(row=row_idx, column=1)
        if row[0] in sev_fills:
            sev_cell.fill = sev_fills[row[0]]
            sev_cell.font = sev_fonts[row[0]]

    # Add overall score row
    ws_s4.append([])
    score_idx = ws_s4.max_row + 1
    raw_deduct = (crit_cnt * 15) + (high_cnt * 7) + (med_cnt * 3) + (low_cnt * 1)
    final_score = max(15, 100 - raw_deduct)
    ws_s4.append(["OVERALL SECURITY SCORE", f"{final_score} / 100", "Risk Level: HIGH", f"Total Deductions: -{raw_deduct}", "Action: Apply Security Patches"])
    for col_i in range(1, 6):
        c = ws_s4.cell(row=score_idx+1, column=col_i)
        c.font = Font(name="Calibri", size=11, bold=True, color="991B1B")
        c.fill = PatternFill(start_color="FEE2E2", end_color="FEE2E2", fill_type="solid")

    autofit_and_border(ws_s4)
    wb_findings.save(os.path.join(results_dir, "findings.xlsx"))


if __name__ == "__main__":
    generate_markdown_reports()
    generate_excel_workbooks()
    print("[Sentinel AI] Successfully generated all security reports and Excel spreadsheets in 'Vulnerability Test Results/'")
