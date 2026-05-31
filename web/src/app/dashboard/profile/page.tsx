"use client";

import React, { useState } from 'react';
import {
  User, Shield, Mail, Lock, CheckCircle2, Edit3, Save, X,
  Activity, AlertOctagon, Terminal, Clock, Zap, Award,
  TrendingUp, Bell, Eye, EyeOff, LogOut, Cpu, Server
} from 'lucide-react';
import { useAuthStore } from '@/lib/store';
import { useRouter } from 'next/navigation';
import { auth as firebaseAuth } from '@/lib/firebase';
import { updatePassword, EmailAuthProvider, reauthenticateWithCredential } from 'firebase/auth';

export default function ProfilePage() {
  const router = useRouter();
  const { user, logout, token } = useAuthStore();
  const [editMode, setEditMode] = useState(false);
  const [showPass, setShowPass] = useState(false);
  const [passSection, setPassSection] = useState(false);
  const [passwordForm, setPasswordForm] = useState({ current: '', next: '', confirm: '' });
  const [passError, setPassError] = useState('');
  const [passSuccess, setPassSuccess] = useState('');
  const [passLoading, setPassLoading] = useState(false);
  const [notifications, setNotifications] = useState({
    threats: true,
    weekly: true,
    scan: false,
    newsletter: false,
  });

  const agentName = user?.email?.split('@')[0] || 'Agent';
  const joinDate = 'May 2026';

  const stats = [
    { label: 'Total Scans', value: '0', icon: Terminal, color: 'text-primary' },
    { label: 'Threats Blocked', value: '0', icon: AlertOctagon, color: 'text-danger' },
    { label: 'Security Score', value: '0%', icon: Shield, color: 'text-success' },
    { label: 'Days Active', value: '0', icon: Clock, color: 'text-secondary' },
  ];

  const badges = [
    { name: 'First Scan', icon: '🔍', desc: 'Completed your first URL scan', earned: true },
    { name: 'Threat Hunter', icon: '🛡️', desc: 'Blocked 100+ threats', earned: true },
    { name: 'SMS Guardian', icon: '📱', desc: 'Analyzed 50 SMS messages', earned: false },
    { name: 'Elite Agent', icon: '⚡', desc: 'Maintain 90%+ score for 30 days', earned: false },
  ];

  const handlePasswordChange = async (e: React.FormEvent) => {
    e.preventDefault();
    setPassError('');
    setPassSuccess('');

    if (passwordForm.next !== passwordForm.confirm) {
      setPassError('New passwords do not match.');
      return;
    }
    if (passwordForm.next.length < 8) {
      setPassError('Password must be at least 8 characters.');
      return;
    }

    setPassLoading(true);
    try {
      const currentUser = firebaseAuth.currentUser;
      if (!currentUser || !user?.email) throw new Error('Not authenticated');
      const credential = EmailAuthProvider.credential(user.email, passwordForm.current);
      await reauthenticateWithCredential(currentUser, credential);
      await updatePassword(currentUser, passwordForm.next);
      setPassSuccess('Password updated successfully!');
      setPasswordForm({ current: '', next: '', confirm: '' });
      setPassSection(false);
    } catch (err: any) {
      setPassError(err.message?.includes('wrong-password')
        ? 'Current password is incorrect.'
        : err.message || 'Failed to update password.');
    } finally {
      setPassLoading(false);
    }
  };

  const handleLogout = () => {
    logout();
    router.push('/');
  };

  return (
    <div className="max-w-5xl mx-auto space-y-6">
      {/* Header */}
      <div>
        <h2 className="text-2xl font-extrabold flex items-center gap-3">
          <User className="w-7 h-7 text-warning drop-shadow-[0_0_8px_#f59e0b]" />
          Agent Profile
        </h2>
        <p className="text-sm text-gray-400 mt-1">Manage your Sentinel AI account, security settings, and agent statistics.</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left: Agent Card */}
        <div className="space-y-4">
          {/* Identity Card */}
          <div className="glass-panel rounded-2xl p-6 border-white/5 text-center relative overflow-hidden">
            <div className="absolute inset-0 bg-gradient-to-br from-primary/5 via-transparent to-secondary/5 pointer-events-none" />
            {/* Avatar */}
            <div className="relative inline-flex mb-4">
              <div className="w-20 h-20 rounded-full bg-gradient-to-br from-primary to-secondary flex items-center justify-center text-background font-extrabold text-3xl shadow-neon">
                {agentName[0]?.toUpperCase()}
              </div>
              <div className="absolute -bottom-1 -right-1 w-6 h-6 rounded-full bg-success border-2 border-background flex items-center justify-center">
                <CheckCircle2 className="w-3.5 h-3.5 text-background" />
              </div>
            </div>
            <h3 className="text-lg font-extrabold text-white capitalize">{agentName}</h3>
            <p className="text-xs text-gray-500 font-mono mt-0.5">{user?.email}</p>
            <div className="mt-3 flex items-center justify-center gap-2">
              <span className="px-3 py-1 bg-primary/15 text-primary border border-primary/30 rounded-full text-xs font-bold">
                {user?.role || 'OPERATOR'}
              </span>
              <span className="px-3 py-1 bg-success/15 text-success border border-success/30 rounded-full text-xs font-bold">
                Active
              </span>
            </div>
            <div className="mt-4 pt-4 border-t border-white/5 flex items-center justify-center gap-2 text-xs text-gray-500">
              <Clock className="w-3.5 h-3.5" />
              <span>Member since {joinDate}</span>
            </div>
          </div>

          {/* Threat Engines */}
          <div className="glass-panel rounded-2xl p-5 border-white/5">
            <div className="flex items-center gap-2 mb-4">
              <Server className="w-4 h-4 text-primary" />
              <h4 className="text-xs font-bold text-gray-400 uppercase tracking-widest">Connected Engines</h4>
            </div>
            <div className="space-y-3">
              {[
                { name: 'URL Classifier', status: 'Online', health: 98, color: 'text-primary' },
                { name: 'NLP SMS Engine', status: 'Online', health: 94, color: 'text-secondary' },
                { name: 'APK Risk Auditor', status: 'Online', health: 87, color: 'text-success' },
                { name: 'IP Reputation DB', status: 'Syncing', health: 72, color: 'text-warning' },
              ].map((engine) => (
                <div key={engine.name}>
                  <div className="flex justify-between items-center text-xs mb-1">
                    <span className="text-gray-400">{engine.name}</span>
                    <span className={`font-bold ${engine.status === 'Online' ? 'text-success' : 'text-warning'}`}>
                      {engine.status}
                    </span>
                  </div>
                  <div className="h-1.5 bg-white/5 rounded-full overflow-hidden">
                    <div
                      className={`h-full rounded-full bg-gradient-to-r ${engine.color === 'text-primary' ? 'from-primary to-cyan-400' : engine.color === 'text-secondary' ? 'from-secondary to-purple-400' : engine.color === 'text-success' ? 'from-success to-green-400' : 'from-warning to-yellow-400'}`}
                      style={{ width: `${engine.health}%` }}
                    />
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Danger Zone */}
          <div className="glass-panel rounded-2xl p-5 border-danger/20 bg-danger/3">
            <h4 className="text-xs font-bold text-danger uppercase tracking-widest mb-3">Danger Zone</h4>
            <button
              onClick={handleLogout}
              className="w-full py-2.5 flex items-center justify-center gap-2 text-sm text-danger border border-danger/30 rounded-lg hover:bg-danger/10 transition-colors font-bold"
            >
              <LogOut className="w-4 h-4" />
              Logout from Console
            </button>
          </div>
        </div>

        {/* Right: Details */}
        <div className="lg:col-span-2 space-y-5">
          {/* Activity Stats */}
          <div className="grid grid-cols-2 gap-3">
            {stats.map((s, i) => (
              <div key={i} className="glass-panel p-4 rounded-xl border-white/5 flex items-center gap-3">
                <s.icon className={`w-8 h-8 ${s.color} shrink-0`} />
                <div>
                  <div className={`text-xl font-extrabold ${s.color}`}>{s.value}</div>
                  <div className="text-[11px] text-gray-500 uppercase tracking-wider">{s.label}</div>
                </div>
              </div>
            ))}
          </div>

          {/* Account Info */}
          <div className="glass-panel rounded-2xl p-6 border-white/5 space-y-4">
            <div className="flex items-center justify-between">
              <h4 className="font-bold text-sm flex items-center gap-2">
                <User className="w-4 h-4 text-primary" /> Account Information
              </h4>
            </div>
            <div className="space-y-3">
              <div className="flex items-center gap-3 p-3 bg-white/3 rounded-lg border border-white/5">
                <Mail className="w-4 h-4 text-gray-500 shrink-0" />
                <div className="flex-1">
                  <p className="text-[10px] text-gray-600 uppercase tracking-wider">Email</p>
                  <p className="text-sm text-gray-200 font-mono">{user?.email || 'N/A'}</p>
                </div>
              </div>
              <div className="flex items-center gap-3 p-3 bg-white/3 rounded-lg border border-white/5">
                <Shield className="w-4 h-4 text-gray-500 shrink-0" />
                <div className="flex-1">
                  <p className="text-[10px] text-gray-600 uppercase tracking-wider">Access Level</p>
                  <p className="text-sm text-primary font-bold">{user?.role || 'OPERATOR'}</p>
                </div>
              </div>
              <div className="flex items-center gap-3 p-3 bg-white/3 rounded-lg border border-white/5">
                <Zap className="w-4 h-4 text-gray-500 shrink-0" />
                <div className="flex-1">
                  <p className="text-[10px] text-gray-600 uppercase tracking-wider">Account Status</p>
                  <p className="text-sm text-success font-bold flex items-center gap-1.5">
                    <span className="w-2 h-2 rounded-full bg-success inline-block animate-pulse" />
                    Active & Verified
                  </p>
                </div>
              </div>
            </div>
          </div>

          {/* Change Password */}
          <div className="glass-panel rounded-2xl p-6 border-white/5 space-y-4">
            <div className="flex items-center justify-between">
              <h4 className="font-bold text-sm flex items-center gap-2">
                <Lock className="w-4 h-4 text-secondary" /> Security Settings
              </h4>
              <button
                onClick={() => setPassSection(!passSection)}
                className="text-xs text-secondary hover:underline font-semibold flex items-center gap-1"
              >
                {passSection ? <><X className="w-3 h-3" /> Cancel</> : <><Edit3 className="w-3 h-3" /> Change Password</>}
              </button>
            </div>

            {passSuccess && (
              <div className="p-3 bg-success/10 border border-success/30 rounded-lg text-xs text-success font-semibold flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4" /> {passSuccess}
              </div>
            )}

            {passSection && (
              <form onSubmit={handlePasswordChange} className="space-y-3">
                {passError && (
                  <div className="p-3 bg-danger/10 border border-danger/30 rounded-lg text-xs text-danger">{passError}</div>
                )}
                {[
                  { key: 'current', label: 'Current Password', placeholder: '••••••••' },
                  { key: 'next', label: 'New Password', placeholder: 'Min 8 characters' },
                  { key: 'confirm', label: 'Confirm New Password', placeholder: '••••••••' },
                ].map(({ key, label, placeholder }) => (
                  <div key={key}>
                    <label className="text-[11px] text-gray-500 uppercase tracking-widest font-bold">{label}</label>
                    <div className="relative mt-1">
                      <input
                        type={showPass ? 'text' : 'password'}
                        placeholder={placeholder}
                        value={passwordForm[key as keyof typeof passwordForm]}
                        onChange={e => setPasswordForm({ ...passwordForm, [key]: e.target.value })}
                        className="w-full bg-background/80 border border-white/10 rounded-lg px-4 py-2.5 text-sm text-gray-200 focus:outline-none focus:border-secondary pr-10"
                        required
                      />
                      {key === 'next' && (
                        <button
                          type="button"
                          onClick={() => setShowPass(!showPass)}
                          className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 hover:text-white"
                        >
                          {showPass ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                        </button>
                      )}
                    </div>
                  </div>
                ))}
                <button
                  type="submit"
                  disabled={passLoading}
                  className="w-full py-2.5 bg-secondary text-white font-bold text-sm rounded-lg hover:opacity-90 transition-opacity flex items-center justify-center gap-2"
                >
                  {passLoading ? 'Updating...' : <><Save className="w-4 h-4" /> Update Password</>}
                </button>
              </form>
            )}

            {!passSection && (
              <div className="p-3 bg-white/3 rounded-lg border border-white/5 text-xs text-gray-500 flex items-center gap-2">
                <Lock className="w-3.5 h-3.5 text-success" />
                Password last changed: Never (Firebase managed)
              </div>
            )}
          </div>

          {/* Notifications */}
          <div className="glass-panel rounded-2xl p-6 border-white/5 space-y-4">
            <h4 className="font-bold text-sm flex items-center gap-2">
              <Bell className="w-4 h-4 text-warning" /> Notification Preferences
            </h4>
            <div className="space-y-3">
              {[
                { key: 'threats', label: 'Threat Alerts', desc: 'Immediate alerts for blocked threats' },
                { key: 'weekly', label: 'Weekly Security Report', desc: 'Summary digest every Monday' },
                { key: 'scan', label: 'Scan Completion', desc: 'Notify when background scans complete' },
                { key: 'newsletter', label: 'Sentinel Newsletter', desc: 'Product updates and security tips' },
              ].map(({ key, label, desc }) => (
                <div key={key} className="flex items-center justify-between p-3 bg-white/3 rounded-lg border border-white/5">
                  <div>
                    <p className="text-sm font-semibold text-white">{label}</p>
                    <p className="text-xs text-gray-500">{desc}</p>
                  </div>
                  <button
                    onClick={() => setNotifications(prev => ({ ...prev, [key]: !prev[key as keyof typeof prev] }))}
                    className={`relative w-11 h-6 rounded-full transition-colors duration-200 ${notifications[key as keyof typeof notifications] ? 'bg-primary' : 'bg-white/10'}`}
                  >
                    <span
                      className={`absolute top-0.5 left-0.5 w-5 h-5 rounded-full bg-white shadow transition-transform duration-200 ${notifications[key as keyof typeof notifications] ? 'translate-x-5' : 'translate-x-0'}`}
                    />
                  </button>
                </div>
              ))}
            </div>
          </div>

          {/* Achievement Badges */}
          <div className="glass-panel rounded-2xl p-6 border-white/5 space-y-4">
            <h4 className="font-bold text-sm flex items-center gap-2">
              <Award className="w-4 h-4 text-warning" /> Achievement Badges
            </h4>
            <div className="grid grid-cols-2 gap-3">
              {badges.map((badge, i) => (
                <div
                  key={i}
                  className={`p-4 rounded-xl border flex items-center gap-3 transition-all ${badge.earned ? 'border-primary/30 bg-primary/5' : 'border-white/5 bg-white/2 opacity-50 grayscale'}`}
                >
                  <span className="text-2xl">{badge.icon}</span>
                  <div>
                    <p className={`text-xs font-bold ${badge.earned ? 'text-white' : 'text-gray-500'}`}>{badge.name}</p>
                    <p className="text-[10px] text-gray-600 leading-tight">{badge.desc}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
