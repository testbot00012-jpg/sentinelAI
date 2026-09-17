"use client";

import React, { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Smartphone, ArrowLeft } from 'lucide-react';
import Link from 'next/link';

export default function AppAuditorPage() {
  const router = useRouter();

  useEffect(() => {
    const timer = setTimeout(() => {
      router.push('/dashboard');
    }, 2500);
    return () => clearTimeout(timer);
  }, [router]);

  return (
    <div className="flex flex-col items-center justify-center min-h-[60vh] text-center p-6 space-y-4">
      <div className="w-16 h-16 rounded-2xl bg-primary/10 border border-primary/20 flex items-center justify-center">
        <Smartphone className="w-8 h-8 text-primary" />
      </div>
      <h2 className="text-xl font-bold text-white">Mobile Device Exclusive Feature</h2>
      <p className="text-sm text-gray-400 max-w-md">
        Local package auditing and permission inspection run directly on your Android smartphone via the Sentinel AI Mobile App. Redirecting to Dashboard...
      </p>
      <Link
        href="/dashboard"
        className="flex items-center gap-2 px-4 py-2 rounded-xl bg-primary/20 text-primary border border-primary/30 text-xs font-bold hover:bg-primary/30 transition-all"
      >
        <ArrowLeft className="w-4 h-4" /> Return to Dashboard
      </Link>
    </div>
  );
}
