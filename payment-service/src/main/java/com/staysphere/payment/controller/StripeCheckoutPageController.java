package com.staysphere.payment.controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@ConditionalOnProperty(name = "payment.gateway", havingValue = "stripe")
public class StripeCheckoutPageController {

    @GetMapping(value = "/api/payments/checkout", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> checkoutPage() throws IOException {
        String html = StreamUtils.copyToString(
                new ClassPathResource("static/stripe-checkout.html").getInputStream(),
                StandardCharsets.UTF_8);
        return ResponseEntity.ok(html);
    }
}
