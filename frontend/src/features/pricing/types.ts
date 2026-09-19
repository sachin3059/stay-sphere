export type PriceCalculation = {
  propertyId: string;
  basePrice: number;
  finalPricePerNight: number;
  totalPrice: number;
  totalNights: number;
  weekendMultiplier?: number;
  peakSeasonMultiplier?: number;
  longStayDiscount?: number;
  appliedRules?: string[];
};
