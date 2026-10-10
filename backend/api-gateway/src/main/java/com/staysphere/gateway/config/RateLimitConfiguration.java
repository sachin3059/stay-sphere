package com.staysphere.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Configuration
public class RateLimitConfiguration {

    /**
     * Prefer authenticated subject (email claim path segment) else client IP.
     */
    @Bean
    @Primary
    public KeyResolver compositeRateLimitKeyResolver() {
        return exchange -> {
            String path = exchange.getRequest().getPath().value();
            String ip = resolveClientIp(exchange);
            if (path.startsWith("/api/auth/")) {
                return Mono.just("auth:" + ip);
            }
            String auth = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) {
                return Mono.just("user:" + ip);
            }
            return Mono.just("ip:" + ip);
        };
    }

    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just("ip:" + resolveClientIp(exchange));
    }

    private static String resolveClientIp(
            org.springframework.web.server.ServerWebExchange exchange) {
        String forwarded = exchange.getRequest().getHeaders()
                .getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        if (exchange.getRequest().getRemoteAddress() != null
                && exchange.getRequest().getRemoteAddress().getAddress() != null) {
            return Objects.requireNonNullElse(
                    exchange.getRequest().getRemoteAddress()
                            .getAddress()
                            .getHostAddress(),
                    "unknown");
        }
        return "unknown";
    }
}
