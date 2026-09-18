import { PropertyCard } from "@/components/properties/PropertyCard";
import { PropertySearchForm } from "@/components/properties/PropertySearchForm";
import { Card } from "@/components/ui/Card";
import { searchProperties } from "@/features/properties/api";
import type { PropertySearchParams } from "@/features/properties/types";
import { ApiError } from "@/lib/api/types";
import { useQuery } from "@tanstack/react-query";
import { Button } from "@/components/ui/Button";
import { useMemo } from "react";
import { Link, useSearchParams } from "react-router-dom";

function paramsFromUrl(searchParams: URLSearchParams): PropertySearchParams {
  const guests = searchParams.get("guests");
  const minPrice = searchParams.get("minPrice");
  const maxPrice = searchParams.get("maxPrice");
  return {
    city: searchParams.get("city") ?? undefined,
    query: searchParams.get("query") ?? undefined,
    guests: guests ? Number(guests) : undefined,
    minPrice: minPrice ? Number(minPrice) : undefined,
    maxPrice: maxPrice ? Number(maxPrice) : undefined,
  };
}

export function ExplorePage() {
  const [urlSearchParams, setUrlSearchParams] = useSearchParams();
  const filters = useMemo(
    () => paramsFromUrl(urlSearchParams),
    [urlSearchParams],
  );

  const { data, isLoading, isFetching, error } = useQuery({
    queryKey: ["properties", filters],
    queryFn: () => searchProperties(filters),
  });

  function handleSearch(next: PropertySearchParams) {
    const qs = new URLSearchParams();
    if (next.city) qs.set("city", next.city);
    if (next.query) qs.set("query", next.query);
    if (next.guests) qs.set("guests", String(next.guests));
    if (next.minPrice) qs.set("minPrice", String(next.minPrice));
    if (next.maxPrice) qs.set("maxPrice", String(next.maxPrice));
    setUrlSearchParams(qs, { replace: true });
  }

  const errorMessage =
    error instanceof ApiError ? error.message : error ? "Failed to load stays." : null;

  return (
    <div className="mx-auto max-w-6xl px-4 py-10 sm:px-6">
      <header className="mb-8">
        <h1 className="font-display text-3xl font-semibold text-ink">
          Explore stays
        </h1>
        <p className="mt-2 text-muted">
          Live listings from the property service via the API gateway.
        </p>
      </header>

      <Card className="mb-10 p-5 sm:p-6">
        <PropertySearchForm
          initial={filters}
          onSearch={handleSearch}
          loading={isFetching}
        />
      </Card>

      {errorMessage && (
        <p className="mb-6 rounded-xl bg-red-50 px-4 py-3 text-sm text-red-700" role="alert">
          {errorMessage}
        </p>
      )}

      {isLoading ? (
        <p className="text-sm text-muted">Loading properties…</p>
      ) : data && data.length > 0 ? (
        <>
          <p className="mb-4 text-sm text-muted">
            {data.length} {data.length === 1 ? "stay" : "stays"} found
          </p>
          <ul className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {data.map((property) => (
              <li key={property.id}>
                <PropertyCard property={property} />
              </li>
            ))}
          </ul>
        </>
      ) : (
        <Card className="p-10 text-center">
          <p className="font-medium text-ink">No stays match your filters</p>
          <p className="mt-2 text-sm text-muted">
            The database may have no listings yet, or your price range is too
            narrow (e.g. min and max both ₹1000 hides ₹2500/night stays).
          </p>
          <div className="mt-6 flex flex-wrap justify-center gap-3">
            <Link to="/explore">
              <Button variant="outline">Clear all filters</Button>
            </Link>
            <Link to="/host/listings/new">
              <Button>List a property</Button>
            </Link>
          </div>
        </Card>
      )}
    </div>
  );
}
