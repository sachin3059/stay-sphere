import { apiData } from "@/lib/api/client";
import { ApiError, type ApiErrorBody, type ApiResponse } from "@/lib/api/types";
import type {
  CreatePropertyPayload,
  CreatePricingRulePayload,
  Property,
  PropertyPage,
  PropertySearchParams,
  PropertyStatus,
  UpdatePropertyPayload,
} from "./types";
import { EXPLORE_PAGE_SIZE } from "./exploreSort";

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
  if (params.checkIn) qs.set("checkIn", params.checkIn);
  if (params.checkOut) qs.set("checkOut", params.checkOut);
  qs.set("page", String(params.page ?? 0));
  qs.set("size", String(params.size ?? EXPLORE_PAGE_SIZE));
  qs.set("sort", params.sort ?? "newest");
  return `?${qs.toString()}`;
}

export async function fetchAllProperties(params?: PropertySearchParams) {
  return apiData<PropertyPage>(`/api/properties${toQuery(params ?? {})}`);
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

export async function updateProperty(
  token: string,
  propertyId: string,
  body: UpdatePropertyPayload,
) {
  return apiData<Property>(`/api/properties/${propertyId}`, {
    method: "PUT",
    body,
    token,
  });
}

export async function updatePropertyStatus(
  token: string,
  propertyId: string,
  status: PropertyStatus,
) {
  return apiData<Property>(`/api/properties/${propertyId}/status`, {
    method: "PATCH",
    body: { status },
    token,
  });
}

export async function deletePropertyImage(
  token: string,
  propertyId: string,
  imageUrl: string,
) {
  const qs = new URLSearchParams({ url: imageUrl });
  return apiData<Property>(
    `/api/properties/${propertyId}/images?${qs}`,
    { method: "DELETE", token },
  );
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

export type PricingRule = {
  id: string;
  propertyId: string;
  basePrice: number;
  minimumStay?: number;
};

export async function fetchPricingRule(propertyId: string) {
  return apiData<PricingRule>(`/api/pricing/rules/${propertyId}`);
}

export async function updatePricingRule(
  token: string,
  propertyId: string,
  body: { basePrice?: number; minimumStay?: number },
) {
  return apiData<unknown>(`/api/pricing/rules/${propertyId}`, {
    method: "PUT",
    body,
    token,
  });
}

export type ImageUploadResult = {
  propertyId: string;
  imageUrls: string[];
  totalImages: number;
};

type PresignedUploadItem = {
  uploadUrl: string;
  objectKey: string;
  publicUrl: string;
  contentType: string;
};

type PresignUploadResult = {
  propertyId: string;
  uploads: PresignedUploadItem[];
};

export async function uploadPropertyImages(
  token: string,
  propertyId: string,
  files: File[],
) {
  if (files.length === 0) {
    throw new Error("No files selected");
  }

  const presign = await apiData<PresignUploadResult>(
    `/api/properties/${propertyId}/images/presign`,
    {
      method: "POST",
      token,
      body: {
        files: files.map((file) => ({
          fileName: file.name,
          contentType: file.type || "application/octet-stream",
          sizeBytes: file.size,
        })),
      },
    },
  );

  if (presign.uploads.length !== files.length) {
    throw new Error("Presign response did not match selected files");
  }

  for (let i = 0; i < files.length; i++) {
    const item = presign.uploads[i];
    const putResponse = await fetch(item.uploadUrl, {
      method: "PUT",
      headers: {
        "Content-Type": item.contentType,
      },
      body: files[i],
    });
    if (!putResponse.ok) {
      throw new ApiError(
        `Direct upload to storage failed (${putResponse.status})`,
        putResponse.status,
      );
    }
  }

  return apiData<ImageUploadResult>(
    `/api/properties/${propertyId}/images/confirm`,
    {
      method: "POST",
      token,
      body: {
        objectKeys: presign.uploads.map((u) => u.objectKey),
      },
    },
  );
}

/** Server-side sort + pagination (F5). */
export async function searchProperties(params: PropertySearchParams) {
  return apiData<PropertyPage>(
    `/api/properties/search/advanced${toQuery(params)}`,
  );
}
