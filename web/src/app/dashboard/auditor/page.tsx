"use client";

import React, { useState } from 'react';
import { 
  Package, Search, CheckCircle2, AlertTriangle, 
  ShieldAlert, Camera, Mic, MapPin, MessageSquare, Phone, 
  Database, Eye, Lock, Filter, ExternalLink, ShieldCheck
} from 'lucide-react';

interface AppEntry {
  name: string;
  packageName: string;
  permissions: string[];
  riskScore: number;
  category: string;
  verdict: 'VERIFIED SAFE' | 'MONITORED' | 'HIGH RISK' | 'CRITICAL RISK';
  isSystem: boolean;
  installer: 'Google Play' | 'Sideloaded APK' | 'System Preload';
  isFakeClone?: boolean;
}

const SAMPLE_APPS: AppEntry[] = [
  // Dangerous / Sideloaded / Trojans
  {
    name: 'BatteryBooster X',
    packageName: 'com.battery.boost.x',
    permissions: ['SYSTEM_ALERT_WINDOW', 'BIND_ACCESSIBILITY_SERVICE', 'INTERNET'],
    riskScore: 85,
    category: 'Accessibility Trojan',
    verdict: 'CRITICAL RISK',
    isSystem: false,
    installer: 'Sideloaded APK',
  },
  {
    name: 'State Bank of India YONO (Clone)',
    packageName: 'com.sbi.lottery.gift',
    permissions: ['READ_SMS', 'RECEIVE_SMS', 'SYSTEM_ALERT_WINDOW', 'INTERNET'],
    riskScore: 95,
    category: 'Fake Banking Clone',
    verdict: 'CRITICAL RISK',
    isSystem: false,
    installer: 'Sideloaded APK',
    isFakeClone: true,
  },
  {
    name: 'FlashLight Pro',
    packageName: 'com.flashlight.sensor.pro',
    permissions: ['CAMERA', 'READ_SMS', 'RECORD_AUDIO', 'ACCESS_FINE_LOCATION'],
    riskScore: 55,
    category: 'Excessive Sensor Harvester',
    verdict: 'HIGH RISK',
    isSystem: false,
    installer: 'Sideloaded APK',
  },
  // Monitored Legitimate Utility
  {
    name: 'Truecaller: Caller ID & Block',
    packageName: 'com.truecaller',
    permissions: ['READ_CALL_LOG', 'SYSTEM_ALERT_WINDOW', 'READ_CONTACTS', 'READ_SMS'],
    riskScore: 15,
    category: 'Caller ID & Spam Filter',
    verdict: 'MONITORED',
    isSystem: false,
    installer: 'Google Play',
  },
  // Verified Safe User Apps
  {
    name: 'Swiggy: Food & Instamart',
    packageName: 'in.swiggy.android',
    permissions: ['ACCESS_FINE_LOCATION', 'INTERNET'],
    riskScore: 5,
    category: 'Verified Ecosystem App',
    verdict: 'VERIFIED SAFE',
    isSystem: false,
    installer: 'Google Play',
  },
  {
    name: 'WhatsApp Messenger',
    packageName: 'com.whatsapp',
    permissions: ['CAMERA', 'RECORD_AUDIO', 'READ_CONTACTS', 'INTERNET'],
    riskScore: 5,
    category: 'Verified Ecosystem App',
    verdict: 'VERIFIED SAFE',
    isSystem: false,
    installer: 'Google Play',
  },
  {
    name: 'Paytm: Payments & UPI',
    packageName: 'net.one97.paytm',
    permissions: ['ACCESS_FINE_LOCATION', 'READ_CONTACTS', 'CAMERA'],
    riskScore: 5,
    category: 'Verified Financial Platform',
    verdict: 'VERIFIED SAFE',
    isSystem: false,
    installer: 'Google Play',
  },
  {
    name: 'Airtel Thanks',
    packageName: 'com.myairtelapp',
    permissions: ['ACCESS_FINE_LOCATION', 'READ_CONTACTS'],
    riskScore: 5,
    category: 'Verified Telecom Service',
    verdict: 'VERIFIED SAFE',
    isSystem: false,
    installer: 'Google Play',
  },
  {
    name: 'Google Chrome',
    packageName: 'com.android.chrome',
    permissions: ['INTERNET', 'CAMERA', 'ACCESS_FINE_LOCATION'],
    riskScore: 0,
    category: 'Preloaded Browser',
    verdict: 'VERIFIED SAFE',
    isSystem: true,
    installer: 'System Preload',
  },
  {
    name: 'MIUI Security Center',
    packageName: 'com.miui.securitycenter',
    permissions: ['SYSTEM_ALERT_WINDOW', 'BIND_ACCESSIBILITY_SERVICE'],
    riskScore: 0,
    category: 'OEM System Service',
    verdict: 'VERIFIED SAFE',
    isSystem: true,
    installer: 'System Preload',
  },
];

