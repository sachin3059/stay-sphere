package com.staysphere.booking.service;

import com.staysphere.booking.entity.Booking;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookingEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishBookingCreated(Booking booking) {
        kafkaTemplate.send("booking-events",
                booking.getId(),
                Map.of(
                        "event", "booking_created",
                        "bookingId", booking.getId(),
                        "propertyId", booking.getPropertyId(),
                        "guestId", booking.getGuestId(),
                        "hostId", booking.getHostId(),
                        "checkIn", booking.getCheckIn().toString(),
                        "checkOut", booking.getCheckOut().toString(),
                        "totalPrice", booking.getTotalPrice().toString(),
                        "idempotencyKey", booking.getIdempotencyKey()
                ));
    }

    public void publishBookingConfirmed(Booking booking) {
        kafkaTemplate.send("booking-events",
                booking.getId(),
                Map.of(
                        "event", "booking_confirmed",
                        "bookingId", booking.getId(),
                        "propertyId", booking.getPropertyId(),
                        "guestId", booking.getGuestId(),
                        "hostId", booking.getHostId(),
                        "checkIn", booking.getCheckIn().toString(),
                        "checkOut", booking.getCheckOut().toString()
                ));
    }

    public void publishBookingCancelled(Booking booking) {
        kafkaTemplate.send("booking-events",
                booking.getId(),
                Map.of(
                        "event", "booking_cancelled",
                        "bookingId", booking.getId(),
                        "propertyId", booking.getPropertyId(),
                        "guestId", booking.getGuestId(),
                        "checkIn", booking.getCheckIn().toString(),
                        "checkOut", booking.getCheckOut().toString()
                ));
    }
}
