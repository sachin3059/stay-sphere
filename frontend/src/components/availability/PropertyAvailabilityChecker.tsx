import { AvailabilityStatusBanner } from "@/components/availability/AvailabilityStatusBanner";
import { Input } from "@/components/ui/Input";
import { checkAvailability } from "@/features/availability/api";
import { useQuery } from "@tanstack/react-query";
import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { Button } from "@/components/ui/Button";

type Props = {
  propertyId: string;
  signedIn: boolean;
};

export function PropertyAvailabilityChecker({ propertyId, signedIn }: Props) {
  const [checkIn, setCheckIn] = useState("");
  const [checkOut, setCheckOut] = useState("");

  const canCheck = Boolean(
    checkIn && checkOut && checkOut > checkIn,
  );

  const { data, isFetching } = useQuery({
    queryKey: ["availability-check", propertyId, checkIn, checkOut],
    queryFn: () => checkAvailability(propertyId, checkIn, checkOut),
    enabled: canCheck,
  });

  const bookHref = useMemo(() => {
    if (!canCheck || !data?.available) return `/properties/${propertyId}/book`;
    const qs = new URLSearchParams({ checkIn, checkOut });
    return `/properties/${propertyId}/book?${qs}`;
  }, [canCheck, checkIn, checkOut, data?.available, propertyId]);

  return (
    <div className="mt-4 space-y-3 border-t border-border pt-4">
      <p className="text-sm font-medium text-ink">Check dates</p>
      <Input
        label="Check-in"
        type="date"
        value={checkIn}
        onChange={(e) => setCheckIn(e.target.value)}
      />
      <Input
        label="Check-out"
        type="date"
        value={checkOut}
        onChange={(e) => setCheckOut(e.target.value)}
      />
      <AvailabilityStatusBanner
        canCheck={canCheck}
        checking={isFetching}
        result={data}
      />
      {signedIn && data?.available && canCheck && (
        <Link to={bookHref}>
          <Button className="w-full" size="sm">
            Book these dates
          </Button>
        </Link>
      )}
    </div>
  );
}
