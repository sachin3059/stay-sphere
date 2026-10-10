package com.staysphere.payment.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentRequest {
    private String bookingId;
    private String hostId;
    private String idempotencyKey;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
}