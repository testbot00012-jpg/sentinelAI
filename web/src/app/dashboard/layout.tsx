"use client";

import React, { useEffect, useState } from 'react';
import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import {
  Shield, LayoutDashboard, Globe, MessageSquare, Package,
  User, LogOut, Menu, X, ChevronRight, Zap, Bell, Settings
} from 'lucide-react';
import { useAuthStore } from '@/lib/store';

const NAV_ITEMS = [
  { href: '/dashboard', label: 'Overview', icon: LayoutDashboard },
  { href: '/dashboard/scanner', label: 'URL Scanner', icon: Globe },
  { href: '/dashboard/sms', label: 'SMS Analyzer', icon: MessageSquare },
  { href: '/dashboard/auditor', label: 'App Auditor', icon: Package },
  { href: '/dashboard/profile', label: 'Agent Profile', icon: User },
];

export default function DashboardLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();
  const { user, logout, isAuthenticated } = useAuthStore();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [notifications] = useState(3);

  useEffect(() => {
    if (!isAuthenticated) {
      router.push('/login');
    }
  }, [isAuthenticated, router]);

  const handleLogout = () => {
    logout();
    router.push('/');
  };

  if (!isAuthenticated) return null;

  return (
    <div className="min-h-screen bg-background text-white flex">
      {/* Mobile Overlay */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 z-20 bg-black/60 backdrop-blur-sm lg:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* Sidebar */}
      <aside
        className={`fixed top-0 left-0 h-full z-30 w-64 glass-panel border-r border-white/5 flex flex-col transition-transform duration-300 ease-in-out ${
          sidebarOpen ? 'translate-x-0' : '-translate-x-full'
        } lg:translate-x-0 lg:static lg:z-auto`}
      >
        {/* Logo */}
        <div className="p-5 flex items-center justify-between border-b border-white/5">
          <Link href="/" className="flex items-center gap-2 group">
            <Shield className="w-8 h-8 text-primary drop-shadow-[0_0_10px_#00e5ff] group-hover:scale-110 transition-transform" />
            <div>
              <div className="font-extrabold text-sm tracking-widest bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
                SENTINEL AI
              </div>
              <div className="text-[10px] text-gray-500 uppercase tracking-wider">Security Console</div>
            </div>
          </Link>
          <button
            className="lg:hidden text-gray-400 hover:text-white"
            onClick={() => setSidebarOpen(false)}
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Navigation */}
        <nav className="flex-1 p-4 space-y-1 overflow-y-auto">
          <div className="text-[10px] font-bold text-gray-600 uppercase tracking-widest mb-3 px-3">
            Navigation
          </div>
          {NAV_ITEMS.map((item) => {
            const isActive = pathname === item.href;
            return (
              <Link
                key={item.href}
                href={item.href}
                onClick={() => setSidebarOpen(false)}
                className={`flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-semibold transition-all duration-200 group ${
                  isActive
                    ? 'bg-primary/15 text-primary border border-primary/25 shadow-[0_0_15px_rgba(0,229,255,0.1)]'
                    : 'text-gray-400 hover:text-white hover:bg-white/5'
                }`}
              >
                <item.icon className={`w-4.5 h-4.5 shrink-0 ${isActive ? 'text-primary' : 'group-hover:text-primary'} transition-colors`} />
                <span>{item.label}</span>
                {isActive && <ChevronRight className="w-3.5 h-3.5 ml-auto text-primary/60" />}
              </Link>
            );
          })}

          <div className="text-[10px] font-bold text-gray-600 uppercase tracking-widest mt-6 mb-3 px-3">
            System
          </div>
          <div className="px-3 py-3 rounded-lg bg-white/3 border border-white/5">
            <div className="flex items-center gap-2 mb-3">
              <div className="w-2 h-2 rounded-full bg-success animate-pulse" />
              <span className="text-xs text-success font-semibold">Shield Active</span>
            </div>
            <div className="space-y-2">
              {[
                { label: 'Threat Engine', val: 98 },
                { label: 'NLP Scanner', val: 94 },
                { label: 'APK Auditor', val: 87 },
              ].map((engine) => (
                <div key={engine.label}>
                  <div className="flex justify-between text-[10px] text-gray-500 mb-1">
                    <span>{engine.label}</span>
                    <span className="text-primary">{engine.val}%</span>
                  </div>
                  <div className="h-1 bg-white/5 rounded-full overflow-hidden">
                    <div
                      className="h-full bg-gradient-to-r from-primary to-secondary rounded-full"
                      style={{ width: `${engine.val}%` }}
                    />
                  </div>
                </div>
              ))}
            </div>
          </div>
        </nav>

        {/* User Profile */}
        <div className="p-4 border-t border-white/5">
          <div className="flex items-center gap-3 px-3 py-3 rounded-lg bg-white/3 mb-2">
            <div className="w-8 h-8 rounded-full bg-gradient-to-br from-primary to-secondary flex items-center justify-center text-background font-bold text-sm shrink-0">
              {user?.email?.[0]?.toUpperCase() || 'A'}
            </div>
            <div className="flex-1 min-w-0">
              <div className="text-xs font-bold text-white truncate">{user?.email || 'Agent'}</div>
              <div className="text-[10px] text-primary font-mono">{user?.role || 'OPERATOR'}</div>
            </div>
          </div>
          <button
            onClick={handleLogout}
            className="w-full flex items-center gap-2 px-3 py-2 text-xs text-gray-400 hover:text-danger transition-colors rounded-lg hover:bg-danger/5 font-semibold"
          >
            <LogOut className="w-4 h-4" />
            <span>Logout</span>
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <div className="flex-1 flex flex-col min-w-0 lg:ml-0">
        {/* Top Bar */}
        <header className="sticky top-0 z-10 glass-panel border-b border-white/5 px-6 py-3 flex items-center justify-between shrink-0">
          <div className="flex items-center gap-4">
            <button
              className="lg:hidden text-gray-400 hover:text-white p-1"
              onClick={() => setSidebarOpen(true)}
            >
              <Menu className="w-5 h-5" />
            </button>
            <div className="hidden lg:block">
              <h1 className="text-sm font-bold text-white">
                {NAV_ITEMS.find(n => n.href === pathname)?.label || 'Dashboard'}
              </h1>
              <p className="text-[10px] text-gray-500 font-mono">Sentinel AI Control Panel</p>
            </div>
          </div>
          <div className="flex items-center gap-3">
            {/* Live Status */}
            <div className="hidden md:flex items-center gap-2 px-3 py-1.5 rounded-lg bg-success/10 border border-success/20">
              <Zap className="w-3.5 h-3.5 text-success" />
              <span className="text-xs font-bold text-success">Systems Nominal</span>
            </div>
            {/* Notifications */}
            <button className="relative p-2 text-gray-400 hover:text-white transition-colors">
              <Bell className="w-5 h-5" />
              {notifications > 0 && (
                <span className="absolute top-1 right-1 w-4 h-4 bg-danger text-white text-[9px] font-bold rounded-full flex items-center justify-center">
                  {notifications}
                </span>
              )}
            </button>
          </div>
        </header>

        {/* Page Content */}
        <main className="flex-1 overflow-auto p-6">
          {children}
        </main>
      </div>
    </div>
  );
}
