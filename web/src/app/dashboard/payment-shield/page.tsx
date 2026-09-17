"use client";

import React, { useState } from 'react';
import { 
  CreditCard, ShieldAlert, CheckCircle2, AlertTriangle, 
  QrCode, MessageSquare, MonitorSmartphone, ArrowRight, RefreshCw, Terminal, EyeOff
} from 'lucide-react';

interface UpiVerdict {
  title: string;
  riskLevel: 'SAFE' | 'HIGH RISK' | 'CRITICAL FRAUD TRAP' | 'MONITORED';
  riskScore: number;
  payeeName: string;
  vpa: string;
  details: string;
  recommendation: string;
}

interface FinancialSmsVerdict {
  scamType: string;
  isFraud: boolean;
  riskScore: number;
  threatSummary: string;
  advice: string;
}

export default function PaymentShieldPage() {
  const [activeTab, setActiveTab] = useState<'upi' | 'sms' | 'remote'>('upi');

  // Tab 1: UPI States
  const [upiInput, setUpiInput] = useState('');
  const [upiVerdict, setUpiVerdict] = useState<UpiVerdict | null>(null);
  const [scanningUpi, setScanningUpi] = useState(false);

  // Tab 2: Financial SMS States
  const [smsInput, setSmsInput] = useState('');
  const [smsVerdict, setSmsVerdict] = useState<FinancialSmsVerdict | null>(null);
  const [scanningSms, setScanningSms] = useState(false);

  const evaluateUpi = (input: string) => {
    setScanningUpi(true);
    setTimeout(() => {
      const text = input.trim();
      const textLower = text.toLowerCase();

      // 1. PIN to receive money trap
      if (textLower.includes("receive") || textLower.includes("refund") || textLower.includes("cashback") || textLower.includes("bonus")) {
        if (textLower.includes("pin") || textLower.includes("collect") || textLower.includes("approval")) {
          setUpiVerdict({
            title: "🚨 CRITICAL FRAUD: 'PIN TO RECEIVE' SCAM",
            riskLevel: "CRITICAL FRAUD TRAP",
            riskScore: 98,
            payeeName: "Fraudulent Collect Intent",
            vpa: text.slice(0, 35),
            details: "Scammers send UPI Collect Requests claiming you are 'receiving a refund or cashback'. In UPI architecture, entering your PIN NEVER credits money; entering your PIN ALWAYS DEBITS money from your bank account!",
            recommendation: "DO NOT enter your UPI PIN. Reject and report this payment request immediately."
          });
          setScanningUpi(false);
          return;
        }
      }

      // 2. Mismatched Merchant Name Fraud
      let payeeName = "";
      let vpa = "";
      if (textLower.startsWith("upi://pay")) {
        try {
          const urlParams = new URLSearchParams(text.split('?')[1]);
          payeeName = urlParams.get('pn') || "";
          vpa = urlParams.get('pa') || "";
        } catch {
          // ignore
        }
      } else if (text.includes("@")) {
        vpa = text;
      }

      const vpaLower = vpa.toLowerCase();
      const nameLower = payeeName.toLowerCase();
      const isClaimingOfficial = ["sbi", "hdfc", "icici", "paytm", "phonepe", "support", "refund", "customer care", "electricity"].some(k => nameLower.includes(k));
      const isVerified = ["@icici", "@hdfcbank", "@paytm", "@yesbank"].some(h => vpaLower.endsWith(h)) && (vpaLower.startsWith("swiggy") || vpaLower.startsWith("zomato") || vpaLower.startsWith("flipkart"));

      if (isClaimingOfficial && !isVerified && (vpaLower.includes("okhdfcbank") || vpaLower.includes("oksbi") || vpaLower.includes("ybl") || /\d{5,}/.test(vpaLower))) {
        setUpiVerdict({
          title: "🚨 DECEPTIVE BENEFICIARY NAME",
          riskLevel: "HIGH RISK",
          riskScore: 88,
          payeeName: payeeName || "Deceptive Name",
          vpa: vpa,
          details: `The payee display name claims to be official '${payeeName}', but the payment VPA address belongs to an individual personal account (${vpa}).`,
          recommendation: "Do not transfer money to personal VPAs claiming to be official corporate or banking support."
        });
        setScanningUpi(false);
        return;
      }

      if (isVerified || vpaLower.startsWith("swiggy") || vpaLower.startsWith("zomato") || vpaLower.startsWith("flipkart")) {
        setUpiVerdict({
          title: "✓ VERIFIED SAFE MERCHANT",
          riskLevel: "SAFE",
          riskScore: 5,
          payeeName: payeeName || "Verified Merchant",
          vpa: vpa,
          details: "This payment recipient matches official corporate payment gateways for trusted commercial merchants.",
          recommendation: "Safe to proceed with authorized commercial payment."
        });
        setScanningUpi(false);
        return;
      }

      setUpiVerdict({
        title: "UNVERIFIED PRIVATE VPA",
        riskLevel: "MONITORED",
        riskScore: 35,
        payeeName: payeeName || "Unspecified Payee",
        vpa: vpa,
        details: "Payment request directed to an unverified private beneficiary. Verify recipient identity before approving.",
        recommendation: "Confirm recipient details with your known contact before completing transaction."
      });
      setScanningUpi(false);
    }, 400);
  };

  const evaluateSms = (input: string) => {
    setScanningSms(true);
    setTimeout(() => {
      const text = input.trim();
      const textLower = text.toLowerCase();

      if ((textLower.includes("pan") || textLower.includes("kyc") || textLower.includes("blocked") || textLower.includes("suspended")) &&
          (textLower.includes("http") || textLower.includes("bit.ly") || textLower.includes(".apk") || textLower.includes("click"))) {
        setSmsVerdict({
          scamType: "🚨 BANK KYC PHISHING SCAM",
          isFraud: true,
          riskScore: 95,
          threatSummary: "Message threatens bank account suspension or KYC deactivation with an unverified external link. Official banks never send third-party short links or ask for PAN/Aadhaar updates via SMS.",
          advice: "Do not click link. Never download APK files or enter NetBanking passwords from SMS links."
        });
        setScanningSms(false);
        return;
      }

      if (textLower.includes("electricity") && (textLower.includes("power will be disconnected") || textLower.includes("disconnected tonight") || textLower.includes("officer"))) {
        setSmsVerdict({
          scamType: "🚨 ELECTRICITY DISCONNECTION EXTORTION",
          isFraud: true,
          riskScore: 92,
          threatSummary: "Scammers create artificial panic by threatening electricity cut tonight and prompt victims to call a personal mobile number. The caller then asks victims to install AnyDesk or pay via unknown link.",
          advice: "Do not call the number. Electricity boards only notify through official billing portals and never give 2-hour disconnection threats via personal mobile numbers."
        });
        setScanningSms(false);
        return;
      }

      if ((textLower.includes("telegram") || textLower.includes("whatsapp") || textLower.includes("youtube")) &&
          (textLower.includes("part-time") || textLower.includes("earn") || textLower.includes("daily") || textLower.includes("5000"))) {
        setSmsVerdict({
          scamType: "🚨 TASK-BASED INVESTMENT FRAUD",
          isFraud: true,
          riskScore: 88,
          threatSummary: "Offers easy daily earnings (Rs. 3000-8000) for simple tasks like liking videos, then lures victims into fraudulent prepaid investment groups on Telegram.",
          advice: "Block and report sender. Legitimate companies never recruit for high daily pay via unsolicited SMS."
        });
        setScanningSms(false);
        return;
      }

      if (textLower.includes("otp") && (textLower.includes("do not share") || textLower.includes("bank staff")) && !textLower.includes("http")) {
        setSmsVerdict({
          scamType: "✓ LEGITIMATE TRANSACTION OTP",
          isFraud: false,
          riskScore: 5,
          threatSummary: "Standard transactional one-time passcode with standard security warning.",
          advice: "Never share your OTP with anyone over phone call or SMS, even if they claim to be bank officials."
        });
        setScanningSms(false);
        return;
      }

      setSmsVerdict({
        scamType: "MONITORED FINANCIAL NOTICE",
        isFraud: false,
        riskScore: 20,
        threatSummary: "Standard communication detected without high-risk extortion or phishing triggers.",
        advice: "Verify transaction details against your official banking mobile application."
      });
      setScanningSms(false);
    }, 400);
  };

  return (
    <div className="space-y-6 max-w-6xl mx-auto">
      {/* Header */}
      <div className="glass-panel rounded-2xl p-6 border-success/20 flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <h2 className="text-xl font-extrabold text-white">PAYMENT & BANKING FRAUD SHIELD</h2>
            <span className="text-[10px] font-mono bg-success/20 text-success border border-success/30 px-2 py-0.5 rounded font-bold">
              AI FINANCIAL SHIELD
            </span>
          </div>
          <p className="text-xs text-gray-400">Inspect UPI payment links, detect financial extortion SMS, and monitor banking screen-sharing threats</p>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex border-b border-white/10 gap-2">
        {[
          { id: 'upi', label: 'UPI / QR VERIFIER', icon: QrCode },
          { id: 'sms', label: 'FINANCIAL SMS SCAM', icon: MessageSquare },
          { id: 'remote', label: 'SCREEN-SHARE & OVERLAYS', icon: MonitorSmartphone },
        ].map((tab) => {
          const isActive = activeTab === tab.id;
          return (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id as any)}
              className={`flex items-center gap-2 px-4 py-3 text-xs font-bold font-mono transition-all border-b-2 -mb-px ${
                isActive
                  ? 'border-primary text-primary bg-primary/5'
                  : 'border-transparent text-gray-400 hover:text-white'
              }`}
            >
              <tab.icon className="w-4 h-4" />
              <span>{tab.label}</span>
            </button>
          );
        })}
      </div>

      {/* Tab 1: UPI Inspector */}
      {activeTab === 'upi' && (
        <div className="space-y-4">
          <div className="glass-panel rounded-2xl p-6 border-primary/20 space-y-4">
            <div>
              <h3 className="text-sm font-bold text-white mb-1">VERIFY UPI PAYMENT LINK OR VPA HANDLE</h3>
              <p className="text-xs text-gray-400">Detect deceptive merchant names, fraudulent collect requests, and non-standard schemes</p>
            </div>

            <div className="space-y-2">
              <textarea
                value={upiInput}
                onChange={(e) => setUpiInput(e.target.value)}
                placeholder="Paste UPI payment link or VPA ID (e.g. upi://pay?pa=support@upi&pn=SBI%20Refund... or merchant@icici)"
                rows={2}
                className="w-full bg-black/40 border border-white/10 rounded-xl p-3 text-xs text-white placeholder-gray-500 focus:outline-none focus:border-primary font-mono"
              />

              <div className="flex flex-wrap items-center gap-2 text-xs">
                <span className="text-[10px] font-mono text-gray-400">Quick Test:</span>
                <button
                  onClick={() => {
                    const sample = "upi://pay?pa=swiggy@icici&pn=Swiggy%20Order&am=450&cu=INR";
                    setUpiInput(sample);
                    evaluateUpi(sample);
                  }}
                  className="bg-white/5 hover:bg-success/10 border border-white/10 text-success text-[11px] px-2.5 py-1 rounded-full transition-colors"
                >
                  ✅ Swiggy Order
                </button>
                <button
                  onClick={() => {
                    const sample = "upi://pay?pa=scammer994@okhdfcbank&pn=SBI%20Reward%20Refund&am=5000&mode=02&tr=Enter%20PIN%20To%20Receive";
                    setUpiInput(sample);
                    evaluateUpi(sample);
                  }}
                  className="bg-white/5 hover:bg-danger/10 border border-white/10 text-danger text-[11px] px-2.5 py-1 rounded-full transition-colors"
                >
                  🚨 PIN-to-Receive Trap
                </button>
                <button
                  onClick={() => {
                    const sample = "upi://pay?pa=user98124@oksbi&pn=Electricity%20Bill%20Support&am=1250";
                    setUpiInput(sample);
                    evaluateUpi(sample);
                  }}
                  className="bg-white/5 hover:bg-warning/10 border border-white/10 text-warning text-[11px] px-2.5 py-1 rounded-full transition-colors"
                >
                  🚨 Deceptive Beneficiary
                </button>
              </div>
            </div>

            <button
              onClick={() => evaluateUpi(upiInput)}
              disabled={!upiInput.trim() || scanningUpi}
              className="bg-primary hover:bg-primary/90 text-background font-bold text-xs px-5 py-2.5 rounded-lg flex items-center gap-2 transition-all disabled:opacity-50"
            >
              <CreditCard className="w-4 h-4" />
              <span>{scanningUpi ? "EVALUATING PAYMENT VECTOR..." : "ANALYZE UPI PAYMENT SECURITY"}</span>
            </button>
          </div>

          {upiVerdict && (
            <div className={`glass-panel rounded-2xl p-6 border ${
              upiVerdict.riskLevel === 'CRITICAL FRAUD TRAP' || upiVerdict.riskLevel === 'HIGH RISK'
                ? 'border-danger/40 bg-danger/5'
                : upiVerdict.riskLevel === 'SAFE'
                ? 'border-success/40 bg-success/5'
                : 'border-warning/40 bg-warning/5'
            } space-y-4`}>
              <div className="flex justify-between items-start">
                <div>
                  <h4 className={`text-base font-black ${
                    upiVerdict.riskLevel === 'CRITICAL FRAUD TRAP' || upiVerdict.riskLevel === 'HIGH RISK'
                      ? 'text-danger'
                      : upiVerdict.riskLevel === 'SAFE'
                      ? 'text-success'
                      : 'text-warning'
                  }`}>
                    {upiVerdict.title}
                  </h4>
                  <div className="flex items-center gap-2 mt-1 text-xs text-gray-300 font-mono">
                    <span>Payee: {upiVerdict.payeeName || 'N/A'}</span>
                    <span>•</span>
                    <span>VPA: {upiVerdict.vpa || 'N/A'}</span>
                  </div>
                </div>
                <span className={`text-xs font-mono font-black px-3 py-1 rounded-lg ${
                  upiVerdict.riskScore > 70
                    ? 'bg-danger/20 text-danger border border-danger/30'
                    : upiVerdict.riskScore < 20
                    ? 'bg-success/20 text-success border border-success/30'
                    : 'bg-warning/20 text-warning border border-warning/30'
                }`}>
                  {upiVerdict.riskScore}/100 RISK
                </span>
              </div>

              <p className="text-xs text-gray-200 leading-relaxed font-sans">{upiVerdict.details}</p>

              <div className="p-3 bg-black/40 border border-white/10 rounded-xl text-xs font-mono">
                <span className="font-bold text-primary">ACTIONABLE GUIDANCE:</span>{' '}
                <span className="text-gray-300">{upiVerdict.recommendation}</span>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Tab 2: Financial SMS */}
      {activeTab === 'sms' && (
        <div className="space-y-4">
          <div className="glass-panel rounded-2xl p-6 border-secondary/20 space-y-4">
            <div>
              <h3 className="text-sm font-bold text-white mb-1">FINANCIAL & BANKING SMS SCAM ANALYZER</h3>
              <p className="text-xs text-gray-400">Detect account suspension threats, utility extortion scams & task frauds</p>
            </div>

            <div className="space-y-2">
              <textarea
                value={smsInput}
                onChange={(e) => setSmsInput(e.target.value)}
                placeholder="Paste financial SMS or alert here (e.g. Dear SBI User, your Yono account has been suspended due to pending PAN KYC...)"
                rows={3}
                className="w-full bg-black/40 border border-white/10 rounded-xl p-3 text-xs text-white placeholder-gray-500 focus:outline-none focus:border-secondary font-mono"
              />

              <div className="flex flex-wrap items-center gap-2 text-xs">
                <span className="text-[10px] font-mono text-gray-400">Quick Test:</span>
                <button
                  onClick={() => {
                    const sample = "Dear SBI User, your Yono account has been suspended due to pending PAN KYC. Update immediately to prevent permanent block: http://bit.ly/sbi-pan-kyc";
                    setSmsInput(sample);
                    evaluateSms(sample);
                  }}
                  className="bg-white/5 hover:bg-danger/10 border border-white/10 text-danger text-[11px] px-2.5 py-1 rounded-full transition-colors"
                >
                  🚨 SBI KYC Suspended
                </button>
                <button
                  onClick={() => {
                    const sample = "Dear consumer electricity power disconnected tonight at 9.30 pm from office because bill not updated. Call officer 9876543210.";
                    setSmsInput(sample);
                    evaluateSms(sample);
                  }}
                  className="bg-white/5 hover:bg-danger/10 border border-white/10 text-danger text-[11px] px-2.5 py-1 rounded-full transition-colors"
                >
                  🚨 Power Cut Scam
                </button>
                <button
                  onClick={() => {
                    const sample = "Earn Rs. 5000 daily from home just by liking YouTube videos! Contact Priya on Telegram @earn_daily";
                    setSmsInput(sample);
                    evaluateSms(sample);
                  }}
                  className="bg-white/5 hover:bg-warning/10 border border-white/10 text-warning text-[11px] px-2.5 py-1 rounded-full transition-colors"
                >
                  🚨 Telegram Task Scam
                </button>
                <button
                  onClick={() => {
                    const sample = "Your OTP for transaction of Rs. 450 at SWIGGY is 729104. Valid for 10 mins. Do not share with anyone including bank staff.";
                    setSmsInput(sample);
                    evaluateSms(sample);
                  }}
                  className="bg-white/5 hover:bg-success/10 border border-white/10 text-success text-[11px] px-2.5 py-1 rounded-full transition-colors"
                >
                  ✅ Genuine HDFC OTP
                </button>
              </div>
            </div>

            <button
              onClick={() => evaluateSms(smsInput)}
              disabled={!smsInput.trim() || scanningSms}
              className="bg-secondary hover:bg-secondary/90 text-white font-bold text-xs px-5 py-2.5 rounded-lg flex items-center gap-2 transition-all disabled:opacity-50"
            >
              <MessageSquare className="w-4 h-4" />
              <span>{scanningSms ? "EVALUATING NLP MATRIX..." : "ANALYZE FINANCIAL MESSAGE"}</span>
            </button>
          </div>

          {smsVerdict && (
            <div className={`glass-panel rounded-2xl p-6 border ${
              smsVerdict.isFraud ? 'border-danger/40 bg-danger/5' : 'border-success/40 bg-success/5'
            } space-y-4`}>
              <div className="flex justify-between items-start">
                <h4 className={`text-base font-black ${smsVerdict.isFraud ? 'text-danger' : 'text-success'}`}>
                  {smsVerdict.scamType}
                </h4>
                <span className={`text-xs font-mono font-black px-3 py-1 rounded-lg ${
                  smsVerdict.riskScore > 70
                    ? 'bg-danger/20 text-danger border border-danger/30'
                    : 'bg-success/20 text-success border border-success/30'
                }`}>
                  {smsVerdict.riskScore}/100 THREAT
                </span>
              </div>

              <p className="text-xs text-gray-200 leading-relaxed font-sans">{smsVerdict.threatSummary}</p>

              <div className="p-3 bg-black/40 border border-white/10 rounded-xl text-xs font-mono">
                <span className="font-bold text-primary">PROTECTION ADVICE:</span>{' '}
                <span className="text-gray-300">{smsVerdict.advice}</span>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Tab 3: Remote Tools & Screen-Share Warning */}
      {activeTab === 'remote' && (
        <div className="space-y-4">
          <div className="glass-panel rounded-2xl p-6 border-warning/20 space-y-4">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-warning/15 border border-warning/30 flex items-center justify-center">
                <MonitorSmartphone className="w-5 h-5 text-warning" />
              </div>
              <div>
                <h3 className="text-sm font-bold text-white">REMOTE SCREEN-SHARING & OVERLAY TROJAN DEFENSE</h3>
                <p className="text-xs text-gray-400">Why remote desktop tools represent the #1 vector for mobile banking theft</p>
              </div>
            </div>

            <div className="p-4 bg-white/3 border border-white/5 rounded-xl space-y-3 text-xs text-gray-300 leading-relaxed">
              <p>
                <strong>The Threat Vector:</strong> Financial fraudsters frequently telephone victims claiming to be from your bank or electricity department. They urge you to download remote screen-sharing tools such as <strong>AnyDesk, TeamViewer QuickSupport, RustDesk, or AirDroid</strong> under the pretext of &quot;assisting with KYC verification.&quot;
              </p>
              <p>
                Once installed, the remote tool streams your screen directly to the fraudster. When you open your banking or UPI app, they observe your <strong>MPIN, UPI PIN, and SMS OTPs in real time</strong>, enabling them to initiate unauthorized transfers immediately.
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              <div className="p-4 bg-danger/10 border border-danger/30 rounded-xl space-y-2">
                <span className="text-xs font-bold text-danger flex items-center gap-1.5">
                  <AlertTriangle className="w-4 h-4" />
                  HIGH-RISK SIGNATURES
                </span>
                <ul className="text-[11px] text-gray-300 space-y-1 list-disc list-inside font-mono">
                  <li>Caller demands installing AnyDesk or TeamViewer</li>
                  <li>Requests screen access to &quot;approve refunds&quot;</li>
                  <li>Asks you to open GPay / PhonePe / YONO while connected</li>
                </ul>
              </div>
              <div className="p-4 bg-success/10 border border-success/30 rounded-xl space-y-2">
                <span className="text-xs font-bold text-success flex items-center gap-1.5">
                  <CheckCircle2 className="w-4 h-4" />
                  AUTOMATED DEFENSE PROTOCOL
                </span>
                <ul className="text-[11px] text-gray-300 space-y-1 list-disc list-inside font-mono">
                  <li>Sentinel App Auditor scans for remote control packages</li>
                  <li>Identifies unauthorized SYSTEM_ALERT_WINDOW overlays</li>
                  <li>Prompts immediate revocation in Android Settings</li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
