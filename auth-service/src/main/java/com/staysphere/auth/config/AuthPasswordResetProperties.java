package com.staysphere.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "staysphere.auth.password-reset")
@Getter
@Setter
public class AuthPasswordResetProperties {

    /**
     * Base URL of the web app (no trailing slash), used in reset links.
     */
    private String frontendBaseUrl = "http://localhost:5173";

    private int tokenTtlMinutes = 60;
}
