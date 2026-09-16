package com.staysphere.pricing.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "pricing_rules")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PricingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String propertyId;

    @Column(nullable = false)
    private BigDecimal basePrice;

    // Weekend multiplier e.g 1.3 = 30% more on weekends
    private BigDecimal weekendMultiplier;

    // Peak season multiplier e.g 1.5 = 50% more in peak
    private BigDecimal peakSeasonMultiplier;

    // Peak season date range
    private LocalDate peakSeasonStart;
    private LocalDate peakSeasonEnd;

    // Minimum stay in nights
    private Integer minimumStay;

    // Discount for long stays e.g 0.9 = 10% off for 7+ nights
    private BigDecimal longStayDiscount;
    private Integer longStayThresholdNights;

    @Enumerated(EnumType.STRING)
    private RuleStatus status;

    public enum RuleStatus {
        ACTIVE, INACTIVE
    }

    @PrePersist
    protected void onCreate() {
        if (status == null) status = RuleStatus.ACTIVE;
        if (weekendMultiplier == null)
            weekendMultiplier = BigDecimal.ONE;
        if (peakSeasonMultiplier == null)
            peakSeasonMultiplier = BigDecimal.ONE;
        if (longStayDiscount == null)
            longStayDiscount = BigDecimal.ONE;
        if (longStayThresholdNights == null)
            longStayThresholdNights = 7;
        if (minimumStay == null)
            minimumStay = 1;
    }
}