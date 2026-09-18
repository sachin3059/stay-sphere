import { apiData, apiRequest } from "@/lib/api/client";
import type { ApiResponse } from "@/lib/api/types";
import type { PaymentRecord, StripeIntentData } from "./types";

export async function createStripeIntent(
  token: string,
  body: {
    bookingId: string;
    hostId: string;
    idempotencyKey: string;
  },
) {
  return apiData<StripeIntentData>("/api/payments/stripe/intent", {
    method: "POST",
    body,
    token,
  });
}

export async function confirmStripePayment(token: string, paymentId: string) {
  return apiData<PaymentRecord>(
    `/api/payments/stripe/confirm/${paymentId}`,
    { method: "POST", token },
  );
}

export async function fetchStripeConfig() {
  return apiData<{ publishableKey: string; gateway: string }>(
    "/api/payments/stripe/config",
  );
}

export async function fetchPayment(token: string, paymentId: string) {
  return apiRequest<ApiResponse<PaymentRecord>>(`/api/payments/${paymentId}`, {
    token,
  }).then((r) => r.data);
}
