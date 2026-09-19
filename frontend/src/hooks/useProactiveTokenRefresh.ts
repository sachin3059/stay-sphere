import {
  getJwtExpiryMs,
  refreshAccessToken,
} from "@/lib/auth/refreshSession";
import { useAuthStore } from "@/store/authStore";
import { useEffect } from "react";

const CHECK_INTERVAL_MS = 30_000;
const REFRESH_BEFORE_EXPIRY_MS = 2 * 60_000;

export function useProactiveTokenRefresh() {
  const accessToken = useAuthStore((s) => s.accessToken);
  const refreshToken = useAuthStore((s) => s.refreshToken);

  useEffect(() => {
    if (!accessToken || !refreshToken) {
      return;
    }

    const tick = () => {
      const currentAccess = useAuthStore.getState().accessToken;
      const currentRefresh = useAuthStore.getState().refreshToken;
      if (!currentAccess || !currentRefresh) {
        return;
      }
      const expMs = getJwtExpiryMs(currentAccess);
      if (!expMs) {
        return;
      }
      const remaining = expMs - Date.now();
      if (remaining > 0 && remaining <= REFRESH_BEFORE_EXPIRY_MS) {
        void refreshAccessToken();
      }
    };

    tick();
    const id = window.setInterval(tick, CHECK_INTERVAL_MS);
    return () => window.clearInterval(id);
  }, [accessToken, refreshToken]);
}
