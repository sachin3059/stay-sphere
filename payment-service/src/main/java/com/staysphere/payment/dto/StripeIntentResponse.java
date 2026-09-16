package com.staysphere.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class StripeIntentResponse {
    private String paymentId;
    private String clientSecret;
    private String paymentIntentId;
    private BigDecimal amount;
    private String currency;
    private String bookingId;
    private String publishableKey;
}
