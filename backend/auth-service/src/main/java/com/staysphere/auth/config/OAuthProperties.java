package com.staysphere.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "staysphere.oauth")
public class OAuthProperties {
    private Google google = new Google();
    private GitHub github = new GitHub();

    @Data
    public static class Google {
        private String clientId = "";
    }

    @Data
    public static class GitHub {
        private String clientId = "";
        private String clientSecret = "";
    }
}
