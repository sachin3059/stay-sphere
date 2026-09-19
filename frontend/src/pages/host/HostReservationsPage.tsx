import { RefundPaymentButton } from "@/components/payments/RefundPaymentButton";
import { Card } from "@/components/ui/Card";
import {
  fetchHostReservations,
  type HostReservation,
} from "@/features/bookings/hostReservations";
import { formatInr } from "@/lib/format";
import { useAuthStore } from "@/store/authStore";
import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";

function statusColor(status: string) {
  switch (status) {
    case "CONFIRMED":
      return "bg-green-100 text-green-800";
    case "PENDING":
      return "bg-amber-100 text-amber-900";
    case "CANCELLED":
    case "EXPIRED":
      return "bg-stone-100 text-stone-600";
    default:
      return "bg-stone-100 text-stone-700";
  }
}

function ReservationRow({
  booking,
  accessToken,
}: {
  booking: HostReservation;
  accessToken: string;
}) {
  return (
    <Card className="p-5">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div>
          <span
            className={`inline-block rounded-full px-2.5 py-0.5 text-xs font-medium ${statusColor(booking.status)}`}
          >
            {booking.status}
          </span>
          <p className="mt-2 font-medium text-ink">{booking.propertyTitle}</p>
          <p className="text-sm text-muted">
            {booking.checkIn} → {booking.checkOut} · {booking.totalGuests}{" "}
            guest{booking.totalGuests > 1 ? "s" : ""}
          </p>
          <p className="text-sm text-muted">{formatInr(booking.totalPrice)}</p>
          <Link
            to={`/properties/${booking.propertyId}`}
            className="mt-2 inline-block text-sm text-brand-700 hover:underline"
          >
            View listing
          </Link>
        </div>
        <div className="flex flex-col items-end gap-2">
          <p className="text-xs text-stone-400">
            Guest ref: {booking.guestId.slice(0, 12)}…
          </p>
          <RefundPaymentButton
            accessToken={accessToken}
            bookingId={booking.id}
            bookingStatus={booking.status}
          />
        </div>
      </div>
    </Card>
  );
}

export function HostReservationsPage() {
  const accessToken = useAuthStore((s) => s.accessToken)!;

  const { data, isLoading, error } = useQuery({
    queryKey: ["host-reservations"],
    queryFn: () => fetchHostReservations(accessToken),
  });

  const confirmed =
    data?.filter((b) => b.status === "CONFIRMED").length ?? 0;
  const pending = data?.filter((b) => b.status === "PENDING").length ?? 0;

  return (
    <div className="mx-auto max-w-3xl px-4 py-10 sm:px-6">
      <h1 className="font-display text-3xl font-semibold">Reservations</h1>
      <p className="mt-2 text-muted">
        Bookings across your listings (newest first).
      </p>

      {data && data.length > 0 && (
        <p className="mt-4 text-sm text-muted">
          {confirmed} confirmed · {pending} awaiting payment
        </p>
      )}

      {error && (
        <p className="mt-6 text-sm text-red-600">Could not load reservations.</p>
      )}

      {isLoading ? (
        <p className="mt-10 text-muted">Loading…</p>
      ) : data && data.length > 0 ? (
        <ul className="mt-8 space-y-4">
          {data.map((b) => (
            <li key={b.id}>
              <ReservationRow booking={b} accessToken={accessToken} />
            </li>
          ))}
        </ul>
      ) : (
        <Card className="mt-10 p-10 text-center">
          <p className="font-medium">No reservations yet</p>
          <p className="mt-2 text-sm text-muted">
            When guests book your properties, they appear here.
          </p>
          <Link
            to="/host/listings"
            className="mt-4 inline-block text-sm font-medium text-brand-700 hover:underline"
          >
            Manage listings
          </Link>
        </Card>
      )}
    </div>
  );
}
