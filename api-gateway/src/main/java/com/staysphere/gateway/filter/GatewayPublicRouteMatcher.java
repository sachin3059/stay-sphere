package com.staysphere.gateway.filter;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.List;

@Component
public class GatewayPublicRouteMatcher {

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    private record Route(HttpMethod method, String pattern) {}

    private static final List<Route> PUBLIC_ROUTES = List.of(
            new Route(HttpMethod.POST, "/api/auth/register"),
            new Route(HttpMethod.POST, "/api/auth/login"),
            new Route(HttpMethod.POST, "/api/auth/refresh"),
            new Route(HttpMethod.POST, "/api/auth/oauth/google"),
            new Route(HttpMethod.POST, "/api/auth/oauth/github"),
            new Route(HttpMethod.GET, "/api/auth/health"),
            new Route(HttpMethod.GET, "/api/properties/health"),
            new Route(HttpMethod.GET, "/api/properties/search"),
            new Route(HttpMethod.GET, "/api/properties/search/**"),
            new Route(HttpMethod.GET, "/api/properties/search/advanced"),
            new Route(HttpMethod.GET, "/api/properties"),
            new Route(HttpMethod.GET, "/api/properties/*"),
            new Route(HttpMethod.GET, "/api/bookings/health"),
            new Route(HttpMethod.GET, "/api/payments/health"),
            new Route(HttpMethod.GET, "/api/payments/stripe/config"),
            new Route(HttpMethod.GET, "/api/payments/checkout"),
            new Route(HttpMethod.POST, "/api/payments/webhooks/stripe"),
            new Route(HttpMethod.GET, "/api/pricing/health"),
            new Route(HttpMethod.GET, "/api/pricing/rules/*"),
            new Route(HttpMethod.POST, "/api/pricing/calculate"),
            new Route(HttpMethod.GET, "/api/waitlist/health"),
            new Route(HttpMethod.GET, "/api/waitlist/property/**"),
            new Route(HttpMethod.GET, "/api/notifications/health"),
            new Route(HttpMethod.GET, "/api/availability/health"),
            new Route(HttpMethod.GET, "/api/availability/*/ranges"),
            new Route(HttpMethod.GET, "/api/availability/*/check"),
            new Route(HttpMethod.GET, "/api/availability/*/blocked")
    );

    public boolean isPublic(String method, String path) {
        HttpMethod httpMethod = HttpMethod.valueOf(method);
        return PUBLIC_ROUTES.stream()
                .anyMatch(route -> route.method == httpMethod
                        && MATCHER.match(route.pattern, path));
    }
}
