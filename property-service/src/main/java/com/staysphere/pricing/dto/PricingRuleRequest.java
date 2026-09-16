package com.staysphere.pricing.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PricingRuleRequest {
    private String propertyId;
    private BigDecimal basePrice;
    private BigDecimal weekendMultiplier;
    private BigDecimal peakSeasonMultiplier;
    private LocalDate peakSeasonStart;
    private LocalDate peakSeasonEnd;
    private Integer minimumStay;
    private BigDecimal longStayDiscount;
    private Integer longStayThresholdNights;
}