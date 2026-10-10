package com.staysphere.payment.gateway;

import com.staysphere.payment.entity.Payment;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "payment.gateway", havingValue = "simulate", matchIfMissing = true)
public class SimulatedPaymentGateway implements PaymentGateway {

    @Override
    public boolean isSimulated() {
        return true;
    }

    @Override
    public GatewayResult charge(Payment payment) {
        boolean success = Math.random() > 0.1;
        if (success) {
            return GatewayResult.builder()
                    .success(true)
                    .transactionId("TXN-" + UUID.randomUUID()
                            .toString().substring(0, 8).toUpperCase())
                    .build();
        }
        return GatewayResult.builder()
                .success(false)
                .failureReason("Payment gateway declined")
                .build();
    }

    @Override
    public GatewayResult refund(Payment payment) {
        return GatewayResult.builder()
                .success(true)
                .transactionId(payment.getTransactionId())
                .build();
    }
}
