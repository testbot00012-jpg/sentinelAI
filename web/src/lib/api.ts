// Central API configuration - single source of truth for backend URL
export const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "https://sentinelai-6kf0.onrender.com";

export const apiUrl = (path: string) => `${API_BASE_URL}${path}`;
