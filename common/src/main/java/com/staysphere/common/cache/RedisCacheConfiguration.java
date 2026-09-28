package com.staysphere.common.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@ConditionalOnBean(StringRedisTemplate.class)
public class RedisCacheConfiguration {

    @Bean
    public ObjectMapper redisCacheObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean
    public RedisJsonCache redisJsonCache(
            StringRedisTemplate redisTemplate,
            ObjectMapper redisCacheObjectMapper) {
        return new RedisJsonCache(redisTemplate, redisCacheObjectMapper);
    }
}
