// Central API configuration - single source of truth for backend URL
export const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "https://sentinel-backend-production-16ff.up.railway.app";

export const apiUrl = (path: string) => `${API_BASE_URL}${path}`;
