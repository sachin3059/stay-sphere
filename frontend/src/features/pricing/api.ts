import { apiData } from "@/lib/api/client";
import type { PriceCalculation } from "./types";

export async function calculateStayPrice(
  propertyId: string,
  checkIn: string,
  checkOut: string,
) {
  return apiData<PriceCalculation>("/api/pricing/calculate", {
    method: "POST",
    body: { propertyId, checkIn, checkOut },
  });
}
