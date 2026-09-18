export type Booking = {
  id: string;
  propertyId: string;
  guestId: string;
  hostId: string;
  checkIn: string;
  checkOut: string;
  totalGuests: number;
  totalPrice: number;
  pricePerNight: number;
  status: string;
  idempotencyKey?: string;
  createdAt?: string;
  confirmedAt?: string;
};

export type CreateBookingPayload = {
  propertyId: string;
  hostId: string;
  checkIn: string;
  checkOut: string;
  totalGuests: number;
};
