"use client";

import React, { useState } from 'react';
import Link from 'next/link';
import { 
  Shield, Terminal, Zap, Globe, Cpu, AlertTriangle, 
  ChevronRight, Lock, Eye, CheckCircle2, HelpCircle, Mail 
} from 'lucide-react';

export default function LandingPage() {
  const [activeTab, setActiveTab] = useState<'phish' | 'sms' | 'apk'>('phish');

  return (
    <div className="relative min-h-screen bg-background text-white cyber-grid selection:bg-primary selection:text-background">
      {/* Glow Effects */}
      <div className="absolute top-[-10%] left-[5%] w-[450px] h-[450px] bg-secondary opacity-15 rounded-full blur-[120px] pointer-events-none" />
      <div className="absolute top-[30%] right-[5%] w-[500px] h-[500px] bg-primary opacity-10 rounded-full blur-[150px] pointer-events-none" />

      {/* Navigation Header */}
      <nav className="sticky top-0 z-50 glass-panel border-b border-white/5 px-6 py-4 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Shield className="w-8 h-8 text-primary drop-shadow-[0_0_8px_#00e5ff]" />
          <span className="font-extrabold text-xl tracking-wider bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
            SENTINEL AI
          </span>
        </div>
        <div className="hidden md:flex items-center gap-8 text-sm font-medium text-gray-400">
          <a href="#features" className="hover:text-primary transition-colors">Features</a>
          <a href="#demo" className="hover:text-primary transition-colors">AI Diagnostics</a>
          <a href="#pricing" className="hover:text-primary transition-colors">Pricing</a>
          <a href="#faq" className="hover:text-primary transition-colors">Security FAQ</a>
        </div>
        <div className="flex items-center gap-4">
          <Link href="/login" className="text-sm font-semibold hover:text-primary transition-colors">
            Login
          </Link>
          <Link href="/register" className="relative group overflow-hidden rounded-lg p-[1px] focus:outline-none">
            <span className="absolute inset-0 bg-gradient-to-r from-primary to-secondary rounded-lg" />
            <div className="px-5 py-2 rounded-lg bg-background text-sm font-semibold relative transition-colors duration-300 group-hover:bg-transparent">
              Access Console
            </div>
          </Link>
        </div>
      </nav>

      {/* Hero Section */}
      <header className="max-w-7xl mx-auto px-6 pt-20 pb-16 text-center relative">
        <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full glass-panel border-primary/20 text-xs text-primary mb-6 animate-pulse">
          <Terminal className="w-4 h-4" /> Next-Generation Threat Defense Suite
        </div>
        <h1 className="text-5xl md:text-7xl font-extrabold tracking-tight mb-8">
          Defend Your Digital Frontier <br />
          <span className="bg-gradient-to-r from-primary via-secondary to-success bg-clip-text text-transparent drop-shadow-neon">
            Powered by Autonomous AI
          </span>
        </h1>
        <p className="max-w-2xl mx-auto text-gray-400 text-lg md:text-xl mb-10 leading-relaxed">
          Sentinel AI monitors permissions, detects phishing URLs instantly, and neutralizes mobile scams before they strike. Unified enterprise Web UI & high-performance Android SDK.
        </p>
        <div className="flex flex-col sm:flex-row items-center justify-center gap-4 mb-20">
          <Link href="/register" className="w-full sm:w-auto px-8 py-4 bg-primary text-background font-bold rounded-lg shadow-neon hover:opacity-90 transition-all flex items-center justify-center gap-2">
            Secure Your Devices <ChevronRight className="w-5 h-5" />
          </Link>
          <a href="#demo" className="w-full sm:w-auto px-8 py-4 glass-panel hover:bg-white/5 font-semibold rounded-lg border-white/10 transition-colors flex items-center justify-center gap-2">
            Try AI Sandbox
          </a>
        </div>

        {/* Floating Stat Cards */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 max-w-5xl mx-auto">
          {[
            { metric: "99.98%", label: "Phishing Detection Accuracy", color: "text-primary" },
            { metric: "< 50ms", label: "Real-Time Scan Latency", color: "text-secondary" },
            { metric: "1.2M+", label: "Identified Malware Signatures", color: "text-success" },
            { metric: "24/7/365", label: "Autonomous Cyber-Scans", color: "text-warning" }
          ].map((stat, i) => (
            <div key={i} className="glass-panel p-6 rounded-xl border-white/5 text-center">
              <div className={`text-3xl font-extrabold mb-2 ${stat.color}`}>{stat.metric}</div>
              <div className="text-xs text-gray-400 font-medium uppercase tracking-wider">{stat.label}</div>
            </div>
          ))}
        </div>
      </header>

      {/* Interactive AI Demo Section */}
      <section id="demo" className="max-w-5xl mx-auto px-6 py-20 relative">
        <div className="text-center mb-12">
          <h2 className="text-3xl md:text-4xl font-extrabold mb-4">
            Test Sentinel's Cyber Intelligence
          </h2>
          <p className="text-gray-400 text-sm md:text-base max-w-lg mx-auto">
            Switch between core defensive vectors below and see how our detection algorithms classify anomalous behaviors.
          </p>
        </div>

        <div className="glass-panel rounded-2xl border-white/10 overflow-hidden shadow-glow">
          {/* Navigation inside Demo */}
          <div className="flex border-b border-white/10">
            {[
              { id: 'phish', label: 'URL Phishing Scan' },
              { id: 'sms', label: 'SMS Scam NLP' },
              { id: 'apk', label: 'APK Risk Profile' }
            ].map(tab => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id as any)}
                className={`flex-1 py-4 text-sm font-semibold transition-all ${
                  activeTab === tab.id 
                    ? 'bg-white/5 text-primary border-b-2 border-primary' 
                    : 'text-gray-400 hover:text-white'
                }`}
              >
                {tab.label}
              </button>
            ))}
          </div>

          {/* Tab Contents */}
          <div className="p-8">
            {activeTab === 'phish' && (
              <div className="space-y-6">
                <div className="space-y-2">
                  <label className="text-xs font-bold text-gray-400 uppercase tracking-widest">Target Scan Endpoint URL</label>
                  <div className="flex gap-2">
                    <input 
                      type="text" 
                      defaultValue="https://secure-login-update-paypal.verify-accounts.net/signin"
                      className="flex-1 bg-background/80 border border-white/10 rounded-lg px-4 py-3 text-sm text-gray-200 focus:outline-none focus:border-primary font-mono"
                    />
                    <button className="px-6 py-3 bg-primary text-background font-bold text-sm rounded-lg hover:opacity-90 transition-opacity">
                      Execute AI Scan
                    </button>
                  </div>
                </div>
                <div className="glass-panel p-4 rounded-lg bg-black/40 space-y-3">
                  <div className="flex justify-between items-center">
                    <span className="text-sm font-semibold">Threat Verdict:</span>
                    <span className="px-2.5 py-1 text-xs font-extrabold uppercase rounded bg-danger/20 text-danger animate-pulse">
                      Phishing (Risk score: 85%)
                    </span>
                  </div>
                  <div className="text-xs text-gray-400 space-y-1.5 font-mono">
                    <p>• Alert: Contains high-risk domain combination (login, update, PayPal, verify)</p>
                    <p>• Warning: Domain matches known patterns of spoofed banking endpoints</p>
                    <p>• Telemetry: Subdomain count exceeds 4 tiers</p>
                  </div>
                </div>
              </div>
            )}

            {activeTab === 'sms' && (
              <div className="space-y-6">
                <div className="space-y-2">
                  <label className="text-xs font-bold text-gray-400 uppercase tracking-widest font-mono">SMS / Email Content Parser</label>
                  <textarea 
                    rows={3}
                    defaultValue="URGENT: Your bank account is temporarily blocked due to unusual activity. Click here to verify your identity and restore access: http://bit.ly/bank-auth"
                    className="w-full bg-background/80 border border-white/10 rounded-lg p-4 text-sm text-gray-200 focus:outline-none focus:border-primary font-sans"
                  />
                  <div className="flex justify-end">
                    <button className="px-6 py-3 bg-secondary text-white font-bold text-sm rounded-lg hover:opacity-90 transition-opacity shadow-glow">
                      Analyze Message
                    </button>
                  </div>
                </div>
                <div className="glass-panel p-4 rounded-lg bg-black/40 space-y-3">
                  <div className="flex justify-between items-center">
                    <span className="text-sm font-semibold">Scam Probability:</span>
                    <span className="px-2.5 py-1 text-xs font-extrabold uppercase rounded bg-warning/20 text-warning">
                      95.0% - Highly Suspect
                    </span>
                  </div>
                  <div className="text-xs text-gray-400 space-y-1.5 font-mono">
                    <p>• Flagged: Urgency indicator ('urgent') discovered</p>
                    <p>• Flagged: Obfuscated URL shortener redirect ('bit.ly') detected</p>
                    <p>• Weight: Context resembles typical account suspension phishing themes</p>
                  </div>
                </div>
              </div>
            )}

            {activeTab === 'apk' && (
              <div className="space-y-6">
                <div className="space-y-2">
                  <label className="text-xs font-bold text-gray-400 uppercase tracking-widest">Requested App Permissions Checklist</label>
                  <div className="grid grid-cols-2 gap-2 text-xs font-mono text-gray-300">
                    <div className="flex items-center gap-2 bg-background p-2.5 rounded border border-white/5">
                      <span className="text-danger">●</span> BIND_ACCESSIBILITY_SERVICE
                    </div>
                    <div className="flex items-center gap-2 bg-background p-2.5 rounded border border-white/5">
                      <span className="text-danger">●</span> SEND_SMS
                    </div>
                    <div className="flex items-center gap-2 bg-background p-2.5 rounded border border-white/5">
                      <span className="text-warning">●</span> ACCESS_FINE_LOCATION
                    </div>
                    <div className="flex items-center gap-2 bg-background p-2.5 rounded border border-white/5">
                      <span className="text-success">●</span> INTERNET
                    </div>
                  </div>
                </div>
                <div className="glass-panel p-4 rounded-lg bg-black/40 space-y-3">
                  <div className="flex justify-between items-center">
                    <span className="text-sm font-semibold">Threat Level Rating:</span>
                    <span className="px-2.5 py-1 text-xs font-extrabold uppercase rounded bg-danger/20 text-danger">
                      Critical Risk: 85/100
                    </span>
                  </div>
                  <p className="text-xs text-warning font-semibold">
                    Category: Ransomware / Banking Trojan Agent
                  </p>
                  <p className="text-xs text-gray-400 leading-relaxed font-mono">
                    Accessibility bindings allow background overlays capable of capturing user logins, while SEND_SMS permissions pose high bill-fraud vectors.
                  </p>
                </div>
              </div>
            )}
          </div>
        </div>
      </section>

      {/* Detailed Features Grid */}
      <section id="features" className="max-w-7xl mx-auto px-6 py-20">
        <div className="text-center mb-16">
          <h2 className="text-3xl md:text-5xl font-extrabold mb-4">
            Complete Threat Intelligence suite
          </h2>
          <p className="text-gray-400 max-w-xl mx-auto">
            Comprehensive diagnostics systems mapping device status metrics, deep ML heuristics, and real-time security optimizations.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {[
            {
              icon: Shield,
              title: "SMS & Phishing Heuristics",
              desc: "Deep NLP pipelines and feature-reputation extractors flag fake links, obfuscated URLs, and text extortion scripts.",
              color: "border-primary/20 hover:border-primary/50 text-primary"
            },
            {
              icon: Cpu,
              title: "Android Permission Auditor",
              desc: "Monitors app metadata structures, warning users of suspicious background access tokens, adware and trojan processes.",
              color: "border-secondary/20 hover:border-secondary/50 text-secondary"
            },
            {
              icon: Zap,
              title: "Performance Optimizations",
              desc: "Examines system battery health status, detects redundant duplicate directories, and reports junk compiler caching files.",
              color: "border-success/20 hover:border-success/50 text-success"
            }
          ].map((feat, i) => (
            <div key={i} className={`glass-panel p-8 rounded-2xl border transition-all duration-300 ${feat.color}`}>
              <feat.icon className="w-12 h-12 mb-6" />
              <h3 className="text-xl font-bold text-white mb-3">{feat.title}</h3>
              <p className="text-gray-400 text-sm leading-relaxed">{feat.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Dynamic Pricing Grid */}
      <section id="pricing" className="max-w-6xl mx-auto px-6 py-20">
        <div className="text-center mb-16">
          <h2 className="text-3xl md:text-5xl font-extrabold mb-4">Pricing Architectures</h2>
          <p className="text-gray-400">Deployable as self-hosted utility or managed enterprise cloud protection tiers.</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-8 max-w-4xl mx-auto">
          {/* Free Tier */}
          <div className="glass-panel p-8 rounded-2xl border-white/5 flex flex-col justify-between">
            <div>
              <h3 className="text-xl font-bold text-white mb-2">Community Edition</h3>
              <p className="text-gray-400 text-sm mb-6">Best for individual power users and self-hosted deployments.</p>
              <div className="text-4xl font-extrabold text-white mb-6">$0<span className="text-sm font-medium text-gray-500"> / forever</span></div>
              <ul className="space-y-3.5 text-sm text-gray-300 mb-8">
                <li className="flex items-center gap-2"><CheckCircle2 className="w-4.5 h-4.5 text-primary" /> Self-Hosted Docker deployment</li>
                <li className="flex items-center gap-2"><CheckCircle2 className="w-4.5 h-4.5 text-primary" /> AI URL Phishing Classifier API</li>
                <li className="flex items-center gap-2"><CheckCircle2 className="w-4.5 h-4.5 text-primary" /> Android app scanning features</li>
                <li className="flex items-center gap-2"><CheckCircle2 className="w-4.5 h-4.5 text-primary" /> Core Dashboard & metrics</li>
              </ul>
            </div>
            <Link href="/register" className="w-full py-3 bg-white/5 border border-white/10 hover:bg-white/10 text-white font-semibold rounded-lg text-center block transition-colors">
              Deploy Locally
            </Link>
          </div>

          {/* Premium Tier */}
          <div className="glass-panel p-8 rounded-2xl border-primary/30 shadow-neon flex flex-col justify-between relative">
            <span className="absolute top-0 right-8 -translate-y-1/2 px-3 py-1 bg-primary text-background font-extrabold text-xs rounded-full">
              POPULAR
            </span>
            <div>
              <h3 className="text-xl font-bold text-white mb-2">Enterprise Shield</h3>
              <p className="text-gray-400 text-sm mb-6">Advanced threat intelligence and live incident response APIs.</p>
              <div className="text-4xl font-extrabold text-primary mb-6">$29<span className="text-sm font-medium text-gray-500"> / month</span></div>
              <ul className="space-y-3.5 text-sm text-gray-300 mb-8">
                <li className="flex items-center gap-2"><CheckCircle2 className="w-4.5 h-4.5 text-success" /> Everything in Community Edition</li>
                <li className="flex items-center gap-2"><CheckCircle2 className="w-4.5 h-4.5 text-success" /> Live SMS & Email Scam Classification API</li>
                <li className="flex items-center gap-2"><CheckCircle2 className="w-4.5 h-4.5 text-success" /> Biometric authorization keys support</li>
                <li className="flex items-center gap-2"><CheckCircle2 className="w-4.5 h-4.5 text-success" /> Detailed PDF Audit Reports</li>
                <li className="flex items-center gap-2"><CheckCircle2 className="w-4.5 h-4.5 text-success" /> Real-time global threat push alerts</li>
              </ul>
            </div>
            <Link href="/register" className="w-full py-3 bg-primary text-background font-bold rounded-lg text-center block hover:opacity-90 transition-opacity">
              Activate Cyber Shield
            </Link>
          </div>
        </div>
      </section>

      {/* Security FAQ */}
      <section id="faq" className="max-w-4xl mx-auto px-6 py-20 border-t border-white/5">
        <div className="text-center mb-16">
          <h2 className="text-3xl font-extrabold mb-4">Security Intel FAQ</h2>
          <p className="text-gray-400">Everything you need to know about Sentinel AI diagnostics architecture.</p>
        </div>

        <div className="space-y-6">
          {[
            {
              q: "How does Sentinel AI analyze links and text messages for scam attempts?",
              a: "Sentinel runs local natural language evaluation blocks and extracts over a dozen URL structure indicators (such as raw IPs, specific domain combinations, depth levels, and redirects). The backend models output an intuitive safety rating with detailed risk indicators."
            },
            {
              q: "Is there support for monitoring Android app vulnerabilities?",
              a: "Yes! The mobile client scans package attributes and cross-references them against our high-risk Android permissions scoring. For example, BIND_ACCESSIBILITY_SERVICE paired with network permission generates severe spy alerts."
            },
            {
              q: "Can I self-host this whole platform?",
              a: "Absolutely. We supply a multi-container Docker orchestration blueprint setting up the Next.js panel, Python API server, and a robust PostgreSQL database setup instantly."
            }
          ].map((faq, i) => (
            <div key={i} className="glass-panel p-6 rounded-xl border-white/5">
              <div className="flex items-start gap-3">
                <HelpCircle className="w-5 h-5 text-primary shrink-0 mt-0.5" />
                <div>
                  <h4 className="font-bold text-white mb-2">{faq.q}</h4>
                  <p className="text-sm text-gray-400 leading-relaxed">{faq.a}</p>
                </div>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* Footer */}
      <footer className="glass-panel border-t border-white/5 px-6 py-12 text-center text-gray-500 text-sm">
        <div className="flex justify-center gap-6 mb-6">
          <Shield className="w-6 h-6 text-primary" />
          <span className="font-bold text-white tracking-widest">SENTINEL SECURITY</span>
        </div>
        <p className="mb-4">© 2026 Sentinel AI Inc. Protecting endpoints globally with autonomous cyber intelligence.</p>
        <p className="text-xs">Next.js 15 • Tailwind CSS • HSL Color Scheme • OpenJDK 17</p>
      </footer>
    </div>
  );
}
