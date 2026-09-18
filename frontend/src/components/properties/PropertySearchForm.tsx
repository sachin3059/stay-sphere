import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import type { PropertySearchParams } from "@/features/properties/types";
import { Search } from "lucide-react";
import { useState } from "react";

type Props = {
  initial?: PropertySearchParams;
  onSearch: (params: PropertySearchParams) => void;
  loading?: boolean;
};

export function PropertySearchForm({ initial, onSearch, loading }: Props) {
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

  return (
    <form
      onSubmit={handleSubmit}
      className="grid gap-3 sm:grid-cols-2 lg:grid-cols-6 lg:items-end"
    >
      <div className="lg:col-span-2">
        <Input
          label="City"
          placeholder="Pune"
          value={city}
          onChange={(e) => setCity(e.target.value)}
        />
      </div>
      <div className="lg:col-span-2">
        <Input
          label="Keywords"
          placeholder="Apartment, WiFi…"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
      </div>
      <Input
        label="Guests"
        type="number"
        min={1}
        value={guests}
        onChange={(e) => setGuests(e.target.value)}
      />
      <div className="grid grid-cols-2 gap-2 sm:col-span-2 lg:col-span-1">
        <Input
          label="Min ₹"
          type="number"
          min={0}
          placeholder="1000"
          value={minPrice}
          onChange={(e) => setMinPrice(e.target.value)}
        />
        <Input
          label="Max ₹"
          type="number"
          min={0}
          placeholder="10000"
          value={maxPrice}
          onChange={(e) => setMaxPrice(e.target.value)}
        />
      </div>
      <Button type="submit" className="sm:col-span-2 lg:col-span-6 lg:w-auto" disabled={loading}>
        <Search className="h-4 w-4" />
        {loading ? "Searching…" : "Search stays"}
      </Button>
    </form>
  );
}
