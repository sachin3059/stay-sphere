package com.staysphere.payment.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class BookingPaymentClient {

    @Value("${staysphere.booking-service-url:http://localhost:8083}")
    private String bookingServiceUrl;

    public BookingSnapshot fetchBooking(String bookingId, String bearerToken) {
        JsonNode root = WebClient.builder()
                .baseUrl(bookingServiceUrl)
                .build()
                .get()
                .uri("/api/bookings/{id}", bookingId)
                .header("Authorization", bearerToken)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (root == null) {
            throw new IllegalStateException("Booking not found for payment");
        }
        return new BookingSnapshot(
                root.path("status").asText(),
                new BigDecimal(root.path("totalPrice").asText("0")),
                root.path("guestId").asText());
    }

    public record BookingSnapshot(String status, BigDecimal totalPrice, String guestId) {}
}
