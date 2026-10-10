package com.staysphere.payment.gateway;

import com.staysphere.payment.entity.Payment;

public interface PaymentGateway {

    boolean isSimulated();

    /**
     * Immediate charge simulation or legacy one-shot flow.
     */
    GatewayResult charge(Payment payment);

    GatewayResult refund(Payment payment);
}
