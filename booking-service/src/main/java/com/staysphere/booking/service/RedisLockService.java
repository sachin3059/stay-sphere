package com.staysphere.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisLockService {

    private final StringRedisTemplate redisTemplate;
    private static final long LOCK_TTL_MINUTES = 10;

    private static final DefaultRedisScript<Long> RELEASE_SCRIPT = new DefaultRedisScript<>();
    static {
        RELEASE_SCRIPT.setResultType(Long.class);
        RELEASE_SCRIPT.setScriptText(
                "if redis.call('get', KEYS[1]) == ARGV[1] then "
                        + "return redis.call('del', KEYS[1]) else return 0 end");
    }

    public LockHandle acquirePropertyLock(String propertyId) {
        String lockKey = "lock:booking:property:" + propertyId;
        String token = UUID.randomUUID().toString();
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, token, Duration.ofMinutes(LOCK_TTL_MINUTES));
        boolean ok = Boolean.TRUE.equals(acquired);
        log.info("Property lock {} : {}", lockKey, ok ? "ACQUIRED" : "BUSY");
        return new LockHandle(lockKey, token, ok);
    }

    public void releaseLock(LockHandle handle) {
        if (handle == null || !handle.acquired()) {
            return;
        }
        Long released = redisTemplate.execute(
                RELEASE_SCRIPT,
                Collections.singletonList(handle.lockKey()),
                handle.token());
        log.info("Lock {} release result: {}", handle.lockKey(), released);
    }

    public record LockHandle(String lockKey, String token, boolean acquired) {}
}
