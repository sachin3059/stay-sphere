import { useAuthStore } from "@/store/authStore";
import type { ReactNode } from "react";
import { Navigate, useLocation } from "react-router-dom";

export function GuestOnlyRoute({ children }: { children: ReactNode }) {
  const accessToken = useAuthStore((s) => s.accessToken);
  const location = useLocation();

  if (accessToken) {
    return <Navigate to="/" replace state={{ from: location }} />;
  }

  return children;
}
