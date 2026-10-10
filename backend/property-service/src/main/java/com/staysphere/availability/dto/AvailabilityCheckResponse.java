package com.staysphere.availability.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class AvailabilityCheckResponse {
    private String propertyId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private boolean available;
    private String message;
}