import { PropertyCard } from "@/components/properties/PropertyCard";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { fetchHostProperties } from "@/features/properties/api";
import { useAuthStore } from "@/store/authStore";
import { useQuery } from "@tanstack/react-query";
import { Plus } from "lucide-react";
import { Link } from "react-router-dom";

export function HostListingsPage() {
  const accessToken = useAuthStore((s) => s.accessToken)!;

  const { data, isLoading, error } = useQuery({
    queryKey: ["host-properties"],
    queryFn: () => fetchHostProperties(accessToken),
  });

  return (
    <div className="mx-auto max-w-6xl px-4 py-10 sm:px-6">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="font-display text-3xl font-semibold text-ink">
            My listings
          </h1>
          <p className="mt-2 text-muted">
            Properties you publish appear on Explore for all guests.
          </p>
        </div>
        <Link to="/host/listings/new">
          <Button>
            <Plus className="h-4 w-4" />
            List a property
          </Button>
        </Link>
      </div>

      {error && (
        <p className="mt-6 text-sm text-red-600">Could not load your listings.</p>
      )}

      {isLoading ? (
        <p className="mt-10 text-muted">Loading…</p>
      ) : data && data.length > 0 ? (
        <ul className="mt-10 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {data.map((p) => (
            <li key={p.id} className="flex flex-col gap-2">
              <PropertyCard property={p} />
              <div className="flex justify-center gap-4 text-sm font-medium">
                <Link
                  to={`/host/listings/${p.id}/photos`}
                  className="text-brand-700 hover:underline"
                >
                  {p.imageUrls?.length ? "Photos" : "Add photos"}
                </Link>
                <Link
                  to={`/host/listings/${p.id}/availability`}
                  className="text-brand-700 hover:underline"
                >
                  Calendar
                </Link>
              </div>
            </li>
          ))}
        </ul>
      ) : (
        <Card className="mt-10 p-10 text-center">
          <p className="font-medium text-ink">You haven’t listed a stay yet</p>
          <p className="mt-2 text-sm text-muted">
            Create your first listing — it will show up on Explore after you
            save.
          </p>
          <Link to="/host/listings/new" className="mt-6 inline-block">
            <Button>List a property</Button>
          </Link>
        </Card>
      )}
    </div>
  );
}
