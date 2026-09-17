import { initializeApp, getApps } from "firebase/app";
import { getAuth } from "firebase/auth";

// Firebase web app configuration for project: sentinelai-9d573
const firebaseConfig = {
  apiKey: "AIzaSyBdceUWJWnVWPjsxnxO0xldgJM4wC3CmIw",
  authDomain: "sentinelai-9d573.firebaseapp.com",
  projectId: "sentinelai-9d573",
  storageBucket: "sentinelai-9d573.firebasestorage.app",
  messagingSenderId: "183218626318",
  appId: process.env.NEXT_PUBLIC_FIREBASE_APP_ID || "1:183218626318:web:e8f73916c85311dd91815f",
  measurementId: "G-QEWGKFDRBD"
};

// Initialize Firebase App (singleton pattern)
const app = getApps().length === 0 ? initializeApp(firebaseConfig) : getApps()[0];

// Initialize and export Auth
export const auth = getAuth(app);
export default app;
