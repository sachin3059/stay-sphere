import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import {
  LocationPicker,
  type LocationPickerValue,
} from "@/components/maps/LocationPicker";
import { Input } from "@/components/ui/Input";
import { hasOlaMapsKey } from "@/features/maps/olaMaps";
import {
  fetchPricingRule,
  fetchPropertyById,
  updateProperty,
  updatePropertyStatus,
  updatePricingRule,
} from "@/features/properties/api";
import type { PropertyType } from "@/features/properties/types";
import { ApiError } from "@/lib/api/types";
import { useAuthStore } from "@/store/authStore";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";

const PROPERTY_TYPES: { value: PropertyType; label: string }[] = [
  { value: "APARTMENT", label: "Apartment" },
  { value: "HOUSE", label: "House" },
  { value: "VILLA", label: "Villa" },
  { value: "STUDIO", label: "Studio" },
  { value: "CABIN", label: "Cabin" },
  { value: "COTTAGE", label: "Cottage" },
];

export function EditListingPage() {
  const { id: propertyId } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const accessToken = useAuthStore((s) => s.accessToken)!;
  const [error, setError] = useState<string | null>(null);

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [city, setCity] = useState("");
  const [country, setCountry] = useState("");
  const [address, setAddress] = useState("");
  const [pricePerNight, setPricePerNight] = useState("");
  const [minimumStay, setMinimumStay] = useState("1");
  const [maxGuests, setMaxGuests] = useState("2");
  const [bedrooms, setBedrooms] = useState("1");
  const [bathrooms, setBathrooms] = useState("1");
  const [propertyType, setPropertyType] = useState<PropertyType>("APARTMENT");
  const [amenities, setAmenities] = useState("");
  const [status, setStatus] = useState<string>("ACTIVE");
  const [location, setLocation] = useState<LocationPickerValue>({
    latitude: 18.5362,
    longitude: 73.8938,
  });

  const { data: property, isLoading } = useQuery({
    queryKey: ["property", propertyId],
    queryFn: () => fetchPropertyById(propertyId!),
    enabled: Boolean(propertyId),
  });

  const { data: pricingRule } = useQuery({
    queryKey: ["pricing-rule", propertyId],
    queryFn: () => fetchPricingRule(propertyId!),
    enabled: Boolean(propertyId),
  });

  useEffect(() => {
    if (!property) return;
    setTitle(property.title);
    setDescription(property.description ?? "");
    setCity(property.city);
    setCountry(property.country);
    setAddress(property.address ?? "");
    setPricePerNight(String(property.pricePerNight));
    setMaxGuests(String(property.maxGuests ?? 2));
    setBedrooms(String(property.bedrooms ?? 1));
    setBathrooms(String(property.bathrooms ?? 1));
    setPropertyType((property.propertyType as PropertyType) ?? "APARTMENT");
    setAmenities((property.amenities ?? []).join(", "));
    setStatus(property.status ?? "ACTIVE");
    setLocation({
      latitude: property.latitude ?? 18.5362,
      longitude: property.longitude ?? 73.8938,
      addressLine: property.address,
      city: property.city,
      country: property.country,
    });
  }, [property]);

  useEffect(() => {
    if (pricingRule?.minimumStay != null) {
      setMinimumStay(String(pricingRule.minimumStay));
    }
  }, [pricingRule]);

  const save = useMutation({
    mutationFn: async () => {
      const price = Number(pricePerNight);
      const updated = await updateProperty(accessToken, propertyId!, {
        title: title.trim(),
        description: description.trim() || undefined,
        city: city.trim(),
        country: country.trim(),
        address: address.trim() || location.addressLine || undefined,
        latitude: location.latitude,
        longitude: location.longitude,
        pricePerNight: price,
        maxGuests: Number(maxGuests),
        bedrooms: Number(bedrooms),
        bathrooms: Number(bathrooms),
        propertyType,
        amenities: amenities
          .split(",")
          .map((a) => a.trim())
          .filter(Boolean),
      });
      await updatePricingRule(accessToken, propertyId!, {
        basePrice: price,
        minimumStay: Number(minimumStay) || 1,
      });
      return updated;
    },
    onSuccess: (updated) => {
      queryClient.invalidateQueries({ queryKey: ["property", propertyId] });
      queryClient.invalidateQueries({ queryKey: ["host-properties"] });
      navigate(`/properties/${updated.id}`, { replace: true });
    },
    onError: (err: unknown) => {
      setError(
        err instanceof ApiError ? err.message : "Could not save listing.",
      );
    },
  });

  const toggleStatus = useMutation({
    mutationFn: (next: "ACTIVE" | "INACTIVE") =>
      updatePropertyStatus(accessToken, propertyId!, next),
    onSuccess: (updated) => {
      setStatus(updated.status ?? "ACTIVE");
      queryClient.invalidateQueries({ queryKey: ["property", propertyId] });
      queryClient.invalidateQueries({ queryKey: ["host-properties"] });
    },
    onError: (err: unknown) => {
      setError(
        err instanceof ApiError ? err.message : "Could not update status.",
      );
    },
  });

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    if (!title.trim() || !city.trim() || !pricePerNight) {
      setError("Title, city, and price are required.");
      return;
    }
    if (!hasOlaMapsKey()) {
      setError("Map API key is missing. Set VITE_OLA_MAPS_API_KEY in frontend/.env.");
      return;
    }
    if (
      !Number.isFinite(location.latitude) ||
      !Number.isFinite(location.longitude)
    ) {
      setError("Pick a location on the map.");
      return;
    }
    save.mutate();
  }

  if (!propertyId) return null;

  if (isLoading || !property) {
    return (
      <div className="mx-auto max-w-2xl px-4 py-16 text-muted">Loading…</div>
    );
  }

  const isInactive = status === "INACTIVE";

  return (
    <div className="mx-auto max-w-2xl px-4 py-10 sm:px-6">
      <Link
        to="/host/listings"
        className="text-sm font-medium text-brand-700 hover:underline"
      >
        ← My listings
      </Link>
      <h1 className="mt-4 font-display text-3xl font-semibold text-ink">
        Edit listing
      </h1>
      <p className="mt-2 text-sm text-muted">{property.title}</p>

      {isInactive && (
        <p className="mt-4 rounded-lg bg-amber-50 px-3 py-2 text-sm text-amber-900">
          This listing is unlisted — guests won&apos;t see it on Explore.
        </p>
      )}

      <Card className="mt-8 p-6 sm:p-8">
        <form onSubmit={handleSubmit} className="space-y-5">
          <Input
            label="Title"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            required
          />
          <div>
            <label className="mb-1.5 block text-sm font-medium text-stone-700">
              Description
            </label>
            <textarea
              className="w-full rounded-xl border border-border px-4 py-2.5 text-sm focus:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-500/20"
              rows={4}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <Input
              label="City"
              value={city}
              onChange={(e) => setCity(e.target.value)}
              required
            />
            <Input
              label="Country"
              value={country}
              onChange={(e) => setCountry(e.target.value)}
              required
            />
          </div>
          <Input
            label="Address"
            value={address}
            onChange={(e) => setAddress(e.target.value)}
          />
          <LocationPicker
            value={{
              ...location,
              addressLine: address || location.addressLine,
              city,
              country,
            }}
            disabled={save.isPending}
            onChange={(loc) => {
              setLocation(loc);
              if (loc.addressLine) setAddress(loc.addressLine);
              if (loc.city) setCity(loc.city);
              if (loc.country) setCountry(loc.country);
            }}
          />
          <div className="grid gap-4 sm:grid-cols-2">
            <Input
              label="Price per night (₹)"
              type="number"
              min={1}
              value={pricePerNight}
              onChange={(e) => setPricePerNight(e.target.value)}
              required
            />
            <Input
              label="Minimum stay (nights)"
              type="number"
              min={1}
              value={minimumStay}
              onChange={(e) => setMinimumStay(e.target.value)}
            />
          </div>
          <div>
            <label className="mb-1.5 block text-sm font-medium text-stone-700">
              Property type
            </label>
            <select
              className="w-full rounded-xl border border-border bg-white px-4 py-2.5 text-sm"
              value={propertyType}
              onChange={(e) =>
                setPropertyType(e.target.value as PropertyType)
              }
            >
              {PROPERTY_TYPES.map((t) => (
                <option key={t.value} value={t.value}>
                  {t.label}
                </option>
              ))}
            </select>
          </div>
          <div className="grid gap-4 sm:grid-cols-3">
            <Input
              label="Max guests"
              type="number"
              min={1}
              value={maxGuests}
              onChange={(e) => setMaxGuests(e.target.value)}
            />
            <Input
              label="Bedrooms"
              type="number"
              min={0}
              value={bedrooms}
              onChange={(e) => setBedrooms(e.target.value)}
            />
            <Input
              label="Bathrooms"
              type="number"
              min={0}
              value={bathrooms}
              onChange={(e) => setBathrooms(e.target.value)}
            />
          </div>
          <Input
            label="Amenities (comma-separated)"
            value={amenities}
            onChange={(e) => setAmenities(e.target.value)}
          />
          {error && (
            <p
              className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700"
              role="alert"
            >
              {error}
            </p>
          )}
          <Button type="submit" className="w-full" disabled={save.isPending}>
            {save.isPending ? "Saving…" : "Save changes"}
          </Button>
        </form>
      </Card>

      <Card className="mt-6 p-6">
        <h2 className="font-medium text-ink">Listing visibility</h2>
        <p className="mt-1 text-sm text-muted">
          Unlist to hide from Explore without deleting your property.
        </p>
        <div className="mt-4 flex flex-wrap gap-3">
          {isInactive ? (
            <Button
              type="button"
              disabled={toggleStatus.isPending}
              onClick={() => toggleStatus.mutate("ACTIVE")}
            >
              Publish again
            </Button>
          ) : (
            <Button
              type="button"
              variant="ghost"
              className="text-amber-800 hover:bg-amber-50"
              disabled={toggleStatus.isPending}
              onClick={() => toggleStatus.mutate("INACTIVE")}
            >
              Unlist property
            </Button>
          )}
          <Link
            to={`/host/listings/${propertyId}/photos`}
            className="inline-flex items-center text-sm font-medium text-brand-700 hover:underline"
          >
            Manage photos
          </Link>
        </div>
      </Card>
    </div>
  );
}
