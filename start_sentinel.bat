@echo off
title Sentinel AI - Enterprise Security Suite
echo =====================================================================
echo           Sentinel AI Enterprise Security & Fraud Suite
echo                Automatic Zero-Configuration Startup
echo =====================================================================
echo.

:: Check for Python
where python >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Python is not installed or not in PATH. Please install Python 3.10+.
    pause
    exit /b 1
)

:: Check for Node.js
where npm >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Node.js / npm is not installed or not in PATH. Please install Node.js 18+.
    pause
    exit /b 1
)

echo [1/3] Checking Backend Python Dependencies...
pip install -r backend/requirements.txt --quiet --no-warn-script-location

echo [2/3] Checking Web Frontend Dependencies...
cd web
if not exist node_modules (
    echo Installing npm packages...
    call npm install --quiet
)
cd ..

echo [3/3] Launching Sentinel AI Platform...
echo.
echo    - FastAPI Backend: http://127.0.0.1:8000
echo    - Next.js Web App: http://localhost:3000
echo    - Android App: Ready for USB Debugging or Standalone
echo.

start "Sentinel AI - FastAPI Backend (Port 8000)" cmd /k "cd backend && python run.py"
timeout /t 3 /nobreak >nul

start "Sentinel AI - Web Console (Port 3000)" cmd /k "cd web && npm run dev"
timeout /t 3 /nobreak >nul

start http://localhost:3000

echo =====================================================================
echo  Sentinel AI is now running! Both Web & Backend are active.
echo  Closing this window will leave the servers running in their tabs.
echo =====================================================================
pause
