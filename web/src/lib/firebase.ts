import { initializeApp, getApps } from "firebase/app";
import { getAuth } from "firebase/auth";

// Firebase web app configuration for project: senthel-f8ddc
// appId is required — get it from Firebase Console > Project Settings > Your Apps (Web)
const firebaseConfig = {
  apiKey: "AIzaSyAJLM9XjNVihLQMY-x4vs7GCJQYFc7By9U",
  authDomain: "senthel-f8ddc.firebaseapp.com",
  projectId: "senthel-f8ddc",
  storageBucket: "senthel-f8ddc.firebasestorage.app",
  messagingSenderId: "1051362851906",
  // Web App ID — fetched from Firebase Console > Project Settings > Web App
  appId: process.env.NEXT_PUBLIC_FIREBASE_APP_ID || "1:1051362851906:web:sentinel-web",
};

// Initialize Firebase App (singleton pattern)
const app = getApps().length === 0 ? initializeApp(firebaseConfig) : getApps()[0];

// Initialize and export Auth
export const auth = getAuth(app);
export default app;
