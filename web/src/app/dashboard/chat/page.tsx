"use client";

import React, { useState, useRef, useEffect } from 'react';
import { Bot, Send, User, Sparkles, Shield, AlertTriangle, RefreshCw, Terminal, ArrowRight } from 'lucide-react';
import { useAuthStore } from '@/lib/store';
import { apiUrl } from '@/lib/api';

interface ChatMessage {
  role: 'user' | 'assistant' | 'system';
  content: string;
}

const QUICK_PROMPTS = [
  "How does Sentinel AI detect phishing URLs?",
  "Why is Accessibility permission dangerous in unverified apps?",
  "How do I recognize a UPI PIN-to-receive scam?",
  "What are banking Trojans and overlay attacks?",
];

export default function ChatbotPage() {
  const { token, user } = useAuthStore();
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      role: 'assistant',
      content: "Hello Agent! I am **Sentinel AI Intel**, powered by high-speed Groq AI inference. I can assist you with real-time threat analysis, phishing verification, dangerous Android permissions, and financial fraud defense. How can I protect you today?"
    }
  ]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSend = async (queryText?: string) => {
    const textToSend = queryText || input;
    if (!textToSend.trim() || loading) return;

    const newMessages: ChatMessage[] = [
      ...messages,
      { role: 'user', content: textToSend }
    ];
    setMessages(newMessages);
    if (!queryText) setInput('');
    setLoading(true);

    try {
      const res = await fetch(apiUrl('/api/chat'), {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
          messages: newMessages.map(m => ({ role: m.role, content: m.content }))
        })
      });

      if (res.ok) {
        const data = await res.json();
        setMessages(prev => [...prev, { role: 'assistant', content: data.reply || data.response || "No response generated." }]);
      } else {
        throw new Error("API error");
      }
    } catch {
      // Fallback response with accurate security heuristics
      let fallback = "Sentinel Core Threat Advisory: Always verify URLs before opening. Never grant Accessibility or Overlay permissions to sideloaded APKs. In UPI payments, remember that entering your PIN always DEBITS money from your account.";
      const q = textToSend.toLowerCase();
      if (q.includes("phish") || q.includes("url")) {
        fallback = "🛡️ **Phishing Detection Advisory**:\nSentinel AI inspects 12 deterministic indicators: deceptive subdomains, raw IP hosts, suspicious TLDs (.xyz, .top, .ru), credential harvesting keywords, and TLS certificate anomalies.";
      } else if (q.includes("accessib") || q.includes("overlay") || q.includes("permission")) {
        fallback = "⚠️ **Dangerous Android Permissions**:\n`BIND_ACCESSIBILITY_SERVICE` and `SYSTEM_ALERT_WINDOW` allow rogue apps to read everything on your screen (including typed OTPs and UPI PINs) and draw fake overlay login prompts over official banking apps.";
      } else if (q.includes("upi") || q.includes("pin") || q.includes("scam") || q.includes("payment")) {
        fallback = "🚨 **Financial Scam Rule #1**:\nA UPI PIN is ONLY entered to deduct money from your bank account. You NEVER need to enter your UPI PIN to receive money, cashback, or refunds! Scammers exploit 'Collect Requests' to steal your funds.";
      }
      setMessages(prev => [...prev, { role: 'assistant', content: fallback }]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-4">
      {/* Top Banner */}
      <div className="glass-panel rounded-xl p-5 border-primary/20 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-full bg-primary/15 border border-primary/30 flex items-center justify-center">
            <Bot className="w-5 h-5 text-primary" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h2 className="text-base font-bold text-white">SENTINEL AI THREAT INTEL</h2>
              <span className="text-[10px] font-mono bg-success/15 text-success border border-success/30 px-2 py-0.5 rounded font-bold">
                GROQ LLAMA-3.3
              </span>
            </div>
            <p className="text-xs text-gray-400">Ask any question regarding phishing, malware APKs, or mobile fraud</p>
          </div>
        </div>
        <button
          onClick={() => setMessages([{ role: 'assistant', content: "Chat history cleared. How can I assist you with cybersecurity?" }])}
          className="text-gray-400 hover:text-white p-2 rounded-lg hover:bg-white/5 transition-colors"
          title="Clear Chat"
        >
          <RefreshCw className="w-4 h-4" />
        </button>
      </div>

      {/* Suggested Quick Prompts */}
      <div className="flex flex-wrap gap-2">
        {QUICK_PROMPTS.map((prompt, idx) => (
          <button
            key={idx}
            onClick={() => handleSend(prompt)}
            disabled={loading}
            className="text-xs bg-white/5 hover:bg-primary/10 border border-white/10 hover:border-primary/30 text-gray-300 hover:text-primary px-3 py-1.5 rounded-full transition-all duration-150 flex items-center gap-1.5 disabled:opacity-50"
          >
            <Sparkles className="w-3 h-3 text-primary" />
            <span>{prompt}</span>
          </button>
        ))}
      </div>

      {/* Chat Messages Window */}
      <div className="glass-panel rounded-xl border-white/5 p-4 min-h-[420px] max-h-[550px] overflow-y-auto space-y-4">
        {messages.map((msg, index) => (
          <div
            key={index}
            className={`flex gap-3 ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}
          >
            {msg.role !== 'user' && (
              <div className="w-8 h-8 rounded-full bg-primary/20 border border-primary/30 flex items-center justify-center shrink-0">
                <Bot className="w-4 h-4 text-primary" />
              </div>
            )}
            <div
              className={`max-w-[80%] rounded-xl px-4 py-3 text-sm leading-relaxed ${
                msg.role === 'user'
                  ? 'bg-primary text-background font-medium'
                  : 'bg-white/5 border border-white/10 text-gray-200'
              }`}
            >
              <div className="whitespace-pre-wrap font-sans">{msg.content}</div>
            </div>
            {msg.role === 'user' && (
              <div className="w-8 h-8 rounded-full bg-secondary/30 border border-secondary/40 flex items-center justify-center shrink-0">
                <User className="w-4 h-4 text-secondary" />
              </div>
            )}
          </div>
        ))}
        {loading && (
          <div className="flex gap-3 justify-start items-center">
            <div className="w-8 h-8 rounded-full bg-primary/20 border border-primary/30 flex items-center justify-center shrink-0">
              <Bot className="w-4 h-4 text-primary animate-pulse" />
            </div>
            <div className="bg-white/5 border border-white/10 rounded-xl px-4 py-2.5 text-xs text-primary font-mono animate-pulse flex items-center gap-2">
              <Terminal className="w-3.5 h-3.5" />
              <span>Analyzing neural security matrices...</span>
            </div>
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* Input Box */}
      <div className="glass-panel rounded-xl border-primary/20 p-2 flex items-center gap-2">
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleSend()}
          placeholder="Ask Sentinel AI about mobile security, phishing, or financial scams..."
          className="flex-1 bg-transparent border-0 text-sm text-white placeholder-gray-500 focus:outline-none px-3"
          disabled={loading}
        />
        <button
          onClick={() => handleSend()}
          disabled={!input.trim() || loading}
          className="bg-primary hover:bg-primary/90 text-background px-4 py-2 rounded-lg font-bold text-xs flex items-center gap-1.5 transition-all disabled:opacity-40 disabled:hover:bg-primary"
        >
          <span>Send</span>
          <Send className="w-3.5 h-3.5" />
        </button>
      </div>
    </div>
  );
}
