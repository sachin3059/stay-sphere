export type StripeIntentData = {
  paymentId: string;
  clientSecret: string;
  paymentIntentId: string;
  amount: number;
  currency: string;
  bookingId: string;
  publishableKey: string;
};

export type PaymentRecord = {
  id: string;
  bookingId: string;
  status: string;
  amount: number;
  currency: string;
  transactionId?: string;
};
