from sqlalchemy import Column, Integer, String, Float, Boolean, DateTime, ForeignKey, Text, JSON
from sqlalchemy.orm import relationship
import datetime
from app.db.database import Base

class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    email = Column(String, unique=True, index=True, nullable=False)
    hashed_password = Column(String, nullable=False)
    full_name = Column(String, nullable=True)
    is_active = Column(Boolean, default=True)
    role = Column(String, default="user") # user, admin
    created_at = Column(DateTime, default=datetime.datetime.utcnow)

    # Relationships
    url_scans = relationship("URLScan", back_populates="user", cascade="all, delete-orphan")
    fraud_scans = relationship("FraudScan", back_populates="user", cascade="all, delete-orphan")
    security_reports = relationship("SecurityReport", back_populates="user", cascade="all, delete-orphan")
    threat_logs = relationship("ThreatLog", back_populates="user", cascade="all, delete-orphan")
    device_stats = relationship("DeviceStat", back_populates="user", cascade="all, delete-orphan")
    activity_logs = relationship("ActivityLog", back_populates="user", cascade="all, delete-orphan")


class URLScan(Base):
    __tablename__ = "url_scans"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=True)
    url = Column(String, nullable=False)
    status = Column(String, nullable=False) # Safe, Suspicious, Phishing
    score = Column(Float, nullable=False)
    details = Column(JSON, nullable=True)
    scanned_at = Column(DateTime, default=datetime.datetime.utcnow)

    user = relationship("User", back_populates="url_scans")


class FraudScan(Base):
    __tablename__ = "fraud_scans"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=True)
    scan_type = Column(String, nullable=False) # SMS, Email
    content = Column(Text, nullable=False)
    scam_probability = Column(Float, nullable=False)
    classification = Column(String, nullable=False)
    explanation = Column(Text, nullable=True)
    scanned_at = Column(DateTime, default=datetime.datetime.utcnow)

    user = relationship("User", back_populates="fraud_scans")


class ThreatLog(Base):
    __tablename__ = "threat_logs"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=True)
    threat_type = Column(String, nullable=False) # Phishing, Fraud SMS, Malware APK, Intrusion
    severity = Column(String, nullable=False) # Low, Medium, High, Critical
    source = Column(String, nullable=False) # Web Scanner, Mobile Agent, System Daemon
    description = Column(Text, nullable=False)
    resolved = Column(Boolean, default=False)
    detected_at = Column(DateTime, default=datetime.datetime.utcnow)

    user = relationship("User", back_populates="threat_logs")


class DeviceStat(Base):
    __tablename__ = "device_stats"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    device_model = Column(String, nullable=True)
    os_version = Column(String, nullable=True)
    security_score = Column(Integer, default=100)
    battery_health = Column(Integer, nullable=True)
    ram_usage_percent = Column(Float, nullable=True)
    storage_usage_percent = Column(Float, nullable=True)
    updated_at = Column(DateTime, default=datetime.datetime.utcnow, onupdate=datetime.datetime.utcnow)

    user = relationship("User", back_populates="device_stats")


class SecurityReport(Base):
    __tablename__ = "security_reports"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    title = Column(String, nullable=False)
    total_scans = Column(Integer, default=0)
    threats_detected = Column(Integer, default=0)
    avg_security_score = Column(Float, default=100.0)
    report_data = Column(JSON, nullable=True)
    created_at = Column(DateTime, default=datetime.datetime.utcnow)

    user = relationship("User", back_populates="security_reports")


class ActivityLog(Base):
    __tablename__ = "activity_logs"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=True)
    action = Column(String, nullable=False) # Login, Scan URL, Change Settings, Export Report
    ip_address = Column(String, nullable=True)
    user_agent = Column(String, nullable=True)
    timestamp = Column(DateTime, default=datetime.datetime.utcnow)

    user = relationship("User", back_populates="activity_logs")
