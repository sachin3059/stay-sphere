import { apiData } from "@/lib/api/client";
import type { JoinWaitlistPayload, WaitlistEntry } from "./types";

export async function joinWaitlist(token: string, body: JoinWaitlistPayload) {
  return apiData<WaitlistEntry>("/api/waitlist/join", {
    method: "POST",
    body,
    token,
  });
}

export async function cancelWaitlistEntry(token: string, entryId: string) {
  return apiData<WaitlistEntry>(`/api/waitlist/${entryId}/cancel`, {
    method: "POST",
    token,
  });
}

export async function fetchMyWaitlist(token: string) {
  return apiData<WaitlistEntry[]>("/api/waitlist/my", { token });
}

export async function fetchPropertyWaitlist(
  propertyId: string,
  checkIn: string,
  checkOut: string,
) {
  const qs = new URLSearchParams({ checkIn, checkOut });
  return apiData<WaitlistEntry[]>(
    `/api/waitlist/property/${propertyId}?${qs}`,
  );
}
