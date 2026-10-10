package com.staysphere.property.cache;

import com.staysphere.common.cache.RedisJsonCache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HexFormat;

@Component
@RequiredArgsConstructor
public class PropertyCacheSupport {

    public static final String DETAIL_PREFIX = "prop:detail:";
    public static final String SEARCH_VERSION_KEY = "prop:search:ver";

    private static final Duration DETAIL_TTL = Duration.ofMinutes(10);
    private static final Duration SEARCH_TTL = Duration.ofSeconds(90);

    private final RedisJsonCache cache;

    public String detailKey(String propertyId) {
        return DETAIL_PREFIX + propertyId;
    }

    public Duration detailTtl() {
        return DETAIL_TTL;
    }

    public Duration searchTtl() {
        return SEARCH_TTL;
    }

    public void evictDetail(String propertyId) {
        cache.evict(detailKey(propertyId));
    }

    public void bumpSearchGeneration() {
        cache.incrementVersion(SEARCH_VERSION_KEY);
    }

    public String searchKey(
            String query,
            String city,
            Integer guests,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            LocalDate checkIn,
            LocalDate checkOut,
            int page,
            int size,
            String sort) {
        long version = cache.currentVersion(SEARCH_VERSION_KEY);
        String payload = version
                + "|" + norm(query)
                + "|" + norm(city)
                + "|" + (guests == null ? "" : guests)
                + "|" + (minPrice == null ? "" : minPrice)
                + "|" + (maxPrice == null ? "" : maxPrice)
                + "|" + (checkIn == null ? "" : checkIn)
                + "|" + (checkOut == null ? "" : checkOut)
                + "|" + page
                + "|" + size
                + "|" + norm(sort);
        return "prop:search:" + sha256(payload);
    }

    public RedisJsonCache redis() {
        return cache;
    }

    private static String norm(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
