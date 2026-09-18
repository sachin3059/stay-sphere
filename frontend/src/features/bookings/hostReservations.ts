import { fetchPropertyBookings } from "@/features/bookings/api";
import type { Booking } from "@/features/bookings/types";
import { fetchHostProperties } from "@/features/properties/api";

export type HostReservation = Booking & {
  propertyTitle: string;
};

export async function fetchHostReservations(
  token: string,
): Promise<HostReservation[]> {
  const properties = await fetchHostProperties(token);
  if (properties.length === 0) {
    return [];
  }

  const nested = await Promise.all(
    properties.map(async (property) => {
      const bookings = await fetchPropertyBookings(token, property.id);
      return bookings.map((b) => ({
        ...b,
        propertyTitle: property.title,
      }));
    }),
  );

  return nested
    .flat()
    .sort((a, b) => {
      const aTime = a.createdAt ?? a.checkIn;
      const bTime = b.createdAt ?? b.checkIn;
      return bTime.localeCompare(aTime);
    });
}
