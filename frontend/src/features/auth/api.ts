import { apiData, apiRequest } from "@/lib/api/client";
import type { ApiResponse } from "@/lib/api/types";
import type { AuthResponseData, LoginPayload, RegisterPayload } from "./types";

export async function loginRequest(payload: LoginPayload) {
  return apiData<AuthResponseData>("/api/auth/login", {
    method: "POST",
    body: payload,
  });
}

export async function registerRequest(payload: RegisterPayload) {
  return apiData<AuthResponseData>("/api/auth/register", {
    method: "POST",
    body: payload,
  });
}

export async function becomeHostRequest(accessToken: string) {
  return apiData<AuthResponseData>("/api/auth/become-host", {
    method: "POST",
    token: accessToken,
  });
}

export async function googleOAuthRequest(idToken: string) {
  return apiData<AuthResponseData>("/api/auth/oauth/google", {
    method: "POST",
    body: { idToken },
  });
}

export async function githubOAuthRequest(code: string) {
  return apiData<AuthResponseData>("/api/auth/oauth/github", {
    method: "POST",
    body: { code },
  });
}

export async function logoutRequest(refreshToken: string) {
  await apiRequest<ApiResponse<string>>("/api/auth/logout", {
    method: "POST",
    body: { refreshToken },
  });
}
