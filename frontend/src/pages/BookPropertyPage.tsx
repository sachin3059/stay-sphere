import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { Input } from "@/components/ui/Input";
import { createBooking } from "@/features/bookings/api";
import { createStripeIntent } from "@/features/payments/api";
import { fetchPropertyById } from "@/features/properties/api";
import { formatInr } from "@/lib/format";
import { ApiError } from "@/lib/api/types";
import { useAuthStore } from "@/store/authStore";
import { useMutation, useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";

export function BookPropertyPage() {
  const { id: propertyId } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const accessToken = useAuthStore((s) => s.accessToken)!;

  const { data: property, isLoading } = useQuery({
    queryKey: ["property", propertyId],
    queryFn: () => fetchPropertyById(propertyId!),
    enabled: Boolean(propertyId),
  });

  const [checkIn, setCheckIn] = useState("");
  const [checkOut, setCheckOut] = useState("");
  const [totalGuests, setTotalGuests] = useState("2");
  const [error, setError] = useState<string | null>(null);

  const bookAndPay = useMutation({
    mutationFn: async () => {
      const idempotencyKey = crypto.randomUUID();
      const booking = await createBooking(
        accessToken,
        {
          propertyId: propertyId!,
          hostId: property!.hostId,
          checkIn,
          checkOut,
          totalGuests: Number(totalGuests),
        },
        idempotencyKey,
      );
      const payIdempotency = `pay-${booking.id}`;
      const intent = await createStripeIntent(accessToken, {
        bookingId: booking.id,
        hostId: property!.hostId,
        idempotencyKey: payIdempotency,
      });
      return { booking, intent };
    },
    onSuccess: ({ booking, intent }) => {
      navigate(`/bookings/${booking.id}/pay`, {
        state: { booking, intent },
      });
    },
    onError: (err: unknown) => {
      setError(
        err instanceof ApiError ? err.message : "Could not create booking.",
      );
    },
  });

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    if (!checkIn || !checkOut || checkIn >= checkOut) {
      setError("Choose valid check-in and check-out dates.");
      return;
    }
    bookAndPay.mutate();
  }

  if (!propertyId) return null;

  if (isLoading || !property) {
    return (
      <div className="mx-auto max-w-lg px-4 py-16 text-muted">Loading…</div>
    );
  }

  return (
    <div className="mx-auto max-w-lg px-4 py-10 sm:px-6">
      <Link
        to={`/properties/${propertyId}`}
        className="text-sm font-medium text-brand-700 hover:underline"
      >
        ← Back to listing
      </Link>
      <h1 className="mt-4 font-display text-2xl font-semibold">
        Book: {property.title}
      </h1>
      <p className="mt-1 text-sm text-muted">
        {formatInr(property.pricePerNight)} / night · {property.city}
      </p>

      <Card className="mt-8 p-6">
        <form onSubmit={handleSubmit} className="space-y-4">
          <Input
            label="Check-in"
            type="date"
            value={checkIn}
            onChange={(e) => setCheckIn(e.target.value)}
            required
          />
          <Input
            label="Check-out"
            type="date"
            value={checkOut}
            onChange={(e) => setCheckOut(e.target.value)}
            required
          />
          <Input
            label="Guests"
            type="number"
            min={1}
            max={property.maxGuests ?? 20}
            value={totalGuests}
            onChange={(e) => setTotalGuests(e.target.value)}
            required
          />
          {error && (
            <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700" role="alert">
              {error}
            </p>
          )}
          <Button
            type="submit"
            className="w-full"
            disabled={bookAndPay.isPending}
          >
            {bookAndPay.isPending
              ? "Reserving…"
              : "Continue to payment"}
          </Button>
        </form>
      </Card>
    </div>
  );
}
