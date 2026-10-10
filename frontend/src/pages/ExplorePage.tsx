import { PropertyCard } from "@/components/properties/PropertyCard";
import { StaySearchBar } from "@/components/search/StaySearchBar";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { PropertyCardSkeleton } from "@/components/ui/Skeleton";
import { searchProperties } from "@/features/properties/api";
import {
  EXPLORE_PAGE_SIZE,
  type ExploreSort,
} from "@/features/properties/exploreSort";
import type { PropertySearchParams } from "@/features/properties/types";
import { ApiError } from "@/lib/api/types";
import { useQuery } from "@tanstack/react-query";
import { useMemo } from "react";
import { Link, useSearchParams } from "react-router-dom";

function paramsFromUrl(searchParams: URLSearchParams): PropertySearchParams {
  const guests = searchParams.get("guests");
  const minPrice = searchParams.get("minPrice");
  const maxPrice = searchParams.get("maxPrice");
  const pageUi = Number(searchParams.get("page") ?? "1") || 1;
  const sort = (searchParams.get("sort") as ExploreSort) || "newest";
  return {
    city: searchParams.get("city") ?? undefined,
    query: searchParams.get("query") ?? undefined,
    guests: guests ? Number(guests) : undefined,
    minPrice: minPrice ? Number(minPrice) : undefined,
    maxPrice: maxPrice ? Number(maxPrice) : undefined,
    checkIn: searchParams.get("checkIn") ?? undefined,
    checkOut: searchParams.get("checkOut") ?? undefined,
    page: Math.max(0, pageUi - 1),
    size: EXPLORE_PAGE_SIZE,
    sort,
  };
}

export function ExplorePage() {
  const [urlSearchParams, setUrlSearchParams] = useSearchParams();
  const filters = useMemo(
    () => paramsFromUrl(urlSearchParams),
    [urlSearchParams],
  );
  const sort = filters.sort ?? "newest";
  const pageUi = (filters.page ?? 0) + 1;

  const { data, isLoading, isFetching, error } = useQuery({
    queryKey: ["properties", filters],
    queryFn: () => searchProperties(filters),
  });

  const items = data?.items ?? [];
  const totalElements = data?.totalElements ?? 0;
  const totalPages = data?.totalPages ?? 0;

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
      checkIn: next.checkIn,
      checkOut: next.checkOut,
      page: "1",
    });
  }

  const errorMessage =
    error instanceof ApiError ? error.message : error ? "Failed to load stays." : null;

  return (
    <div className="page-container py-8 sm:py-10">
      <header className="mb-8">
        <h1 className="section-heading">Explore stays</h1>
        <p className="mt-2 text-muted">
          {filters.city
            ? `Places in ${filters.city}`
            : "Homes, apartments, and unique stays"}
          {filters.checkIn && filters.checkOut && (
            <span>
              {" "}
              · {filters.checkIn} → {filters.checkOut}
            </span>
          )}
        </p>
      </header>

      <div className="mb-10">
        <StaySearchBar
          initial={filters}
          onSearch={handleSearch}
          loading={isFetching}
          variant="filters"
        />
      </div>

      {errorMessage && (
        <p
          className="mb-6 rounded-xl bg-red-50 px-4 py-3 text-sm text-red-700"
          role="alert"
        >
          {errorMessage}
        </p>
      )}

      {isLoading ? (
        <ul className="grid gap-x-6 gap-y-10 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
          {Array.from({ length: 8 }).map((_, i) => (
            <li key={i}>
              <PropertyCardSkeleton />
            </li>
          ))}
        </ul>
      ) : items.length > 0 ? (
        <>
          <div className="mb-6 flex flex-wrap items-center justify-between gap-3 border-b border-stone-200 pb-4">
            <p className="text-sm font-medium text-ink">
              {totalElements} {totalElements === 1 ? "stay" : "stays"}
              {totalPages > 1 && (
                <span className="font-normal text-muted">
                  {" "}
                  · Page {pageUi} of {totalPages}
                </span>
              )}
            </p>
            <label className="flex items-center gap-2 text-sm text-stone-700">
              <span className="text-muted">Sort by</span>
              <select
                className="rounded-lg border border-stone-300 bg-white px-3 py-2 text-sm font-medium focus:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-500/15"
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
          <ul className="grid gap-x-6 gap-y-10 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            {items.map((property) => (
              <li key={property.id}>
                <PropertyCard
                  property={property}
                  checkIn={filters.checkIn}
                  checkOut={filters.checkOut}
                />
              </li>
            ))}
          </ul>
          {totalPages > 1 && (
            <div className="mt-10 flex flex-wrap justify-center gap-3">
              <Button
                type="button"
                variant="outline"
                size="sm"
                disabled={pageUi <= 1}
                onClick={() =>
                  updateParams({ page: String(pageUi - 1) })
                }
              >
                Previous
              </Button>
              <Button
                type="button"
                variant="outline"
                size="sm"
                disabled={pageUi >= totalPages}
                onClick={() =>
                  updateParams({ page: String(pageUi + 1) })
                }
              >
                Next
              </Button>
            </div>
          )}
        </>
      ) : (
        <Card className="border-dashed p-12 text-center">
          <p className="text-lg font-semibold text-ink">No stays match your search</p>
          <p className="mt-2 text-sm text-muted">
            Try a different city, different dates, or widen your price range.
          </p>
          <div className="mt-8 flex flex-wrap justify-center gap-3">
            <Link to="/explore">
              <Button variant="outline">Clear filters</Button>
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
