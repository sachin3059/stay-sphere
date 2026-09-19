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
import { Bath, Bed, MapPin, Users } from "lucide-react";
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
      <div className="mx-auto max-w-4xl px-4 py-16 text-muted sm:px-6">
        Loading property…
      </div>
    );
  }

  if (error || !data) {
    const message =
      error instanceof ApiError ? error.message : "Property not found.";
    return (
      <div className="mx-auto max-w-lg px-4 py-16 text-center sm:px-6">
        <p className="text-red-700">{message}</p>
        <Link to="/explore" className="mt-4 inline-block text-brand-700 hover:underline">
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
    <div className="mx-auto max-w-4xl px-4 py-8 sm:px-6 sm:py-12">
      <Link
        to="/explore"
        className="text-sm font-medium text-brand-700 hover:underline"
      >
        ← All stays
      </Link>

      <div className="mt-6">
        <PropertyImageGallery
          title={property.title}
          imageUrls={property.imageUrls}
          propertyTypeLabel={formatPropertyType(property.propertyType)}
        />
      </div>

      <div className="mt-8 flex flex-col gap-6 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <p className="text-sm font-medium text-brand-700">
            {formatPropertyType(property.propertyType)}
          </p>
          <h1 className="mt-1 font-display text-3xl font-semibold text-ink">
            {property.title}
          </h1>
          <p className="mt-2 flex items-center gap-1 text-muted">
            <MapPin className="h-4 w-4" />
            {property.address
              ? `${property.address}, `
              : ""}
            {property.city}, {property.country}
          </p>
        </div>
        <Card className="shrink-0 p-5 sm:min-w-[240px]">
          <p className="text-2xl font-semibold text-ink">
            {formatInr(property.pricePerNight)}
            <span className="text-base font-normal text-muted"> / night</span>
          </p>
          {accessToken ? (
            <Link to={`/properties/${property.id}/book`} className="mt-4 block">
              <Button className="w-full">Book this stay</Button>
            </Link>
          ) : (
            <Link
              to="/login"
              state={{ from: `/properties/${property.id}/book` }}
              className="mt-4 block"
            >
              <Button className="w-full">Sign in to book</Button>
            </Link>
          )}
          <p className="mt-2 text-center text-xs text-stone-400">
            Secure checkout with Stripe
          </p>
          <PropertyAvailabilityChecker
            propertyId={property.id}
            signedIn={Boolean(accessToken)}
          />
        </Card>
      </div>

      <div className="mt-8 flex flex-wrap gap-4 text-sm text-stone-600">
        {property.maxGuests != null && (
          <span className="flex items-center gap-1.5">
            <Users className="h-4 w-4" /> {property.maxGuests} guests max
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

      {property.description && (
        <section className="mt-10">
          <h2 className="font-display text-xl font-semibold">About this stay</h2>
          <p className="mt-3 leading-relaxed text-muted">{property.description}</p>
        </section>
      )}

      {property.amenities && property.amenities.length > 0 && (
        <section className="mt-10">
          <h2 className="font-display text-xl font-semibold">Amenities</h2>
          <ul className="mt-4 flex flex-wrap gap-2">
            {property.amenities.map((a) => (
              <li
                key={a}
                className="rounded-full border border-border bg-white px-3 py-1 text-sm text-stone-700"
              >
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
  );
}
