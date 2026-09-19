import { apiData, getApiBaseUrl } from "@/lib/api/client";
import { fetchWithAuthRetry } from "@/lib/auth/refreshSession";
import { ApiError, type ApiErrorBody, type ApiResponse } from "@/lib/api/types";
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

export type ImageUploadResult = {
  propertyId: string;
  imageUrls: string[];
  totalImages: number;
};

export async function uploadPropertyImages(
  token: string,
  propertyId: string,
  files: File[],
) {
  if (files.length === 0) {
    throw new Error("No files selected");
  }
  const form = new FormData();
  for (const file of files) {
    form.append("files", file);
  }
  const url = `${getApiBaseUrl()}/api/properties/${propertyId}/images`;
  const response = await fetchWithAuthRetry(url, {
    method: "POST",
    token,
    body: form,
  });

  const text = await response.text();
  let json: unknown = null;
  if (text) {
    try {
      json = JSON.parse(text);
    } catch {
      /* non-json */
    }
  }

  if (!response.ok) {
    const errBody = json as ApiErrorBody | null;
    const message =
      errBody?.message ??
      (typeof json === "object" && json && "error" in json
        ? String((json as { error: string }).error)
        : response.statusText) ??
      "Upload failed";
    throw new ApiError(message, response.status, errBody ?? undefined);
  }

  const wrapped = json as ApiResponse<ImageUploadResult>;
  return wrapped.data;
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
