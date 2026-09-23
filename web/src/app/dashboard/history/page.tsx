"use client";

import React, { useState, useEffect, useCallback } from 'react';
import { 
  Shield, History, Trash2, RefreshCw, Filter, Search, 
  ExternalLink, AlertOctagon, CheckCircle2, ShieldAlert,
  Globe, MessageSquare, CreditCard, Smartphone, ChevronRight
} from 'lucide-react';
import { useAuthStore } from '@/lib/store';
import { apiUrl } from '@/lib/api';
import Link from 'next/link';

interface HistoryItem {
  id: string;
  scan_type: string;
  target: string;
  verdict: string;
  score: number;
  severity: string;
  timestamp: string;
  created_at: string;
}

export default function SecurityHistoryPage() {
  const { token, user } = useAuthStore();
  const [items, setItems] = useState<HistoryItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedFilter, setSelectedFilter] = useState('All');
  const [searchQuery, setSearchQuery] = useState('');
  const [deletingId, setDeletingId] = useState<string | null>(null);
  const [clearingAll, setClearingAll] = useState(false);

  const fetchHistory = useCallback(async (isInitial = false) => {
    if (isInitial) setLoading(true);
    try {
      const effectiveEmail = user?.email || (typeof window !== 'undefined' ? (localStorage.getItem('sentinel_email') || localStorage.getItem('user_email')) : '') || '';
      const headers: Record<string, string> = {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token || 'local_authenticated_agent'}`
      };
      if (effectiveEmail) headers['X-User-Email'] = effectiveEmail;

      const res = await fetch(apiUrl('/api/scan/history'), { headers });
      if (res.ok) {
        const data = await res.json();
        setItems(data.history || []);
      }
    } catch {
      // Safe fallback
    } finally {
      if (isInitial) setLoading(false);
    }
  }, [token, user?.email]);

  useEffect(() => {
    fetchHistory(true);

    // Live sync polling with Android every 2.5 seconds
    const interval = setInterval(() => {
      fetchHistory(false);
    }, 2500);

    const handleFocus = () => {
      fetchHistory(false);
    };

    window.addEventListener('focus', handleFocus);
    window.addEventListener('sentinel_scan_completed', () => fetchHistory(false));

    return () => {
      clearInterval(interval);
      window.removeEventListener('focus', handleFocus);
      window.removeEventListener('sentinel_scan_completed', () => fetchHistory(false));
    };
  }, [fetchHistory]);

  const handleDeleteItem = async (id: string) => {
    if (!id || deletingId) return;
    setDeletingId(id);

    // Optimistic UI update
    setItems((prev) => prev.filter((item) => item.id !== id));

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
      if (typeof window !== 'undefined') {
        window.dispatchEvent(new Event('sentinel_scan_completed'));
      }
      await fetchHistory(false);
    } catch (e) {
      console.error('Failed to delete history item:', e);
    } finally {
      setDeletingId(null);
    }
  };

  const handleClearAll = async () => {
    if (clearingAll) return;
    const confirmed = window.confirm("Are you sure you want to clear all scan history? This will delete all logged scans and threats across both web and your Android mobile app.");
    if (!confirmed) return;

    setClearingAll(true);
    // Optimistic update
    setItems([]);

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
      if (typeof window !== 'undefined') {
        window.dispatchEvent(new Event('sentinel_scan_completed'));
      }
      await fetchHistory(false);
    } catch (e) {
      console.error('Failed to clear history:', e);
    } finally {
      setClearingAll(false);
    }
  };

  const filters = ['All', 'URLs', 'SMS', 'Payment', 'APK'];

  const filteredItems = items.filter((item) => {
    // Filter by type
    if (selectedFilter === 'URLs' && !item.scan_type.toLowerCase().includes('url')) return false;
    if (selectedFilter === 'SMS' && !item.scan_type.toLowerCase().includes('sms') && !item.scan_type.toLowerCase().includes('message')) return false;
    if (selectedFilter === 'Payment' && !item.scan_type.toLowerCase().includes('payment')) return false;
    if (selectedFilter === 'APK' && !item.scan_type.toLowerCase().includes('apk') && !item.scan_type.toLowerCase().includes('app')) return false;

    // Filter by search query
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      const matchTarget = item.target.toLowerCase().includes(q);
      const matchVerdict = item.verdict.toLowerCase().includes(q);
      const matchType = item.scan_type.toLowerCase().includes(q);
      if (!matchTarget && !matchVerdict && !matchType) return false;
    }

    return true;
  });

  const getScanIcon = (scanType: string) => {
    const s = scanType.toLowerCase();
    if (s.includes('url')) return <Globe className="w-4 h-4 text-cyan-400 shrink-0" />;
    if (s.includes('sms') || s.includes('message')) return <MessageSquare className="w-4 h-4 text-secondary shrink-0" />;
    if (s.includes('payment')) return <CreditCard className="w-4 h-4 text-emerald-400 shrink-0" />;
    if (s.includes('apk') || s.includes('app')) return <Smartphone className="w-4 h-4 text-amber-400 shrink-0" />;
    return <Shield className="w-4 h-4 text-primary shrink-0" />;
  };

  const getSeverityBadge = (severity: string, verdict: string) => {
    const s = severity.toLowerCase();
    const v = verdict.toLowerCase();
    const isDanger = s.includes('crit') || s.includes('high') || v.includes('phishing') || v.includes('scam') || v.includes('malware');
    const isWarning = s.includes('med') || v.includes('suspicious');

    if (isDanger) {
      return (
        <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded text-xs font-bold bg-danger/15 text-danger border border-danger/30">
          <AlertOctagon className="w-3 h-3" /> {severity.toUpperCase()}
        </span>
      );
    }
    if (isWarning) {
      return (
        <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded text-xs font-bold bg-warning/15 text-warning border border-warning/30">
          <ShieldAlert className="w-3 h-3" /> {severity.toUpperCase()}
        </span>
      );
    }
    return (
      <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded text-xs font-bold bg-success/15 text-success border border-success/30">
        <CheckCircle2 className="w-3 h-3" /> SAFE
      </span>
    );
  };

  return (
    <div className="space-y-6 max-w-7xl mx-auto">
      {/* Header Banner */}
      <div className="glass-panel rounded-2xl p-6 border-white/5 flex flex-col md:flex-row items-start md:items-center justify-between gap-4 relative overflow-hidden">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <History className="w-5 h-5 text-primary" />
            <h2 className="text-xl font-extrabold text-white">Security Scan History</h2>
            <span className="text-[10px] bg-primary/10 text-primary border border-primary/20 px-2 py-0.5 rounded-full font-mono">
              Live Cloud Sync
            </span>
          </div>
          <p className="text-sm text-gray-400">
            Audit logs and inspection records synchronized across your Android mobile app and web console.
          </p>
        </div>
        <div className="flex items-center gap-3 shrink-0">
          {items.length > 0 && (
            <button
              onClick={handleClearAll}
              disabled={clearingAll}
              className="flex items-center gap-1.5 px-3.5 py-2 rounded-xl text-xs font-bold bg-danger/10 hover:bg-danger/20 text-danger border border-danger/30 transition-all"
            >
              <Trash2 className="w-3.5 h-3.5" /> Clear All History
            </button>
          )}
          <button
            onClick={() => fetchHistory(false)}
            className="flex items-center gap-1.5 px-3 py-2 rounded-xl text-xs font-medium text-gray-400 hover:text-white glass-panel hover:border-primary/40 transition-all"
          >
            <RefreshCw className="w-3.5 h-3.5" /> Refresh
          </button>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3">
        {/* Category Pills */}
        <div className="flex items-center gap-1.5 overflow-x-auto pb-1 sm:pb-0">
          {filters.map((f) => (
            <button
              key={f}
              onClick={() => setSelectedFilter(f)}
              className={`px-3.5 py-1.5 rounded-xl text-xs font-bold transition-all whitespace-nowrap ${
                selectedFilter === f
                  ? 'bg-primary text-black shadow-[0_0_12px_rgba(0,229,255,0.3)]'
                  : 'glass-panel text-gray-400 hover:text-white border-white/5 hover:border-white/20'
              }`}
            >
              {f}
            </button>
          ))}
        </div>

        {/* Search Input */}
        <div className="relative min-w-[240px]">
          <Search className="w-4 h-4 text-gray-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search targets or verdicts..."
            className="w-full bg-white/5 border border-white/10 rounded-xl pl-9 pr-4 py-1.5 text-xs text-white placeholder-gray-500 focus:outline-none focus:border-primary transition-all font-mono"
          />
        </div>
      </div>

      {/* History Records List */}
      <div className="glass-panel p-6 rounded-2xl border-white/5 overflow-x-auto">
        {loading ? (
          <div className="flex flex-col items-center justify-center py-12 gap-3">
            <RefreshCw className="w-8 h-8 text-primary animate-spin" />
            <p className="text-xs text-gray-400 font-mono">Synchronizing unified security logs...</p>
          </div>
        ) : filteredItems.length > 0 ? (
          <table className="w-full text-left text-sm">
            <thead>
              <tr className="text-[10px] text-gray-500 uppercase tracking-widest border-b border-white/5 font-mono">
                <th className="pb-3 pl-1">Timestamp</th>
                <th className="pb-3">Security Module</th>
                <th className="pb-3">Target Asset / Destination</th>
                <th className="pb-3">Verdict</th>
                <th className="pb-3">Risk Severity</th>
                <th className="pb-3 text-right pr-2">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/5">
              {filteredItems.map((item) => (
                <tr key={item.id} className="hover:bg-white/2 transition-colors">
                  <td className="py-3.5 pl-1 text-xs text-gray-400 font-mono whitespace-nowrap">
                    {item.timestamp}
                  </td>
                  <td className="py-3.5">
                    <div className="flex items-center gap-2">
                      {getScanIcon(item.scan_type)}
                      <span className="text-xs font-semibold text-white truncate max-w-[180px]">
                        {item.scan_type}
                      </span>
                    </div>
                  </td>
                  <td className="py-3.5 text-xs text-gray-300 font-mono truncate max-w-[320px]">
                    {item.target}
                  </td>
                  <td className="py-3.5 text-xs font-bold">
                    <span className={item.verdict.toLowerCase().includes('phishing') || item.verdict.toLowerCase().includes('scam') || item.verdict.toLowerCase().includes('malware') ? 'text-danger' : 'text-success'}>
                      {item.verdict}
                    </span>
                  </td>
                  <td className="py-3.5">
                    {getSeverityBadge(item.severity, item.verdict)}
                  </td>
                  <td className="py-3.5 text-right pr-2">
                    <button
                      onClick={() => handleDeleteItem(item.id)}
                      disabled={deletingId === item.id}
                      title="Delete record from cloud & mobile"
                      className="p-1.5 text-gray-500 hover:text-danger hover:bg-danger/10 rounded-lg transition-colors inline-flex items-center"
                    >
                      <Trash2 className={`w-3.5 h-3.5 ${deletingId === item.id ? 'animate-spin' : ''}`} />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <div className="flex flex-col items-center justify-center py-16 text-center">
            <div className="w-14 h-14 rounded-2xl bg-white/5 border border-white/10 flex items-center justify-center mb-4">
              <History className="w-7 h-7 text-gray-500" />
            </div>
            <h3 className="text-base font-bold text-white mb-1">No Scan History Found</h3>
            <p className="text-xs text-gray-400 max-w-sm mb-6">
              {searchQuery || selectedFilter !== 'All' 
                ? "No scan events matched your search or filter criteria." 
                : "Live security events recorded from your Android app or web scanner will appear here synchronized in real time."}
            </p>
            <Link
              href="/dashboard/scanner"
              className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-primary text-black font-bold text-xs hover:bg-primary/90 transition-all shadow-[0_0_15px_rgba(0,229,255,0.25)]"
            >
              <Globe className="w-4 h-4" /> Run URL Scan
            </Link>
          </div>
        )}
      </div>
    </div>
  );
}
