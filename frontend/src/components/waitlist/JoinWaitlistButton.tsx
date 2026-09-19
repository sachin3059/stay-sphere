import { Button } from "@/components/ui/Button";
import { joinWaitlist } from "@/features/waitlist/api";
import { ApiError } from "@/lib/api/types";
import { useAuthStore } from "@/store/authStore";
import { useMutation } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { useState } from "react";

type Props = {
  propertyId: string;
  checkIn: string;
  checkOut: string;
  totalGuests: number;
  className?: string;
};

export function JoinWaitlistButton({
  propertyId,
  checkIn,
  checkOut,
  totalGuests,
  className,
}: Props) {
  const accessToken = useAuthStore((s) => s.accessToken);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const join = useMutation({
    mutationFn: () =>
      joinWaitlist(accessToken!, {
        propertyId,
        checkIn,
        checkOut,
        totalGuests,
      }),
    onSuccess: (entry) => {
      setError(null);
      setMessage(
        `You're on the waitlist (position ${entry.queuePosition}). We'll notify you if dates open up.`,
      );
    },
    onError: (err: unknown) => {
      setMessage(null);
      setError(
        err instanceof ApiError ? err.message : "Could not join waitlist.",
      );
    },
  });

  if (!accessToken) {
    return (
      <Link to="/login" state={{ from: `/properties/${propertyId}/book` }}>
        <Button type="button" variant="outline" className={className ?? "w-full"}>
          Sign in to join waitlist
        </Button>
      </Link>
    );
  }

  return (
    <div className={className}>
      <Button
        type="button"
        variant="outline"
        className="w-full"
        disabled={join.isPending || Boolean(message)}
        onClick={() => {
          setError(null);
          join.mutate();
        }}
      >
        {join.isPending ? "Joining…" : message ? "On waitlist" : "Join waitlist"}
      </Button>
      {message && (
        <p className="mt-2 text-sm text-brand-800">
          {message}{" "}
          <Link to="/waitlist/my" className="font-medium underline">
            View waitlist
          </Link>
        </p>
      )}
      {error && (
        <p className="mt-2 text-sm text-red-600" role="alert">{error}</p>
      )}
    </div>
  );
}
