package com.staysphere.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.staysphere.auth.config.OAuthProperties;
import com.staysphere.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class GoogleOAuthService {

    private final OAuthProperties oauthProperties;
    private final RestClient restClient = RestClient.create();

    public VerifiedGoogleUser verifyIdToken(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new BadRequestException("Google ID token is required");
        }
        String clientId = oauthProperties.getGoogle().getClientId();
        if (clientId == null || clientId.isBlank()) {
            throw new BadRequestException("Google sign-in is not configured");
        }

        JsonNode body = restClient.get()
                .uri("https://oauth2.googleapis.com/tokeninfo?id_token={token}", idToken)
                .retrieve()
                .body(JsonNode.class);

        if (body == null || body.has("error")) {
            throw new BadRequestException("Invalid Google token");
        }

        String aud = text(body, "aud");
        if (!clientId.equals(aud)) {
            throw new BadRequestException("Google token audience mismatch");
        }

        if (!"true".equalsIgnoreCase(text(body, "email_verified"))) {
            throw new BadRequestException("Google email is not verified");
        }

        String sub = text(body, "sub");
        String email = text(body, "email");
        String name = text(body, "name");
        if (sub == null || email == null) {
            throw new BadRequestException("Google token missing user info");
        }

        return new VerifiedGoogleUser(sub, email.toLowerCase(), name != null ? name : email);
    }

    private static String text(JsonNode node, String field) {
        return node.hasNonNull(field) ? node.get(field).asText() : null;
    }

    public record VerifiedGoogleUser(String sub, String email, String fullName) {}
}
