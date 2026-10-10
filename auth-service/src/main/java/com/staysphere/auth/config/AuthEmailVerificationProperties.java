package com.staysphere.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "staysphere.auth.email-verification")
@Getter
@Setter
public class AuthEmailVerificationProperties {

    private String frontendBaseUrl = "http://localhost:5173";

    private int tokenTtlHours = 48;
}
