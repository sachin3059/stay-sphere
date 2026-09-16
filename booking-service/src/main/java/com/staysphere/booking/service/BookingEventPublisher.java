package com.staysphere.booking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.staysphere.booking.entity.Booking;
import com.staysphere.booking.outbox.OutboxEvent;
import com.staysphere.booking.outbox.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookingEventPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public void publishBookingCreated(Booking booking) {
        Map<String, Object> event = baseEvent("booking_created", booking);
        event.put("totalPrice", booking.getTotalPrice().toString());
        event.put("idempotencyKey", booking.getIdempotencyKey());
        save("booking-events", booking.getId(), event);
    }

    public void publishBookingConfirmed(Booking booking) {
        save("booking-events", booking.getId(), baseEvent("booking_confirmed", booking));
    }

    public void publishBookingCancelled(Booking booking) {
        save("booking-events", booking.getId(), baseEvent("booking_cancelled", booking));
    }

    private Map<String, Object> baseEvent(String type, Booking booking) {
        Map<String, Object> event = new HashMap<>();
        event.put("event", type);
        event.put("eventId", booking.getId() + ":" + type + ":" + System.currentTimeMillis());
        event.put("bookingId", booking.getId());
        event.put("propertyId", booking.getPropertyId());
        event.put("guestId", booking.getGuestId());
        event.put("hostId", booking.getHostId());
        event.put("checkIn", booking.getCheckIn().toString());
        event.put("checkOut", booking.getCheckOut().toString());
        return event;
    }

    private void save(String topic, String key, Map<String, Object> payload) {
        try {
            outboxEventRepository.save(OutboxEvent.builder()
                    .topic(topic)
                    .messageKey(key)
                    .payload(objectMapper.writeValueAsString(payload))
                    .build());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to enqueue outbox event", e);
        }
    }
}
