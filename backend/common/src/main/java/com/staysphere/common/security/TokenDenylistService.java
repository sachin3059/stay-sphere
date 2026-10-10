package com.staysphere.common.security;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class TokenDenylistService {

    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;

    public TokenDenylistService(ObjectProvider<StringRedisTemplate> redisTemplateProvider) {
        this.redisTemplateProvider = redisTemplateProvider;
    }

    public void deny(String token, long ttlMs) {
        StringRedisTemplate redis = redisTemplateProvider.getIfAvailable();
        if (redis == null) {
            return;
        }
        redis.opsForValue().set(denyKey(token), "1", Duration.ofMillis(ttlMs));
    }

    public boolean isDenied(String token) {
        StringRedisTemplate redis = redisTemplateProvider.getIfAvailable();
        if (redis == null) {
            return false;
        }
        return Boolean.TRUE.equals(redis.hasKey(denyKey(token)));
    }

    private String denyKey(String token) {
        return "deny:access:" + token;
    }
}
