import { apiData } from "@/lib/api/client";
import type { Property } from "@/features/properties/types";

export type AdminUser = {
  id: string;
  email: string;
  fullName: string;
  role: "GUEST" | "HOST" | "ADMIN";
  socialOnly: boolean;
  createdAt: string;
};

export async function fetchAdminUsers(token: string) {
  return apiData<AdminUser[]>("/api/admin/users", { token });
}

export async function updateAdminUserRole(
  token: string,
  userId: string,
  role: AdminUser["role"],
) {
  return apiData<AdminUser>(`/api/admin/users/${userId}/role`, {
    method: "PATCH",
    token,
    body: { role },
  });
}

export async function fetchAdminListings(token: string) {
  return apiData<Property[]>("/api/admin/properties", { token });
}

export async function updateAdminListingStatus(
  token: string,
  propertyId: string,
  status: string,
) {
  return apiData<Property>(`/api/admin/properties/${propertyId}/status`, {
    method: "PATCH",
    token,
    body: { status },
  });
}
