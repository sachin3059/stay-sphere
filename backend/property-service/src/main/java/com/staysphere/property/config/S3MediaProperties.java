package com.staysphere.property.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "staysphere.s3")
public class S3MediaProperties {

    private String bucket;
    private String region = "ap-south-1";
    /** Optional CloudFront or custom origin, no trailing slash. */
    private String publicBaseUrl = "";
    private int presignExpiryMinutes = 15;
    private long maxImageBytes = 10L * 1024 * 1024;
    private long maxVideoBytes = 500L * 1024 * 1024;
    private String keyPrefix = "properties";
}
