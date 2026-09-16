package com.staysphere.payment.controller;

import com.staysphere.payment.service.StripeCheckoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments/webhooks")
@RequiredArgsConstructor
@ConditionalOnBean(StripeCheckoutService.class)
public class StripeWebhookController {

    private final StripeCheckoutService stripeCheckoutService;

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        stripeCheckoutService.handleWebhook(payload, signature);
        return ResponseEntity.ok("ok");
    }
}
