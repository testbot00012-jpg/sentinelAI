// Central API configuration - single source of truth for backend URL
// Automatically connects to local backend when running on localhost, and falls back to Railway cloud
export const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_URL ||
  (typeof window !== "undefined" && window.location.hostname === "localhost"
    ? "http://localhost:8000"
    : "https://sentinel-backend-production-16ff.up.railway.app");

export const apiUrl = (path: string) => `${API_BASE_URL}${path}`;
