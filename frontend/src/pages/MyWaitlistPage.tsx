import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import {
  cancelWaitlistEntry,
  fetchMyWaitlist,
} from "@/features/waitlist/api";
import { useAuthStore } from "@/store/authStore";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link } from "react-router-dom";

function statusStyle(status: string) {
  switch (status) {
    case "OFFERED":
      return "bg-green-100 text-green-800";
    case "WAITING":
      return "bg-amber-100 text-amber-900";
    case "BOOKED":
      return "bg-brand-100 text-brand-900";
    default:
      return "bg-stone-100 text-stone-600";
  }
}

export function MyWaitlistPage() {
  const accessToken = useAuthStore((s) => s.accessToken)!;
  const queryClient = useQueryClient();

  const { data, isLoading, error } = useQuery({
    queryKey: ["my-waitlist"],
    queryFn: () => fetchMyWaitlist(accessToken),
  });

  const cancel = useMutation({
    mutationFn: (id: string) => cancelWaitlistEntry(accessToken, id),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: ["my-waitlist"] }),
  });

  return (
    <div className="mx-auto max-w-3xl px-4 py-10 sm:px-6">
      <h1 className="font-display text-3xl font-semibold">My waitlist</h1>
      <p className="mt-2 text-muted">
        Dates you&apos;re waiting on when a stay isn&apos;t available.
      </p>

      {error && (
        <p className="mt-6 text-sm text-red-600">Could not load waitlist.</p>
      )}

      {isLoading ? (
        <p className="mt-10 text-muted">Loading…</p>
      ) : data && data.length > 0 ? (
        <ul className="mt-8 space-y-4">
          {data.map((entry) => (
            <li key={entry.id}>
              <Card className="p-5">
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div>
                    <span
                      className={`inline-block rounded-full px-2.5 py-0.5 text-xs font-medium ${statusStyle(entry.status)}`}
                    >
                      {entry.status}
                    </span>
                    <p className="mt-2 font-medium text-ink">
                      {entry.checkIn} → {entry.checkOut}
                    </p>
                    <p className="text-sm text-muted">
                      Queue position {entry.queuePosition} · {entry.totalGuests}{" "}
                      guest{entry.totalGuests > 1 ? "s" : ""}
                    </p>
                    {entry.status === "OFFERED" && entry.slotExpiresAt && (
                      <p className="mt-1 text-sm text-green-800">
                        Slot offered — book before{" "}
                        {new Date(entry.slotExpiresAt).toLocaleString()}
                      </p>
                    )}
                    <Link
                      to={`/properties/${entry.propertyId}`}
                      className="mt-2 inline-block text-sm text-brand-700 hover:underline"
                    >
                      View property
                    </Link>
                    {entry.status === "OFFERED" && (
                      <Link
                        to={`/properties/${entry.propertyId}/book?checkIn=${entry.checkIn}&checkOut=${entry.checkOut}`}
                        className="mt-2 ml-4 inline-block text-sm font-medium text-brand-700 hover:underline"
                      >
                        Book now
                      </Link>
                    )}
                  </div>
                  {(entry.status === "WAITING" || entry.status === "OFFERED") && (
                    <Button
                      variant="outline"
                      size="sm"
                      disabled={cancel.isPending}
                      onClick={() => cancel.mutate(entry.id)}
                    >
                      Leave waitlist
                    </Button>
                  )}
                </div>
              </Card>
            </li>
          ))}
        </ul>
      ) : (
        <Card className="mt-10 p-10 text-center">
          <p className="font-medium">No waitlist entries</p>
          <Link to="/explore" className="mt-4 inline-block">
            <Button>Explore stays</Button>
          </Link>
        </Card>
      )}
    </div>
  );
}
