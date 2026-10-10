package com.staysphere.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.staysphere.auth.config.OAuthProperties;
import com.staysphere.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class GitHubOAuthService {

    private final OAuthProperties oauthProperties;
    private final RestClient restClient = RestClient.create();

    public VerifiedGitHubUser exchangeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new BadRequestException("GitHub authorization code is required");
        }
        String clientId = oauthProperties.getGithub().getClientId();
        String clientSecret = oauthProperties.getGithub().getClientSecret();
        if (clientId.isBlank() || clientSecret.isBlank()) {
            throw new BadRequestException("GitHub sign-in is not configured");
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("code", code);

        JsonNode tokenResponse = restClient.post()
                .uri("https://github.com/login/oauth/access_token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(form)
                .retrieve()
                .body(JsonNode.class);

        if (tokenResponse == null || !tokenResponse.hasNonNull("access_token")) {
            throw new BadRequestException("GitHub token exchange failed");
        }
        String accessToken = tokenResponse.get("access_token").asText();

        JsonNode user = restClient.get()
                .uri("https://api.github.com/user")
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/vnd.github+json")
                .retrieve()
                .body(JsonNode.class);

        if (user == null || !user.hasNonNull("id")) {
            throw new BadRequestException("Could not load GitHub profile");
        }

        String githubId = user.get("id").asText();
        String login = user.hasNonNull("login") ? user.get("login").asText() : "GitHub user";
        String name = user.hasNonNull("name") ? user.get("name").asText() : login;

        String email = user.hasNonNull("email") && !user.get("email").asText().isBlank()
                ? user.get("email").asText()
                : fetchPrimaryEmail(accessToken);

        if (email == null || email.isBlank()) {
            throw new BadRequestException(
                    "GitHub account has no public email. Add one in GitHub settings.");
        }

        return new VerifiedGitHubUser(
                githubId, email.toLowerCase(), name);
    }

    private String fetchPrimaryEmail(String accessToken) {
        JsonNode emails = restClient.get()
                .uri("https://api.github.com/user/emails")
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/vnd.github+json")
                .retrieve()
                .body(JsonNode.class);

        if (emails == null || !emails.isArray()) {
            return null;
        }
        for (JsonNode entry : emails) {
            if (entry.has("primary") && entry.get("primary").asBoolean()
                    && entry.hasNonNull("email")) {
                return entry.get("email").asText();
            }
        }
        for (JsonNode entry : emails) {
            if (entry.hasNonNull("email")) {
                return entry.get("email").asText();
            }
        }
        return null;
    }

    public record VerifiedGitHubUser(String githubId, String email, String fullName) {}
}
