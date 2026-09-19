import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { Input } from "@/components/ui/Input";
import {
  blockPropertyDates,
  fetchBlockedDates,
} from "@/features/availability/api";
import { fetchPropertyById } from "@/features/properties/api";
import { ApiError } from "@/lib/api/types";
import { useAuthStore } from "@/store/authStore";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { Link, useParams } from "react-router-dom";

function reasonLabel(reason: string) {
  switch (reason) {
    case "BOOKED":
      return "Booked";
    case "MAINTENANCE":
      return "Maintenance";
    case "HOST_BLOCK":
      return "Host block";
    default:
      return reason;
  }
}

export function HostListingAvailabilityPage() {
  const { id: propertyId } = useParams<{ id: string }>();
  const accessToken = useAuthStore((s) => s.accessToken)!;
  const user = useAuthStore((s) => s.user)!;
  const queryClient = useQueryClient();

  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [reason, setReason] = useState<"HOST_BLOCK" | "MAINTENANCE">(
    "HOST_BLOCK",
  );
  const [error, setError] = useState<string | null>(null);

  const { data: property, isLoading: loadingProperty } = useQuery({
    queryKey: ["property", propertyId],
    queryFn: () => fetchPropertyById(propertyId!),
    enabled: Boolean(propertyId),
  });

  const { data: blocked, isLoading: loadingBlocked } = useQuery({
    queryKey: ["blocked-dates", propertyId],
    queryFn: () => fetchBlockedDates(propertyId!),
    enabled: Boolean(propertyId),
  });

  const block = useMutation({
    mutationFn: () =>
      blockPropertyDates(accessToken, {
        propertyId: propertyId!,
        startDate,
        endDate,
        reason,
      }),
    onSuccess: () => {
      setError(null);
      setStartDate("");
      setEndDate("");
      queryClient.invalidateQueries({ queryKey: ["blocked-dates", propertyId] });
    },
    onError: (err: unknown) => {
      setError(
        err instanceof ApiError ? err.message : "Could not block dates.",
      );
    },
  });

  if (!propertyId) return null;

  if (loadingProperty || !property) {
    return (
      <div className="mx-auto max-w-lg px-4 py-16 text-muted">Loading…</div>
    );
  }

  const isOwner = property.hostId === user.email;

  if (!isOwner) {
    return (
      <div className="mx-auto max-w-lg px-4 py-16 text-center">
        <p className="text-muted">You can only manage availability for your own listings.</p>
        <Link to="/host/listings" className="mt-4 inline-block text-brand-700 hover:underline">
          My listings
        </Link>
      </div>
    );
  }

  function handleBlock(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    if (!startDate || !endDate || startDate >= endDate) {
      setError("Choose a valid start and end date.");
      return;
    }
    block.mutate();
  }

  return (
    <div className="mx-auto max-w-lg px-4 py-10 sm:px-6">
      <Link
        to="/host/listings"
        className="text-sm font-medium text-brand-700 hover:underline"
      >
        ← My listings
      </Link>
      <h1 className="mt-4 font-display text-2xl font-semibold">
        Calendar: {property.title}
      </h1>
      <p className="mt-2 text-sm text-muted">
        Block dates so guests cannot book. Confirmed bookings appear as{" "}
        <span className="font-medium">Booked</span> automatically.
      </p>

      <Card className="mt-8 p-6">
        <h2 className="font-medium text-ink">Block dates</h2>
        <form onSubmit={handleBlock} className="mt-4 space-y-4">
          <Input
            label="From"
            type="date"
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
            required
          />
          <Input
            label="To (last blocked night)"
            type="date"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
            required
          />
          <div>
            <label className="mb-1.5 block text-sm font-medium text-stone-700">
              Reason
            </label>
            <select
              className="w-full rounded-xl border border-border bg-white px-4 py-2.5 text-sm"
              value={reason}
              onChange={(e) =>
                setReason(e.target.value as "HOST_BLOCK" | "MAINTENANCE")
              }
            >
              <option value="HOST_BLOCK">Unavailable (host)</option>
              <option value="MAINTENANCE">Maintenance</option>
            </select>
          </div>
          {error && (
            <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700" role="alert">
              {error}
            </p>
          )}
          <Button type="submit" className="w-full" disabled={block.isPending}>
            {block.isPending ? "Saving…" : "Block dates"}
          </Button>
        </form>
      </Card>

      <Card className="mt-6 p-6">
        <h2 className="font-medium text-ink">Blocked & booked periods</h2>
        {loadingBlocked ? (
          <p className="mt-4 text-sm text-muted">Loading…</p>
        ) : blocked && blocked.length > 0 ? (
          <ul className="mt-4 space-y-3">
            {blocked.map((b) => (
              <li
                key={b.id}
                className="flex flex-wrap items-center justify-between gap-2 rounded-lg border border-border px-3 py-2 text-sm"
              >
                <span>
                  {b.startDate} → {b.endDate}
                </span>
                <span className="rounded-full bg-stone-100 px-2 py-0.5 text-xs text-stone-700">
                  {reasonLabel(b.reason)}
                </span>
              </li>
            ))}
          </ul>
        ) : (
          <p className="mt-4 text-sm text-muted">No blocked dates yet.</p>
        )}
      </Card>
    </div>
  );
}
