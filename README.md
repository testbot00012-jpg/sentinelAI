# Sentinel AI - Smart Mobile Security, Fraud Detection & Optimization Platform

Sentinel AI is a production-grade, state-of-the-art security intelligence suite featuring real-time URL Phishing classification, NLP SMS Scam heuristic checks, permission threat auditors, and performance analytics dashboards.

---

## Technical Stack Architecture

- **Backend**: FastAPI (Python 3.13), SQLAlchemy, PostgreSQL, Redis, Scikit-learn, Firebase ID Token JWT validation.
- **Web Console**: Next.js 15 App Router, TypeScript, Tailwind CSS, Recharts, Zustand.
- **Android App**: Kotlin, Jetpack Compose, Material Design 3, Retrofit, MVVM Architecture.

---

## 🔥 Firebase Authentication Integration

Sentinel AI utilizes **Firebase Authentication** as its primary identity provider.
- **ID Token Exchange**: The Android app or Web App handles user sign-in directly using the Firebase SDK and acquires a Firebase ID Token.
- **Secure API Requests**: The client sends the ID Token in the `Authorization: Bearer <ID_TOKEN>` header for API queries.
- **Automatic User Provisioning (SSO)**: The backend validates the ID Token's signatures against Google's public certificates. On successful validation, it automatically provisions a local PostgreSQL user record on the fly to track threat histories and devices!
- **Setup Configuration**: To connect your live Firebase project, set your project identifier inside `backend/.env`:
  ```ini
  FIREBASE_PROJECT_ID=your-firebase-project-id
  ```

---


## ⚡ Deployment & Orchestration Guide

Deploy the entire platform ecosystem (Web Dashboard, API Engine, Database, Redis Cache, Nginx Proxy) instantly with a single multi-container command:

```bash
docker-compose up --build
```

Once initialized, the platform will be mapped to the following standard gateways:
- **Web Dashboard**: [http://localhost](http://localhost) (Proxied via Nginx)
- **FastAPI Core Swagger Console**: [http://localhost:8000/docs](http://localhost:8000/docs)
- **Raw API Health endpoint**: [http://localhost:8000/api/health](http://localhost:8000/api/health)

---

## 📱 Building the Android APK (Kotlin Jetpack Compose)

Compile the production-ready Android mobile platform using standard Gradle steps:

### Prerequisites
- JDK 17 (Already installed in the local system environment)
- Android Studio or Android SDK

### Steps to Compile
1. Navigate into the android directory:
   ```powershell
   cd android
   ```
2. Run the Gradle build wrapper task to generate a debug APK:
   ```powershell
   ./gradlew assembleDebug
   ```
3. Locate the compiled APK at:
   `android/app/build/outputs/apk/debug/app-debug.apk`

---

## 📊 Complete Free Security APIs Guide & Documentation

Sentinel AI does not require expensive subscription keys. The entire classification model engine runs **100% free and locally** using our advanced rule-based heuristics & NLP TF-IDF score emulators inside Python.

For developers seeking external threat-intelligence databases, the following **completely free security APIs** are highly recommended:

1. **PhishTank API (URL Threat Intelligence)**
   - **Purpose**: Detect phishing URLs against a community-run blacklist.
   - **Cost**: 100% Free.
   - **How to Get**: Register for a developer key at [https://www.phishtank.com/developer_info.php](https://www.phishtank.com/developer_info.php).

2. **AlienVault OTX (Open Threat Exchange)**
   - **Purpose**: Query IP addresses, domains, and file hashes for known malware threat scores.
   - **Cost**: 100% Free.
   - **How to Get**: Sign up for an account at [https://otx.alienvault.com](https://otx.alienvault.com) to retrieve your personal API key immediately.

3. **URLScan.io API**
   - **Purpose**: Run automated scans of unknown websites to capture screenshots, domain reputations, and cookies risk factors.
   - **Cost**: Free Tier provides 5,000 scans per month.
   - **How to Get**: Register for a free account at [https://urlscan.io/about-api/](https://urlscan.io/about-api/).
