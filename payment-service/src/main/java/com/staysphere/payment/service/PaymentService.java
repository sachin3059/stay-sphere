package com.staysphere.payment.service;

import com.staysphere.common.exception.BadRequestException;
import com.staysphere.payment.client.BookingPaymentClient;
import com.staysphere.payment.dto.PaymentRequest;
import com.staysphere.payment.dto.PaymentResponse;
import com.staysphere.payment.entity.Payment;
import com.staysphere.payment.exception.PaymentException;
import com.staysphere.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final BookingPaymentClient bookingPaymentClient;

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request,
                                          String guestId,
                                          String authorizationHeader) {
        if (paymentRepository.existsByIdempotencyKey(
                request.getIdempotencyKey())) {
            log.info("Duplicate payment request for idempotency key: {}",
                    request.getIdempotencyKey());
            return mapToResponse(
                    paymentRepository.findByIdempotencyKey(
                            request.getIdempotencyKey()).orElseThrow());
        }

        BookingPaymentClient.BookingSnapshot booking =
                bookingPaymentClient.fetchBooking(
                        request.getBookingId(), authorizationHeader);
        if (!"PENDING".equalsIgnoreCase(booking.status())) {
            throw new PaymentException("Booking is not payable in current state");
        }
        if (!booking.guestId().equals(guestId)) {
            throw new BadRequestException("Guest does not own this booking");
        }
        if (booking.totalPrice().compareTo(request.getAmount()) != 0) {
            throw new PaymentException("Payment amount does not match booking total");
        }

        Payment payment = Payment.builder()
                .bookingId(request.getBookingId())
                .guestId(guestId)
                .hostId(request.getHostId())
                .idempotencyKey(request.getIdempotencyKey())
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ?
                        request.getCurrency() : "INR")
                .paymentMethod(Payment.PaymentMethod.valueOf(
                        request.getPaymentMethod().toUpperCase()))
                .status(Payment.PaymentStatus.PENDING)
                .build();

        Payment saved;
        try {
            saved = paymentRepository.save(payment);
        } catch (DataIntegrityViolationException ex) {
            return mapToResponse(paymentRepository.findByIdempotencyKey(
                    request.getIdempotencyKey()).orElseThrow());
        }

        // Simulate payment processing
        // In production this calls Stripe/Razorpay API
        boolean paymentSuccess = simulatePaymentGateway(saved);

        if (paymentSuccess) {
            saved.setStatus(Payment.PaymentStatus.SUCCESS);
            saved.setTransactionId("TXN-" + UUID.randomUUID()
                    .toString().substring(0, 8).toUpperCase());
            saved.setProcessedAt(LocalDateTime.now());
            paymentRepository.save(saved);

            kafkaTemplate.send("payment-events",
                    saved.getId(),
                    Map.of(
                            "event", "payment_success",
                            "paymentId", saved.getId(),
                            "bookingId", saved.getBookingId(),
                            "guestId", saved.getGuestId(),
                            "hostId", saved.getHostId(),
                            "amount", saved.getAmount().toString(),
                            "transactionId", saved.getTransactionId()
                    ));

            log.info("Payment success: {}", saved.getId());
        } else {
            saved.setStatus(Payment.PaymentStatus.FAILED);
            saved.setFailureReason("Payment gateway declined");
            saved.setProcessedAt(LocalDateTime.now());
            paymentRepository.save(saved);

            kafkaTemplate.send("payment-events",
                    saved.getId(),
                    Map.of(
                            "event", "payment_failed",
                            "paymentId", saved.getId(),
                            "bookingId", saved.getBookingId(),
                            "guestId", saved.getGuestId()
                    ));

            log.info("Payment failed: {}", saved.getId());
        }

        return mapToResponse(saved);
    }

    @Transactional
    public PaymentResponse refundPayment(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new PaymentException("Payment not found: " + paymentId));

        if (payment.getStatus() != Payment.PaymentStatus.SUCCESS) {
            throw new PaymentException("Only successful payments can be refunded");
        }

        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        payment.setProcessedAt(LocalDateTime.now());
        Payment saved = paymentRepository.save(payment);

        kafkaTemplate.send("payment-events",
                saved.getId(),
                Map.of(
                        "event", "payment_refunded",
                        "paymentId", saved.getId(),
                        "bookingId", saved.getBookingId(),
                        "guestId", saved.getGuestId(),
                        "amount", saved.getAmount().toString()
                ));

        log.info("Payment refunded: {}", saved.getId());
        return mapToResponse(saved);
    }

    public PaymentResponse getPayment(String paymentId) {
        return mapToResponse(paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new PaymentException("Payment not found")));
    }

    public PaymentResponse getPaymentByBooking(String bookingId) {
        return mapToResponse(paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() ->
                        new PaymentException("Payment not found for booking")));
    }

    private boolean simulatePaymentGateway(Payment payment) {
        // Simulates 90% success rate
        // Replace with actual Stripe/Razorpay call in production
        return Math.random() > 0.1;
    }

    private PaymentResponse mapToResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .bookingId(p.getBookingId())
                .guestId(p.getGuestId())
                .hostId(p.getHostId())
                .idempotencyKey(p.getIdempotencyKey())
                .amount(p.getAmount())
                .currency(p.getCurrency())
                .status(p.getStatus().name())
                .paymentMethod(p.getPaymentMethod() != null ?
                        p.getPaymentMethod().name() : null)
                .transactionId(p.getTransactionId())
                .failureReason(p.getFailureReason())
                .createdAt(p.getCreatedAt())
                .processedAt(p.getProcessedAt())
                .build();
    }
}