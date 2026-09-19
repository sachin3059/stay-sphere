import { apiData } from "@/lib/api/client";
import type {
  AvailabilityCheck,
  AvailableRange,
  BlockDatesPayload,
  BlockedDate,
} from "./types";

export async function checkAvailability(
  propertyId: string,
  checkIn: string,
  checkOut: string,
) {
  const qs = new URLSearchParams({ checkIn, checkOut });
  return apiData<AvailabilityCheck>(
    `/api/availability/${propertyId}/check?${qs}`,
  );
}

export async function fetchAvailableRanges(
  propertyId: string,
  from: string,
  to: string,
) {
  const qs = new URLSearchParams({ from, to });
  return apiData<AvailableRange[]>(
    `/api/availability/${propertyId}/ranges?${qs}`,
  );
}

export async function fetchBlockedDates(propertyId: string) {
  return apiData<BlockedDate[]>(`/api/availability/${propertyId}/blocked`);
}

export async function blockPropertyDates(
  token: string,
  payload: BlockDatesPayload,
) {
  return apiData<BlockedDate>("/api/availability/block", {
    method: "POST",
    body: payload,
    token,
  });
}
