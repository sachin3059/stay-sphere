import { useAuthStore } from "@/store/authStore";
import type { AuthResponseData } from "./types";

export function applyAuthResponse(data: AuthResponseData) {
  useAuthStore.getState().setSession({
    accessToken: data.accessToken,
    refreshToken: data.refreshToken,
    email: data.email,
    fullName: data.fullName,
    role: data.role,
  });
}
