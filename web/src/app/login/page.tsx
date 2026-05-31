"use client";

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { Shield, Mail, Lock, AlertCircle, ArrowRight, Eye, EyeOff, UserPlus } from 'lucide-react';
import { useAuthStore } from '@/lib/store';
import { auth as firebaseAuth } from '@/lib/firebase';
import { signInWithEmailAndPassword } from 'firebase/auth';
import { apiUrl } from '@/lib/api';

// Map Firebase error codes to friendly messages
function getFirebaseErrorMessage(code: string): { message: string; hint?: string } {
  switch (code) {
    case 'auth/invalid-credential':
    case 'auth/wrong-password':
    case 'auth/user-not-found':
      return {
        message: 'Invalid email or password.',
        hint: 'No account found with these credentials. Please check your details or register a new account.'
      };
    case 'auth/invalid-email':
      return { message: 'Invalid email address format.' };
    case 'auth/user-disabled':
      return { message: 'This account has been disabled. Contact support.' };
    case 'auth/too-many-requests':
      return {
        message: 'Too many failed attempts. Account temporarily locked.',
        hint: 'Please wait a few minutes before trying again, or reset your password.'
      };
    case 'auth/network-request-failed':
      return { message: 'Network error. Check your internet connection.' };
    case 'auth/app-not-authorized':
      return { message: 'Firebase not authorized. Check project configuration.' };
    default:
      return { message: 'Authentication failed. Please try again.' };
  }
}

