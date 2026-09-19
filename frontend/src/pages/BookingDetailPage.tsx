import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { cancelBooking, fetchBooking } from "@/features/bookings/api";
import { fetchPropertyById } from "@/features/properties/api";
import { RefundPaymentButton } from "@/components/payments/RefundPaymentButton";
import {
  createStripeIntent,
  fetchPaymentByBooking,
} from "@/features/payments/api";
import { formatInr } from "@/lib/format";
import { ApiError } from "@/lib/api/types";
import { useAuthStore } from "@/store/authStore";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link, useNavigate, useParams } from "react-router-dom";
import { useState } from "react";

function statusColor(status: string) {
  switch (status) {
    case "CONFIRMED":
    case "SUCCESS":
    case "SUCCEEDED":
      return "bg-green-100 text-green-800";
    case "PENDING":
      return "bg-amber-100 text-amber-900";
    case "FAILED":
      return "bg-red-100 text-red-800";
    case "CANCELLED":
    case "EXPIRED":
    case "REFUNDED":
      return "bg-stone-100 text-stone-600";
    default:
      return "bg-stone-100 text-stone-700";
  }
}

function formatPaymentMethod(method?: string) {
  if (!method) return "—";
  if (method.toUpperCase() === "STRIPE") return "Card (Stripe)";
  return method;
}

