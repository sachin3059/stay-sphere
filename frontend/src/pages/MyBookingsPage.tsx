import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { cancelBooking, fetchMyBookings } from "@/features/bookings/api";
import { formatInr } from "@/lib/format";
import { useAuthStore } from "@/store/authStore";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link, useLocation } from "react-router-dom";

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

export function MyBookingsPage() {
  const accessToken = useAuthStore((s) => s.accessToken)!;
  const queryClient = useQueryClient();
  const location = useLocation();
  const flash = (location.state as { message?: string } | null)?.message;

  const { data, isLoading, error } = useQuery({
    queryKey: ["my-bookings"],
    queryFn: () => fetchMyBookings(accessToken),
  });

  const cancel = useMutation({
    mutationFn: (id: string) => cancelBooking(accessToken, id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["my-bookings"] }),
  });

  return (
    <div className="mx-auto max-w-3xl px-4 py-10 sm:px-6">
      <h1 className="font-display text-3xl font-semibold">My trips</h1>
      <p className="mt-2 text-muted">Bookings tied to your account.</p>

      {flash && (
        <p className="mt-4 rounded-xl bg-brand-50 px-4 py-3 text-sm text-brand-900">
          {flash}
        </p>
      )}

      {error && (
        <p className="mt-6 text-sm text-red-600">Could not load bookings.</p>
      )}

      {isLoading ? (
        <p className="mt-10 text-muted">Loading…</p>
      ) : data && data.length > 0 ? (
        <ul className="mt-8 space-y-4">
          {data.map((b) => (
            <li key={b.id}>
              <Card className="p-5">
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div>
                    <span
                      className={`inline-block rounded-full px-2.5 py-0.5 text-xs font-medium ${statusColor(b.status)}`}
                    >
                      {b.status}
                    </span>
                    <p className="mt-2 font-medium text-ink">
                      {b.checkIn} → {b.checkOut}
                    </p>
                    <p className="text-sm text-muted">
                      {b.totalGuests} guest{b.totalGuests > 1 ? "s" : ""} ·{" "}
                      {formatInr(b.totalPrice)}
                    </p>
                    <Link
                      to={`/properties/${b.propertyId}`}
                      className="mt-2 inline-block text-sm text-brand-700 hover:underline"
                    >
                      View property
                    </Link>
                  </div>
                  {b.status === "PENDING" && (
                    <Button
                      variant="outline"
                      size="sm"
                      disabled={cancel.isPending}
                      onClick={() => cancel.mutate(b.id)}
                    >
                      Cancel
                    </Button>
                  )}
                </div>
              </Card>
            </li>
          ))}
        </ul>
      ) : (
        <Card className="mt-10 p-10 text-center">
          <p className="font-medium">No trips yet</p>
          <Link to="/explore" className="mt-4 inline-block">
            <Button>Find a stay</Button>
          </Link>
        </Card>
      )}
    </div>
  );
}
