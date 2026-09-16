package com.staysphere.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenDenylistService {

    private final StringRedisTemplate redisTemplate;

    public void deny(String token, long ttlMs) {
        redisTemplate.opsForValue().set(denyKey(token), "1", Duration.ofMillis(ttlMs));
    }

    public boolean isDenied(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(denyKey(token)));
    }

    private String denyKey(String token) {
        return "deny:access:" + token;
    }
}
