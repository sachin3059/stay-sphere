package com.staysphere.availability.cache;

import com.staysphere.common.cache.RedisJsonCache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class AvailabilityCacheSupport {

    private static final Duration RANGE_TTL = Duration.ofMinutes(3);
    private static final String VERSION_PREFIX = "availability:ver:";

    private final RedisJsonCache cache;

    public Duration rangeTtl() {
        return RANGE_TTL;
    }

    public String rangeKey(
            String propertyId,
            LocalDate from,
            LocalDate to) {
        long version = cache.currentVersion(versionKey(propertyId));
        return "availability:"
                + propertyId
                + ":v"
                + version
                + ":"
                + from
                + ":"
                + to;
    }

    public void bumpVersion(String propertyId) {
        cache.incrementVersion(versionKey(propertyId));
    }

    public RedisJsonCache redis() {
        return cache;
    }

    private String versionKey(String propertyId) {
        return VERSION_PREFIX + propertyId;
    }
}
