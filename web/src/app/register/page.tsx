"use client";

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { Shield, Mail, Lock, User, AlertCircle, ArrowRight, Eye, EyeOff, CheckCircle2 } from 'lucide-react';
import { useAuthStore } from '@/lib/store';
import { auth as firebaseAuth } from '@/lib/firebase';
import { createUserWithEmailAndPassword, updateProfile } from 'firebase/auth';
import { apiUrl } from '@/lib/api';

function getFirebaseErrorMessage(code: string): string {
  switch (code) {
    case 'auth/email-already-in-use':
      return 'An account with this email already exists. Please login instead.';
    case 'auth/invalid-email':
      return 'Invalid email address format.';
    case 'auth/weak-password':
      return 'Password is too weak. Use at least 8 characters with letters and numbers.';
    case 'auth/operation-not-allowed':
      return 'Email/Password sign-up is not enabled. Contact the administrator.';
    case 'auth/network-request-failed':
      return 'Network error. Check your internet connection.';
    case 'auth/too-many-requests':
      return 'Too many requests. Please wait a moment and try again.';
    default:
      return 'Registration failed. Please try again.';
  }
}

const passwordStrength = (pwd: string) => {
  let score = 0;
  if (pwd.length >= 8) score++;
  if (pwd.length >= 12) score++;
  if (/[A-Z]/.test(pwd)) score++;
  if (/[0-9]/.test(pwd)) score++;
  if (/[^A-Za-z0-9]/.test(pwd)) score++;
  return score;
};

