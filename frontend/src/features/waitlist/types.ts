export type WaitlistEntry = {
  id: string;
  propertyId: string;
  guestId: string;
  checkIn: string;
  checkOut: string;
  totalGuests: number;
  queuePosition: number;
  status: string;
  slotOfferedAt?: string;
  slotExpiresAt?: string;
  createdAt?: string;
};

export type JoinWaitlistPayload = {
  propertyId: string;
  checkIn: string;
  checkOut: string;
  totalGuests: number;
};
