export function formatInr(amount: number): string {
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0,
  }).format(amount);
}

export function formatPropertyType(type?: string): string {
  if (!type) return "Stay";
  return type.charAt(0) + type.slice(1).toLowerCase().replace(/_/g, " ");
}
