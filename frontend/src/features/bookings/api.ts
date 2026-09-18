import { apiRequest } from "@/lib/api/client";
import type { Booking, CreateBookingPayload } from "./types";

export async function createBooking(
  token: string,
  payload: CreateBookingPayload,
  idempotencyKey: string,
) {
  return apiRequest<Booking>("/api/bookings", {
    method: "POST",
    body: payload,
    token,
    idempotencyKey,
  });
}

export async function fetchMyBookings(token: string) {
  return apiRequest<Booking[]>("/api/bookings/my", { token });
}

export async function fetchPropertyBookings(token: string, propertyId: string) {
  return apiRequest<Booking[]>(`/api/bookings/property/${propertyId}`, {
    token,
  });
}

export async function fetchBooking(token: string, bookingId: string) {
  return apiRequest<Booking>(`/api/bookings/${bookingId}`, { token });
}

export async function confirmBooking(token: string, bookingId: string) {
  return apiRequest<Booking>(`/api/bookings/${bookingId}/confirm`, {
    method: "POST",
    token,
  });
}

export async function cancelBooking(token: string, bookingId: string) {
  return apiRequest<Booking>(`/api/bookings/${bookingId}/cancel`, {
    method: "POST",
    token,
  });
}
