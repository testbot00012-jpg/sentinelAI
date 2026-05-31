"use client";

import React, { useState, useEffect } from 'react';
import { 
  Shield, Terminal, Cpu, AlertOctagon, Activity, 
  CheckCircle2, RefreshCw, BarChart2, Zap, TrendingUp,
  Lock, Globe, MessageSquare, Package
} from 'lucide-react';
import { useAuthStore } from '@/lib/store';
import { apiUrl } from '@/lib/api';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, AreaChart, Area } from 'recharts';
import Link from 'next/link';

const THREAT_COLORS = ['#00E5FF', '#7C3AED', '#FF4D4D', '#FF9500'];

export default function Dashboard() {
  const { token, user } = useAuthStore();
  const [metrics, setMetrics] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchMetrics();
  }, []);

  const fetchMetrics = async () => {
    setLoading(true);
    try {
      const res = await fetch(apiUrl('/api/analytics/metrics'), {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (res.ok) {
        const data = await res.json();
        setMetrics(data);
      } else {
        throw new Error('Failed');
      }
    } catch {
      // Populate with empty default values when backend is unreachable
      setMetrics({
        summary: {
          security_score: 0,
          threats_blocked: 0,
          total_scans: 0,
          device_health: { battery: 0, ram: 0, storage: 0, model: "Unknown Node" }
        },
        trends: [],
        threat_distribution: []
      });
    } finally {
      setLoading(false);
    }
  };

  const quickActions = [
    { label: 'Scan URL', icon: Globe, href: '/dashboard/scanner', color: 'text-primary', bg: 'bg-primary/10 border-primary/20 hover:border-primary/50' },
    { label: 'Analyze SMS', icon: MessageSquare, href: '/dashboard/sms', color: 'text-secondary', bg: 'bg-secondary/10 border-secondary/20 hover:border-secondary/50' },
    { label: 'App Audit', icon: Package, href: '/dashboard/auditor', color: 'text-success', bg: 'bg-success/10 border-success/20 hover:border-success/50' },
    { label: 'My Profile', icon: Activity, href: '/dashboard/profile', color: 'text-warning', bg: 'bg-warning/10 border-warning/20 hover:border-warning/50' },
  ];

  const recentThreats = [
    { time: '2 mins ago', type: 'Phishing URL', source: 'paypal-verify-update.net', score: '98%', action: 'Blocked', severity: 'danger' },
    { time: '15 mins ago', type: 'SMS Scam', source: '+91 98765 01234', score: '93%', action: 'Quarantined', severity: 'warning' },
    { time: '1 hour ago', type: 'Malware APK', source: 'Flash_Update_v4.apk', score: '99%', action: 'Blocked', severity: 'danger' },
    { time: '3 hours ago', type: 'Suspicious IP', source: '185.220.101.47', score: '71%', action: 'Flagged', severity: 'warning' },
    { time: '5 hours ago', type: 'Phishing URL', source: 'amaz0n-login.tk', score: '96%', action: 'Blocked', severity: 'danger' },
  ];

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="flex flex-col items-center gap-4">
          <Shield className="w-12 h-12 text-primary animate-pulse drop-shadow-[0_0_12px_#00e5ff]" />
          <p className="text-sm text-gray-400 font-mono animate-pulse">Initializing Threat Intelligence...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6 max-w-7xl mx-auto">
      {/* Welcome Banner */}
      <div className="glass-panel rounded-2xl p-6 border-primary/10 flex flex-col md:flex-row items-start md:items-center justify-between gap-4 relative overflow-hidden">
        <div className="absolute right-0 top-0 w-64 h-64 bg-primary opacity-5 rounded-full blur-[80px] pointer-events-none -translate-y-1/2 translate-x-1/4" />
        <div>
          <h2 className="text-xl font-extrabold text-white">
            Welcome back, <span className="bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">{user?.email?.split('@')[0] || 'Agent'}</span>
          </h2>
          <p className="text-sm text-gray-400 mt-1">Your cybersecurity shield is active and monitoring all vectors.</p>
        </div>
        <div className="flex items-center gap-3 shrink-0">
          <div className="w-3 h-3 rounded-full bg-success animate-pulse" />
          <span className="text-sm font-bold text-success">All Systems Active</span>
        </div>
      </div>

      {/* KPI Metrics */}
      {metrics && (
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          {[
            {
              label: 'Security Score',
              value: `${metrics.summary.security_score}%`,
              icon: Shield,
              color: 'text-success',
              bg: 'border-success/20',
              sub: '↑ 2% this week',
              subColor: 'text-success'
            },
            {
              label: 'Threats Blocked',
              value: metrics.summary.threats_blocked,
              icon: AlertOctagon,
              color: 'text-danger',
              bg: 'border-danger/20',
              sub: 'Last 30 days',
              subColor: 'text-gray-500'
            },
            {
              label: 'Total Scans',
              value: metrics.summary.total_scans.toLocaleString(),
              icon: Terminal,
              color: 'text-primary',
              bg: 'border-primary/20',
              sub: '↑ 142 today',
              subColor: 'text-primary'
            },
            {
              label: 'RAM Usage',
              value: `${metrics.summary.device_health.ram}%`,
              icon: Cpu,
              color: 'text-secondary',
              bg: 'border-secondary/20',
              sub: 'System nominal',
              subColor: 'text-gray-500'
            },
          ].map((kpi, i) => (
            <div key={i} className={`glass-panel p-5 rounded-xl border ${kpi.bg} flex flex-col gap-3`}>
              <div className="flex justify-between items-start">
                <span className="text-xs text-gray-500 uppercase tracking-widest font-mono leading-tight">{kpi.label}</span>
                <kpi.icon className={`w-5 h-5 ${kpi.color} opacity-80`} />
              </div>
              <div className={`text-3xl font-extrabold ${kpi.color}`}>{kpi.value}</div>
              <div className={`text-[11px] font-semibold ${kpi.subColor}`}>{kpi.sub}</div>
            </div>
          ))}
        </div>
      )}

      {/* Quick Actions */}
      <div>
        <h3 className="text-xs font-bold text-gray-500 uppercase tracking-widest mb-3">Quick Actions</h3>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
          {quickActions.map((action) => (
            <Link
              key={action.href}
              href={action.href}
              className={`glass-panel p-4 rounded-xl border ${action.bg} flex items-center gap-3 transition-all duration-200 group`}
            >
              <action.icon className={`w-5 h-5 ${action.color} group-hover:scale-110 transition-transform`} />
              <span className="text-sm font-semibold text-white">{action.label}</span>
            </Link>
          ))}
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Area Chart */}
        {metrics && (
          <div className="lg:col-span-2 glass-panel p-6 rounded-2xl border-white/5">
            <div className="flex items-center justify-between mb-6">
              <h3 className="font-bold flex items-center gap-2 text-sm">
                <TrendingUp className="w-4.5 h-4.5 text-primary" /> Security Score Trend
              </h3>
              <span className="text-xs text-gray-500 font-mono bg-white/5 px-2 py-1 rounded">Last 7 Days</span>
            </div>
            <div className="h-56">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={metrics.trends}>
                  <defs>
                    <linearGradient id="scoreGrad" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#00E5FF" stopOpacity={0.3} />
                      <stop offset="95%" stopColor="#00E5FF" stopOpacity={0.02} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.04)" />
                  <XAxis dataKey="day" stroke="#475569" fontSize={11} />
                  <YAxis stroke="#475569" fontSize={11} domain={[75, 100]} />
                  <Tooltip
                    contentStyle={{ backgroundColor: '#0A1028', borderColor: 'rgba(0,229,255,0.2)', borderRadius: '8px' }}
                    labelStyle={{ color: '#00E5FF', fontWeight: 'bold', fontSize: '12px' }}
                    itemStyle={{ color: '#94a3b8', fontSize: '11px' }}
                  />
                  <Area type="monotone" dataKey="score" stroke="#00E5FF" strokeWidth={2.5} fill="url(#scoreGrad)" dot={{ r: 4, fill: '#00E5FF' }} activeDot={{ r: 6 }} />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          </div>
        )}

        {/* Threat Pie */}
        {metrics && (
          <div className="glass-panel p-6 rounded-2xl border-white/5">
            <h3 className="font-bold flex items-center gap-2 text-sm mb-6">
              <BarChart2 className="w-4.5 h-4.5 text-secondary" /> Threat Distribution
            </h3>
            <div className="h-44 flex justify-center items-center">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={metrics.threat_distribution}
                    cx="50%" cy="50%"
                    innerRadius={45} outerRadius={70}
                    paddingAngle={4}
                    dataKey="value"
                  >
                    {metrics.threat_distribution.map((_: any, index: number) => (
                      <Cell key={`cell-${index}`} fill={THREAT_COLORS[index % THREAT_COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip
                    contentStyle={{ backgroundColor: '#0A1028', borderColor: 'rgba(255,255,255,0.1)', borderRadius: '8px', fontSize: '12px' }}
                  />
                </PieChart>
              </ResponsiveContainer>
            </div>
            <div className="space-y-2 mt-2">
              {metrics.threat_distribution.map((item: any, i: number) => (
                <div key={i} className="flex items-center justify-between text-xs">
                  <div className="flex items-center gap-2">
                    <span className="w-2 h-2 rounded-full shrink-0" style={{ backgroundColor: THREAT_COLORS[i] }} />
                    <span className="text-gray-400">{item.name}</span>
                  </div>
                  <span className="font-bold text-white">{item.value}</span>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>

      {/* Recent Threats Table */}
      <div className="glass-panel p-6 rounded-2xl border-white/5 overflow-x-auto">
        <div className="flex items-center justify-between mb-5">
          <h3 className="font-bold flex items-center gap-2 text-sm">
            <Shield className="w-4.5 h-4.5 text-primary" /> Live Threat Intelligence Feed
          </h3>
          <button
            onClick={fetchMetrics}
            className="flex items-center gap-1.5 text-xs text-gray-400 hover:text-primary transition-colors"
          >
            <RefreshCw className="w-3.5 h-3.5" /> Refresh
          </button>
        </div>
        <table className="w-full text-left text-sm">
          <thead>
            <tr className="text-[10px] text-gray-600 uppercase tracking-widest border-b border-white/5">
              <th className="pb-3 pl-1">Time</th>
              <th className="pb-3">Threat Type</th>
              <th className="pb-3 hidden md:table-cell">Source Vector</th>
              <th className="pb-3">Risk Score</th>
              <th className="pb-3">Action</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-white/5">
            {recentThreats.map((log, i) => (
              <tr key={i} className="hover:bg-white/2 transition-colors">
                <td className="py-3 pl-1 text-xs text-gray-500 font-mono whitespace-nowrap">{log.time}</td>
                <td className="py-3">
                  <span className={`px-2 py-0.5 text-xs font-bold rounded ${log.severity === 'danger' ? 'bg-danger/10 text-danger' : 'bg-warning/10 text-warning'}`}>
                    {log.type}
                  </span>
                </td>
                <td className="py-3 text-xs text-gray-400 font-mono hidden md:table-cell truncate max-w-[200px]">{log.source}</td>
                <td className={`py-3 text-xs font-extrabold ${log.severity === 'danger' ? 'text-danger' : 'text-warning'}`}>{log.score}</td>
                <td className="py-3">
                  <span className="flex items-center gap-1 text-xs text-success font-bold">
                    <CheckCircle2 className="w-3.5 h-3.5" /> {log.action}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
