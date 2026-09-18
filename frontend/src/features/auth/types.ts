import type { UserRole } from "@/store/authStore";

export type AuthResponseData = {
  accessToken: string;
  refreshToken: string;
  email: string;
  role: UserRole;
  fullName: string;
  accessTokenExpiry?: number;
  refreshTokenExpiry?: number;
};

export type LoginPayload = {
  email: string;
  password: string;
};

export type RegisterPayload = {
  email: string;
  password: string;
  fullName: string;
};
