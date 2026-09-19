import type { PaymentRecord } from "./types";

export function canRequestRefund(
  bookingStatus: string,
  payment: PaymentRecord | null | undefined,
): boolean {
  return bookingStatus === "CANCELLED" && payment?.status === "SUCCESS";
}
