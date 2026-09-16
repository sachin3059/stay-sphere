package com.staysphere.payment.controller;

import com.staysphere.common.ApiResponse;
import com.staysphere.payment.dto.PaymentResponse;
import com.staysphere.payment.dto.StripeConfigResponse;
import com.staysphere.payment.dto.StripeIntentRequest;
import com.staysphere.payment.dto.StripeIntentResponse;
import com.staysphere.payment.service.StripeCheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments/stripe")
@RequiredArgsConstructor
@ConditionalOnBean(StripeCheckoutService.class)
public class StripePaymentController {

    private final StripeCheckoutService stripeCheckoutService;

    @GetMapping("/config")
    public ResponseEntity<ApiResponse<StripeConfigResponse>> config() {
        return ResponseEntity.ok(
                ApiResponse.success("Stripe config",
                        stripeCheckoutService.getPublicConfig()));
    }

    @PostMapping("/intent")
    public ResponseEntity<ApiResponse<StripeIntentResponse>> createIntent(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody StripeIntentRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("PaymentIntent created",
                        stripeCheckoutService.createPaymentIntent(
                                request,
                                getCurrentUserId(),
                                authorization)));
    }

    @PostMapping("/confirm/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> confirm(
            @PathVariable String paymentId) {
        return ResponseEntity.ok(
                ApiResponse.success("Payment status synced",
                        stripeCheckoutService.confirmPayment(
                                paymentId, getCurrentUserId())));
    }

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext()
                .getAuthentication().getName();
    }
}
