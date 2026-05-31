import { initializeApp, getApps } from "firebase/app";
import { getAuth } from "firebase/auth";

// Pre-configured with the user provided google-services credentials
const firebaseConfig = {
  apiKey: process.env.NEXT_PUBLIC_FIREBASE_API_KEY || "AIzaSyAJLM9XjNVihLQMY-x4vs7GCJQYFc7By9U",
  authDomain: process.env.NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN || "senthel-f8ddc.firebaseapp.com",
  projectId: process.env.NEXT_PUBLIC_FIREBASE_PROJECT_ID || "senthel-f8ddc",
  storageBucket: process.env.NEXT_PUBLIC_FIREBASE_STORAGE_BUCKET || "senthel-f8ddc.firebasestorage.app",
  messagingSenderId: process.env.NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID || "1051362851906",
  appId: process.env.NEXT_PUBLIC_FIREBASE_APP_ID || ""
};

// Initialize Firebase App
const app = getApps().length === 0 ? initializeApp(firebaseConfig) : getApps()[0];

// Initialize Auth
export const auth = getAuth(app);
export default app;
