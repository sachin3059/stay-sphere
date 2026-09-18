import { apiData } from "@/lib/api/client";
import type {
  CreatePropertyPayload,
  CreatePricingRulePayload,
  Property,
  PropertySearchParams,
} from "./types";

function toQuery(params: PropertySearchParams): string {
  const qs = new URLSearchParams();
  if (params.query?.trim()) qs.set("query", params.query.trim());
  if (params.city?.trim()) qs.set("city", params.city.trim());
  if (params.guests != null && params.guests > 0) {
    qs.set("guests", String(params.guests));
  }
  if (params.minPrice != null && params.minPrice > 0) {
    qs.set("minPrice", String(params.minPrice));
  }
  if (params.maxPrice != null && params.maxPrice > 0) {
    qs.set("maxPrice", String(params.maxPrice));
  }
  const s = qs.toString();
  return s ? `?${s}` : "";
}

export async function fetchAllProperties() {
  return apiData<Property[]>("/api/properties");
}

export async function fetchPropertyById(id: string) {
  return apiData<Property>(`/api/properties/${id}`);
}

export async function fetchHostProperties(token: string) {
  return apiData<Property[]>("/api/properties/host", { token });
}

export async function createProperty(
  token: string,
  body: CreatePropertyPayload,
) {
  return apiData<Property>("/api/properties", {
    method: "POST",
    body,
    token,
  });
}

export async function createPricingRule(
  token: string,
  body: CreatePricingRulePayload,
) {
  return apiData<unknown>("/api/pricing/rules", {
    method: "POST",
    body,
    token,
  });
}

export async function searchProperties(params: PropertySearchParams) {
  const hasFilter =
    params.query ||
    params.city ||
    params.guests ||
    params.minPrice ||
    params.maxPrice;
  if (!hasFilter) {
    return fetchAllProperties();
  }
  return apiData<Property[]>(
    `/api/properties/search/advanced${toQuery(params)}`,
  );
}