export default function RegisterPage() {
  const router = useRouter();
  const { setAuth, isAuthenticated } = useAuthStore();
  const [formData, setFormData] = useState({ fullName: '', email: '', password: '', confirm: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    if (isAuthenticated) router.push('/dashboard');
  }, [isAuthenticated, router]);

  const strength = passwordStrength(formData.password);
  const strengthLabel = ['', 'Weak', 'Fair', 'Good', 'Strong', 'Excellent'][strength];
  const strengthColor = ['', 'text-danger', 'text-warning', 'text-yellow-400', 'text-success', 'text-primary'][strength];
  const strengthBarColor = ['', 'bg-danger', 'bg-warning', 'bg-yellow-400', 'bg-success', 'bg-primary'][strength];

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (formData.password !== formData.confirm) {
      setError('Passwords do not match. Please re-enter your new password.');
      return;
    }
    if (formData.password.length < 6) {
      setError('Password must be at least 6 characters long.');
      return;
    }

    setLoading(true);
    try {
      // Step 1 — Create Firebase account
      const userCredential = await createUserWithEmailAndPassword(
        firebaseAuth,
        formData.email,
        formData.password
      );

      // Step 2 — Set display name
      await updateProfile(userCredential.user, {
        displayName: formData.fullName,
      });

      const idToken = await userCredential.user.getIdToken();

      // Step 3 — Sync with backend (non-blocking)
      let backendToken = idToken;
      let role = 'OPERATOR';
      try {
        const response = await fetch(apiUrl('/api/auth/verify'), {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ id_token: idToken }),
        });
        const data = await response.json();
        if (response.ok) {
          backendToken = data.access_token;
          role = data.role || 'OPERATOR';
        }
      } catch {
        // Backend offline — continue with Firebase token
      }

      // Step 4 — Store auth + redirect
      setSuccess(true);
      setAuth(backendToken, role, formData.email);

      setTimeout(() => router.push('/dashboard'), 1200);

    } catch (err: any) {
      const code = err?.code || '';
      setError(getFirebaseErrorMessage(code));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="relative min-h-screen bg-background text-white flex items-center justify-center px-4 py-12 cyber-grid">
      {/* Ambient glow */}
      <div className="absolute top-[15%] left-[15%] w-[400px] h-[400px] bg-secondary opacity-8 rounded-full blur-[120px] pointer-events-none" />
      <div className="absolute bottom-[10%] right-[10%] w-[300px] h-[300px] bg-primary opacity-8 rounded-full blur-[100px] pointer-events-none" />

      <div className="w-full max-w-md glass-panel p-8 rounded-2xl border-white/8 relative z-10">
        {/* Header */}
        <div className="flex flex-col items-center mb-8">
          <Link href="/">
            <div className="relative mb-3 cursor-pointer group">
              <div className="absolute inset-0 bg-secondary/20 rounded-full blur-xl group-hover:bg-secondary/30 transition-all" />
              <Shield className="w-14 h-14 text-primary drop-shadow-[0_0_12px_#00e5ff] relative z-10 group-hover:scale-110 transition-transform" />
            </div>
          </Link>
          <h1 className="text-2xl font-extrabold tracking-wider">CREATE ACCESS PORTAL</h1>
          <p className="text-xs text-gray-500 mt-1 uppercase tracking-widest">Register with Sentinel Shield</p>
        </div>

        {/* Success state */}
        {success && (
          <div className="mb-5 p-4 rounded-xl border border-success/30 bg-success/8 flex items-center gap-3">
            <CheckCircle2 className="w-5 h-5 text-success shrink-0" />
            <div>
              <p className="text-sm text-success font-bold">Account created successfully!</p>
              <p className="text-xs text-success/70">Redirecting to your dashboard...</p>
            </div>
          </div>
        )}

        {/* Error Banner */}
        {error && (
          <div className="mb-5 p-4 rounded-xl border border-danger/30 bg-danger/8 flex items-start gap-3">
            <AlertCircle className="w-5 h-5 text-danger shrink-0 mt-0.5" />
            <div>
              <p className="text-sm text-danger font-semibold">{error}</p>
              {error.includes('already exists') && (
                <Link href="/login" className="text-xs text-primary hover:underline font-bold mt-1 inline-block">
                  → Login instead
                </Link>
              )}
            </div>
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Full Name */}
          <div className="space-y-1.5">
            <label className="text-xs font-bold text-gray-400 uppercase tracking-wider">Full Name</label>
            <div className="relative">
              <User className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-500" />
              <input
                type="text"
                required
                value={formData.fullName}
                onChange={e => setFormData({ ...formData, fullName: e.target.value })}
                placeholder="Your full name"
                className="w-full bg-white/3 border border-white/10 rounded-xl pl-11 pr-4 py-3 text-sm text-gray-200 focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/50 transition-colors placeholder:text-gray-600"
              />
            </div>
          </div>

          {/* Email */}
          <div className="space-y-1.5">
            <label className="text-xs font-bold text-gray-400 uppercase tracking-wider">Email Address</label>
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
            <label className="text-xs font-bold text-gray-400 uppercase tracking-wider">Password</label>
            <div className="relative">
              <Lock className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-500" />
              <input
                type={showPassword ? 'text' : 'password'}
                required
                value={formData.password}
                onChange={e => setFormData({ ...formData, password: e.target.value })}
                placeholder="Min 6 characters"
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
            {/* Password strength bar */}
            {formData.password.length > 0 && (
              <div className="space-y-1">
                <div className="flex gap-1">
                  {[1, 2, 3, 4, 5].map(i => (
                    <div
                      key={i}
                      className={`h-1 flex-1 rounded-full transition-all duration-300 ${i <= strength ? strengthBarColor : 'bg-white/10'}`}
                    />
                  ))}
                </div>
                <p className={`text-[11px] font-semibold ${strengthColor}`}>
                  Password strength: {strengthLabel}
                </p>
              </div>
            )}
          </div>

          {/* Confirm Password */}
          <div className="space-y-1.5">
            <label className="text-xs font-bold text-gray-400 uppercase tracking-wider">Confirm Password</label>
            <div className="relative">
              <Lock className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-500" />
              <input
                type={showPassword ? 'text' : 'password'}
                required
                value={formData.confirm}
                onChange={e => setFormData({ ...formData, confirm: e.target.value })}
                placeholder="Re-enter password"
                className={`w-full bg-white/3 border rounded-xl pl-11 pr-4 py-3 text-sm text-gray-200 focus:outline-none focus:ring-1 font-mono transition-colors placeholder:text-gray-600 ${
                  formData.confirm && formData.confirm !== formData.password
                    ? 'border-danger/50 focus:border-danger focus:ring-danger/50'
                    : formData.confirm && formData.confirm === formData.password
                    ? 'border-success/50 focus:border-success focus:ring-success/50'
                    : 'border-white/10 focus:border-primary focus:ring-primary/50'
                }`}
              />
              {formData.confirm && (
                <div className="absolute right-3.5 top-1/2 -translate-y-1/2">
                  {formData.confirm === formData.password
                    ? <CheckCircle2 className="w-4 h-4 text-success" />
                    : <AlertCircle className="w-4 h-4 text-danger" />
                  }
                </div>
              )}
            </div>
          </div>

          {/* Submit */}
          <button
            type="submit"
            disabled={loading || success}
            className="w-full py-3.5 bg-primary text-background font-extrabold rounded-xl shadow-neon hover:opacity-90 active:scale-[0.98] transition-all flex items-center justify-center gap-2 mt-2 text-sm uppercase tracking-widest disabled:opacity-60 disabled:cursor-not-allowed"
          >
            {loading ? (
              <>
                <span className="w-4 h-4 border-2 border-background/30 border-t-background rounded-full animate-spin" />
                Creating Account...
              </>
            ) : success ? (
              <>
                <CheckCircle2 className="w-4 h-4" />
                Account Created!
              </>
            ) : (
              <>
                Establish Secure Connection
                <ArrowRight className="w-4 h-4" />
              </>
            )}
          </button>
        </form>

        {/* Footer */}
        <div className="mt-6 pt-6 border-t border-white/5 text-center">
          <p className="text-xs text-gray-500">
            Already have an account?{' '}
            <Link href="/login" className="text-primary hover:text-primary/80 font-bold transition-colors">
              Access System Console →
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
