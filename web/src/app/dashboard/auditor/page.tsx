"use client";

import React, { useState, useEffect } from 'react';
import { Package, Search, AlertOctagon, CheckCircle2, AlertTriangle, Loader2, ShieldAlert, Cpu, Wifi, Camera, Mic, MapPin, MessageSquare, Phone, Database, Eye, Lock } from 'lucide-react';

interface AppEntry {
  name: string;
  packageName: string;
  permissions: string[];
  riskScore: number;
  category: string;
  verdict: 'Safe' | 'Suspicious' | 'Dangerous';
}

const PERMISSION_ICONS: Record<string, React.ComponentType<any>> = {
  'CAMERA': Camera,
  'MICROPHONE': Mic,
  'LOCATION': MapPin,
  'SMS': MessageSquare,
  'CONTACTS': Phone,
  'STORAGE': Database,
  'OVERLAY': Eye,
  'ACCESSIBILITY': Lock,
  'INTERNET': Wifi,
};

const PERMISSION_RISK: Record<string, { color: string; level: string }> = {
  'READ_SMS': { color: 'text-danger', level: 'Critical' },
  'SEND_SMS': { color: 'text-danger', level: 'Critical' },
  'BIND_ACCESSIBILITY_SERVICE': { color: 'text-danger', level: 'Critical' },
  'SYSTEM_ALERT_WINDOW': { color: 'text-danger', level: 'Critical' },
  'RECORD_AUDIO': { color: 'text-warning', level: 'High' },
  'ACCESS_FINE_LOCATION': { color: 'text-warning', level: 'High' },
  'READ_CONTACTS': { color: 'text-warning', level: 'High' },
  'CAMERA': { color: 'text-warning', level: 'Medium' },
  'READ_CALL_LOG': { color: 'text-danger', level: 'Critical' },
  'PROCESS_OUTGOING_CALLS': { color: 'text-warning', level: 'High' },
  'WRITE_EXTERNAL_STORAGE': { color: 'text-primary', level: 'Low' },
  'INTERNET': { color: 'text-success', level: 'Normal' },
  'VIBRATE': { color: 'text-success', level: 'Normal' },
  'RECEIVE_BOOT_COMPLETED': { color: 'text-primary', level: 'Low' },
};

const SAMPLE_APPS: AppEntry[] = [
  {
    name: 'FlashLight Pro',
    packageName: 'com.flashlight.pro',
    permissions: ['CAMERA', 'READ_SMS', 'SEND_SMS', 'RECORD_AUDIO', 'ACCESS_FINE_LOCATION', 'READ_CONTACTS', 'INTERNET'],
    riskScore: 94,
    category: 'Spyware / Adware',
    verdict: 'Dangerous',
  },
  {
    name: 'Google Chrome',
    packageName: 'com.android.chrome',
    permissions: ['INTERNET', 'CAMERA', 'ACCESS_FINE_LOCATION', 'WRITE_EXTERNAL_STORAGE', 'VIBRATE'],
    riskScore: 18,
    category: 'Trusted Browser',
    verdict: 'Safe',
  },
  {
    name: 'BatteryBooster X',
    packageName: 'com.battery.boost.x',
    permissions: ['SYSTEM_ALERT_WINDOW', 'BIND_ACCESSIBILITY_SERVICE', 'INTERNET', 'RECEIVE_BOOT_COMPLETED'],
    riskScore: 82,
    category: 'Ransomware Vector',
    verdict: 'Dangerous',
  },
  {
    name: 'WhatsApp Messenger',
    packageName: 'com.whatsapp',
    permissions: ['CAMERA', 'RECORD_AUDIO', 'READ_CONTACTS', 'INTERNET', 'VIBRATE', 'WRITE_EXTERNAL_STORAGE'],
    riskScore: 22,
    category: 'Social / Communication',
    verdict: 'Safe',
  },
  {
    name: 'FakeBank Helper',
    packageName: 'com.bank.unofficial.helper',
    permissions: ['READ_SMS', 'PROCESS_OUTGOING_CALLS', 'READ_CALL_LOG', 'RECORD_AUDIO', 'INTERNET', 'ACCESS_FINE_LOCATION'],
    riskScore: 97,
    category: 'Banking Trojan',
    verdict: 'Dangerous',
  },
  {
    name: 'Spotify Music',
    packageName: 'com.spotify.music',
    permissions: ['INTERNET', 'CAMERA', 'VIBRATE', 'WRITE_EXTERNAL_STORAGE', 'RECORD_AUDIO'],
    riskScore: 12,
    category: 'Media / Entertainment',
    verdict: 'Safe',
  },
];

