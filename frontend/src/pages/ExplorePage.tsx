import { PropertyCard } from "@/components/properties/PropertyCard";
import { PropertySearchForm } from "@/components/properties/PropertySearchForm";
import { Card } from "@/components/ui/Card";
import { searchProperties } from "@/features/properties/api";
import {
  EXPLORE_PAGE_SIZE,
  paginateProperties,
  sortProperties,
  type ExploreSort,
} from "@/features/properties/exploreSort";
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
  const sort = (urlSearchParams.get("sort") as ExploreSort) || "newest";
  const page = Number(urlSearchParams.get("page") ?? "1") || 1;

  const { data, isLoading, isFetching, error } = useQuery({
    queryKey: ["properties", filters],
    queryFn: () => searchProperties(filters),
  });

  const sorted = useMemo(
    () => sortProperties(data ?? [], sort),
    [data, sort],
  );
  const paged = useMemo(
    () => paginateProperties(sorted, page, EXPLORE_PAGE_SIZE),
    [sorted, page],
  );

  function updateParams(patch: Record<string, string | undefined>) {
    const qs = new URLSearchParams(urlSearchParams);
    for (const [key, value] of Object.entries(patch)) {
      if (value == null || value === "") qs.delete(key);
      else qs.set(key, value);
    }
    setUrlSearchParams(qs, { replace: true });
  }

  function handleSearch(next: PropertySearchParams) {
    updateParams({
      city: next.city,
      query: next.query,
      guests: next.guests != null ? String(next.guests) : undefined,
      minPrice: next.minPrice != null ? String(next.minPrice) : undefined,
      maxPrice: next.maxPrice != null ? String(next.maxPrice) : undefined,
      page: "1",
    });
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
          <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
            <p className="text-sm text-muted">
              {paged.total} {paged.total === 1 ? "stay" : "stays"} found
              {paged.totalPages > 1 && (
                <span>
                  {" "}
                  · page {paged.page} of {paged.totalPages}
                </span>
              )}
            </p>
            <label className="flex items-center gap-2 text-sm text-stone-700">
              Sort
              <select
                className="rounded-lg border border-border bg-white px-2 py-1.5"
                value={sort}
                onChange={(e) =>
                  updateParams({
                    sort: e.target.value,
                    page: "1",
                  })
                }
              >
                <option value="newest">Newest</option>
                <option value="price_asc">Price: low to high</option>
                <option value="price_desc">Price: high to low</option>
              </select>
            </label>
          </div>
          <ul className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {paged.items.map((property) => (
              <li key={property.id}>
                <PropertyCard property={property} />
              </li>
            ))}
          </ul>
          {paged.totalPages > 1 && (
            <div className="mt-8 flex flex-wrap justify-center gap-2">
              <Button
                type="button"
                variant="outline"
                size="sm"
                disabled={paged.page <= 1}
                onClick={() =>
                  updateParams({ page: String(paged.page - 1) })
                }
              >
                Previous
              </Button>
              <Button
                type="button"
                variant="outline"
                size="sm"
                disabled={paged.page >= paged.totalPages}
                onClick={() =>
                  updateParams({ page: String(paged.page + 1) })
                }
              >
                Next
              </Button>
            </div>
          )}
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
