import { apiData } from "@/lib/api/client";
import type { AppNotification } from "./types";

export async function fetchMyNotifications(token: string) {
  return apiData<AppNotification[]>("/api/notifications/my", { token });
}
