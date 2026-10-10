package com.staysphere.property.search;

import com.staysphere.common.exception.BadRequestException;

public enum PropertySearchSort {
    NEWEST,
    PRICE_ASC,
    PRICE_DESC,
    RELEVANCE;

    public static PropertySearchSort fromParam(String raw, boolean hasTextQuery) {
        if (raw == null || raw.isBlank()) {
            return hasTextQuery ? RELEVANCE : NEWEST;
        }
        String normalized = raw.trim().toLowerCase();
        return switch (normalized) {
            case "newest" -> NEWEST;
            case "price_asc" -> PRICE_ASC;
            case "price_desc" -> PRICE_DESC;
            case "relevance" -> RELEVANCE;
            default -> throw new BadRequestException(
                    "Invalid sort. Use newest, price_asc, price_desc, or relevance.");
        };
    }

    public String orderByClause(boolean hasTextQuery) {
        return switch (this) {
            case PRICE_ASC -> "p.price_per_night ASC, p.created_at DESC";
            case PRICE_DESC -> "p.price_per_night DESC, p.created_at DESC";
            case RELEVANCE -> hasTextQuery
                    ? "ts_rank(p.search_vector, plainto_tsquery('english', :query)) DESC, "
                            + "p.created_at DESC"
                    : "p.created_at DESC";
            case NEWEST -> "p.created_at DESC";
        };
    }
}