export default function AppAuditorPage() {
  const [selectedTab, setSelectedTab] = useState<'user' | 'system' | 'all'>('user');
  const [selectedFilter, setSelectedFilter] = useState<'ALL' | 'CRITICAL' | 'MONITORED' | 'SAFE' | 'SIDELOADED'>('ALL');
  const [searchTerm, setSearchTerm] = useState('');

  const filteredApps = SAMPLE_APPS.filter(app => {
    // Tab match
    const tabMatch = 
      selectedTab === 'user' ? !app.isSystem :
      selectedTab === 'system' ? app.isSystem : true;

    // Filter match
    const filterMatch = 
      selectedFilter === 'CRITICAL' ? (app.verdict === 'CRITICAL RISK' || app.verdict === 'HIGH RISK') :
      selectedFilter === 'MONITORED' ? app.verdict === 'MONITORED' :
      selectedFilter === 'SAFE' ? app.verdict === 'VERIFIED SAFE' :
      selectedFilter === 'SIDELOADED' ? app.installer === 'Sideloaded APK' : true;

    // Search match
    const searchMatch = !searchTerm.trim() ? true : (
      app.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      app.packageName.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return tabMatch && filterMatch && searchMatch;
  });

  const userAppsCount = SAMPLE_APPS.filter(a => !a.isSystem).length;
  const systemAppsCount = SAMPLE_APPS.filter(a => a.isSystem).length;
  const sideloadedCount = SAMPLE_APPS.filter(a => a.installer === 'Sideloaded APK').length;
  const highRiskCount = SAMPLE_APPS.filter(a => a.riskScore >= 50).length;

  return (
    <div className="space-y-6 max-w-6xl mx-auto">
      {/* Header */}
      <div className="glass-panel rounded-2xl p-6 border-warning/20 flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <h2 className="text-xl font-extrabold text-white">PERMISSION & APPLICATION THREAT AUDITOR</h2>
            <span className="text-[10px] font-mono bg-warning/20 text-warning border border-warning/30 px-2 py-0.5 rounded font-bold">
              PACKAGE TELEMETRY
            </span>
          </div>
          <p className="text-xs text-gray-400">Audits installed Android packages, detects sideloaded Trojans, fake app clones & Accessibility abuse</p>
        </div>
      </div>

      {/* Search and Tabs */}
      <div className="space-y-3">
        {/* Search Bar */}
        <div className="relative">
          <Search className="w-4 h-4 text-gray-400 absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none" />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Search audited packages by name or package ID (e.g. Swiggy, com.whatsapp)..."
            className="w-full bg-black/40 border border-white/10 rounded-xl pl-10 pr-4 py-2.5 text-xs text-white placeholder-gray-500 focus:outline-none focus:border-primary font-mono"
          />
        </div>

        {/* Category Tabs */}
        <div className="flex border-b border-white/10 gap-2">
          {[
            { id: 'user', label: `USER APPS (${userAppsCount})` },
            { id: 'system', label: `SYSTEM (${systemAppsCount})` },
            { id: 'all', label: `ALL (${SAMPLE_APPS.length})` },
          ].map((tab) => {
            const isActive = selectedTab === tab.id;
            return (
              <button
                key={tab.id}
                onClick={() => setSelectedTab(tab.id as any)}
                className={`px-4 py-2.5 text-xs font-bold font-mono transition-all border-b-2 -mb-px ${
                  isActive
                    ? 'border-primary text-primary bg-primary/5'
                    : 'border-transparent text-gray-400 hover:text-white'
                }`}
              >
                {tab.label}
              </button>
            );
          })}
        </div>

        {/* Filter Pills */}
        <div className="flex flex-wrap gap-2 pt-1">
          {[
            { id: 'ALL', label: 'All' },
            { id: 'CRITICAL', label: `High Risk (${highRiskCount})` },
            { id: 'MONITORED', label: 'Monitored' },
            { id: 'SAFE', label: 'Verified Safe' },
            { id: 'SIDELOADED', label: `Sideloaded (${sideloadedCount})` },
          ].map((pill) => {
            const isSelected = selectedFilter === pill.id;
            return (
              <button
                key={pill.id}
                onClick={() => setSelectedFilter(pill.id as any)}
                className={`text-xs px-3 py-1 rounded-md font-medium transition-all ${
                  isSelected
                    ? 'bg-primary/20 text-primary border border-primary/40 font-bold'
                    : 'bg-white/5 border border-white/10 text-gray-400 hover:text-white'
                }`}
              >
                {pill.label}
              </button>
            );
          })}
        </div>
      </div>

      {/* Package List */}
      <div className="space-y-3">
        {filteredApps.map((app, index) => (
          <div
            key={index}
            className="glass-panel rounded-xl p-4 border border-white/5 hover:border-white/15 transition-all flex flex-col md:flex-row md:items-center justify-between gap-4"
          >
            <div className="space-y-1.5 flex-1 min-w-0">
              <div className="flex items-center gap-2 flex-wrap">
                <span className="text-sm font-bold text-white truncate">{app.name}</span>
                {app.installer === 'Google Play' && (
                  <span className="text-[9px] font-mono bg-success/15 text-success border border-success/30 px-2 py-0.5 rounded font-bold">
                    ✓ GOOGLE PLAY
                  </span>
                )}
                {app.installer === 'Sideloaded APK' && (
                  <span className="text-[9px] font-mono bg-warning/15 text-warning border border-warning/30 px-2 py-0.5 rounded font-bold">
                    ⚠️ SIDELOADED
                  </span>
                )}
                {app.installer === 'System Preload' && (
                  <span className="text-[9px] font-mono bg-white/10 text-gray-400 border border-white/10 px-2 py-0.5 rounded font-bold">
                    OEM PRELOAD
                  </span>
                )}
              </div>

              <div className="text-xs text-gray-400 font-mono truncate">{app.packageName}</div>

              {app.isFakeClone && (
                <div className="text-xs font-bold text-danger font-mono bg-danger/10 border border-danger/25 px-2.5 py-1 rounded-md inline-block">
                  🚨 FAKE APP CLONE: Impersonating official State Bank of India YONO
                </div>
              )}

              <div className="flex flex-wrap gap-1.5 pt-1">
                {app.permissions.map((perm, pIdx) => (
                  <span
                    key={pIdx}
                    className={`text-[10px] font-mono px-2 py-0.5 rounded ${
                      perm.includes('ACCESSIBILITY') || perm.includes('SYSTEM_ALERT') || perm.includes('SMS')
                        ? 'bg-danger/10 text-danger border border-danger/20'
                        : 'bg-white/5 text-gray-400 border border-white/5'
                    }`}
                  >
                    {perm}
                  </span>
                ))}
              </div>
            </div>

            <div className="flex items-center gap-3 shrink-0">
              <span className={`text-xs font-mono font-bold px-3 py-1.5 rounded-lg ${
                app.verdict === 'CRITICAL RISK'
                  ? 'bg-danger/20 text-danger border border-danger/30'
                  : app.verdict === 'HIGH RISK'
                  ? 'bg-warning/20 text-warning border border-warning/30'
                  : app.verdict === 'MONITORED'
                  ? 'bg-primary/20 text-primary border border-primary/30'
                  : 'bg-success/20 text-success border border-success/30'
              }`}>
                {app.verdict}
              </span>
            </div>
          </div>
        ))}

        {filteredApps.length === 0 && (
          <div className="text-center py-12 text-gray-400 text-xs font-mono">
            No applications match the current filter or search criteria.
          </div>
        )}
      </div>
    </div>
  );
}
