import './globals.css';
import type { Metadata } from 'next';

export const metadata: Metadata = {
  title: 'Sentinel AI | Smart Mobile Security, Fraud Detection & Threat Analytics',
  description: 'Production-ready full stack cybersecurity platforms powering AI Phishing alerts, SMS scam analysis, and APK risk analyzer suite.',
  keywords: 'Cybersecurity, Phishing Scanner, SMS Fraud, APK Malware Analyzer, Sentinel AI Dashboard',
  icons: {
    icon: '/favicon.ico',
  }
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <head>
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link rel="preconnect" href="https://fonts.gstatic.com" crossOrigin="anonymous" />
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&family=Outfit:wght@400;500;600;700;800&display=swap" rel="stylesheet" />
      </head>
      <body className="antialiased">
        {children}
      </body>
    </html>
  );
}
