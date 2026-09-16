package com.staysphere.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.staysphere.payment.entity.Payment;
import com.staysphere.payment.outbox.PaymentOutboxEvent;
import com.staysphere.payment.outbox.PaymentOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private final PaymentOutboxRepository paymentOutboxRepository;
    private final ObjectMapper objectMapper;

    public void publishSuccess(Payment payment) {
        Map<String, Object> event = base(payment, "payment_success");
        event.put("amount", payment.getAmount().toString());
        event.put("transactionId", payment.getTransactionId());
        enqueue(payment.getId(), event);
    }

    public void publishFailed(Payment payment) {
        enqueue(payment.getId(), base(payment, "payment_failed"));
    }

    public void publishRefunded(Payment payment) {
        Map<String, Object> event = base(payment, "payment_refunded");
        event.put("amount", payment.getAmount().toString());
        enqueue(payment.getId(), event);
    }

    private Map<String, Object> base(Payment payment, String type) {
        Map<String, Object> event = new HashMap<>();
        event.put("event", type);
        event.put("eventId", payment.getId() + ":" + type);
        event.put("paymentId", payment.getId());
        event.put("bookingId", payment.getBookingId());
        event.put("guestId", payment.getGuestId());
        event.put("hostId", payment.getHostId());
        return event;
    }

    private void enqueue(String key, Map<String, Object> payload) {
        try {
            paymentOutboxRepository.save(PaymentOutboxEvent.builder()
                    .topic("payment-events")
                    .messageKey(key)
                    .payload(objectMapper.writeValueAsString(payload))
                    .build());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to enqueue payment event", e);
        }
    }
}
