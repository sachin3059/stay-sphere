package com.staysphere.payment.gateway;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GatewayResult {
    private final boolean success;
    private final String transactionId;
    private final String failureReason;
}
