package com.staysphere.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StripeIntentRequest {

    @NotBlank
    private String bookingId;

    @NotBlank
    private String hostId;

    @NotBlank
    private String idempotencyKey;
}
