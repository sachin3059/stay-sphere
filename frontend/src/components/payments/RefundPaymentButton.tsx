import { Button } from "@/components/ui/Button";
import {
  fetchPaymentByBooking,
  refundPayment,
} from "@/features/payments/api";
import { canRequestRefund } from "@/features/payments/refund";
import type { PaymentRecord } from "@/features/payments/types";
import { ApiError } from "@/lib/api/types";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";

type Props = {
  accessToken: string;
  bookingId: string;
  bookingStatus: string;
  /** When parent already loaded payment, pass it to skip a fetch. */
  payment?: PaymentRecord | null;
  size?: "sm" | "md";
};

export function RefundPaymentButton({
  accessToken,
  bookingId,
  bookingStatus,
  payment: paymentProp,
  size = "sm",
}: Props) {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);

  const needsFetch =
    bookingStatus === "CANCELLED" && paymentProp === undefined;

  const { data: fetchedPayment } = useQuery({
    queryKey: ["payment-by-booking", bookingId],
    queryFn: () => fetchPaymentByBooking(accessToken, bookingId),
    enabled: needsFetch,
  });

  const payment = paymentProp !== undefined ? paymentProp : fetchedPayment;

  const refund = useMutation({
    mutationFn: async () => {
      const p =
        payment ?? (await fetchPaymentByBooking(accessToken, bookingId));
      if (!p) {
        throw new ApiError("No payment found for this booking.", 404);
      }
      return refundPayment(accessToken, p.id);
    },
    onSuccess: () => {
      setError(null);
      queryClient.invalidateQueries({
        queryKey: ["payment-by-booking", bookingId],
      });
      queryClient.invalidateQueries({ queryKey: ["booking", bookingId] });
      queryClient.invalidateQueries({ queryKey: ["host-reservations"] });
      queryClient.invalidateQueries({ queryKey: ["my-bookings"] });
    },
    onError: (err: unknown) => {
      setError(
        err instanceof ApiError ? err.message : "Refund could not be processed.",
      );
    },
  });

  if (!canRequestRefund(bookingStatus, payment)) {
    return null;
  }

  return (
    <div className="flex flex-col items-start gap-1">
      <Button
        type="button"
        variant="outline"
        size={size}
        disabled={refund.isPending}
        onClick={() => refund.mutate()}
      >
        {refund.isPending ? "Refunding…" : "Issue refund"}
      </Button>
      {error && (
        <p className="text-xs text-red-600" role="alert">{error}</p>
      )}
    </div>
  );
}
