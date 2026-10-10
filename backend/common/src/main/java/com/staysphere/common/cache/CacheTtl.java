package com.staysphere.common.cache;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

public final class CacheTtl {

    private CacheTtl() {
    }

    /** Base TTL plus random jitter to reduce synchronized expiry (stampede). */
    public static Duration withJitter(Duration base, double jitterRatio) {
        if (jitterRatio <= 0) {
            return base;
        }
        long millis = base.toMillis();
        long spread = (long) (millis * jitterRatio);
        long jitter = spread == 0 ? 0 : ThreadLocalRandom.current().nextLong(-spread, spread + 1);
        return Duration.ofMillis(Math.max(1_000, millis + jitter));
    }
}
