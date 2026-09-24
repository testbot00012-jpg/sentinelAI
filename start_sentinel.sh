#!/usr/bin/env bash
# Sentinel AI - Automated Zero-Configuration Startup Script for Linux / macOS

echo "====================================================================="
echo "          Sentinel AI Enterprise Security & Fraud Suite"
echo "               Automatic Zero-Configuration Startup"
echo "====================================================================="
echo ""

# Check for Python
if ! command -v python3 &> /dev/null; then
    echo "[ERROR] python3 could not be found. Please install Python 3.10+."
    exit 1
fi

# Check for Node.js
if ! command -v npm &> /dev/null; then
    echo "[ERROR] npm could not be found. Please install Node.js 18+."
    exit 1
fi

echo "[1/3] Checking Backend Python Dependencies..."
pip3 install -r backend/requirements.txt --quiet

echo "[2/3] Checking Web Frontend Dependencies..."
cd web
if [ ! -d "node_modules" ]; then
    echo "Installing npm packages..."
    npm install --quiet
fi
cd ..

echo "[3/3] Launching Sentinel AI Platform..."
echo ""
echo "   - FastAPI Backend: http://127.0.0.1:8000"
echo "   - Next.js Web App: http://localhost:3000"
echo ""

(cd backend && python3 run.py) &
BACKEND_PID=$!

sleep 3

(cd web && npm run dev) &
WEB_PID=$!

sleep 3

# Open browser if available
if command -v xdg-open &> /dev/null; then
    xdg-open http://localhost:3000
elif command -v open &> /dev/null; then
    open http://localhost:3000
fi

echo "Sentinel AI is running. Press CTRL+C to terminate both servers."

trap "kill $BACKEND_PID $WEB_PID; exit" SIGINT SIGTERM
wait
