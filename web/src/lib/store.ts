import { create } from 'zustand';

interface UserInfo {
  email: string;
  role: string;
}

interface AuthState {
  token: string | null;
  user: UserInfo | null;
  isAuthenticated: boolean;
  setAuth: (token: string, role: string, email: string) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>((set) => {
  // Safe SSR checks
  const isClient = typeof window !== 'undefined';
  const initialToken = isClient ? localStorage.getItem('sentinel_token') : null;
  const initialEmail = isClient ? localStorage.getItem('sentinel_email') : null;
  const initialRole = isClient ? localStorage.getItem('sentinel_role') : null;

  return {
    token: initialToken,
    user: initialToken && initialEmail && initialRole ? { email: initialEmail, role: initialRole } : null,
    isAuthenticated: !!initialToken,
    setAuth: (token, role, email) => {
      if (isClient) {
        localStorage.setItem('sentinel_token', token);
        localStorage.setItem('sentinel_email', email);
        localStorage.setItem('sentinel_role', role);
      }
      set({ token, user: { email, role }, isAuthenticated: true });
    },
    logout: () => {
      if (isClient) {
        localStorage.removeItem('sentinel_token');
        localStorage.removeItem('sentinel_email');
        localStorage.removeItem('sentinel_role');
      }
      set({ token: null, user: null, isAuthenticated: false });
    }
  };
});
