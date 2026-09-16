package com.staysphere.payment.config;

import com.stripe.Stripe;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeClientConfig {

    public StripeClientConfig(PaymentProperties paymentProperties) {
        String secretKey = paymentProperties.getStripe().getSecretKey();
        if (secretKey != null && !secretKey.isBlank()) {
            Stripe.apiKey = secretKey;
        }
    }
}
