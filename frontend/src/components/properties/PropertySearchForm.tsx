import { StaySearchBar } from "@/components/search/StaySearchBar";
import type { PropertySearchParams } from "@/features/properties/types";

type Props = {
  initial?: PropertySearchParams;
  onSearch: (params: PropertySearchParams) => void;
  loading?: boolean;
};

/** @deprecated Prefer StaySearchBar directly */
export function PropertySearchForm({ initial, onSearch, loading }: Props) {
  return (
    <StaySearchBar
      initial={initial}
      onSearch={onSearch}
      loading={loading}
      variant="filters"
    />
  );
}
