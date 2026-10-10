package com.staysphere.gateway.filter;

import com.staysphere.gateway.config.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter
        implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;
    private final GatewayPublicRouteMatcher publicRouteMatcher;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();

        log.info("Gateway request: {} {}", method, path);

        boolean isPublic = publicRouteMatcher.isPublic(method, path);

        if (isPublic) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for: {}", path);
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.isTokenValid(token)) {
            log.warn("Invalid JWT token for: {}", path);
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        String email = jwtUtil.extractEmail(token);
        String role = jwtUtil.extractRole(token);

        log.info("JWT valid — user: {} role: {} path: {}",
                email, role, path);

        return chain.filter(exchange);
    }

    private Mono<Void> onError(ServerWebExchange exchange,
                               HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
