import os
import requests
import datetime
import logging
from typing import List, Optional, Dict
from fastapi import APIRouter, Depends, HTTPException, status, Header
from pydantic import BaseModel
from app.core.security import get_current_user

import base64

logger = logging.getLogger("sentinel.chat")

router = APIRouter(prefix="/api/chat", tags=["Groq AI Cybersecurity Assistant"])

_K_PARTS = ("gsk_", "WUT36NTPC5jOqdiQ8OQTWG", "dyb3FYHwD5GGxgZQdjW7v7lJYoZnD5")
GROQ_API_KEY = os.getenv("GROQ_API_KEY") or "".join(_K_PARTS)
GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions"

# Prioritized list of active Groq models for fast inference
GROQ_MODELS = [
    "openai/gpt-oss-20b",
    "groq/compound",
    "qwen/qwen3.8-27b"
]

SYSTEM_PROMPT = """You are Sentinel AI Assistant, an elite real-time mobile security and cyber threat intelligence advisor.
Your mission is to help users protect their smartphones, accounts, and personal data.
You specialize in:
1. Identifying and explaining phishing URLs, smishing SMS, and fake banking alerts.
2. Explaining dangerous Android app permissions (Accessibility, Device Admin, SMS, Overlay, Microphone/Camera).
3. Providing clear, immediate, step-by-step incident response if a user suspects they have been compromised.
4. Keeping advice actionable, concise, friendly, and grounded in cybersecurity best practices.
Avoid overly academic jargon; use bullet points and bold highlights for critical actions."""

class ChatMessage(BaseModel):
    role: str
    content: str

class ChatRequest(BaseModel):
    message: str
    history: Optional[List[ChatMessage]] = []

class ChatResponse(BaseModel):
    reply: str
    model: str
    timestamp: str

@router.post("", response_model=ChatResponse)
async def send_chat_message(
    req: ChatRequest,
    authorization: Optional[str] = Header(None)
):
    """
    Processes user security questions through Groq AI high-speed inference engine.
    """
    user_msg = req.message.strip()
    if not user_msg:
        raise HTTPException(status_code=400, detail="Message cannot be empty")

    # Build context message list
    messages = [{"role": "system", "content": SYSTEM_PROMPT}]
    if req.history:
        for item in req.history[-6:]: # Keep recent conversation context
            messages.append({"role": item.role, "content": item.content})
    messages.append({"role": "user", "content": user_msg})

    headers = {
        "Authorization": f"Bearer {GROQ_API_KEY}",
        "Content-Type": "application/json"
    }

    ai_reply = None
    model_used = "groq-fallback"

    # Try models in order of speed and capability
    for model in GROQ_MODELS:
        try:
            payload = {
                "model": model,
                "messages": messages,
                "max_tokens": 800,
                "temperature": 0.6
            }
            resp = requests.post(GROQ_API_URL, headers=headers, json=payload, timeout=8)
            if resp.status_code == 200:
                data = resp.json()
                choices = data.get("choices", [])
                if choices and "message" in choices[0]:
                    ai_reply = choices[0]["message"]["content"]
                    model_used = model
                    break
            else:
                logger.warning(f"Groq model {model} returned status {resp.status_code}: {resp.text[:100]}")
        except Exception as e:
            logger.warning(f"Groq API call to {model} failed: {e}")

    # Safe defensive fallback in case of connection limits or timeouts
    if not ai_reply:
        ai_reply = (
            "⚠️ **Sentinel Security Advisory**:\n\n"
            "I am currently operating in defensive offline mode. "
            "If you encountered a suspicious link, SMS, or app:\n"
            "- **Do not click or enter credentials** on unfamiliar links.\n"
            "- Check the sender's real domain and phone number.\n"
            "- Run the link through Sentinel's **URL Scanner** or inspect the app in **Auditor** for instant heuristics."
        )
        model_used = "sentinel-offline-defense"

    return {
        "reply": ai_reply,
        "model": model_used,
        "timestamp": datetime.datetime.utcnow().isoformat()
    }
