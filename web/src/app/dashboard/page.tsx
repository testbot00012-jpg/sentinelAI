"use client";

import React, { useState, useEffect, useCallback } from 'react';
import { 
  Shield, Terminal, AlertOctagon, Activity, 
  CheckCircle2, RefreshCw, Bot, CreditCard, Sparkles,
  Globe, MessageSquare, ShieldCheck, ExternalLink, Trash2
} from 'lucide-react';
import { useAuthStore } from '@/lib/store';
import { apiUrl } from '@/lib/api';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts';
import Link from 'next/link';

const THREAT_COLORS = ['#00E5FF', '#7C3AED', '#FF4D4D'];

export default function Dashboard() {
  const { token, user } = useAuthStore();
  const [metrics, setMetrics] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [deletingId, setDeletingId] = useState<string | null>(null);
  const [clearingAll, setClearingAll] = useState(false);

  const fetchMetrics = useCallback(async (isInitial = false) => {
    if (isInitial) setLoading(true);
    try {
      const effectiveEmail = user?.email || (typeof window !== 'undefined' ? (localStorage.getItem('sentinel_email') || localStorage.getItem('user_email')) : '') || '';
      const headers: Record<string, string> = {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token || 'local_authenticated_agent'}`
      };
      if (effectiveEmail) headers['X-User-Email'] = effectiveEmail;

      const res = await fetch(apiUrl('/api/analytics/metrics'), { headers });
      if (res.ok) {
        const data = await res.json();
        setMetrics(data);
      }
    } catch {
      // Safe fail-safe: keep existing state without throwing uncaught errors
    } finally {
      if (isInitial) setLoading(false);
    }
  }, [token, user?.email]);

  useEffect(() => {
    fetchMetrics(true);

    // High-speed 2.5s interval for instant real-time synchronization with Android app
    const interval = setInterval(() => {
      fetchMetrics(false);
    }, 2500);

    // Instant sync trigger when a scan is completed on Scanner or SMS pages
    const handleScanCompleted = () => {
      fetchMetrics(false);
    };

    const handleFocus = () => {
      fetchMetrics(false);
    };

    window.addEventListener('sentinel_scan_completed', handleScanCompleted);
    window.addEventListener('focus', handleFocus);

    return () => {
      clearInterval(interval);
      window.removeEventListener('sentinel_scan_completed', handleScanCompleted);
      window.removeEventListener('focus', handleFocus);
    };
  }, [fetchMetrics]);

  const handleDeleteHistoryItem = async (id: string) => {
    if (!id || deletingId) return;
    setDeletingId(id);

    // Immediate optimistic update for fast UX
    setMetrics((prev: any) => {
      if (!prev) return prev;
      return {
        ...prev,
        recent_threats: prev.recent_threats?.filter((t: any) => t.id !== id) || [],
        recent_scans: prev.recent_scans?.filter((s: any) => s.id !== id) || [],
      };
    });

    try {
      const effectiveEmail = user?.email || (typeof window !== 'undefined' ? (localStorage.getItem('sentinel_email') || localStorage.getItem('user_email')) : '') || '';
      const headers: Record<string, string> = {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token || 'local_authenticated_agent'}`
      };
      if (effectiveEmail) headers['X-User-Email'] = effectiveEmail;

      await fetch(apiUrl(`/api/scan/history/${id}`), {
        method: 'DELETE',
        headers
      });
      await fetchMetrics(false);
    } catch (e) {
      console.error('Failed to delete history item:', e);
    } finally {
      setDeletingId(null);
    }
  };

  const handleClearAllHistory = async () => {
    if (clearingAll) return;
    const confirmed = window.confirm("Are you sure you want to clear all scan history and recorded threats? This will also remove them from your synchronized mobile app.");
    if (!confirmed) return;

    setClearingAll(true);
    // Immediate optimistic update
    setMetrics((prev: any) => {
      if (!prev) return prev;
      return {
        ...prev,
        recent_threats: [],
        recent_scans: [],
        summary: {
          ...prev.summary,
          total_scans: 0,
          threats_blocked: 0
        }
      };
    });

    try {
      const effectiveEmail = user?.email || (typeof window !== 'undefined' ? (localStorage.getItem('sentinel_email') || localStorage.getItem('user_email')) : '') || '';
      const headers: Record<string, string> = {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token || 'local_authenticated_agent'}`
      };
      if (effectiveEmail) headers['X-User-Email'] = effectiveEmail;

      await fetch(apiUrl('/api/scan/history'), {
        method: 'DELETE',
        headers
      });
      await fetchMetrics(false);
    } catch (e) {
      console.error('Failed to clear history:', e);
    } finally {
      setClearingAll(false);
    }
  };

  const quickActions = [
    { label: 'AI Intel Chat', icon: Bot, href: '/dashboard/chat', color: 'text-primary', bg: 'bg-primary/10 border-primary/20 hover:border-primary/50' },
    { label: 'Scan URL', icon: Globe, href: '/dashboard/scanner', color: 'text-cyan-400', bg: 'bg-cyan-500/10 border-cyan-500/20 hover:border-cyan-500/50' },
    { label: 'Analyze SMS', icon: MessageSquare, href: '/dashboard/sms', color: 'text-secondary', bg: 'bg-secondary/10 border-secondary/20 hover:border-secondary/50' },
    { label: 'Payment Shield', icon: CreditCard, href: '/dashboard/payment-shield', color: 'text-emerald-400', bg: 'bg-emerald-500/10 border-emerald-500/20 hover:border-emerald-500/50' },
    { label: 'Agent Profile', icon: Activity, href: '/dashboard/profile', color: 'text-amber-400', bg: 'bg-amber-500/10 border-amber-500/20 hover:border-amber-500/50' },
  ];

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="flex flex-col items-center gap-4">
          <Shield className="w-12 h-12 text-primary animate-pulse drop-shadow-[0_0_12px_#00e5ff]" />
          <p className="text-sm text-gray-400 font-mono animate-pulse">Synchronizing Threat Intelligence...</p>
        </div>
      </div>
    );
  }

  const recentThreats = metrics?.recent_threats || [];
  const recentScans = metrics?.recent_scans || [];
  const totalScans = metrics?.summary?.total_scans ?? 0;
  const threatsBlocked = metrics?.summary?.threats_blocked ?? 0;
  const securityScore = metrics?.summary?.security_score ?? 98;
  const phishingCount = metrics?.threat_distribution?.[0]?.value ?? 0;

  return (
    <div className="space-y-6 max-w-7xl mx-auto">
      {/* Welcome Banner */}
      <div className="glass-panel rounded-2xl p-6 border-primary/10 flex flex-col md:flex-row items-start md:items-center justify-between gap-4 relative overflow-hidden">
        <div className="absolute right-0 top-0 w-64 h-64 bg-primary opacity-5 rounded-full blur-[80px] pointer-events-none -translate-y-1/2 translate-x-1/4" />
        <div>
          <h2 className="text-xl font-extrabold text-white">
            Welcome back, <span className="bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">{user?.email?.split('@')[0] || 'Agent'}</span>
          </h2>
          <p className="text-sm text-gray-400 mt-1">Your cybersecurity shield is active with live cloud telemetry synchronization.</p>
        </div>
        <div className="flex items-center gap-3 shrink-0">
          <div className="w-3 h-3 rounded-full bg-success animate-pulse" />
          <span className="text-sm font-bold text-success">Cloud Sync Active</span>
        </div>
      </div>

      {/* Groq AI Security Assistant Hero Banner */}
      <div className="glass-panel rounded-2xl p-5 border-primary/20 bg-gradient-to-r from-primary/10 via-secondary/5 to-transparent flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 relative overflow-hidden">
        <div className="flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-primary/20 border border-primary/40 flex items-center justify-center shrink-0">
            <Bot className="w-6 h-6 text-primary animate-pulse" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h3 className="font-extrabold text-white text-base">Sentinel AI Intelligence Assistant</h3>
              <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-secondary/20 text-secondary border border-secondary/30">
                Local Neural LLM v2.5
              </span>
            </div>
            <p className="text-xs text-gray-400 mt-0.5">
              Ask questions about suspicious UPI requests, phishing SMS, malicious APKs, or system security threats.
            </p>
          </div>
        </div>
        <Link
          href="/dashboard/chat"
          className="flex items-center gap-2 px-4 py-2 rounded-xl bg-primary text-black font-bold text-xs hover:bg-primary/90 transition-all shrink-0 shadow-[0_0_15px_rgba(0,229,255,0.3)]"
        >
          <Sparkles className="w-4 h-4" /> Launch AI Chat
        </Link>
      </div>

      {/* Quick Actions */}
      <div>
        <h3 className="text-xs font-bold text-gray-500 uppercase tracking-widest mb-3">Security Modules</h3>
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-3">
          {quickActions.map((action) => (
            <Link
              key={action.href}
              href={action.href}
              className={`glass-panel p-3.5 rounded-xl border ${action.bg} flex flex-col sm:flex-row items-center gap-2.5 transition-all duration-200 group text-center sm:text-left`}
            >
              <action.icon className={`w-5 h-5 ${action.color} group-hover:scale-110 transition-transform shrink-0`} />
              <span className="text-xs font-semibold text-white truncate">{action.label}</span>
            </Link>
          ))}
        </div>
      </div>

      {/* KPI Metrics */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="glass-panel p-5 rounded-xl border border-success/20 flex flex-col gap-3">
          <div className="flex justify-between items-start">
            <span className="text-xs text-gray-500 uppercase tracking-widest font-mono leading-tight">Security Score</span>
            <Shield className="w-5 h-5 text-success opacity-80" />
          </div>
          <div className="text-3xl font-extrabold text-success">{securityScore}%</div>
          <div className="text-[11px] font-semibold text-success">Active Cloud Guard</div>
        </div>

        <div className="glass-panel p-5 rounded-xl border border-danger/20 flex flex-col gap-3">
          <div className="flex justify-between items-start">
            <span className="text-xs text-gray-500 uppercase tracking-widest font-mono leading-tight">Threats Blocked</span>
            <AlertOctagon className="w-5 h-5 text-danger opacity-80" />
          </div>
          <div className="text-3xl font-extrabold text-danger">{threatsBlocked}</div>
          <div className="text-[11px] font-semibold text-gray-400">Recorded Threats</div>
        </div>

        <div className="glass-panel p-5 rounded-xl border border-primary/20 flex flex-col gap-3">
          <div className="flex justify-between items-start">
            <span className="text-xs text-gray-500 uppercase tracking-widest font-mono leading-tight">Total Scans</span>
            <Terminal className="w-5 h-5 text-primary opacity-80" />
          </div>
          <div className="text-3xl font-extrabold text-primary">{totalScans.toLocaleString()}</div>
          <div className="text-[11px] font-semibold text-primary">Live Database Sync</div>
        </div>

        <div className="glass-panel p-5 rounded-xl border border-cyan-500/20 flex flex-col gap-3">
          <div className="flex justify-between items-start">
            <span className="text-xs text-gray-500 uppercase tracking-widest font-mono leading-tight">Phishing Intercepted</span>
            <Globe className="w-5 h-5 text-cyan-400 opacity-80" />
          </div>
          <div className="text-3xl font-extrabold text-cyan-400">{phishingCount}</div>
          <div className="text-[11px] font-semibold text-cyan-400">Zero-Day Protected</div>
        </div>
      </div>

      {/* Analytics Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Security Trends Line Chart */}
        <div className="glass-panel p-6 rounded-2xl border-white/5 lg:col-span-2 flex flex-col">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="font-bold text-sm text-white">Threat Monitoring Trends</h3>
              <p className="text-xs text-gray-500">Security score progression across evaluation cycles</p>
            </div>
            <div className="flex items-center gap-1.5 text-xs text-primary font-mono bg-primary/10 px-2.5 py-1 rounded-lg border border-primary/20">
              <ShieldCheck className="w-3.5 h-3.5" /> High Integrity
            </div>
          </div>
          <div className="h-56 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={metrics?.trends || [
                { day: 'Mon', score: 95 },
                { day: 'Tue', score: 92 },
                { day: 'Wed', score: 88 },
                { day: 'Thu', score: 90 },
                { day: 'Fri', score: 94 },
                { day: 'Sat', score: 98 },
                { day: 'Sun', score: securityScore },
              ]}>
                <CartesianGrid strokeDasharray="3 3" stroke="#ffffff10" />
                <XAxis dataKey="day" stroke="#6b7280" fontSize={11} tickLine={false} />
                <YAxis stroke="#6b7280" fontSize={11} domain={[70, 100]} tickLine={false} />
                <Tooltip
                  contentStyle={{ backgroundColor: '#0e172a', border: '1px solid #1e293b', borderRadius: '8px' }}
                  labelStyle={{ color: '#00e5ff' }}
                />
                <Line
                  type="monotone"
                  dataKey="score"
                  stroke="#00e5ff"
                  strokeWidth={2.5}
                  dot={{ fill: '#00e5ff', r: 4 }}
                  activeDot={{ r: 6, fill: '#7c3aed' }}
                />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Threat Distribution Pie Chart */}
        <div className="glass-panel p-6 rounded-2xl border-white/5 flex flex-col">
          <h3 className="font-bold text-sm text-white mb-1">Threat Distribution</h3>
          <p className="text-xs text-gray-500 mb-4">Identified attacks by security vector</p>
          <div className="h-44 w-full flex items-center justify-center">
            {metrics?.threat_distribution && metrics.threat_distribution.some((d: any) => d.value > 0) ? (
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={metrics.threat_distribution}
                    cx="50%"
                    cy="50%"
                    innerRadius={45}
                    outerRadius={65}
                    paddingAngle={4}
                    dataKey="value"
                  >
                    {metrics.threat_distribution.map((_: any, index: number) => (
                      <Cell key={`cell-${index}`} fill={THREAT_COLORS[index % THREAT_COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip
                    contentStyle={{ backgroundColor: '#0e172a', border: '1px solid #1e293b', borderRadius: '8px' }}
                  />
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <div className="flex flex-col items-center justify-center text-center p-4">
                <ShieldCheck className="w-10 h-10 text-success/60 mb-2" />
                <span className="text-xs text-gray-400 font-semibold">Zero Threats Recorded</span>
                <span className="text-[10px] text-gray-600 mt-0.5">All scanned destinations are verified</span>
              </div>
            )}
          </div>
          <div className="space-y-2 mt-auto pt-3 border-t border-white/5">
            {(metrics?.threat_distribution || [
              { name: "Phishing URLs", value: 0 },
              { name: "Scam SMS", value: 0 },
              { name: "Malware APKs", value: 0 }
            ]).map((item: any, i: number) => (
              <div key={i} className="flex items-center justify-between text-xs">
                <div className="flex items-center gap-2">
                  <span className="w-2 h-2 rounded-full shrink-0" style={{ backgroundColor: THREAT_COLORS[i % THREAT_COLORS.length] }} />
                  <span className="text-gray-400">{item.name}</span>
                </div>
                <span className="font-bold text-white">{item.value}</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Real Live Scanned Threats Table (Directly from MongoDB Atlas) */}
      <div className="glass-panel p-6 rounded-2xl border-white/5 overflow-x-auto">
        <div className="flex items-center justify-between mb-5">
          <div className="flex items-center gap-2">
            <Shield className="w-4.5 h-4.5 text-danger" />
            <h3 className="font-bold text-sm text-white">Live Detected Threats</h3>
            <span className="text-[10px] bg-white/5 text-gray-400 px-2 py-0.5 rounded font-mono">Real-time MongoDB Atlas</span>
          </div>
          <div className="flex items-center gap-2">
            {recentThreats.length > 0 && (
              <button
                onClick={handleClearAllHistory}
                disabled={clearingAll}
                className="flex items-center gap-1 text-[11px] text-danger hover:bg-danger/10 px-2.5 py-1 rounded-lg border border-danger/20 transition-all font-semibold"
                title="Clear all scans and threats from cloud & mobile"
              >
                <Trash2 className="w-3 h-3" /> Clear All Threats
              </button>
            )}
            <button
              onClick={() => fetchMetrics(false)}
              className="flex items-center gap-1.5 text-xs text-gray-400 hover:text-primary transition-colors"
            >
              <RefreshCw className="w-3.5 h-3.5" /> Refresh
            </button>
          </div>
        </div>
        <table className="w-full text-left text-sm">
          <thead>
            <tr className="text-[10px] text-gray-500 uppercase tracking-widest border-b border-white/5 font-mono">
              <th className="pb-3 pl-1">Detected Time</th>
              <th className="pb-3">Threat Category</th>
              <th className="pb-3 hidden md:table-cell">Targeted Vector / Destination</th>
              <th className="pb-3">Confidence Score</th>
              <th className="pb-3">Shield Action</th>
              <th className="pb-3 text-right pr-2">Action</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-white/5">
            {recentThreats.length > 0 ? (
              recentThreats.map((log: any, i: number) => (
                <tr key={log.id || i} className="hover:bg-white/2 transition-colors">
                  <td className="py-3.5 pl-1 text-xs text-gray-400 font-mono whitespace-nowrap">{log.time}</td>
                  <td className="py-3.5">
                    <span className={`px-2.5 py-1 text-xs font-bold rounded ${log.severity === 'danger' ? 'bg-danger/15 text-danger border border-danger/30' : 'bg-warning/15 text-warning border border-warning/30'}`}>
                      {log.type}
                    </span>
                  </td>
                  <td className="py-3.5 text-xs text-gray-300 font-mono hidden md:table-cell truncate max-w-[280px]">{log.source}</td>
                  <td className={`py-3.5 text-xs font-extrabold ${log.severity === 'danger' ? 'text-danger' : 'text-warning'}`}>{log.score}</td>
                  <td className="py-3.5">
                    <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs text-danger font-bold bg-danger/10 border border-danger/20">
                      <AlertOctagon className="w-3.5 h-3.5" /> Blocked
                    </span>
                  </td>
                  <td className="py-3.5 text-right pr-2">
                    <button
                      onClick={() => handleDeleteHistoryItem(log.id)}
                      disabled={deletingId === log.id}
                      title="Delete threat record (syncs across web & app)"
                      className="p-1 text-gray-500 hover:text-danger hover:bg-danger/10 rounded transition-colors"
                    >
                      <Trash2 className={`w-3.5 h-3.5 ${deletingId === log.id ? 'animate-spin' : ''}`} />
                    </button>
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan={6} className="py-8 text-center text-gray-500 font-mono text-xs">
                  <div className="flex flex-col items-center justify-center gap-2">
                    <CheckCircle2 className="w-6 h-6 text-success/70" />
                    <span>No threats recorded in your account yet. All scanned destinations are clean.</span>
                  </div>
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* Real Recent Scans Activity (Directly from MongoDB Atlas) */}
      <div className="glass-panel p-6 rounded-2xl border-white/5 overflow-x-auto">
        <div className="flex items-center justify-between mb-5">
          <div className="flex items-center gap-2">
            <Terminal className="w-4.5 h-4.5 text-primary" />
            <h3 className="font-bold text-sm text-white">Recent Real-Time Scans</h3>
            <span className="text-[10px] bg-white/5 text-gray-400 px-2 py-0.5 rounded font-mono">Syncing Across Web & App</span>
          </div>
          <div className="flex items-center gap-3">
            {recentScans.length > 0 && (
              <button
                onClick={handleClearAllHistory}
                disabled={clearingAll}
                className="flex items-center gap-1 text-[11px] text-danger hover:bg-danger/10 px-2.5 py-1 rounded-lg border border-danger/20 transition-all font-semibold"
                title="Clear all scans from cloud & mobile"
              >
                <Trash2 className="w-3 h-3" /> Clear Scans
              </button>
            )}
            <Link
              href="/dashboard/history"
              className="flex items-center gap-1 text-xs text-gray-400 hover:text-primary transition-colors"
            >
              Full History
            </Link>
            <Link
              href="/dashboard/scanner"
              className="flex items-center gap-1 text-xs text-primary hover:underline font-semibold"
            >
              New Scan <ExternalLink className="w-3.5 h-3.5" />
            </Link>
          </div>
        </div>
        <table className="w-full text-left text-sm">
          <thead>
            <tr className="text-[10px] text-gray-500 uppercase tracking-widest border-b border-white/5 font-mono">
              <th className="pb-3 pl-1">Scan Time</th>
              <th className="pb-3">Target URL</th>
              <th className="pb-3">Verdict Status</th>
              <th className="pb-3">Risk Score</th>
              <th className="pb-3">Access Action</th>
              <th className="pb-3 text-right pr-2">Action</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-white/5">
            {recentScans.length > 0 ? (
              recentScans.map((scan: any, i: number) => {
                const isThreat = scan.status === 'Phishing' || scan.status === 'Suspicious';
                return (
                  <tr key={scan.id || i} className="hover:bg-white/2 transition-colors">
                    <td className="py-3.5 pl-1 text-xs text-gray-400 font-mono whitespace-nowrap">{scan.time}</td>
                    <td className="py-3.5 text-xs text-white font-mono truncate max-w-[280px]">{scan.url}</td>
                    <td className="py-3.5">
                      <span className={`px-2 py-0.5 text-xs font-bold rounded ${isThreat ? 'bg-danger/15 text-danger border border-danger/30' : 'bg-success/15 text-success border border-success/30'}`}>
                        {scan.status}
                      </span>
                    </td>
                    <td className={`py-3.5 text-xs font-extrabold ${isThreat ? 'text-danger' : 'text-success'}`}>{scan.score}</td>
                    <td className="py-3.5">
                      <span className={`inline-flex items-center gap-1 text-xs font-bold ${isThreat ? 'text-danger' : 'text-success'}`}>
                        {isThreat ? <AlertOctagon className="w-3.5 h-3.5" /> : <CheckCircle2 className="w-3.5 h-3.5" />}
                        {scan.action}
                      </span>
                    </td>
                    <td className="py-3.5 text-right pr-2">
                      <button
                        onClick={() => handleDeleteHistoryItem(scan.id)}
                        disabled={deletingId === scan.id}
                        title="Delete scan record (syncs across web & app)"
                        className="p-1 text-gray-500 hover:text-danger hover:bg-danger/10 rounded transition-colors"
                      >
                        <Trash2 className={`w-3.5 h-3.5 ${deletingId === scan.id ? 'animate-spin' : ''}`} />
                      </button>
                    </td>
                  </tr>
                );
              })
            ) : (
              <tr>
                <td colSpan={6} className="py-8 text-center text-gray-500 font-mono text-xs">
                  No scan history recorded yet. Enter a URL or SMS to perform your first scan.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
