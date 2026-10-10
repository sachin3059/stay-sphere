package com.staysphere.payment.service;

import com.staysphere.common.exception.BadRequestException;
import com.staysphere.payment.client.BookingPaymentClient;
import com.staysphere.payment.dto.PaymentRequest;
import com.staysphere.payment.dto.PaymentResponse;
import com.staysphere.payment.entity.Payment;
import com.staysphere.payment.exception.PaymentException;
import com.staysphere.payment.gateway.GatewayResult;
import com.staysphere.payment.gateway.PaymentGateway;
import com.staysphere.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final BookingPaymentClient bookingPaymentClient;
    private final PaymentLifecycleService paymentLifecycleService;

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request,
                                          String guestId,
                                          String authorizationHeader) {
        if (!paymentGateway.isSimulated()) {
            throw new PaymentException(
                    "Direct payment is disabled when payment.gateway=stripe. "
                            + "Use POST /api/payments/stripe/intent and the checkout page.");
        }

        if (paymentRepository.existsByIdempotencyKey(request.getIdempotencyKey())) {
            log.info("Duplicate payment request for idempotency key: {}",
                    request.getIdempotencyKey());
            return paymentLifecycleService.toResponse(
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
                        request.getCurrency() : booking.currency())
                .paymentMethod(Payment.PaymentMethod.valueOf(
                        request.getPaymentMethod().toUpperCase()))
                .status(Payment.PaymentStatus.PENDING)
                .build();

        Payment saved;
        try {
            saved = paymentRepository.save(payment);
        } catch (DataIntegrityViolationException ex) {
            return paymentLifecycleService.toResponse(
                    paymentRepository.findByIdempotencyKey(
                            request.getIdempotencyKey()).orElseThrow());
        }

        GatewayResult result = paymentGateway.charge(saved);
        if (result.isSuccess()) {
            paymentLifecycleService.markSuccess(saved, result.getTransactionId());
            log.info("Payment success: {}", saved.getId());
        } else {
            paymentLifecycleService.markFailed(saved, result.getFailureReason());
            log.info("Payment failed: {}", saved.getId());
        }

        return paymentLifecycleService.toResponse(
                paymentRepository.findById(saved.getId()).orElseThrow());
    }

    @Transactional
    public PaymentResponse refundPayment(String paymentId,
                                         String callerId,
                                         String authorizationHeader) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new PaymentException("Payment not found: " + paymentId));

        if (payment.getStatus() == Payment.PaymentStatus.REFUNDED) {
            throw new PaymentException("Payment is already refunded");
        }
        if (payment.getStatus() != Payment.PaymentStatus.SUCCESS) {
            throw new PaymentException("Only successful payments can be refunded");
        }

        assertRefundAllowed(payment, callerId, authorizationHeader);

        GatewayResult refund = paymentGateway.refund(payment);
        if (!refund.isSuccess()) {
            throw new PaymentException(
                    "Refund failed: " + refund.getFailureReason());
        }

        paymentLifecycleService.markRefunded(payment);
        return paymentLifecycleService.toResponse(
                paymentRepository.findById(paymentId).orElseThrow());
    }

    public PaymentResponse getPayment(String paymentId) {
        return paymentLifecycleService.toResponse(paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new PaymentException("Payment not found")));
    }

    public PaymentResponse getPaymentByBooking(String bookingId) {
        return paymentLifecycleService.toResponse(
                paymentRepository.findByBookingId(bookingId)
                        .orElseThrow(() ->
                                new PaymentException("Payment not found for booking")));
    }

    private void assertRefundAllowed(Payment payment,
                                     String callerId,
                                     String authorizationHeader) {
        if (!payment.getGuestId().equals(callerId)
                && !payment.getHostId().equals(callerId)) {
            throw new BadRequestException(
                    "Only the guest or host for this payment may request a refund");
        }

        BookingPaymentClient.BookingSnapshot booking =
                bookingPaymentClient.fetchBooking(
                        payment.getBookingId(), authorizationHeader);
        if (!"CANCELLED".equalsIgnoreCase(booking.status())) {
            throw new PaymentException(
                    "Refunds are only allowed after the booking is cancelled");
        }
    }
}
