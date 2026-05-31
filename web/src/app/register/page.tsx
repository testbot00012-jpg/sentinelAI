"use client";

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { Shield, Mail, Lock, User, AlertCircle, ArrowRight } from 'lucide-react';
import { useAuthStore } from '../../lib/store';
import { auth as firebaseAuth } from '../../lib/firebase';
import { createUserWithEmailAndPassword, updateProfile } from 'firebase/auth';

export default function RegisterPage() {
  const router = useRouter();
  const { setAuth, isAuthenticated } = useAuthStore();
  const [formData, setFormData] = useState({ email: '', password: '', fullName: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isAuthenticated) {
      router.push('/dashboard');
    }
  }, [isAuthenticated, router]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      // 1. Register with Firebase Web SDK
      const userCredential = await createUserWithEmailAndPassword(firebaseAuth, formData.email, formData.password);
      
      // Update User profile details in Firebase identity metadata
      await updateProfile(userCredential.user, {
        displayName: formData.fullName
      });

      const idToken = await userCredential.user.getIdToken();

      // 2. Synchronize credentials with FastAPI PostgreSQL database
      const response = await fetch('http://localhost:8000/api/auth/verify', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ id_token: idToken })
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.detail || 'Registration process failed.');
      }

      setAuth(data.access_token, data.role, formData.email);
      router.push('/dashboard');
    } catch (err: any) {
      setError(err.message || 'Server connection failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="relative min-h-screen bg-background text-white flex items-center justify-center px-4 py-12 cyber-grid">
      <div className="absolute top-[20%] left-[20%] w-[350px] h-[350px] bg-secondary opacity-15 rounded-full blur-[100px] pointer-events-none" />
      
      <div className="w-full max-w-md glass-panel p-8 rounded-2xl border-white/5 relative z-10">
        <div className="flex flex-col items-center mb-8">
          <Link href="/">
            <Shield className="w-12 h-12 text-primary drop-shadow-[0_0_8px_#00e5ff] mb-3 cursor-pointer" />
          </Link>
          <h2 className="text-2xl font-extrabold tracking-wider">CREATING ACCESS PORTAL</h2>
          <p className="text-xs text-gray-400 mt-1 uppercase tracking-widest">Register with Sentinel Shield</p>
        </div>

        {error && (
          <div className="mb-6 p-4 rounded bg-danger/10 border border-danger/20 flex items-center gap-3 text-sm text-danger">
            <AlertCircle className="w-5 h-5 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-5">
          <div className="space-y-1.5">
            <label className="text-xs font-bold text-gray-400 uppercase tracking-wider">Full Name</label>
            <div className="relative">
              <User className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4.5 h-4.5 text-gray-500" />
              <input 
                type="text" 
                required
                value={formData.fullName}
                onChange={e => setFormData({ ...formData, fullName: e.target.value })}
                placeholder="Agent Name" 
                className="w-full bg-background/80 border border-white/10 rounded-lg pl-11 pr-4 py-3 text-sm text-gray-200 focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary"
              />
            </div>
          </div>

          <div className="space-y-1.5">
            <label className="text-xs font-bold text-gray-400 uppercase tracking-wider">Email Address</label>
            <div className="relative">
              <Mail className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4.5 h-4.5 text-gray-500" />
              <input 
                type="email" 
                required
                value={formData.email}
                onChange={e => setFormData({ ...formData, email: e.target.value })}
                placeholder="agent@sentinel.ai" 
                className="w-full bg-background/80 border border-white/10 rounded-lg pl-11 pr-4 py-3 text-sm text-gray-200 focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary"
              />
            </div>
          </div>

          <div className="space-y-1.5">
            <label className="text-xs font-bold text-gray-400 uppercase tracking-wider">Access Password</label>
            <div className="relative">
              <Lock className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4.5 h-4.5 text-gray-500" />
              <input 
                type="password" 
                required
                value={formData.password}
                onChange={e => setFormData({ ...formData, password: e.target.value })}
                placeholder="••••••••" 
                className="w-full bg-background/80 border border-white/10 rounded-lg pl-11 pr-4 py-3 text-sm text-gray-200 focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary"
              />
            </div>
          </div>

          <button 
            type="submit" 
            disabled={loading}
            className="w-full py-3.5 bg-primary text-background font-bold rounded-lg shadow-neon hover:opacity-90 transition-opacity flex items-center justify-center gap-2 mt-4 text-sm uppercase tracking-wider"
          >
            {loading ? 'Initializing Agent...' : 'Establish Secure Connection'} 
            {!loading && <ArrowRight className="w-4 h-4" />}
          </button>
        </form>

        <p className="mt-8 text-center text-xs text-gray-400">
          Already registered?{' '}
          <Link href="/login" className="text-primary hover:underline font-semibold">
            Access System Console
          </Link>
        </p>
      </div>
    </div>
  );
}

