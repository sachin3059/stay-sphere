package com.staysphere.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisLockService {

    private final StringRedisTemplate redisTemplate;
    private static final long LOCK_TTL_MINUTES = 10;

    public boolean acquireLock(String lockKey) {
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "LOCKED",
                        Duration.ofMinutes(LOCK_TTL_MINUTES));
        log.info("Lock {} : {}",
                lockKey, acquired ? "ACQUIRED" : "ALREADY LOCKED");
        return Boolean.TRUE.equals(acquired);
    }

    public void releaseLock(String lockKey) {
        redisTemplate.delete(lockKey);
        log.info("Lock {} released", lockKey);
    }

    public String buildLockKey(String propertyId,
                               String checkIn, String checkOut) {
        return String.format("lock:booking:%s:%s:%s",
                propertyId, checkIn, checkOut);
    }
}