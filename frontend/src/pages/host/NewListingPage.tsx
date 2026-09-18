import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { Input } from "@/components/ui/Input";
import { PropertyPhotoPicker } from "@/components/properties/PropertyPhotoPicker";
import {
  createPricingRule,
  createProperty,
  uploadPropertyImages,
} from "@/features/properties/api";
import type { PropertyType } from "@/features/properties/types";
import { ApiError } from "@/lib/api/types";
import { useAuthStore } from "@/store/authStore";
import { useMutation } from "@tanstack/react-query";
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

const PROPERTY_TYPES: { value: PropertyType; label: string }[] = [
  { value: "APARTMENT", label: "Apartment" },
  { value: "HOUSE", label: "House" },
  { value: "VILLA", label: "Villa" },
  { value: "STUDIO", label: "Studio" },
  { value: "CABIN", label: "Cabin" },
  { value: "COTTAGE", label: "Cottage" },
];

export function NewListingPage() {
  const navigate = useNavigate();
  const accessToken = useAuthStore((s) => s.accessToken)!;
  const [error, setError] = useState<string | null>(null);

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [city, setCity] = useState("Pune");
  const [country, setCountry] = useState("India");
  const [address, setAddress] = useState("");
  const [pricePerNight, setPricePerNight] = useState("2500");
  const [maxGuests, setMaxGuests] = useState("4");
  const [bedrooms, setBedrooms] = useState("2");
  const [bathrooms, setBathrooms] = useState("1");
  const [propertyType, setPropertyType] = useState<PropertyType>("APARTMENT");
  const [amenities, setAmenities] = useState("WiFi, Kitchen");
  const [photos, setPhotos] = useState<File[]>([]);

  const submit = useMutation({
    mutationFn: async () => {
      const price = Number(pricePerNight);
      const property = await createProperty(accessToken, {
        title: title.trim(),
        description: description.trim() || undefined,
        city: city.trim(),
        country: country.trim(),
        address: address.trim() || undefined,
        latitude: 18.5362,
        longitude: 73.8938,
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
      await createPricingRule(accessToken, {
        propertyId: property.id,
        basePrice: price,
        minimumStay: 1,
      });
      if (photos.length > 0) {
        await uploadPropertyImages(accessToken, property.id, photos);
      }
      return property;
    },
    onSuccess: (property) => {
      navigate(`/properties/${property.id}`, { replace: true });
    },
    onError: (err: unknown) => {
      setError(
        err instanceof ApiError ? err.message : "Could not create listing.",
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
    submit.mutate();
  }

  return (
    <div className="mx-auto max-w-2xl px-4 py-10 sm:px-6">
      <Link
        to="/host/listings"
        className="text-sm font-medium text-brand-700 hover:underline"
      >
        ← My listings
      </Link>
      <h1 className="mt-4 font-display text-3xl font-semibold text-ink">
        List a new stay
      </h1>
      <p className="mt-2 text-sm text-muted">
        We create a pricing rule automatically so guests can book later.
      </p>

      <Card className="mt-8 p-6 sm:p-8">
        <form onSubmit={handleSubmit} className="space-y-5">
          <Input
            label="Title"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="Cozy apartment in Koregaon Park"
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
              placeholder="What makes your place special?"
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
          <div className="grid gap-4 sm:grid-cols-2">
            <Input
              label="Price per night (₹)"
              type="number"
              min={1}
              value={pricePerNight}
              onChange={(e) => setPricePerNight(e.target.value)}
              required
            />
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
          <PropertyPhotoPicker
            files={photos}
            onChange={setPhotos}
            disabled={submit.isPending}
          />
          {error && (
            <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700" role="alert">
              {error}
            </p>
          )}
          <Button type="submit" className="w-full" disabled={submit.isPending}>
            {submit.isPending ? "Publishing…" : "Publish listing"}
          </Button>
        </form>
      </Card>
    </div>
  );
}
