package com.staysphere.booking.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class BookingRequest {
    private String propertyId;
    private String hostId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer totalGuests;
    private java.math.BigDecimal pricePerNight;
}