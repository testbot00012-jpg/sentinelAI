import os
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from app.routes import auth, scan, analytics, admin

app = FastAPI(
    title="Sentinel AI - Smart Mobile Security, Fraud Detection & Optimization Platform API",
    description="Backend ML Scoring, SMS Fraud heuristics, APK threat analyzer telemetry suite.",
    version="1.0.0"
)

# CORS Policy configuration
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"], # Allow development clients to connect directly
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Route mounting
app.include_router(auth.router)
app.include_router(scan.router)
app.include_router(analytics.router)
app.include_router(admin.router)

@app.get("/")
def read_root():
    return {
        "status": "online",
        "service": "Sentinel AI Cyber Intelligence API Engine",
        "timestamp": "2026-05-31T09:03:04"
    }

@app.get("/api/health")
def health_check():
    return {"status": "healthy", "database": "connected"}
