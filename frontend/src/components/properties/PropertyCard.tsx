import { Card } from "@/components/ui/Card";
import { formatInr, formatPropertyType } from "@/lib/format";
import type { Property } from "@/features/properties/types";
import { MapPin, Users } from "lucide-react";
import { Link } from "react-router-dom";

type Props = {
  property: Property;
};

export function PropertyCard({ property }: Props) {
  const image = property.imageUrls?.[0];

  return (
    <Link to={`/properties/${property.id}`} className="group block h-full">
      <Card className="flex h-full flex-col overflow-hidden transition-shadow group-hover:shadow-md">
        <div className="relative aspect-[4/3] bg-gradient-to-br from-brand-100 to-stone-200">
          {image ? (
            <img
              src={image}
              alt={property.title}
              className="h-full w-full object-cover"
              loading="lazy"
            />
          ) : (
            <div className="flex h-full items-center justify-center text-sm font-medium text-brand-800/60">
              {formatPropertyType(property.propertyType)}
            </div>
          )}
        </div>
        <div className="flex flex-1 flex-col p-4">
          <div className="flex items-start justify-between gap-2">
            <h3 className="font-display text-lg font-semibold leading-snug text-ink group-hover:text-brand-700">
              {property.title}
            </h3>
            <p className="shrink-0 text-sm font-semibold text-ink">
              {formatInr(property.pricePerNight)}
              <span className="font-normal text-muted">/night</span>
            </p>
          </div>
          <p className="mt-1 flex items-center gap-1 text-sm text-muted">
            <MapPin className="h-3.5 w-3.5 shrink-0" />
            {property.city}, {property.country}
          </p>
          {property.maxGuests != null && (
            <p className="mt-2 flex items-center gap-1 text-xs text-stone-500">
              <Users className="h-3.5 w-3.5" />
              Up to {property.maxGuests} guests
            </p>
          )}
        </div>
      </Card>
    </Link>
  );
}
