export type NotificationDeliveryStatus = "PENDING" | "SENT" | "FAILED";

export type NotificationType =
  | "BOOKING_CREATED"
  | "BOOKING_CONFIRMED"
  | "BOOKING_CANCELLED"
  | "PAYMENT_SUCCESS"
  | "PAYMENT_FAILED"
  | "PAYMENT_REFUNDED"
  | "SLOT_OFFERED"
  | "GENERAL";

export interface AppNotification {
  id: string;
  recipientId: string;
  recipientEmail: string;
  subject: string;
  message: string;
  notificationType: NotificationType | string;
  status: NotificationDeliveryStatus;
  referenceId?: string | null;
  createdAt: string;
  sentAt?: string | null;
}
