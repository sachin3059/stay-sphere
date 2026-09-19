import type { AvailabilityCheck } from "@/features/availability/types";

type Props = {
  checking: boolean;
  result: AvailabilityCheck | undefined;
  canCheck: boolean;
};

export function AvailabilityStatusBanner({ checking, result, canCheck }: Props) {
  if (!canCheck) {
    return null;
  }

  if (checking) {
    return (
      <p className="text-sm text-muted" role="status">Checking availability…</p>
    );
  }

  if (!result) {
    return null;
  }

  if (result.available) {
    return (
      <p
        className="rounded-lg bg-green-50 px-3 py-2 text-sm text-green-800"
        role="status"
      >
        {result.message}
      </p>
    );
  }

  return (
    <p
      className="rounded-lg bg-amber-50 px-3 py-2 text-sm text-amber-900"
      role="alert"
    >
      {result.message}
    </p>
  );
}
