import { Button } from "@/components/ui/Button";
import {
  PaymentElement,
  useElements,
  useStripe,
} from "@stripe/react-stripe-js";
import { useState } from "react";

type Props = {
  onPaid: () => void;
  disabled?: boolean;
};

export function StripeCheckoutForm({ onPaid, disabled }: Props) {
  const stripe = useStripe();
  const elements = useElements();
  const [error, setError] = useState<string | null>(null);
  const [processing, setProcessing] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!stripe || !elements) return;

    setProcessing(true);
    setError(null);

    const { error: submitError } = await stripe.confirmPayment({
      elements,
      confirmParams: {
        return_url: `${window.location.origin}/bookings/complete`,
      },
      redirect: "if_required",
    });

    setProcessing(false);

    if (submitError) {
      setError(submitError.message ?? "Payment failed.");
      return;
    }

    onPaid();
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <PaymentElement
        options={{
          layout: "tabs",
        }}
      />
      {error && (
        <p className="text-sm text-red-600" role="alert">{error}</p>
      )}
      <Button
        type="submit"
        className="w-full"
        disabled={!stripe || processing || disabled}
      >
        {processing ? "Processing…" : "Pay now"}
      </Button>
      <p className="text-center text-xs text-stone-400">
        Test card: 4242 4242 4242 4242 · any future expiry · any CVC
      </p>
    </form>
  );
}
