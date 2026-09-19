import type { Property } from "./types";

export type ExploreSort = "newest" | "price_asc" | "price_desc";

export const EXPLORE_PAGE_SIZE = 9;

export function sortProperties(
  items: Property[],
  sort: ExploreSort,
): Property[] {
  const list = [...items];
  switch (sort) {
    case "price_asc":
      return list.sort((a, b) => a.pricePerNight - b.pricePerNight);
    case "price_desc":
      return list.sort((a, b) => b.pricePerNight - a.pricePerNight);
    case "newest":
    default:
      return list.sort((a, b) => {
        const ta = a.createdAt ? new Date(a.createdAt).getTime() : 0;
        const tb = b.createdAt ? new Date(b.createdAt).getTime() : 0;
        return tb - ta;
      });
  }
}

export function paginateProperties<T>(items: T[], page: number, pageSize: number) {
  const totalPages = Math.max(1, Math.ceil(items.length / pageSize));
  const safePage = Math.min(Math.max(1, page), totalPages);
  const start = (safePage - 1) * pageSize;
  return {
    items: items.slice(start, start + pageSize),
    page: safePage,
    totalPages,
    total: items.length,
  };
}
