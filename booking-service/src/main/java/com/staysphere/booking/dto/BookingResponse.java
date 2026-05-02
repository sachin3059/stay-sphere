package com.staysphere.booking.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class BookingResponse {
    private String id;
    private String propertyId;
    private String guestId;
    private String hostId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer totalGuests;
    private BigDecimal totalPrice;
    private BigDecimal pricePerNight;
    private String status;
    private String idempotencyKey;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
}