package com.staysphere.pricing.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class PriceCalculationResponse {
    private String propertyId;
    private BigDecimal basePrice;
    private BigDecimal finalPricePerNight;
    private BigDecimal totalPrice;
    private Long totalNights;
    private BigDecimal weekendMultiplier;
    private BigDecimal peakSeasonMultiplier;
    private BigDecimal longStayDiscount;
    private List<String> appliedRules;
}