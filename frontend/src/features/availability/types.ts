export type AvailabilityCheck = {
  propertyId: string;
  checkIn: string;
  checkOut: string;
  available: boolean;
  message: string;
};

export type AvailableRange = {
  from: string;
  to: string;
  nights: number;
};

export type BlockedDate = {
  id: string;
  propertyId: string;
  startDate: string;
  endDate: string;
  reason: string;
  referenceId?: string;
  createdAt?: string;
};

export type BlockDatesPayload = {
  propertyId: string;
  startDate: string;
  endDate: string;
  reason: "MAINTENANCE" | "HOST_BLOCK";
};
