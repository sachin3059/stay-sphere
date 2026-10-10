package com.staysphere.booking.listener;

import com.staysphere.booking.entity.ProcessedEvent;
import com.staysphere.booking.repository.ProcessedEventRepository;
import com.staysphere.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentSuccessListener {

    private final BookingService bookingService;
    private final ProcessedEventRepository processedEventRepository;

    @KafkaListener(topics = "payment-events", groupId = "booking-payment-group")
    @Transactional
    public void onPaymentEvent(Map<String, Object> event) {
        if (!"payment_success".equals(event.get("event"))) {
            return;
        }
        String eventId = String.valueOf(event.get("eventId"));
        if (processedEventRepository.existsById(eventId)) {
            return;
        }
        String bookingId = (String) event.get("bookingId");
        if (bookingId == null || bookingId.isBlank()) {
            log.warn("payment_success missing bookingId, eventId={}", eventId);
            return;
        }
        bookingService.confirmBookingInternal(bookingId);
        processedEventRepository.save(
                new ProcessedEvent(eventId, "payment_success_confirm"));
        log.info("Auto-confirmed booking {} after payment_success", bookingId);
    }
}
