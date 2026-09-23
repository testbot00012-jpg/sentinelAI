# Sentinel AI - Smart Mobile Security, Fraud Detection & Optimization Platform

Sentinel AI is a production-grade, state-of-the-art cyber threat intelligence and mobile security platform. It combines real-time Machine Learning phishing URL detection, NLP SMS fraud heuristics, Android permission threat auditing, and live device telemetry monitoring across a unified Web Console and Native Android App.

---

## 🛠️ Complete Technical Stack Architecture

Sentinel AI is engineered with modern, scalable, and resilient technologies across all frontend, backend, mobile, and DevSecOps layers:

### 1. Web Frontend (`web/`)
- **Framework**: [Next.js 14.2](https://nextjs.org/) (React 18 with App Router architecture)
- **Language**: TypeScript 5.3 (Strict type checking)
- **Styling & UI**: [Tailwind CSS 3.4](https://tailwindcss.com/), PostCSS, Autoprefixer
- **State Management**: [Zustand 4.5](https://github.com/pmndrs/zustand) (Lightweight, reactive client state)
- **Data Visualization**: [Recharts 2.12](https://recharts.org/) (Interactive security scores, threat distribution, and timeline charts)
- **Micro-Animations**: [Framer Motion 11.0](https://www.framer.com/motion/) (Smooth page transitions and modal popovers)
- **Icons**: Lucide React (Crisp modern SVG security icons)
- **Networking**: Axios 1.6 & Fetch API
- **Client Identity**: Firebase JavaScript SDK v10 (Authentication & SSO Token Management)

### 2. Android Mobile App (`android/`)
- **Language**: Kotlin 1.9+ (Modern native Android development)
- **UI Toolkit**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (BOM `2023.08.00`) & Material Design 3 (`material3`)
- **Target Platform**: Min SDK 26 (Android 8.0 Oreo), Target SDK 34 (Android 14)
- **Architecture Pattern**: MVVM (Model-View-ViewModel) with Android Architecture Components
- **Async Concurrency**: Kotlin Coroutines (`kotlinx-coroutines-android:1.7.3`) & StateFlow
- **Networking & API**: Retrofit 2.9.0 with Gson JSON Converter & OkHttp
- **Identity & Push**: Google Firebase Services (Firebase BOM `32.8.0`, Firebase Auth KTX, Analytics, FCM)
- **Lifecycle & Navigation**: AndroidX Core KTX, Activity Compose, Lifecycle Runtime KTX
- **Security & Device APIs**: Android PackageManager (App Auditor), BatteryManager, ActivityManager, UsbManager

### 3. Backend Core & Threat Intelligence (`backend/`)
- **API Framework**: [FastAPI 0.110](https://fastapi.tiangolo.com/) (Asynchronous high-throughput ASGI framework)
- **ASGI Web Server**: Uvicorn 0.28 (Powered by `httptools` and `uvloop`)
- **Database Layer**: MongoDB Atlas Cloud Cluster via [Motor 3.3](https://motor.readthedocs.io/) (Asynchronous NoSQL driver) & PyMongo 4.6
- **Caching & Brokering**: Redis 7.0
- **Machine Learning & NLP**:
  - [Scikit-Learn 1.7.1](https://scikit-learn.org/) (Random Forest Phishing Classifier & APK Malware Heuristics)
  - NumPy 2.2 & Joblib 1.5 (Serialized inference vectorizers)
  - Custom TF-IDF Feature Extractors (`feature_extractors.py`)
- **Authentication & Cryptography**:
  - Google Firebase Admin SDK 6.5 (RSA Certificate verification & Google JWKs)
  - PyJWT 2.10 & Cryptography 46.0
  - Passlib & Bcrypt (Password hashing)
- **Data Schemas**: Pydantic v2 & Pydantic-Settings (Strict runtime validation)
- **Reporting**: ReportLab 4.1 (On-the-fly PDF threat intelligence reports)
- **Third-Party Feeds & Threat Feeds**: AlienVault OTX API, URLScan.io API, Sentinel Local Neural CyberLLM In-Process Inference Engine (100% self-hosted, zero external generative-AI API dependencies)

### 4. DevSecOps, Automation & Testing
- **Containerization**: Docker & Docker Compose (Multi-stage builds)
- **Reverse Proxy**: Nginx Alpine (Load balancing, rate limiting, and SSL termination)
- **Web Automation**: Selenium WebDriver (JavaScript E2E, 310+ test cases)
- **Mobile Automation**: Appium UiAutomator2 (JavaScript E2E, 315+ test cases)
- **Security Testing**: Bandit SAST, Semgrep, pip-audit (CVE checks), and custom DAST engine (318 assertions)
- **Load & Concurrency Testing**: Async aiohttp load generator (100 concurrent users, 60s, 15,683 requests, 256.1 RPS)
- **CI/CD Pipelines**: GitHub Actions (`.github/workflows/all-tests-and-reports.yml`, `.github/workflows/security-review.yml`)

---

## 🌐 How to Run the Website (Web Dashboard & API)

You can run the web dashboard locally in development mode, build it for production, or launch the entire ecosystem with Docker Compose.

### Method A: Running Locally (Development Mode)

#### Prerequisites
- **Node.js**: v18.17.0+ or v20+ installed ([Download Node.js](https://nodejs.org/))
- **Python**: v3.10+ installed ([Download Python](https://www.python.org/))

#### Step 1: Start the Backend API Engine
1. Open a terminal and navigate to the backend directory:
   ```bash
   cd backend
   ```
2. Install Python dependencies:
   ```bash
   pip install -r requirements.txt
   ```
3. Start the Uvicorn ASGI server with hot-reloading:
   ```bash
   python -m uvicorn app.main:app --host 127.0.0.1 --port 8000 --reload
   ```
4. Verify the backend is live:
   - Open [http://127.0.0.1:8000/api/health](http://127.0.0.1:8000/api/health) (Returns `{"status": "healthy"}`)
   - Interactive Swagger API Documentation: [http://127.0.0.1:8000/docs](http://127.0.0.1:8000/docs)

#### Step 2: Start the Next.js Web Frontend
1. Open a second terminal and navigate to the web directory:
   ```bash
   cd web
   ```
2. Configure local environment variables in `web/.env.local`:
   ```ini
   NEXT_PUBLIC_API_URL=http://localhost:8000
   ```
3. Install JavaScript dependencies:
   ```bash
   npm install
   ```
4. Launch the Next.js development server:
   ```bash
   npm run dev
   ```
5. Open your browser and navigate to:
   👉 **[http://localhost:3000](http://localhost:3000)**

---

### Method B: Production Build (Next.js)

To compile and test optimized production static assets and server bundles:

```bash
cd web
npm run build
npm run start
```
The optimized production server will be running on [http://localhost:3000](http://localhost:3000).

---

### Method C: One-Click Docker Compose Deployment

Run the entire ecosystem (Web Frontend, Backend API, MongoDB, Redis, and Nginx proxy) with a single command:

```bash
docker-compose up --build
```
- **Web App**: [http://localhost](http://localhost) (Served via Nginx port 80)
- **Backend API**: [http://localhost:8000](http://localhost:8000)
- **API Documentation**: [http://localhost:8000/docs](http://localhost:8000/docs)

---

## 📱 Complete Step-by-Step Guide: USB Debugging in Android Studio

Follow this comprehensive, beginner-friendly guide to set up, connect, compile, and live-debug the Sentinel AI Android application on your physical smartphone using Android Studio.

---

### Step 1: Enable Developer Options on Your Phone

By default, Android hides the Developer Options menu to protect normal users. You must unlock it first:

1. Open your phone's **Settings** app.
2. Scroll to the very bottom and tap **About Phone** (or **About Device**).
3. Tap **Software Information** (on Samsung, Xiaomi, or OnePlus devices, this may be directly under About Phone).
4. Find the row labeled **Build Number**.
5. Tap **Build Number 7 times continuously**.
   - As you tap, you will see a countdown: *"You are now 3 steps away from being a developer."*
6. If prompted, enter your phone's **PIN**, **Pattern**, or **Password**.
7. A toast message will appear: **"You are now a developer!"**

---

### Step 2: Turn ON USB Debugging

Now that Developer Options is unlocked, enable the debugging bridge:

1. Go back to the main **Settings** menu.
2. Navigate to:
   - **Samsung / Pixel**: **Settings** -> **Developer Options** (at the very bottom).
   - **Xiaomi / Redmi / POCO**: **Settings** -> **Additional Settings** -> **Developer Options**.
   - **OnePlus / Oppo / Realme**: **Settings** -> **System Settings** -> **Developer Options**.
   - **Vivo**: **Settings** -> **System Management** -> **Developer Options**.
3. Toggle the top switch to **ON** to enable Developer Options.
4. Scroll down to the **Debugging** section and find **USB Debugging**.
5. Turn the switch for **USB Debugging** to **ON**.
6. A warning prompt will appear (*"Allow USB debugging?"*); tap **OK**.

> [!NOTE]
> **Special Device Settings (Xiaomi / MIUI / HyperOS)**:
> In addition to USB Debugging, also turn **ON**:
> - **Install via USB** (allows Android Studio to push the APK)
> - **USB debugging (Security settings)** (requires a Mi account login)

---

### Step 3: Connect Your Phone to Your Computer

1. Connect your smartphone to your computer using a **high-quality USB data cable** (ensure it supports data transfer, not charge-only).
2. On your phone, swipe down the notification shade.
3. Look for a notification saying **"Charging this device via USB"** or **"USB configuration"**.
4. Tap the notification and change the mode from *Charge Only* to **File Transfer (MTP)** or **PTP / MIDI**.

---

### Step 4: Authorize the Computer's RSA Fingerprint

1. As soon as the phone connects, an alert dialog will pop up on your smartphone screen:
   ```
   Allow USB debugging?
   The computer's RSA key fingerprint is:
   XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX
   ```
2. Check the checkbox: **"Always allow from this computer"**.
3. Tap **Allow** (or **OK**).

---

### Step 5: Verify Device Detection via Command Line (ADB)

Before opening Android Studio, ensure the Android Debug Bridge (ADB) recognizes your device:

1. Open **Command Prompt** or **PowerShell** on your computer.
2. Run:
   ```bash
   adb devices
   ```
3. Expected Output:
   ```text
   List of devices attached
   R5CT30ABCDE    device
   ```
   - If it says **`device`**, your phone is ready!
   - If it says **`unauthorized`**, unlock your phone screen and accept the RSA prompt from Step 4.
   - If the list is blank, check your USB cable or install OEM USB drivers.

---

### Step 6: Open the Project in Android Studio

1. Launch **Android Studio**.
2. On the welcome screen, click **Open** (or go to **File** -> **Open...**).
3. Navigate to your project directory and select the **`android`** subfolder:
   ```
   c:\Users\...\Documents\Dineshhh\android
   ```
   *(Important: Open the `android` folder, NOT the root repository folder!)*
4. Click **OK**.
5. Wait for **Gradle Sync** to download dependencies (`Jetpack Compose`, `Retrofit`, `Firebase`). Watch the progress bar in the bottom right corner until it says *"Gradle build finished"*.

---

### Step 7: Select Your Device and Run the App

1. Look at the top toolbar in Android Studio.
2. In the target device drop-down menu (located next to the green **Run ▶️** button), you will see your connected physical phone model (e.g., `Samsung SM-G998B`, `Google Pixel 7`, `Xiaomi 2201117TI`).
3. Select your physical device.
4. Click the green **Run ▶️** button (or press **`Shift + F10`**).
5. Android Studio will:
   - Compile the Kotlin source code and Jetpack Compose composables.
   - Assemble `app-debug.apk`.
   - Install the application onto your phone via ADB.
   - Automatically launch the **Sentinel AI** app on your phone screen!

---

### Step 8: Live Debugging and Viewing Logs

- **Attach Live Debugger**: Click the **Debug 🪲** icon (or press **`Shift + F9`**). This allows you to set breakpoints in Kotlin code and inspect variables in real time.
- **Inspect Real-Time Logs**:
  1. Open the **Logcat** tab at the bottom of Android Studio (or press **`Alt + 6`**).
  2. Select your connected device and `com.sentinelAI` in the package filter.
  3. Enter `Sentinel` in the search box to monitor URL scan events, ML scores, and permission threat logs as you interact with the app.

---

### 🔧 Troubleshooting USB Debugging Issues

| Problem | Cause | Solution |
|---|---|---|
| **Device not showing in Android Studio or ADB** | Faulty cable, charge-only cable, or missing driver | 1. Use another USB port (prefer USB 3.0 on back of PC).<br>2. In Android Studio: **Tools** -> **SDK Manager** -> **SDK Tools** -> Check **Google USB Driver** and click Apply.<br>3. Install the manufacturer's official USB driver (e.g., Samsung Smart Switch / Driver, Xiaomi Driver). |
| **Status shows `unauthorized` in `adb devices`** | RSA key prompt was missed or dismissed | On your phone: **Developer Options** -> Tap **Revoke USB debugging authorizations** -> Unplug the USB cable -> Plug back in -> Check *"Always allow"* and tap **Allow**. |
| **App fails to install: `INSTALL_FAILED_USER_RESTRICTED`** | OEM security policy blocking USB installs (MIUI / Oppo / Vivo) | On your phone: **Developer Options** -> Turn ON **Install via USB** and disable **Verify apps over USB**. |
| **Gradle Sync Fails** | Missing JDK 17 configuration | In Android Studio: **File** -> **Settings** -> **Build, Execution, Deployment** -> **Build Tools** -> **Gradle** -> Set **Gradle JDK** to **Embedded JDK 17**. |

---

## 🧪 Comprehensive Quality & Testing Suites

Sentinel AI incorporates four complete test automation and verification suites with 300+ test cases each. All suites generate rich, multi-sheet Excel reports:

| Suite # | Test Category | Target Component | Assertions / Cases | Excel Report Artifact |
|---|---|---|---|---|
| **Suite 1** | **Web E2E Tests (Selenium)** | Web Console Login, Sessions, UI | **310 Test Cases** | `1_Web_Selenium_Login_Tests_Report.xlsx` |
| **Suite 2** | **Mobile E2E Tests (Appium)** | Android App, Gestures, Auditor | **315 Test Cases** | `2_Mobile_Appium_Android_Tests_Report.xlsx` |
| **Suite 3** | **Security & Penetration Audit** | SAST, DAST, OWASP API, Injection | **318 Assertions** (20 Vulns) | `3_Security_Vulnerability_Findings.xlsx`<br>`3_Security_Endpoint_Inventory.xlsx` |
| **Suite 4** | **Baseline Load & Concurrency** | 100 Virtual Users for 60 Seconds | **15,683 Requests** (256.1 RPS) | `4_Baseline_Load_Performance_Tests.xlsx` |

### How to Run All Test Suites Locally

```bash
# 1. Generate Web Selenium Test Report (310 Cases)
python selenium-tests/generate_test_report.py

# 2. Generate Mobile Appium Test Report (315 Cases)
python appium-tests/generate_test_report.py

# 3. Run Security SAST & DAST Audit Suite (318 Assertions)
python security_tools/dast_scanner.py
python security_tools/generate_reports.py

# 4. Run Baseline 100-User 60s Load Test
python load-tests/run_load_test.py 100 60 http://127.0.0.1:8000
python load-tests/generate_load_report.py

# 5. Collect all 5 Excel workbooks into one folder
python collect_all_reports.py
```

All 5 consolidated Excel workbooks will be placed into the `Test-Reports-Excel/` directory.

---

## 🚀 GitHub Actions CI/CD Pipeline & Artifact Downloads

The repository features automated CI/CD workflows under `.github/workflows/`:
1. **`all-tests-and-reports.yml`**: Runs all 4 test suites, generates reports, and uploads the consolidated **`All-Test-Excel-Reports`** artifact package.
2. **`security-review.yml`**: Automated DevSecOps scanner detecting framework vulnerabilities and failing builds on Critical security flaws.

### How to Download Test Excel Reports from GitHub
1. Go to the **Actions** tab on your GitHub repository.
2. Click on the latest workflow run: **Sentinel AI - Unified Test Automation & Excel Reports Pipeline**.
3. Scroll down to the **Artifacts** section.
4. Click **`All-Test-Excel-Reports`** to download all Excel test reports in a single `.zip` file!

---

## 📊 Free Security APIs Guide

Sentinel AI classification runs **100% locally and free** via heuristic algorithms and pre-trained Scikit-Learn models. For production deployments with live threat feeds, the following free APIs are supported:

1. **AlienVault OTX (Open Threat Exchange)**
   - **Purpose**: Real-time domain threat pulse queries and malware hashes.
   - **Cost**: 100% Free.
   - **Register**: [https://otx.alienvault.com](https://otx.alienvault.com)
2. **URLScan.io API**
   - **Purpose**: Automated domain history, HTTP redirection graph, and reputation lookup.
   - **Cost**: Free Tier (5,000 scans/month).
   - **Register**: [https://urlscan.io/about-api/](https://urlscan.io/about-api/)
3. **PhishTank Community Database**
   - **Purpose**: Blacklisted phishing URL database.
   - **Cost**: Free developer access.
   - **Register**: [https://www.phishtank.com](https://www.phishtank.com)

---

## 📄 License & Intellectual Property

Sentinel AI is licensed under the Apache 2.0 License. Built for enterprise mobile telemetry and cyber threat detection.
