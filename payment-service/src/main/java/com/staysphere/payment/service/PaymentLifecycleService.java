package com.staysphere.payment.service;

import com.staysphere.payment.dto.PaymentResponse;
import com.staysphere.payment.entity.Payment;
import com.staysphere.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentLifecycleService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher paymentEventPublisher;

    @Transactional
    public void markSuccess(Payment payment, String transactionId) {
        if (payment.getStatus() == Payment.PaymentStatus.SUCCESS) {
            return;
        }
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment.setTransactionId(transactionId);
        payment.setFailureReason(null);
        payment.setProcessedAt(LocalDateTime.now());
        Payment saved = paymentRepository.save(payment);
        paymentEventPublisher.publishSuccess(saved);
    }

    @Transactional
    public void markRefunded(Payment payment) {
        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        payment.setProcessedAt(LocalDateTime.now());
        Payment saved = paymentRepository.save(payment);
        paymentEventPublisher.publishRefunded(saved);
    }

    @Transactional
    public void markFailed(Payment payment, String reason) {
        if (payment.getStatus() == Payment.PaymentStatus.SUCCESS) {
            return;
        }
        payment.setStatus(Payment.PaymentStatus.FAILED);
        payment.setFailureReason(reason);
        payment.setProcessedAt(LocalDateTime.now());
        Payment saved = paymentRepository.save(payment);
        paymentEventPublisher.publishFailed(saved);
    }

    public PaymentResponse toResponse(Payment p) {
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
