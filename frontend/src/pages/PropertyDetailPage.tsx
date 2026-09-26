import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { PropertyAvailabilityChecker } from "@/components/availability/PropertyAvailabilityChecker";
import { PropertyImageGallery } from "@/components/properties/PropertyImageGallery";
import { PropertyLocationMap } from "@/components/properties/PropertyLocationMap";
import { fetchPropertyById } from "@/features/properties/api";
import { formatInr, formatPropertyType } from "@/lib/format";
import { ApiError } from "@/lib/api/types";
import { useAuthStore } from "@/store/authStore";
import { useQuery } from "@tanstack/react-query";
import { Bath, Bed, MapPin, Share2, Users } from "lucide-react";
import { Link, useParams } from "react-router-dom";

export function PropertyDetailPage() {
  const { id } = useParams<{ id: string }>();
  const accessToken = useAuthStore((s) => s.accessToken);

  const { data, isLoading, error } = useQuery({
    queryKey: ["property", id],
    queryFn: () => fetchPropertyById(id!),
    enabled: Boolean(id),
  });

  if (!id) {
    return null;
  }

  if (isLoading) {
    return (
      <div className="page-container py-16 text-muted">Loading stay…</div>
    );
  }

  if (error || !data) {
    const message =
      error instanceof ApiError ? error.message : "Property not found.";
    return (
      <div className="page-container max-w-lg py-16 text-center">
        <p className="text-red-700">{message}</p>
        <Link to="/explore" className="mt-4 inline-block font-semibold text-brand-600 hover:underline">
          Back to explore
        </Link>
      </div>
    );
  }

  const property = data;
  const locationLabel = [
    property.address,
    property.city,
    property.country,
  ]
    .filter(Boolean)
    .join(", ");

  return (
    <div className="page-container py-6 sm:py-8">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight text-ink sm:text-[1.75rem]">
            {property.title}
          </h1>
          <p className="mt-2 flex flex-wrap items-center gap-2 text-sm text-muted">
            <span className="flex items-center gap-1">
              <MapPin className="h-4 w-4 shrink-0" />
              {property.city}, {property.country}
            </span>
            <span aria-hidden>·</span>
            <span className="font-medium text-ink">★ New</span>
          </p>
        </div>
        <button
          type="button"
          className="flex items-center gap-2 rounded-lg px-3 py-2 text-sm font-semibold underline hover:bg-stone-100"
          onClick={() => navigator.clipboard?.writeText(window.location.href)}
        >
          <Share2 className="h-4 w-4" />
          Share
        </button>
      </div>

      <div className="mt-6">
        <PropertyImageGallery
          title={property.title}
          imageUrls={property.imageUrls}
          propertyTypeLabel={formatPropertyType(property.propertyType)}
        />
      </div>

      <div className="mt-10 grid gap-12 lg:grid-cols-[1fr_360px] lg:items-start">
        <div className="min-w-0">
          <div className="flex flex-wrap items-center justify-between gap-4 border-b border-stone-200 pb-8">
            <div>
              <p className="text-lg font-semibold text-ink">
                {formatPropertyType(property.propertyType)} hosted stay
              </p>
              <div className="mt-3 flex flex-wrap gap-4 text-sm text-stone-600">
                {property.maxGuests != null && (
                  <span className="flex items-center gap-1.5">
                    <Users className="h-4 w-4" /> {property.maxGuests} guests
                  </span>
                )}
                {property.bedrooms != null && (
                  <span className="flex items-center gap-1.5">
                    <Bed className="h-4 w-4" /> {property.bedrooms} bedrooms
                  </span>
                )}
                {property.bathrooms != null && (
                  <span className="flex items-center gap-1.5">
                    <Bath className="h-4 w-4" /> {property.bathrooms} baths
                  </span>
                )}
              </div>
            </div>
          </div>

          {property.description && (
            <section className="border-b border-stone-200 py-8">
              <h2 className="text-xl font-semibold text-ink">About this place</h2>
              <p className="mt-4 whitespace-pre-line leading-relaxed text-muted">
                {property.description}
              </p>
            </section>
          )}

          {property.amenities && property.amenities.length > 0 && (
            <section className="border-b border-stone-200 py-8">
              <h2 className="text-xl font-semibold text-ink">What this place offers</h2>
              <ul className="mt-6 grid gap-3 sm:grid-cols-2">
                {property.amenities.map((a) => (
                  <li
                    key={a}
                    className="flex items-center gap-3 text-sm text-stone-700"
                  >
                    <span className="h-1.5 w-1.5 rounded-full bg-ink" aria-hidden />
                    {a}
                  </li>
                ))}
              </ul>
            </section>
          )}

          <PropertyLocationMap
            latitude={property.latitude}
            longitude={property.longitude}
            label={locationLabel}
          />
        </div>

        <div className="lg:sticky lg:top-24">
          <Card className="border-stone-200 p-6 shadow-[0_6px_16px_rgba(0,0,0,0.08)]">
            <p className="text-2xl font-semibold text-ink">
              {formatInr(property.pricePerNight)}
              <span className="text-base font-normal text-muted"> / night</span>
            </p>
            {accessToken ? (
              <Link to={`/properties/${property.id}/book`} className="mt-5 block">
                <Button className="w-full rounded-lg" size="lg">Reserve</Button>
              </Link>
            ) : (
              <Link
                to="/login"
                state={{ from: `/properties/${property.id}/book` }}
                className="mt-5 block"
              >
                <Button className="w-full rounded-lg" size="lg">Sign in to reserve</Button>
              </Link>
            )}
            <p className="mt-3 text-center text-xs text-muted">
              You won&apos;t be charged yet
            </p>
            <PropertyAvailabilityChecker
              propertyId={property.id}
              signedIn={Boolean(accessToken)}
            />
          </Card>
        </div>
      </div>
    </div>
  );
}
