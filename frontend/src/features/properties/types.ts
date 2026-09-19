export type Property = {
  id: string;
  hostId: string;
  title: string;
  description?: string;
  city: string;
  country: string;
  address?: string;
  latitude?: number;
  longitude?: number;
  pricePerNight: number;
  maxGuests?: number;
  bedrooms?: number;
  bathrooms?: number;
  propertyType?: string;
  status?: string;
  amenities?: string[];
  imageUrls?: string[];
  createdAt?: string;
};

export type PropertySearchParams = {
  query?: string;
  city?: string;
  guests?: number;
  minPrice?: number;
  maxPrice?: number;
};

export type PropertyType =
  | "APARTMENT"
  | "HOUSE"
  | "VILLA"
  | "STUDIO"
  | "CABIN"
  | "COTTAGE";

export type PropertyStatus = "ACTIVE" | "INACTIVE" | "UNDER_REVIEW";

export type UpdatePropertyPayload = CreatePropertyPayload;

export type CreatePropertyPayload = {
  title: string;
  description?: string;
  city: string;
  country: string;
  address?: string;
  latitude?: number;
  longitude?: number;
  pricePerNight: number;
  maxGuests?: number;
  bedrooms?: number;
  bathrooms?: number;
  propertyType: PropertyType;
  amenities?: string[];
};

export type CreatePricingRulePayload = {
  propertyId: string;
  basePrice: number;
  minimumStay?: number;
};
