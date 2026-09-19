import type { PriceCalculation } from "@/features/pricing/types";
import { formatInr } from "@/lib/format";
import { ApiError } from "@/lib/api/types";

type Props = {
  loading: boolean;
  quote: PriceCalculation | undefined;
  error: unknown;
};

export function BookingPriceSummary({ loading, quote, error }: Props) {
  if (!loading && !quote && !error) {
    return null;
  }

  if (loading) {
    return (
      <p className="text-sm text-muted" role="status">Calculating price…</p>
    );
  }

  if (error) {
    const message =
      error instanceof ApiError
        ? error.message
        : "Could not calculate price for these dates.";
    return (
      <p className="rounded-lg bg-amber-50 px-3 py-2 text-sm text-amber-900" role="alert">
        {message}
      </p>
    );
  }

  if (!quote) {
    return null;
  }

  return (
    <div className="rounded-xl border border-border bg-stone-50/80 px-4 py-3 text-sm">
      <div className="flex items-baseline justify-between gap-2">
        <span className="text-muted">
          {quote.totalNights} night{quote.totalNights !== 1 ? "s" : ""} ×{" "}
          {formatInr(quote.finalPricePerNight)}
        </span>
        <span className="text-lg font-semibold text-ink">
          {formatInr(quote.totalPrice)}
        </span>
      </div>
      {quote.appliedRules && quote.appliedRules.length > 0 && (
        <ul className="mt-2 space-y-0.5 text-xs text-stone-600">
          {quote.appliedRules.map((rule) => (
            <li key={rule}>· {rule}</li>
          ))}
        </ul>
      )}
      <p className="mt-2 text-xs text-stone-500">
        This total is calculated by the server and matches your booking charge.
      </p>
    </div>
  );
}
