import datetime
import logging
from typing import List, Optional, Dict, Any
from fastapi import APIRouter, Depends, HTTPException, status, Header
from pydantic import BaseModel
from app.ml.sentinel_llm import get_sentinel_llm

logger = logging.getLogger("sentinel.chat")

router = APIRouter(prefix="/api/chat", tags=["Sentinel Neural AI Cybersecurity Assistant"])

class ChatMessage(BaseModel):
    role: str
    content: str

class ChatRequest(BaseModel):
    message: str
    history: Optional[List[ChatMessage]] = []
    scan_context: Optional[Dict[str, Any]] = None

class ChatResponse(BaseModel):
    reply: str
    model: str
    confidence: float
    evidence: List[str]
    timestamp: str

@router.post("", response_model=ChatResponse)
async def send_chat_message(
    req: ChatRequest,
    authorization: Optional[str] = Header(None)
):
    """
    Processes user security questions through SentinelAI's internally developed
    local neural cybersecurity intelligence engine.
    Completely self-hosted and offline-capable—no third-party generative cloud APIs required.
    """
    user_msg = req.message.strip()
    if not user_msg:
        raise HTTPException(status_code=400, detail="Message cannot be empty")

    llm = get_sentinel_llm()
    history_dicts = [{"role": m.role, "content": m.content} for m in req.history] if req.history else []
    
    result = llm.generate_response(
        prompt=user_msg,
        history=history_dicts,
        scan_context=req.scan_context
    )

    return {
        "reply": result["reply"],
        "model": result["model"],
        "confidence": result["confidence"],
        "evidence": result["evidence"],
        "timestamp": result["timestamp"]
    }
