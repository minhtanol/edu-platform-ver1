import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export type Role = 'ADMIN' | 'TEACHER' | 'STUDENT';
type Session = { accessToken: string; refreshToken: string; id: string; email: string; fullName: string; roles: Role[] };
type AuthState = Partial<Session> & { setSession: (session: Session) => void; logout: () => void; hasRole: (role: Role) => boolean };

export const useAuthStore = create<AuthState>()(persist((set, get) => ({
  setSession: (session) => set(session),
  logout: () => set({ accessToken: undefined, refreshToken: undefined, email: undefined, fullName: undefined, roles: undefined }),
  hasRole: (role) => Boolean(get().roles?.includes(role))
}), { name: 'edu-session' }));
