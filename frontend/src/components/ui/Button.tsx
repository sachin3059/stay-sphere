import { cn } from "@/lib/cn";
import type { ButtonHTMLAttributes } from "react";

type Variant = "primary" | "secondary" | "ghost" | "outline";
type Size = "sm" | "md" | "lg";

const variants: Record<Variant, string> = {
  primary:
    "bg-brand-600 text-white hover:bg-brand-700 shadow-sm active:scale-[0.98]",
  secondary: "bg-ink text-white hover:bg-stone-800",
  ghost: "text-ink hover:bg-stone-100",
  outline:
    "border border-stone-300 bg-white text-ink hover:border-ink hover:bg-stone-50",
};

const sizes: Record<Size, string> = {
  sm: "h-9 px-4 text-sm font-semibold",
  md: "h-11 px-5 text-sm font-semibold",
  lg: "h-12 px-6 text-base font-semibold",
};

type Props = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: Variant;
  size?: Size;
};

export function Button({
  className,
  variant = "primary",
  size = "md",
  type = "button",
  ...props
}: Props) {
  return (
    <button
      type={type}
      className={cn(
        "inline-flex items-center justify-center gap-2 rounded-lg transition-all focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-600 disabled:pointer-events-none disabled:opacity-50",
        variants[variant],
        sizes[size],
        className,
      )}
      {...props}
    />
  );
}
