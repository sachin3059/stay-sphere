import { create } from "zustand";
import { persist } from "zustand/middleware";

export type UserRole = "GUEST" | "HOST" | "ADMIN";

export type AuthUser = {
  email: string;
  fullName: string;
  role: UserRole;
};

type AuthState = {
  accessToken: string | null;
  refreshToken: string | null;
  user: AuthUser | null;
  setSession: (payload: {
    accessToken: string;
    refreshToken: string;
    email: string;
    fullName: string;
    role: UserRole;
  }) => void;
  clearSession: () => void;
  isAuthenticated: () => boolean;
};

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      accessToken: null,
      refreshToken: null,
      user: null,
      setSession: ({
        accessToken,
        refreshToken,
        email,
        fullName,
        role,
      }) =>
        set({
          accessToken,
          refreshToken,
          user: { email, fullName, role },
        }),
      clearSession: () =>
        set({ accessToken: null, refreshToken: null, user: null }),
      isAuthenticated: () => Boolean(get().accessToken),
    }),
    { name: "staysphere-auth" },
  ),
);
