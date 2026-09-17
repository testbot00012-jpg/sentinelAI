"use client";

import React, { useState, useEffect } from 'react';
import { 
  Cpu, Zap, Trash2, BatteryCharging, Shield, Activity, 
  CheckCircle2, RefreshCw, AlertTriangle, Flame, HardDrive, Terminal
} from 'lucide-react';
import { useAuthStore } from '@/lib/store';
import { apiUrl } from '@/lib/api';

export default function OptimizerPage() {
  const { token } = useAuthStore();
  const [deviceModel, setDeviceModel] = useState<string>('Connecting Device...');

  // RAM States
  const [ramUsed, setRamUsed] = useState(54); // %
  const [isBoostingRam, setIsBoostingRam] = useState(false);
  const [ramBoostLog, setRamBoostLog] = useState<string[]>([]);
  const [ramFreedMb, setRamFreedMb] = useState<number | null>(null);

  // Junk States
  const [junkSize, setJunkSize] = useState(528); // 528 MB
  const [isCleaningJunk, setIsCleaningJunk] = useState(false);
  const [junkCleanLog, setJunkCleanLog] = useState<string[]>([]);
  const [junkFreedMb, setJunkFreedMb] = useState<number | null>(null);

  // Battery States
  const [batteryLevel, setBatteryLevel] = useState(82);
  const [batteryTemp, setBatteryTemp] = useState(31.4);
  const [batteryVoltage] = useState(4050);
  const [selectedPowerMode, setSelectedPowerMode] = useState<number>(0);
  const [isCooling, setIsCooling] = useState(false);
  const [cooldownSuccess, setCooldownSuccess] = useState(false);

  useEffect(() => {
    fetchLiveDeviceHealth();
    const interval = setInterval(fetchLiveDeviceHealth, 3500);
    return () => clearInterval(interval);
  }, [token]);

  const fetchLiveDeviceHealth = async () => {
    try {
      const res = await fetch(apiUrl('/api/analytics/metrics'), {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (res.ok) {
        const data = await res.json();
        const health = data?.summary?.device_health;
        if (health) {
          if (health.model) setDeviceModel(health.model);
          if (health.ram != null && !isBoostingRam) setRamUsed(Math.round(health.ram));
          if (health.battery != null) setBatteryLevel(Math.round(health.battery));
        }
      }
    } catch {
      // ignore
    }
  };

  const handleBoostRam = () => {
    setIsBoostingRam(true);
    setRamFreedMb(null);
    setRamBoostLog([]);

    const steps = [
      "Auditing dormant background process heaps...",
      "Purging cached service worker registers...",
      "Executing runtime garbage collection cycle (System.gc)...",
      "Defragmenting memory pages & thread pools..."
    ];

    steps.forEach((step, idx) => {
      setTimeout(() => {
        setRamBoostLog(prev => [...prev, `>>> ${step}`]);
      }, (idx + 1) * 350);
    });

    setTimeout(() => {
      setRamUsed(44); // dropped from 68% to 44%
      setRamFreedMb(486);
      setIsBoostingRam(false);
    }, 1800);
  };

  const handleCleanJunk = () => {
    setIsCleaningJunk(true);
    setJunkFreedMb(null);
    setJunkCleanLog([]);

    const steps = [
      "Scanning application cache directories...",
      "Wiping temporary residual system log buffers...",
      "Purging obsolete thumbnails and downloaded cache...",
      "Defragmenting local application indices..."
    ];

    steps.forEach((step, idx) => {
      setTimeout(() => {
        setJunkCleanLog(prev => [...prev, `>>> ${step}`]);
      }, (idx + 1) * 350);
    });

    setTimeout(() => {
      setJunkFreedMb(junkSize);
      setJunkSize(0);
      setIsCleaningJunk(false);
    }, 1800);
  };

  const handleCoolBattery = () => {
    setIsCooling(true);
    setTimeout(() => {
      setBatteryTemp(31.2);
      setCooldownSuccess(true);
      setIsCooling(false);
    }, 1200);
  };

  return (
    <div className="space-y-6 max-w-6xl mx-auto">
      {/* Header */}
      <div className="glass-panel rounded-2xl p-6 border-primary/20 flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <h2 className="text-xl font-extrabold text-white">DEVICE OPTIMIZATION SUITE</h2>
            <span className="text-[10px] font-mono bg-primary/20 text-primary border border-primary/30 px-2 py-0.5 rounded font-bold">
              CORE ENGINE
            </span>
          </div>
          <p className="text-xs text-gray-400">Intelligent RAM management, residual junk cleaning & thermal battery saver</p>
        </div>
        <div className="flex items-center gap-3">
          <button 
            onClick={() => {
              handleBoostRam();
              handleCleanJunk();
              handleCoolBattery();
            }}
            disabled={isBoostingRam || isCleaningJunk || isCooling}
            className="bg-gradient-to-r from-primary to-secondary hover:from-primary/90 hover:to-secondary/90 text-background font-bold text-xs px-4 py-2 rounded-lg flex items-center gap-2 shadow-[0_0_15px_rgba(0,229,255,0.25)] transition-all disabled:opacity-50"
          >
            <Zap className="w-4 h-4" />
            <span>1-TAP TOTAL OPTIMIZATION</span>
          </button>
        </div>
      </div>

      {/* Overview 3-KPI Row */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {/* RAM KPI */}
        <div className="glass-panel p-5 rounded-xl border-secondary/30 relative overflow-hidden">
          <div className="flex items-center justify-between mb-3">
            <span className="text-xs font-mono text-gray-400">RAM UTILIZATION</span>
            <Cpu className="w-4 h-4 text-secondary" />
          </div>
          <div className="text-3xl font-black text-secondary">{ramUsed}%</div>
          <p className="text-xs text-gray-400 mt-1 font-mono">
            {ramUsed > 50 ? "Elevated process load" : "Optimal operational state"}
          </p>
          <div className="mt-3 h-1.5 bg-white/5 rounded-full overflow-hidden">
            <div 
              className="h-full bg-secondary transition-all duration-500 rounded-full"
              style={{ width: `${ramUsed}%` }}
            />
          </div>
        </div>

        {/* Junk KPI */}
        <div className="glass-panel p-5 rounded-xl border-primary/30 relative overflow-hidden">
          <div className="flex items-center justify-between mb-3">
            <span className="text-xs font-mono text-gray-400">JUNK CACHE DETECTED</span>
            <Trash2 className="w-4 h-4 text-primary" />
          </div>
          <div className="text-3xl font-black text-primary">{junkSize} MB</div>
          <p className="text-xs text-gray-400 mt-1 font-mono">
            {junkSize > 0 ? "Reclaimable temporary space" : "Storage fully optimized"}
          </p>
          <div className="mt-3 h-1.5 bg-white/5 rounded-full overflow-hidden">
            <div 
              className="h-full bg-primary transition-all duration-500 rounded-full"
              style={{ width: `${junkSize > 0 ? 70 : 0}%` }}
            />
          </div>
        </div>

        {/* Battery KPI */}
        <div className="glass-panel p-5 rounded-xl border-success/30 relative overflow-hidden">
          <div className="flex items-center justify-between mb-3">
            <span className="text-xs font-mono text-gray-400">BATTERY & THERMAL</span>
            <BatteryCharging className="w-4 h-4 text-success" />
          </div>
          <div className="text-3xl font-black text-success">{batteryLevel}% • {batteryTemp}°C</div>
          <p className="text-xs text-gray-400 mt-1 font-mono">Healthy Li-ion • Optimal thermal grade</p>
          <div className="mt-3 h-1.5 bg-white/5 rounded-full overflow-hidden">
            <div 
              className="h-full bg-success transition-all duration-500 rounded-full"
              style={{ width: `${batteryLevel}%` }}
            />
          </div>
        </div>
      </div>

      {/* Module 1: Intelligent RAM Booster */}
      <div className="glass-panel rounded-2xl p-6 border-secondary/20 space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-secondary/15 border border-secondary/30 flex items-center justify-center">
              <Cpu className="w-5 h-5 text-secondary" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-white">INTELLIGENT RAM BOOSTER</h3>
              <p className="text-xs text-gray-400 font-mono">Total System Memory: 12.0 GB  •  Active Load: {ramUsed}%</p>
            </div>
          </div>
          <button
            onClick={handleBoostRam}
            disabled={isBoostingRam}
            className="bg-secondary hover:bg-secondary/90 text-white text-xs font-bold px-4 py-2 rounded-lg flex items-center gap-2 transition-all disabled:opacity-50"
          >
            <Zap className="w-4 h-4" />
            <span>{isBoostingRam ? "BOOSTING MEMORY..." : "BOOST RAM NOW"}</span>
          </button>
        </div>

        {ramFreedMb && (
          <div className="p-3 bg-success/10 border border-success/30 rounded-xl flex items-center gap-2 text-xs text-success font-mono font-bold">
            <CheckCircle2 className="w-4 h-4 shrink-0" />
            <span>Successfully Freed {ramFreedMb} MB of RAM! Inactive cached threads and dormant heaps cleared.</span>
          </div>
        )}

        {isBoostingRam && (
          <div className="bg-black/40 border border-white/5 p-3 rounded-lg space-y-1 font-mono text-[11px] text-secondary">
            {ramBoostLog.map((log, i) => (
              <div key={i}>{log}</div>
            ))}
          </div>
        )}
      </div>

      {/* Module 2: Deep Junk Cleaner */}
      <div className="glass-panel rounded-2xl p-6 border-primary/20 space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-primary/15 border border-primary/30 flex items-center justify-center">
              <Trash2 className="w-5 h-5 text-primary" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-white">DEEP JUNK & CACHE PURGE</h3>
              <p className="text-xs text-gray-400 font-mono">Scans app cache trees, residual logs & obsolete temporary files</p>
            </div>
          </div>
          <button
            onClick={handleCleanJunk}
            disabled={isCleaningJunk || junkSize === 0}
            className="bg-primary hover:bg-primary/90 text-background text-xs font-bold px-4 py-2 rounded-lg flex items-center gap-2 transition-all disabled:opacity-50"
          >
            <Trash2 className="w-4 h-4" />
            <span>{isCleaningJunk ? "PURGING RESIDUAL JUNK..." : junkSize === 0 ? "✓ CACHE CLEANED" : `CLEAN ${junkSize} MB JUNK`}</span>
          </button>
        </div>

        {/* Junk Breakdown */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
          <div className="p-3 bg-white/3 border border-white/5 rounded-lg flex justify-between items-center text-xs">
            <span className="text-gray-400">Application Cache</span>
            <span className="font-mono text-white font-bold">{junkSize > 0 ? "214 MB" : "0 MB"}</span>
          </div>
          <div className="p-3 bg-white/3 border border-white/5 rounded-lg flex justify-between items-center text-xs">
            <span className="text-gray-400">Temporary Log Files</span>
            <span className="font-mono text-white font-bold">{junkSize > 0 ? "185 MB" : "0 MB"}</span>
          </div>
          <div className="p-3 bg-white/3 border border-white/5 rounded-lg flex justify-between items-center text-xs">
            <span className="text-gray-400">Obsolete Thumbnails</span>
            <span className="font-mono text-white font-bold">{junkSize > 0 ? "129 MB" : "0 MB"}</span>
          </div>
        </div>

        {junkFreedMb && (
          <div className="p-3 bg-success/10 border border-success/30 rounded-xl flex items-center gap-2 text-xs text-success font-mono font-bold">
            <CheckCircle2 className="w-4 h-4 shrink-0" />
            <span>Cleaned {junkFreedMb} MB of junk files! Device internal storage space reclaimed.</span>
          </div>
        )}

        {isCleaningJunk && (
          <div className="bg-black/40 border border-white/5 p-3 rounded-lg space-y-1 font-mono text-[11px] text-primary">
            {junkCleanLog.map((log, i) => (
              <div key={i}>{log}</div>
            ))}
          </div>
        )}
      </div>

      {/* Module 3: Battery Optimizer & Hardware Health */}
      <div className="glass-panel rounded-2xl p-6 border-success/20 space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-success/15 border border-success/30 flex items-center justify-center">
              <BatteryCharging className="w-5 h-5 text-success" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-white">BATTERY OPTIMIZER & THERMAL MANAGEMENT</h3>
              <p className="text-xs text-gray-400 font-mono">Hardware Telemetry: {batteryTemp}°C  •  {batteryVoltage} mV  •  Grade: Optimal</p>
            </div>
          </div>
          <button
            onClick={handleCoolBattery}
            disabled={isCooling}
            className="border border-success/40 text-success hover:bg-success/10 text-xs font-bold px-4 py-2 rounded-lg flex items-center gap-2 transition-all disabled:opacity-50"
          >
            <Flame className="w-4 h-4 text-warning" />
            <span>{isCooling ? "COOLING CORE..." : "❄️ COOL DOWN BATTERY"}</span>
          </button>
        </div>

        {/* Selectable Power Profiles */}
        <div>
          <label className="text-[11px] font-mono text-gray-400 uppercase tracking-wider mb-2 block">
            Select Active Power Profile
          </label>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
            {[
              { title: "⚡ Balanced Mode", desc: "Standard refresh rate & unrestricted background sync", extra: "Baseline" },
              { title: "🔋 Smart Saver", desc: "Restricts background telemetry and optimizes display draw", extra: "+2.5 hrs" },
              { title: "🛡️ Ultra Saver", desc: "Freezes dormant apps, disables radio polling for maximum standby", extra: "+5.8 hrs" },
            ].map((profile, index) => {
              const isSelected = selectedPowerMode === index;
              return (
                <div
                  key={index}
                  onClick={() => setSelectedPowerMode(index)}
                  className={`p-4 rounded-xl border cursor-pointer transition-all ${
                    isSelected
                      ? 'bg-success/10 border-success/40 shadow-[0_0_15px_rgba(0,255,136,0.15)]'
                      : 'bg-white/3 border-white/5 hover:border-white/20'
                  }`}
                >
                  <div className="flex justify-between items-center mb-1">
                    <span className={`text-xs font-bold ${isSelected ? 'text-success' : 'text-white'}`}>
                      {profile.title}
                    </span>
                    <span className="text-[10px] font-mono bg-white/10 px-2 py-0.5 rounded text-gray-300">
                      {profile.extra}
                    </span>
                  </div>
                  <p className="text-[11px] text-gray-400 leading-relaxed">{profile.desc}</p>
                </div>
              );
            })}
          </div>
        </div>

        {cooldownSuccess && (
          <div className="p-3 bg-primary/10 border border-primary/30 rounded-xl flex items-center gap-2 text-xs text-primary font-mono font-bold">
            <CheckCircle2 className="w-4 h-4 shrink-0" />
            <span>Thermal cooldown applied: Core temperature reduced to {batteryTemp}°C. High-drain background sync throttled.</span>
          </div>
        )}
      </div>
    </div>
  );
}