export default function LoginPage() {
  const router = useRouter();
  const { setAuth, isAuthenticated } = useAuthStore();
  const [formData, setFormData] = useState({ email: '', password: '' });
  const [error, setError] = useState<{ message: string; hint?: string } | null>(null);
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showRegisterSuggestion, setShowRegisterSuggestion] = useState(false);

  useEffect(() => {
    if (isAuthenticated) {
      router.push('/dashboard');
    }
  }, [isAuthenticated, router]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setShowRegisterSuggestion(false);
    setLoading(true);

    try {
      // Step 1 — Sign in via Firebase
      const userCredential = await signInWithEmailAndPassword(
        firebaseAuth,
        formData.email,
        formData.password
      );
      const idToken = await userCredential.user.getIdToken();

      // Step 2 — Sync with backend (non-blocking; fall back gracefully if offline)
      try {
        const response = await fetch(apiUrl('/api/auth/verify'), {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ id_token: idToken }),
        });
        const data = await response.json();
        if (response.ok) {
          setAuth(data.access_token, data.role || 'OPERATOR', formData.email);
          router.push('/dashboard');
          return;
        }
      } catch {
        // Backend offline — use Firebase token directly
      }

      // Fallback: backend offline — still allow login with Firebase token
      setAuth(idToken, 'OPERATOR', formData.email);
      router.push('/dashboard');

    } catch (err: any) {
      const code = err?.code || '';
      const parsed = getFirebaseErrorMessage(code);
      setError(parsed);
      // Show "register instead" suggestion for credential errors
      if (['auth/invalid-credential', 'auth/user-not-found', 'auth/wrong-password'].includes(code)) {
        setShowRegisterSuggestion(true);
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="relative min-h-screen bg-background text-white flex items-center justify-center px-4 py-12 cyber-grid">
      {/* Ambient glow */}
      <div className="absolute top-[15%] right-[15%] w-[400px] h-[400px] bg-primary opacity-8 rounded-full blur-[120px] pointer-events-none" />
      <div className="absolute bottom-[10%] left-[10%] w-[300px] h-[300px] bg-secondary opacity-8 rounded-full blur-[100px] pointer-events-none" />

      <div className="w-full max-w-md glass-panel p-8 rounded-2xl border-white/8 relative z-10">
        {/* Header */}
        <div className="flex flex-col items-center mb-8">
          <Link href="/">
            <div className="relative mb-3 cursor-pointer group">
              <div className="absolute inset-0 bg-primary/20 rounded-full blur-xl group-hover:bg-primary/30 transition-all" />
              <Shield className="w-14 h-14 text-primary drop-shadow-[0_0_12px_#00e5ff] relative z-10 group-hover:scale-110 transition-transform" />
            </div>
          </Link>
          <h1 className="text-2xl font-extrabold tracking-wider">SYSTEM ACCESS PORTAL</h1>
          <p className="text-xs text-gray-500 mt-1 uppercase tracking-widest">Sentinel Security Console</p>
        </div>

        {/* Error Banner */}
        {error && (
          <div className="mb-5 rounded-xl border border-danger/30 bg-danger/8 overflow-hidden">
            <div className="flex items-start gap-3 p-4">
              <AlertCircle className="w-5 h-5 text-danger shrink-0 mt-0.5" />
              <div className="flex-1">
                <p className="text-sm text-danger font-semibold">{error.message}</p>
                {error.hint && (
                  <p className="text-xs text-danger/70 mt-1 leading-relaxed">{error.hint}</p>
                )}
              </div>
            </div>
            {/* Register suggestion strip */}
            {showRegisterSuggestion && (
              <div className="border-t border-danger/20 bg-white/3 px-4 py-3 flex items-center justify-between">
                <span className="text-xs text-gray-400">Don't have an account?</span>
                <Link
                  href="/register"
                  className="flex items-center gap-1.5 text-xs font-bold text-primary hover:text-primary/80 transition-colors"
                >
                  <UserPlus className="w-3.5 h-3.5" />
                  Create Account
                </Link>
              </div>
            )}
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit} className="space-y-5">
          {/* Email */}
          <div className="space-y-1.5">
            <label className="text-xs font-bold text-gray-400 uppercase tracking-wider">
              Access Email
            </label>
            <div className="relative">
              <Mail className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-500" />
              <input
                type="email"
                required
                value={formData.email}
                onChange={e => setFormData({ ...formData, email: e.target.value })}
                placeholder="agent@sentinel.ai"
                className="w-full bg-white/3 border border-white/10 rounded-xl pl-11 pr-4 py-3 text-sm text-gray-200 focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/50 font-mono transition-colors placeholder:text-gray-600"
              />
            </div>
          </div>

          {/* Password */}
          <div className="space-y-1.5">
            <div className="flex justify-between items-center">
              <label className="text-xs font-bold text-gray-400 uppercase tracking-wider">
                Access Password
              </label>
              <button
                type="button"
                className="text-xs text-secondary hover:text-secondary/80 transition-colors font-medium"
              >
                Reset key?
              </button>
            </div>
            <div className="relative">
              <Lock className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-500" />
              <input
                type={showPassword ? 'text' : 'password'}
                required
                value={formData.password}
                onChange={e => setFormData({ ...formData, password: e.target.value })}
                placeholder="••••••••"
                className="w-full bg-white/3 border border-white/10 rounded-xl pl-11 pr-12 py-3 text-sm text-gray-200 focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/50 font-mono transition-colors placeholder:text-gray-600"
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-3.5 top-1/2 -translate-y-1/2 text-gray-500 hover:text-white transition-colors"
              >
                {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
              </button>
            </div>
          </div>

          {/* Submit */}
          <button
            type="submit"
            disabled={loading}
            className="w-full py-3.5 bg-primary text-background font-extrabold rounded-xl shadow-neon hover:opacity-90 active:scale-[0.98] transition-all flex items-center justify-center gap-2 mt-2 text-sm uppercase tracking-widest disabled:opacity-60 disabled:cursor-not-allowed"
          >
            {loading ? (
              <>
                <span className="w-4 h-4 border-2 border-background/30 border-t-background rounded-full animate-spin" />
                Authenticating...
              </>
            ) : (
              <>
                Authenticate Agent Connection
                <ArrowRight className="w-4 h-4" />
              </>
            )}
          </button>
        </form>

        {/* Footer */}
        <div className="mt-8 pt-6 border-t border-white/5 text-center">
          <p className="text-xs text-gray-500">
            New to Sentinel AI?{' '}
            <Link href="/register" className="text-primary hover:text-primary/80 font-bold transition-colors">
              Establish Access Credentials →
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
