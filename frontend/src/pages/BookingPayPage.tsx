import { Card } from "@/components/ui/Card";
import { StripeCheckoutForm } from "@/components/payments/StripeCheckoutForm";
import type { Booking } from "@/features/bookings/types";
import { confirmBooking, fetchBooking } from "@/features/bookings/api";
import { confirmStripePayment } from "@/features/payments/api";
import type { StripeIntentData } from "@/features/payments/types";
import { formatInr } from "@/lib/format";
import { useAuthStore } from "@/store/authStore";
import { Elements } from "@stripe/react-stripe-js";
import { loadStripe } from "@stripe/stripe-js";
import { useEffect, useMemo, useState } from "react";
import { Link, useLocation, useNavigate, useParams } from "react-router-dom";

type PayState = {
  booking: Booking;
  intent: StripeIntentData;
};

function BookingPayCheckout({
  booking,
  intent,
}: {
  booking: Booking;
  intent: StripeIntentData;
}) {
  const navigate = useNavigate();
  const accessToken = useAuthStore((s) => s.accessToken)!;
  const [syncing, setSyncing] = useState(false);

  const stripePromise = useMemo(
    () => loadStripe(intent.publishableKey),
    [intent.publishableKey],
  );

  useEffect(() => {
    sessionStorage.setItem("stripePendingPaymentId", intent.paymentId);
    sessionStorage.setItem("stripePendingBookingId", booking.id);
  }, [intent.paymentId, booking.id]);

  async function handlePaid() {
    setSyncing(true);
    try {
      await confirmStripePayment(accessToken, intent.paymentId);
      let updated = await fetchBooking(accessToken, booking.id);
      if (updated.status === "PENDING") {
        updated = await confirmBooking(accessToken, booking.id);
      }
      navigate("/bookings/my", {
        replace: true,
        state: { message: `Booking ${updated.status.toLowerCase()}!` },
      });
    } finally {
      setSyncing(false);
    }
  }

  return (
    <Card className="mt-6 p-6">
      <p className="text-lg font-semibold">
        Total {formatInr(booking.totalPrice)}
      </p>
      <div className="mt-6">
        <Elements
          stripe={stripePromise}
          options={{ clientSecret: intent.clientSecret }}
        >
          <StripeCheckoutForm onPaid={handlePaid} disabled={syncing} />
        </Elements>
      </div>
      {syncing && (
        <p className="mt-4 text-sm text-muted">Confirming your booking…</p>
      )}
    </Card>
  );
}

export function BookingPayPage() {
  const { bookingId } = useParams<{ bookingId: string }>();
  const location = useLocation();
  const state = location.state as PayState | null;

  if (!bookingId || !state?.intent || !state?.booking || state.booking.id !== bookingId) {
    return (
      <div className="mx-auto max-w-md px-4 py-16 text-center">
        <p className="text-muted">Start checkout from a property listing.</p>
        <Link to="/explore" className="mt-4 inline-block text-brand-700 hover:underline">
          Explore stays
        </Link>
      </div>
    );
  }

  const { intent, booking } = state;

  return (
    <div className="mx-auto max-w-lg px-4 py-10 sm:px-6">
      <h1 className="font-display text-2xl font-semibold">Complete payment</h1>
      <p className="mt-2 text-sm text-muted">
        Booking {booking.id.slice(0, 8)}… · {booking.checkIn} → {booking.checkOut}
      </p>
      <BookingPayCheckout booking={booking} intent={intent} />
    </div>
  );
}
