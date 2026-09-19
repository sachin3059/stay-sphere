package com.staysphere.payment.service;

import com.staysphere.common.exception.BadRequestException;
import com.staysphere.payment.client.BookingPaymentClient;
import com.staysphere.payment.config.PaymentProperties;
import com.staysphere.payment.dto.StripeConfigResponse;
import com.staysphere.payment.dto.StripeIntentRequest;
import com.staysphere.payment.dto.StripeIntentResponse;
import com.staysphere.payment.dto.PaymentResponse;
import com.staysphere.payment.entity.Payment;
import com.staysphere.payment.exception.PaymentException;
import com.staysphere.payment.gateway.StripePaymentGateway;
import com.staysphere.payment.repository.PaymentRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnBean(StripePaymentGateway.class)
@RequiredArgsConstructor
@Slf4j
public class StripeCheckoutService {

    private final PaymentProperties paymentProperties;
    private final StripePaymentGateway stripePaymentGateway;
    private final PaymentRepository paymentRepository;
    private final BookingPaymentClient bookingPaymentClient;
    private final PaymentLifecycleService paymentLifecycleService;

    public StripeConfigResponse getPublicConfig() {
        return StripeConfigResponse.builder()
                .gateway(paymentProperties.getGateway())
                .publishableKey(stripePaymentGateway.publishableKey())
                .build();
    }

    @Transactional
    public StripeIntentResponse createPaymentIntent(StripeIntentRequest request,
                                                    String guestId,
                                                    String authorizationHeader) {
        if (paymentRepository.existsByIdempotencyKey(request.getIdempotencyKey())) {
            Payment existing = paymentRepository.findByIdempotencyKey(
                    request.getIdempotencyKey()).orElseThrow();
            return toIntentResponse(existing, null);
        }

        BookingPaymentClient.BookingSnapshot booking =
                bookingPaymentClient.fetchBooking(request.getBookingId(), authorizationHeader);
        if (!"PENDING".equalsIgnoreCase(booking.status())) {
            throw new PaymentException("Booking is not payable in current state");
        }
        if (!booking.guestId().equals(guestId)) {
            throw new BadRequestException("Guest does not own this booking");
        }

        Payment payment = Payment.builder()
                .bookingId(request.getBookingId())
                .guestId(guestId)
                .hostId(request.getHostId())
                .idempotencyKey(request.getIdempotencyKey())
                .amount(booking.totalPrice())
                .currency(booking.currency() != null ? booking.currency() : "INR")
                .paymentMethod(Payment.PaymentMethod.CREDIT_CARD)
                .status(Payment.PaymentStatus.PENDING)
                .build();

        Payment saved;
        try {
            saved = paymentRepository.save(payment);
        } catch (DataIntegrityViolationException ex) {
            Payment existing = paymentRepository.findByIdempotencyKey(
                    request.getIdempotencyKey()).orElseThrow();
            return toIntentResponse(existing, null);
        }

        PaymentIntent intent = stripePaymentGateway.createPaymentIntent(saved);
        saved.setTransactionId(intent.getId());
        paymentRepository.save(saved);

        return toIntentResponse(saved, intent.getClientSecret());
    }

    @Transactional
    public PaymentResponse confirmPayment(String paymentId, String guestId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found: " + paymentId));
        if (!payment.getGuestId().equals(guestId)) {
            throw new BadRequestException("Guest does not own this payment");
        }
        if (payment.getStatus() == Payment.PaymentStatus.SUCCESS) {
            return paymentLifecycleService.toResponse(payment);
        }
        if (payment.getTransactionId() == null) {
            throw new PaymentException("Payment has no Stripe PaymentIntent");
        }

        PaymentIntent intent = stripePaymentGateway.retrievePaymentIntent(
                payment.getTransactionId());
        syncPaymentFromIntent(payment, intent);
        return paymentLifecycleService.toResponse(
                paymentRepository.findById(paymentId).orElseThrow());
    }

    @Transactional
    public void handleWebhook(String payload, String signatureHeader) {
        String secret = stripePaymentGateway.webhookSecret();
        if (secret == null || secret.isBlank()) {
            throw new PaymentException("STRIPE_WEBHOOK_SECRET is not configured");
        }
        Event event;
        try {
            event = Webhook.constructEvent(payload, signatureHeader, secret);
        } catch (SignatureVerificationException e) {
            throw new BadRequestException("Invalid Stripe webhook signature");
        }

        if (!"payment_intent.succeeded".equals(event.getType())
                && !"payment_intent.payment_failed".equals(event.getType())) {
            return;
        }

        PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new PaymentException("Unable to deserialize PaymentIntent"));

        Payment payment = resolvePayment(intent);
        if (payment == null) {
            log.warn("No payment found for PaymentIntent {}", intent.getId());
            return;
        }
        if (payment.getStatus() == Payment.PaymentStatus.REFUNDED) {
            log.info("Ignoring Stripe event {} for refunded payment {}",
                    event.getType(), payment.getId());
            return;
        }
        if ("payment_intent.payment_failed".equals(event.getType())) {
            paymentLifecycleService.markFailed(
                    payment,
                    intent.getLastPaymentError() != null
                            ? intent.getLastPaymentError().getMessage()
                            : "Payment failed");
        } else {
            syncPaymentFromIntent(payment, intent);
        }
    }

    private Payment resolvePayment(PaymentIntent intent) {
        String paymentId = intent.getMetadata().get("paymentId");
        if (paymentId != null) {
            return paymentRepository.findById(paymentId).orElse(null);
        }
        return paymentRepository.findByTransactionId(intent.getId()).orElse(null);
    }

    private void syncPaymentFromIntent(Payment payment, PaymentIntent intent) {
        if ("succeeded".equals(intent.getStatus())) {
            paymentLifecycleService.markSuccess(payment, intent.getId());
        } else if ("canceled".equals(intent.getStatus())) {
            paymentLifecycleService.markFailed(payment, "Payment canceled");
        } else if (intent.getLastPaymentError() != null) {
            paymentLifecycleService.markFailed(
                    payment, intent.getLastPaymentError().getMessage());
        }
    }

    private StripeIntentResponse toIntentResponse(Payment payment, String clientSecret) {
        if (clientSecret == null && payment.getTransactionId() != null) {
            PaymentIntent intent = stripePaymentGateway.retrievePaymentIntent(
                    payment.getTransactionId());
            clientSecret = intent.getClientSecret();
        }
        return StripeIntentResponse.builder()
                .paymentId(payment.getId())
                .clientSecret(clientSecret)
                .paymentIntentId(payment.getTransactionId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .bookingId(payment.getBookingId())
                .publishableKey(stripePaymentGateway.publishableKey())
                .build();
    }
}
