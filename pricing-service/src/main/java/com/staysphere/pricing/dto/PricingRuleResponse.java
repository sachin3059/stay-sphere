package com.staysphere.pricing.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class PricingRuleResponse {
    private String id;
    private String propertyId;
    private BigDecimal basePrice;
    private BigDecimal weekendMultiplier;
    private BigDecimal peakSeasonMultiplier;
    private LocalDate peakSeasonStart;
    private LocalDate peakSeasonEnd;
    private Integer minimumStay;
    private BigDecimal longStayDiscount;
    private Integer longStayThresholdNights;
    private String status;
}