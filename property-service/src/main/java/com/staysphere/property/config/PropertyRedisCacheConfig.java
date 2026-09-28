package com.staysphere.property.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.staysphere.common.cache.RedisJsonCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class PropertyRedisCacheConfig {

    @Bean
    public ObjectMapper propertyCacheObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean
    public RedisJsonCache redisJsonCache(
            StringRedisTemplate redisTemplate,
            ObjectMapper propertyCacheObjectMapper) {
        return new RedisJsonCache(redisTemplate, propertyCacheObjectMapper);
    }
}
