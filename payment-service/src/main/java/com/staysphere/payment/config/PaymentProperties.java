package com.staysphere.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "payment")
public class PaymentProperties {

    /**
     * simulate = random local gateway; stripe = Stripe PaymentIntents + webhooks.
     */
    private String gateway = "simulate";

    private Stripe stripe = new Stripe();

    @Getter
    @Setter
    public static class Stripe {
        private String secretKey = "";
        private String publishableKey = "";
        private String webhookSecret = "";
    }
}
