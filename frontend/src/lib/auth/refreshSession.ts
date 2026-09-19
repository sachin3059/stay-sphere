import { applyAuthResponse } from "@/features/auth/session";
import type { AuthResponseData } from "@/features/auth/types";
import { getApiBaseUrl } from "@/lib/api/client";
import type { ApiResponse } from "@/lib/api/types";
import { useAuthStore } from "@/store/authStore";

let refreshInFlight: Promise<string | null> | null = null;

export function isAuthRefreshExemptPath(path: string): boolean {
  const normalized = path.startsWith("/") ? path : `/${path}`;
  return (
    normalized.startsWith("/api/auth/login") ||
    normalized.startsWith("/api/auth/register") ||
    normalized.startsWith("/api/auth/refresh")
  );
}

export function getJwtExpiryMs(accessToken: string): number | null {
  try {
    const segment = accessToken.split(".")[1];
    if (!segment) return null;
    const payload = JSON.parse(atob(segment)) as { exp?: number };
    return payload.exp ? payload.exp * 1000 : null;
  } catch {
    return null;
  }
}

export function handleSessionExpired(): void {
  useAuthStore.getState().clearSession();
  const path = window.location.pathname;
  if (path === "/login" || path === "/register") {
    return;
  }
  const params = new URLSearchParams({ session: "expired" });
  window.location.assign(`/login?${params.toString()}`);
}

export async function refreshAccessToken(): Promise<string | null> {
  if (refreshInFlight) {
    return refreshInFlight;
  }

  const refreshToken = useAuthStore.getState().refreshToken;
  if (!refreshToken) {
    handleSessionExpired();
    return null;
  }

  refreshInFlight = (async () => {
    try {
      const response = await fetch(`${getApiBaseUrl()}/api/auth/refresh`, {
        method: "POST",
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ refreshToken }),
      });

      if (!response.ok) {
        handleSessionExpired();
        return null;
      }

      const wrapped = (await response.json()) as ApiResponse<AuthResponseData>;
      applyAuthResponse(wrapped.data);
      return wrapped.data.accessToken;
    } catch {
      handleSessionExpired();
      return null;
    } finally {
      refreshInFlight = null;
    }
  })();

  return refreshInFlight;
}

type AuthedInit = RequestInit & { token?: string | null };

export async function fetchWithAuthRetry(
  url: string,
  init: AuthedInit,
  retried = false,
): Promise<Response> {
  const { token, ...rest } = init;
  const headers = new Headers(rest.headers);
  headers.set("Accept", headers.get("Accept") ?? "application/json");
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  const response = await fetch(url, { ...rest, headers });

  if (
    response.status === 401 &&
    !retried &&
    token &&
    !url.includes("/api/auth/")
  ) {
    const newToken = await refreshAccessToken();
    if (newToken) {
      return fetchWithAuthRetry(url, { ...init, token: newToken }, true);
    }
  }

  return response;
}
