package com.staysphere.common.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
public class RedisJsonCache {

    private static final String LOCK_PREFIX = "cache:lock:";
    private static final double DEFAULT_JITTER = 0.15;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public <T> Optional<T> get(String key, Class<T> type) {
        String json = redisTemplate.opsForValue().get(key);
        if (json == null || json.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, type));
        } catch (JsonProcessingException e) {
            log.warn("Invalid cache payload for key {}, evicting", key);
            redisTemplate.delete(key);
            return Optional.empty();
        }
    }

    public <T> void put(String key, T value, Duration ttl) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(
                    key, json, CacheTtl.withJitter(ttl, DEFAULT_JITTER));
        } catch (JsonProcessingException e) {
            log.warn("Could not serialize cache value for key {}", key, e);
        }
    }

    public void evict(String key) {
        redisTemplate.delete(key);
    }

    public long incrementVersion(String versionKey) {
        Long next = redisTemplate.opsForValue().increment(versionKey);
        return next == null ? 0L : next;
    }

    public long currentVersion(String versionKey) {
        String raw = redisTemplate.opsForValue().get(versionKey);
        if (raw == null || raw.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * Cache-aside with a short Redis lock so only one loader repopulates a hot key.
     */
    public <T> T getOrLoad(
            String key,
            Class<T> type,
            Duration ttl,
            Supplier<T> loader) {
        Optional<T> cached = get(key, type);
        if (cached.isPresent()) {
            return cached.get();
        }

        String lockKey = LOCK_PREFIX + key;
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", Duration.ofSeconds(10));

        if (Boolean.TRUE.equals(acquired)) {
            try {
                Optional<T> again = get(key, type);
                if (again.isPresent()) {
                    return again.get();
                }
                T value = loader.get();
                put(key, value, ttl);
                return value;
            } finally {
                redisTemplate.delete(lockKey);
            }
        }

        for (int attempt = 0; attempt < 8; attempt++) {
            try {
                Thread.sleep(40L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            Optional<T> retry = get(key, type);
            if (retry.isPresent()) {
                return retry.get();
            }
        }
        T value = loader.get();
        put(key, value, ttl);
        return value;
    }
}
