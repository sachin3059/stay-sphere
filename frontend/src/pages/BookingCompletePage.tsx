import { Button } from "@/components/ui/Button";
import { confirmBooking, fetchBooking } from "@/features/bookings/api";
import { confirmStripePayment } from "@/features/payments/api";
import { useAuthStore } from "@/store/authStore";
import { useEffect, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";

const PAYMENT_ID_KEY = "stripePendingPaymentId";
const BOOKING_ID_KEY = "stripePendingBookingId";

export function BookingCompletePage() {
  const accessToken = useAuthStore((s) => s.accessToken);
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const [status, setStatus] = useState<"idle" | "syncing" | "done" | "error">(
    "idle",
  );

  const redirectStatus = params.get("redirect_status");

  useEffect(() => {
    if (!accessToken || redirectStatus !== "succeeded") {
      return;
    }
    const paymentId = sessionStorage.getItem(PAYMENT_ID_KEY);
    const bookingId = sessionStorage.getItem(BOOKING_ID_KEY);
    if (!paymentId || !bookingId) {
      setStatus("error");
      return;
    }

    let cancelled = false;
    setStatus("syncing");

    (async () => {
      try {
        await confirmStripePayment(accessToken, paymentId);
        let updated = await fetchBooking(accessToken, bookingId);
        if (updated.status === "PENDING") {
          updated = await confirmBooking(accessToken, bookingId);
        }
        sessionStorage.removeItem(PAYMENT_ID_KEY);
        sessionStorage.removeItem(BOOKING_ID_KEY);
        if (!cancelled) {
          setStatus("done");
          navigate("/bookings/my", {
            replace: true,
            state: { message: `Booking ${updated.status.toLowerCase()}!` },
          });
        }
      } catch {
        if (!cancelled) setStatus("error");
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [accessToken, redirectStatus, navigate]);

  if (!accessToken) {
    return (
      <div className="mx-auto max-w-md px-4 py-16 text-center">
        <p className="text-muted">Sign in to finish confirming your payment.</p>
        <Link to="/login" state={{ from: "/bookings/complete" }} className="mt-4 inline-block">
          <Button>Sign in</Button>
        </Link>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-md px-4 py-16 text-center">
      {status === "syncing" && <p className="text-muted">Confirming payment…</p>}
      {status === "error" && (
        <>
          <p className="text-muted">
            Payment may have succeeded. Check My trips or contact support if a charge
            appears.
          </p>
          <Link to="/bookings/my" className="mt-4 inline-block">
            <Button>My trips</Button>
          </Link>
        </>
      )}
      {status === "idle" && redirectStatus !== "succeeded" && (
        <>
          <p className="text-muted">No payment to confirm.</p>
          <Link to="/bookings/my" className="mt-4 inline-block">
            <Button variant="outline">My trips</Button>
          </Link>
        </>
      )}
    </div>
  );
}
