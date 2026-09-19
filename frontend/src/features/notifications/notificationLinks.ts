import type { AppNotification } from "./types";

/** Best-effort deep links until trip detail (Phase 14) ships. */
export function notificationActionHref(
  n: AppNotification,
  role: "GUEST" | "HOST" | string,
): { to: string; label: string } | null {
  const ref = n.referenceId;
  if (!ref) return null;

  switch (n.notificationType) {
    case "BOOKING_CREATED":
      return { to: `/bookings/${ref}`, label: "View trip" };
    case "BOOKING_CONFIRMED":
      return role === "HOST"
        ? { to: "/host/reservations", label: "View reservations" }
        : { to: `/bookings/${ref}`, label: "View trip" };
    case "BOOKING_CANCELLED":
      return { to: `/bookings/${ref}`, label: "View trip" };
    case "PAYMENT_SUCCESS":
    case "PAYMENT_FAILED":
    case "PAYMENT_REFUNDED":
      return { to: "/bookings/my", label: "View my trips" };
    case "SLOT_OFFERED":
      return { to: "/waitlist/my", label: "View waitlist offer" };
    default:
      return null;
  }
}

export function notificationTypeLabel(type: string): string {
  switch (type) {
    case "BOOKING_CREATED":
      return "Booking request";
    case "BOOKING_CONFIRMED":
      return "Booking confirmed";
    case "BOOKING_CANCELLED":
      return "Booking cancelled";
    case "PAYMENT_SUCCESS":
      return "Payment successful";
    case "PAYMENT_FAILED":
      return "Payment failed";
    case "PAYMENT_REFUNDED":
      return "Refund processed";
    case "SLOT_OFFERED":
      return "Waitlist slot";
    default:
      return "Update";
  }
}
