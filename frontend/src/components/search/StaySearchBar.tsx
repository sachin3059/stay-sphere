import { Button } from "@/components/ui/Button";
import type { PropertySearchParams } from "@/features/properties/types";
import { Search } from "lucide-react";
import { useState } from "react";

type Props = {
  initial?: PropertySearchParams;
  onSearch: (params: PropertySearchParams) => void;
  loading?: boolean;
  variant?: "hero" | "filters";
};

export function StaySearchBar({
  initial,
  onSearch,
  loading,
  variant = "filters",
}: Props) {
  const [city, setCity] = useState(initial?.city ?? "");
  const [query, setQuery] = useState(initial?.query ?? "");
  const [guests, setGuests] = useState(
    initial?.guests != null ? String(initial.guests) : "2",
  );
  const [minPrice, setMinPrice] = useState(
    initial?.minPrice != null ? String(initial.minPrice) : "",
  );
  const [maxPrice, setMaxPrice] = useState(
    initial?.maxPrice != null ? String(initial.maxPrice) : "",
  );

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    onSearch({
      city: city.trim() || undefined,
      query: query.trim() || undefined,
      guests: guests ? Number(guests) : undefined,
      minPrice: minPrice ? Number(minPrice) : undefined,
      maxPrice: maxPrice ? Number(maxPrice) : undefined,
    });
  }

  if (variant === "hero") {
    return (
      <form
        onSubmit={handleSubmit}
        className="flex flex-col gap-2 rounded-[2rem] border border-stone-200 bg-white p-2 shadow-[0_6px_20px_rgba(0,0,0,0.08)] sm:flex-row sm:items-stretch"
      >
        <label className="flex flex-1 flex-col justify-center rounded-full px-5 py-3 transition-colors hover:bg-stone-50 sm:rounded-none sm:border-r sm:border-stone-200">
          <span className="text-xs font-semibold text-ink">Where</span>
          <input
            type="text"
            placeholder="Search destinations"
            value={city}
            onChange={(e) => setCity(e.target.value)}
            className="mt-0.5 w-full border-0 bg-transparent p-0 text-sm text-stone-600 placeholder:text-stone-400 focus:outline-none focus:ring-0"
          />
        </label>
        <label className="flex w-full flex-col justify-center rounded-full px-5 py-3 transition-colors hover:bg-stone-50 sm:w-32 sm:rounded-none sm:border-r sm:border-stone-200">
          <span className="text-xs font-semibold text-ink">Guests</span>
          <input
            type="number"
            min={1}
            value={guests}
            onChange={(e) => setGuests(e.target.value)}
            className="mt-0.5 w-full border-0 bg-transparent p-0 text-sm text-stone-600 focus:outline-none focus:ring-0"
          />
        </label>
        <div className="flex items-center justify-end p-1 sm:pl-2">
          <Button
            type="submit"
            size="lg"
            className="h-12 w-full rounded-full px-6 sm:w-auto"
            disabled={loading}
          >
            <Search className="h-5 w-5" />
            Search
          </Button>
        </div>
      </form>
    );
  }

  return (
    <form
      onSubmit={handleSubmit}
      className="rounded-2xl border border-stone-200 bg-white p-4 shadow-sm sm:p-5"
    >
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-12 lg:items-end">
        <label className="lg:col-span-3">
          <span className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-stone-500">
            Location
          </span>
          <input
            type="text"
            placeholder="City"
            value={city}
            onChange={(e) => setCity(e.target.value)}
            className="w-full rounded-xl border border-stone-200 bg-stone-50/50 px-4 py-2.5 text-sm focus:border-brand-500 focus:bg-white focus:outline-none focus:ring-2 focus:ring-brand-500/15"
          />
        </label>
        <label className="lg:col-span-3">
          <span className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-stone-500">
            Keywords
          </span>
          <input
            type="text"
            placeholder="Apartment, WiFi…"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            className="w-full rounded-xl border border-stone-200 bg-stone-50/50 px-4 py-2.5 text-sm focus:border-brand-500 focus:bg-white focus:outline-none focus:ring-2 focus:ring-brand-500/15"
          />
        </label>
        <label className="lg:col-span-2">
          <span className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-stone-500">
            Guests
          </span>
          <input
            type="number"
            min={1}
            value={guests}
            onChange={(e) => setGuests(e.target.value)}
            className="w-full rounded-xl border border-stone-200 bg-stone-50/50 px-4 py-2.5 text-sm focus:border-brand-500 focus:bg-white focus:outline-none focus:ring-2 focus:ring-brand-500/15"
          />
        </label>
        <div className="grid grid-cols-2 gap-2 lg:col-span-3">
          <label>
            <span className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-stone-500">
              Min ₹
            </span>
            <input
              type="number"
              min={0}
              placeholder="1000"
              value={minPrice}
              onChange={(e) => setMinPrice(e.target.value)}
              className="w-full rounded-xl border border-stone-200 bg-stone-50/50 px-4 py-2.5 text-sm focus:border-brand-500 focus:bg-white focus:outline-none focus:ring-2 focus:ring-brand-500/15"
            />
          </label>
          <label>
            <span className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-stone-500">
              Max ₹
            </span>
            <input
              type="number"
              min={0}
              placeholder="10000"
              value={maxPrice}
              onChange={(e) => setMaxPrice(e.target.value)}
              className="w-full rounded-xl border border-stone-200 bg-stone-50/50 px-4 py-2.5 text-sm focus:border-brand-500 focus:bg-white focus:outline-none focus:ring-2 focus:ring-brand-500/15"
            />
          </label>
        </div>
        <div className="sm:col-span-2 lg:col-span-1">
          <Button type="submit" className="w-full" disabled={loading}>
            <Search className="h-4 w-4" />
            {loading ? "Searching…" : "Search"}
          </Button>
        </div>
      </div>
    </form>
  );
}
