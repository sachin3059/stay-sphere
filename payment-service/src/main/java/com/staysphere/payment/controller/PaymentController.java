package com.staysphere.payment.controller;

import com.staysphere.common.ApiResponse;
import com.staysphere.payment.dto.PaymentRequest;
import com.staysphere.payment.dto.PaymentResponse;
import com.staysphere.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext()
                .getAuthentication().getName();
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @RequestHeader("Authorization") String authorization,
            @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Payment processed successfully",
                        paymentService.processPayment(
                                request, getCurrentUserId(), authorization)));
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<ApiResponse<PaymentResponse>> refundPayment(
            @PathVariable String id) {
        return ResponseEntity.ok(
                ApiResponse.success("Payment refunded successfully",
                        paymentService.refundPayment(id)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
            @PathVariable String id) {
        return ResponseEntity.ok(
                ApiResponse.success("Payment fetched successfully",
                        paymentService.getPayment(id)));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByBooking(
            @PathVariable String bookingId) {
        return ResponseEntity.ok(
                ApiResponse.success("Payment fetched successfully",
                        paymentService.getPaymentByBooking(bookingId)));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
                ApiResponse.success("Payment service is running"));
    }
}