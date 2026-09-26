import { cn } from "@/lib/cn";

type Props = {
  className?: string;
};

export function Skeleton({ className }: Props) {
  return (
    <div
      className={cn("animate-pulse rounded-lg bg-stone-200/80", className)}
      aria-hidden
    />
  );
}

export function PropertyCardSkeleton() {
  return (
    <div className="space-y-3">
      <Skeleton className="aspect-square w-full rounded-xl" />
      <Skeleton className="h-4 w-3/4" />
      <Skeleton className="h-4 w-1/2" />
      <Skeleton className="h-4 w-1/3" />
    </div>
  );
}