export function BookingDetailPage() {
  const { bookingId } = useParams<{ bookingId: string }>();
  const accessToken = useAuthStore((s) => s.accessToken)!;
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [actionError, setActionError] = useState<string | null>(null);

  const { data: booking, isLoading, error } = useQuery({
    queryKey: ["booking", bookingId],
    queryFn: () => fetchBooking(accessToken, bookingId!),
    enabled: Boolean(bookingId),
  });

  const { data: payment, isLoading: paymentLoading } = useQuery({
    queryKey: ["payment-by-booking", bookingId],
    queryFn: () => fetchPaymentByBooking(accessToken, bookingId!),
    enabled: Boolean(bookingId && booking),
  });

  const { data: property } = useQuery({
    queryKey: ["property", booking?.propertyId],
    queryFn: () => fetchPropertyById(booking!.propertyId),
    enabled: Boolean(booking?.propertyId),
  });

  const cancel = useMutation({
    mutationFn: () => cancelBooking(accessToken, bookingId!),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["booking", bookingId] });
      queryClient.invalidateQueries({ queryKey: ["my-bookings"] });
    },
    onError: (err: unknown) => {
      setActionError(
        err instanceof ApiError ? err.message : "Could not cancel booking.",
      );
    },
  });

  const resumePay = useMutation({
    mutationFn: async () => {
      const intent = await createStripeIntent(accessToken, {
        bookingId: booking!.id,
        hostId: booking!.hostId,
        idempotencyKey: `pay-${booking!.id}`,
      });
      return intent;
    },
    onSuccess: (intent) => {
      setActionError(null);
      navigate(`/bookings/${booking!.id}/pay`, {
        state: { booking, intent },
      });
    },
    onError: (err: unknown) => {
      setActionError(
        err instanceof ApiError ? err.message : "Could not start payment.",
      );
    },
  });

  if (!bookingId) return null;

  if (isLoading) {
    return (
      <div className="mx-auto max-w-2xl px-4 py-16 text-muted">Loading…</div>
    );
  }

  if (error || !booking) {
    return (
      <div className="mx-auto max-w-2xl px-4 py-16 text-center">
        <p className="text-muted">Trip not found or you don&apos;t have access.</p>
        <Link
          to="/bookings/my"
          className="mt-4 inline-block text-brand-700 hover:underline"
        >
          Back to my trips
        </Link>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-2xl px-4 py-10 sm:px-6">
      <Link
        to="/bookings/my"
        className="text-sm font-medium text-brand-700 hover:underline"
      >
        ← My trips
      </Link>

      <div className="mt-4 flex flex-wrap items-start justify-between gap-3">
        <div>
          <h1 className="font-display text-3xl font-semibold">Trip details</h1>
          {property && (
            <p className="mt-1 text-muted">{property.title}</p>
          )}
        </div>
        <span
          className={`inline-block rounded-full px-2.5 py-0.5 text-xs font-medium ${statusColor(booking.status)}`}
        >
          {booking.status}
        </span>
      </div>

      <Card className="mt-8 p-6">
        <h2 className="text-sm font-semibold uppercase tracking-wide text-muted">
          Stay
        </h2>
        <dl className="mt-4 space-y-3 text-sm">
          <div className="flex justify-between gap-4">
            <dt className="text-muted">Check-in</dt>
            <dd className="font-medium text-ink">{booking.checkIn}</dd>
          </div>
          <div className="flex justify-between gap-4">
            <dt className="text-muted">Check-out</dt>
            <dd className="font-medium text-ink">{booking.checkOut}</dd>
          </div>
          <div className="flex justify-between gap-4">
            <dt className="text-muted">Guests</dt>
            <dd className="font-medium text-ink">{booking.totalGuests}</dd>
          </div>
          <div className="flex justify-between gap-4">
            <dt className="text-muted">Total</dt>
            <dd className="font-medium text-ink">
              {formatInr(booking.totalPrice)}
            </dd>
          </div>
          <div className="flex justify-between gap-4">
            <dt className="text-muted">Booking ID</dt>
            <dd className="font-mono text-xs text-ink">{booking.id}</dd>
          </div>
        </dl>
        <Link
          to={`/properties/${booking.propertyId}`}
          className="mt-4 inline-block text-sm font-medium text-brand-700 hover:underline"
        >
          View property
        </Link>
      </Card>

      <Card className="mt-6 p-6">
        <h2 className="text-sm font-semibold uppercase tracking-wide text-muted">
          Payment
        </h2>
        {paymentLoading ? (
          <p className="mt-4 text-sm text-muted">Loading payment…</p>
        ) : payment ? (
          <dl className="mt-4 space-y-3 text-sm">
            <div className="flex justify-between gap-4">
              <dt className="text-muted">Status</dt>
              <dd>
                <span
                  className={`inline-block rounded-full px-2.5 py-0.5 text-xs font-medium ${statusColor(payment.status)}`}
                >
                  {payment.status}
                </span>
              </dd>
            </div>
            <div className="flex justify-between gap-4">
              <dt className="text-muted">Amount</dt>
              <dd className="font-medium text-ink">
                {formatInr(payment.amount)}
                {payment.currency && payment.currency !== "INR"
                  ? ` ${payment.currency}`
                  : ""}
              </dd>
            </div>
            <div className="flex justify-between gap-4">
              <dt className="text-muted">Method</dt>
              <dd className="font-medium text-ink">
                {formatPaymentMethod(payment.paymentMethod)}
              </dd>
            </div>
            {payment.transactionId && (
              <div className="flex justify-between gap-4">
                <dt className="text-muted">Transaction</dt>
                <dd className="max-w-[14rem] truncate font-mono text-xs text-ink">
                  {payment.transactionId}
                </dd>
              </div>
            )}
            {payment.processedAt && (
              <div className="flex justify-between gap-4">
                <dt className="text-muted">Paid at</dt>
                <dd className="text-ink">
                  {new Date(payment.processedAt).toLocaleString()}
                </dd>
              </div>
            )}
            {payment.failureReason && (
              <p className="rounded-lg bg-red-50 px-3 py-2 text-red-800">
                {payment.failureReason}
              </p>
            )}
          </dl>
        ) : (
          <p className="mt-4 text-sm text-muted">
            No payment on file yet. Complete checkout to pay for this trip.
          </p>
        )}
        {booking.status === "CANCELLED" && payment?.status === "REFUNDED" && (
          <p className="mt-4 text-sm text-green-800">
            This payment has been refunded.
          </p>
        )}
        <div className="mt-4">
          <RefundPaymentButton
            accessToken={accessToken}
            bookingId={booking.id}
            bookingStatus={booking.status}
            payment={payment}
            size="md"
          />
        </div>
      </Card>

      {actionError && (
        <p
          className="mt-4 rounded-xl bg-red-50 px-4 py-3 text-sm text-red-700"
          role="alert"
        >
          {actionError}
        </p>
      )}

      {(booking.status === "PENDING" || booking.status === "CONFIRMED") && (
        <div className="mt-6 flex flex-wrap gap-3">
          {booking.status === "PENDING" && (
            <Button
              disabled={resumePay.isPending}
              onClick={() => resumePay.mutate()}
            >
              {resumePay.isPending ? "Loading…" : "Complete payment"}
            </Button>
          )}
          <Button
            variant="outline"
            disabled={cancel.isPending}
            onClick={() => cancel.mutate()}
          >
            {booking.status === "CONFIRMED"
              ? "Cancel trip"
              : "Cancel booking"}
          </Button>
        </div>
      )}
    </div>
  );
}
