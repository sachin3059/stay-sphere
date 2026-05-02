package com.staysphere.pricing.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PriceCalculationRequest {
    private String propertyId;
    private LocalDate checkIn;
    private LocalDate checkOut;
}