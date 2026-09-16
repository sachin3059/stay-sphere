package com.staysphere.payment.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StripeConfigResponse {
    private String publishableKey;
    private String gateway;
}
