import { PropertyCard } from "@/components/properties/PropertyCard";
import { PropertyCardSkeleton } from "@/components/ui/Skeleton";
import { StaySearchBar } from "@/components/search/StaySearchBar";
import { Button } from "@/components/ui/Button";
import { searchProperties } from "@/features/properties/api";
import type { PropertySearchParams } from "@/features/properties/types";
import { useQuery } from "@tanstack/react-query";
import { ShieldCheck, Sparkles, Wifi } from "lucide-react";
import { Link, useNavigate } from "react-router-dom";

const categories = [
  { label: "Beachfront", emoji: "🏖️" },
  { label: "City apartments", emoji: "🏙️" },
  { label: "Mountain retreats", emoji: "⛰️" },
  { label: "Family friendly", emoji: "👨‍👩‍👧" },
  { label: "Work-friendly", emoji: "💻" },
];

const trustPoints = [
  {
    icon: ShieldCheck,
    title: "Book with confidence",
    text: "Overlapping stays are blocked at the database. Your dates are held while you pay.",
  },
  {
    icon: Sparkles,
    title: "Transparent pricing",
    text: "See the full quote before checkout — no surprise fees at the door.",
  },
  {
    icon: Wifi,
    title: "Real listings",
    text: "Photos, amenities, maps, and host calendars — everything in one place.",
  },
];

export function HomePage() {
  const navigate = useNavigate();

  const { data: featured, isLoading } = useQuery({
    queryKey: ["properties", "featured"],
    queryFn: () => searchProperties({}),
  });

  const preview = (featured ?? []).slice(0, 8);

  function handleSearch(params: PropertySearchParams) {
    const qs = new URLSearchParams();
    if (params.city) qs.set("city", params.city);
    if (params.guests != null) qs.set("guests", String(params.guests));
    const q = qs.toString();
    navigate(q ? `/explore?${q}` : "/explore");
  }

  return (
    <div>
      <section className="relative overflow-hidden border-b border-border bg-gradient-to-b from-brand-50/80 to-surface pb-4 pt-10 sm:pt-14">
        <div className="page-container">
          <div className="mx-auto max-w-3xl text-center">
            <h1 className="text-4xl font-bold leading-tight tracking-tight text-ink sm:text-5xl sm:leading-[1.1]">
              Find a place that feels like home
            </h1>
            <p className="mt-4 text-base text-muted sm:text-lg">
              Discover stays across India — search by city, compare prices, and
              book in minutes with secure checkout.
            </p>
          </div>

          <div className="mx-auto mt-10 max-w-4xl">
            <StaySearchBar variant="hero" onSearch={handleSearch} />
          </div>

          <ul className="mt-10 flex gap-3 overflow-x-auto pb-2 scrollbar-none">
            {categories.map((cat) => (
              <li key={cat.label} className="shrink-0">
                <Link
                  to="/explore"
                  className="flex items-center gap-2 rounded-full border border-stone-200 bg-white px-4 py-2.5 text-sm font-semibold text-ink shadow-sm transition hover:border-stone-300 hover:shadow"
                >
                  <span aria-hidden>{cat.emoji}</span>
                  {cat.label}
                </Link>
              </li>
            ))}
          </ul>
        </div>
      </section>

      <section className="page-container py-14 sm:py-16">
        <div className="flex flex-wrap items-end justify-between gap-4">
          <div>
            <h2 className="section-heading">Popular stays</h2>
            <p className="mt-2 text-muted">
              Hand-picked from live listings — updated as hosts publish.
            </p>
          </div>
          <Link to="/explore">
            <Button variant="outline" size="sm">View all</Button>
          </Link>
        </div>

        {isLoading ? (
          <ul className="mt-8 grid gap-x-6 gap-y-10 sm:grid-cols-2 lg:grid-cols-4">
            {Array.from({ length: 8 }).map((_, i) => (
              <li key={i}>
                <PropertyCardSkeleton />
              </li>
            ))}
          </ul>
        ) : preview.length > 0 ? (
          <ul className="mt-8 grid gap-x-6 gap-y-10 sm:grid-cols-2 lg:grid-cols-4">
            {preview.map((property) => (
              <li key={property.id}>
                <PropertyCard property={property} />
              </li>
            ))}
          </ul>
        ) : (
          <div className="mt-10 rounded-2xl border border-dashed border-stone-300 bg-surface-muted px-6 py-14 text-center">
            <p className="font-semibold text-ink">No listings yet</p>
            <p className="mt-2 text-sm text-muted">
              Be the first host in your area.
            </p>
            <Link to="/host/listings/new" className="mt-6 inline-block">
              <Button>List a property</Button>
            </Link>
          </div>
        )}
      </section>

      <section className="border-t border-stone-200 bg-surface-muted">
        <div className="page-container py-14 sm:py-16">
          <h2 className="section-heading text-center">Why guests choose us</h2>
          <ul className="mt-12 grid gap-8 sm:grid-cols-3">
            {trustPoints.map(({ icon: Icon, title, text }) => (
              <li key={title} className="text-center sm:text-left">
                <span
                  className="mx-auto flex h-12 w-12 items-center justify-center rounded-2xl bg-white text-brand-600 shadow-sm sm:mx-0"
                  aria-hidden
                >
                  <Icon className="h-6 w-6" />
                </span>
                <h3 className="mt-4 text-lg font-semibold text-ink">{title}</h3>
                <p className="mt-2 text-sm leading-relaxed text-muted">{text}</p>
              </li>
            ))}
          </ul>
        </div>
      </section>
    </div>
  );
}
