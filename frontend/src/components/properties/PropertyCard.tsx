import { formatInr, formatPropertyType } from "@/lib/format";
import type { Property } from "@/features/properties/types";
import { MapPin } from "lucide-react";
import { Link } from "react-router-dom";

type Props = {
  property: Property;
};

export function PropertyCard({ property }: Props) {
  const image = property.imageUrls?.[0];
  const location = [property.city, property.country].filter(Boolean).join(", ");

  return (
    <Link to={`/properties/${property.id}`} className="group block">
      <div className="relative aspect-square overflow-hidden rounded-xl bg-stone-100">
        {image ? (
          <img
            src={image}
            alt={property.title}
            className="h-full w-full object-cover transition duration-300 group-hover:scale-105"
            loading="lazy"
          />
        ) : (
          <div className="flex h-full flex-col items-center justify-center gap-2 px-4 text-center text-sm font-medium text-stone-500">
            <MapPin className="h-8 w-8 text-stone-400" aria-hidden />
            {formatPropertyType(property.propertyType)}
          </div>
        )}
      </div>

      <div className="mt-3 space-y-1">
        <div className="flex items-start justify-between gap-2">
          <p className="line-clamp-1 font-semibold text-[15px] text-ink">
            {location || property.title}
          </p>
          <span className="shrink-0 text-sm text-ink" aria-hidden>
            ★ <span className="font-normal text-stone-600">New</span>
          </span>
        </div>
        <p className="line-clamp-1 text-sm text-muted">{property.title}</p>
        {property.maxGuests != null && (
          <p className="text-sm text-muted">
            Up to {property.maxGuests} guests
          </p>
        )}
        <p className="pt-0.5 text-[15px] text-ink">
          <span className="font-semibold">{formatInr(property.pricePerNight)}</span>
          <span className="font-normal text-ink"> night</span>
        </p>
      </div>
    </Link>
  );
}