export default function AppAuditorPage() {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedApp, setSelectedApp] = useState<AppEntry | null>(null);
  const [filter, setFilter] = useState<'All' | 'Safe' | 'Suspicious' | 'Dangerous'>('All');
  const [apps] = useState<AppEntry[]>(SAMPLE_APPS);
  const [scanning, setScanning] = useState(true);

  useEffect(() => {
    const t = setTimeout(() => setScanning(false), 1800);
    return () => clearTimeout(t);
  }, []);

  const filteredApps = apps.filter(app => {
    const matchSearch = app.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      app.packageName.toLowerCase().includes(searchTerm.toLowerCase());
    const matchFilter = filter === 'All' || app.verdict === filter;
    return matchSearch && matchFilter;
  });

  const stats = {
    safe: apps.filter(a => a.verdict === 'Safe').length,
    suspicious: apps.filter(a => a.verdict === 'Suspicious').length,
    dangerous: apps.filter(a => a.verdict === 'Dangerous').length,
  };

  const getVerdictStyle = (verdict: string) => {
    if (verdict === 'Dangerous') return { badge: 'bg-danger/15 text-danger border-danger/30', icon: ShieldAlert, dot: 'bg-danger' };
    if (verdict === 'Suspicious') return { badge: 'bg-warning/15 text-warning border-warning/30', icon: AlertTriangle, dot: 'bg-warning' };
    return { badge: 'bg-success/15 text-success border-success/30', icon: CheckCircle2, dot: 'bg-success' };
  };

  const getScoreColor = (score: number) => {
    if (score >= 70) return 'text-danger';
    if (score >= 40) return 'text-warning';
    return 'text-success';
  };

  if (scanning) {
    return (
      <div className="flex flex-col items-center justify-center h-72 gap-6">
        <div className="relative">
          <div className="w-20 h-20 rounded-full border-2 border-success/20 animate-ping absolute inset-0" />
          <div className="w-20 h-20 rounded-full border-2 border-success/50 flex items-center justify-center">
            <Package className="w-9 h-9 text-success animate-pulse" />
          </div>
        </div>
        <div className="text-center">
          <p className="text-sm font-bold text-success font-mono animate-pulse">Running App Permission Audit...</p>
          <p className="text-xs text-gray-500 mt-1">Scanning {apps.length} installed applications against threat database</p>
        </div>
        <div className="w-64 h-1.5 bg-white/5 rounded-full overflow-hidden">
          <div className="h-full bg-gradient-to-r from-success to-primary rounded-full animate-[scan_1.8s_ease-in-out_forwards]" style={{ width: '100%', animation: 'none', transition: 'width 1.8s ease-in-out' }} />
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto space-y-6">
      {/* Header */}
      <div>
        <h2 className="text-2xl font-extrabold flex items-center gap-3">
          <Package className="w-7 h-7 text-success drop-shadow-[0_0_8px_#22c55e]" />
          Application Permission Auditor
        </h2>
        <p className="text-sm text-gray-400 mt-1">Analyse installed app permission profiles to detect potential spyware, adware, and banking trojans.</p>
      </div>

      {/* Stats Row */}
      <div className="grid grid-cols-3 gap-4">
        {[
          { label: 'Safe Apps', value: stats.safe, color: 'text-success', bg: 'border-success/20 bg-success/5', icon: CheckCircle2 },
          { label: 'Suspicious', value: stats.suspicious, color: 'text-warning', bg: 'border-warning/20 bg-warning/5', icon: AlertTriangle },
          { label: 'Dangerous', value: stats.dangerous, color: 'text-danger', bg: 'border-danger/20 bg-danger/5', icon: AlertOctagon },
        ].map((s, i) => (
          <div key={i} className={`glass-panel p-4 rounded-xl border ${s.bg} flex items-center gap-3`}>
            <s.icon className={`w-8 h-8 ${s.color} shrink-0`} />
            <div>
              <div className={`text-2xl font-extrabold ${s.color}`}>{s.value}</div>
              <div className="text-xs text-gray-500 font-mono uppercase tracking-wider">{s.label}</div>
            </div>
          </div>
        ))}
      </div>

      <div className={`grid gap-6 ${selectedApp ? 'grid-cols-1 lg:grid-cols-5' : 'grid-cols-1'}`}>
        {/* App List */}
        <div className={selectedApp ? 'lg:col-span-2' : ''}>
          <div className="glass-panel rounded-2xl border-white/5 overflow-hidden">
            {/* Toolbar */}
            <div className="p-4 border-b border-white/5 flex gap-3">
              <div className="relative flex-1">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-500" />
                <input
                  type="text"
                  placeholder="Search apps..."
                  value={searchTerm}
                  onChange={e => setSearchTerm(e.target.value)}
                  className="w-full bg-background/80 border border-white/10 rounded-lg pl-9 pr-3 py-2.5 text-sm text-gray-200 focus:outline-none focus:border-success focus:ring-1 focus:ring-success"
                />
              </div>
              <select
                value={filter}
                onChange={e => setFilter(e.target.value as any)}
                className="bg-background/80 border border-white/10 rounded-lg px-3 py-2.5 text-sm text-gray-300 focus:outline-none focus:border-success"
              >
                {['All', 'Safe', 'Suspicious', 'Dangerous'].map(f => (
                  <option key={f} value={f}>{f}</option>
                ))}
              </select>
            </div>

            {/* List */}
            <div className="divide-y divide-white/5 max-h-[520px] overflow-y-auto">
              {filteredApps.length === 0 ? (
                <div className="p-8 text-center text-gray-500 text-sm">No apps match your search.</div>
              ) : filteredApps.map((app, i) => {
                const style = getVerdictStyle(app.verdict);
                const isSelected = selectedApp?.packageName === app.packageName;
                return (
                  <button
                    key={i}
                    onClick={() => setSelectedApp(isSelected ? null : app)}
                    className={`w-full text-left p-4 hover:bg-white/3 transition-colors flex items-center gap-3 ${isSelected ? 'bg-white/5 border-l-2 border-primary' : ''}`}
                  >
                    <div className={`w-10 h-10 rounded-xl flex items-center justify-center shrink-0 border ${style.badge}`}>
                      <Package className="w-5 h-5" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="text-sm font-bold text-white truncate">{app.name}</div>
                      <div className="text-[11px] text-gray-500 font-mono truncate">{app.packageName}</div>
                    </div>
                    <div className="flex flex-col items-end gap-1 shrink-0">
                      <span className={`text-sm font-extrabold ${getScoreColor(app.riskScore)}`}>{app.riskScore}%</span>
                      <span className={`px-1.5 py-0.5 text-[9px] font-bold rounded border ${style.badge}`}>{app.verdict}</span>
                    </div>
                  </button>
                );
              })}
            </div>
          </div>
        </div>

        {/* Detail Panel */}
        {selectedApp && (() => {
          const style = getVerdictStyle(selectedApp.verdict);
          return (
            <div className="lg:col-span-3 space-y-4">
              <div className={`glass-panel rounded-2xl p-6 border ${style.badge} space-y-5`}>
                {/* App Header */}
                <div className="flex items-start gap-4">
                  <div className={`w-14 h-14 rounded-2xl flex items-center justify-center border ${style.badge} shrink-0`}>
                    <Package className="w-7 h-7" />
                  </div>
                  <div className="flex-1">
                    <h3 className="text-xl font-extrabold text-white">{selectedApp.name}</h3>
                    <p className="text-xs text-gray-500 font-mono">{selectedApp.packageName}</p>
                    <div className="flex items-center gap-2 mt-2">
                      <span className={`px-2 py-0.5 text-xs font-bold rounded border ${style.badge}`}>{selectedApp.verdict}</span>
                      <span className="text-xs text-gray-500">{selectedApp.category}</span>
                    </div>
                  </div>
                  <div className="text-right shrink-0">
                    <div className="text-xs text-gray-500 uppercase tracking-widest font-mono">Risk Score</div>
                    <div className={`text-4xl font-extrabold ${getScoreColor(selectedApp.riskScore)}`}>{selectedApp.riskScore}<span className="text-lg">%</span></div>
                  </div>
                </div>

                {/* Score Bar */}
                <div className="h-2 bg-white/5 rounded-full overflow-hidden">
                  <div
                    className={`h-full rounded-full transition-all duration-700 ${selectedApp.riskScore >= 70 ? 'bg-gradient-to-r from-danger to-red-400' : selectedApp.riskScore >= 40 ? 'bg-gradient-to-r from-warning to-yellow-400' : 'bg-gradient-to-r from-success to-green-400'}`}
                    style={{ width: `${selectedApp.riskScore}%` }}
                  />
                </div>

                {/* Permissions */}
                <div>
                  <p className="text-xs font-bold text-gray-500 uppercase tracking-widest mb-3">
                    Requested Permissions ({selectedApp.permissions.length})
                  </p>
                  <div className="grid grid-cols-1 gap-2">
                    {selectedApp.permissions.map((perm, i) => {
                      const risk = PERMISSION_RISK[perm] || { color: 'text-gray-400', level: 'Unknown' };
                      return (
                        <div key={i} className="flex items-center justify-between p-3 rounded-lg bg-white/3 border border-white/5">
                          <div className="flex items-center gap-2">
                            <Cpu className={`w-3.5 h-3.5 ${risk.color} shrink-0`} />
                            <span className="text-xs font-mono text-gray-300">{perm}</span>
                          </div>
                          <span className={`text-[10px] font-bold px-2 py-0.5 rounded ${
                            risk.level === 'Critical' ? 'bg-danger/15 text-danger' :
                            risk.level === 'High' ? 'bg-warning/15 text-warning' :
                            risk.level === 'Medium' ? 'bg-orange-500/15 text-orange-400' :
                            risk.level === 'Low' ? 'bg-primary/15 text-primary' :
                            'bg-success/15 text-success'
                          }`}>
                            {risk.level}
                          </span>
                        </div>
                      );
                    })}
                  </div>
                </div>

                {/* Verdict Summary */}
                <div className={`p-4 rounded-lg border ${style.badge} bg-opacity-10`}>
                  <p className="text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">Sentinel AI Verdict</p>
                  <p className="text-sm text-gray-300 leading-relaxed">
                    {selectedApp.verdict === 'Dangerous'
                      ? `⚠ CRITICAL RISK: "${selectedApp.name}" requests ${selectedApp.permissions.filter(p => PERMISSION_RISK[p]?.level === 'Critical').length} critical permissions. This permission combination is consistent with ${selectedApp.category} malware patterns. Immediate removal is strongly recommended.`
                      : selectedApp.verdict === 'Suspicious'
                        ? `⚡ This app has some elevated permission requests. Monitor its behavior and check for unusual battery drain or data usage.`
                        : `✓ "${selectedApp.name}" has a clean permission profile matching its stated functionality. No suspicious patterns detected.`
                    }
                  </p>
                </div>
              </div>
            </div>
          );
        })()}
      </div>
    </div>
  );
}
