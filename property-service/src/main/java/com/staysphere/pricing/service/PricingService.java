package com.staysphere.pricing.service;

import com.staysphere.pricing.dto.*;
import com.staysphere.pricing.entity.PricingRule;
import com.staysphere.pricing.repository.PricingRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PricingService {

    private final PricingRuleRepository pricingRuleRepository;

    public PricingRuleResponse createRule(PricingRuleRequest request) {
        PricingRule rule = PricingRule.builder()
                .propertyId(request.getPropertyId())
                .basePrice(request.getBasePrice())
                .weekendMultiplier(request.getWeekendMultiplier())
                .peakSeasonMultiplier(request.getPeakSeasonMultiplier())
                .peakSeasonStart(request.getPeakSeasonStart())
                .peakSeasonEnd(request.getPeakSeasonEnd())
                .minimumStay(request.getMinimumStay())
                .longStayDiscount(request.getLongStayDiscount())
                .longStayThresholdNights(request.getLongStayThresholdNights())
                .status(PricingRule.RuleStatus.ACTIVE)
                .build();

        return mapToResponse(pricingRuleRepository.save(rule));
    }

    public PriceCalculationResponse calculatePrice(
            PriceCalculationRequest request) {

        PricingRule rule = pricingRuleRepository
                .findByPropertyIdAndStatus(
                        request.getPropertyId(),
                        PricingRule.RuleStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException(
                        "No active pricing rule for property: "
                                + request.getPropertyId()));

        long totalNights = ChronoUnit.DAYS.between(
                request.getCheckIn(), request.getCheckOut());

        if (totalNights < rule.getMinimumStay()) {
            throw new RuntimeException(
                    "Minimum stay is " + rule.getMinimumStay() + " nights");
        }

        List<String> appliedRules = new ArrayList<>();
        BigDecimal multiplier = BigDecimal.ONE;

        // Weekend multiplier — check if any night falls on Fri/Sat
        boolean hasWeekend = hasWeekendNight(
                request.getCheckIn(), request.getCheckOut());
        if (hasWeekend && rule.getWeekendMultiplier()
                .compareTo(BigDecimal.ONE) > 0) {
            multiplier = multiplier.multiply(rule.getWeekendMultiplier());
            appliedRules.add("Weekend surcharge: ×"
                    + rule.getWeekendMultiplier());
        }

        // Peak season multiplier
        boolean isPeakSeason = isPeakSeason(
                request.getCheckIn(), request.getCheckOut(),
                rule.getPeakSeasonStart(), rule.getPeakSeasonEnd());
        if (isPeakSeason && rule.getPeakSeasonMultiplier()
                .compareTo(BigDecimal.ONE) > 0) {
            multiplier = multiplier.multiply(rule.getPeakSeasonMultiplier());
            appliedRules.add("Peak season surcharge: ×"
                    + rule.getPeakSeasonMultiplier());
        }

        // Long stay discount
        if (totalNights >= rule.getLongStayThresholdNights()
                && rule.getLongStayDiscount()
                .compareTo(BigDecimal.ONE) < 0) {
            multiplier = multiplier.multiply(rule.getLongStayDiscount());
            appliedRules.add("Long stay discount: ×"
                    + rule.getLongStayDiscount());
        }

        if (appliedRules.isEmpty()) {
            appliedRules.add("Base price applied");
        }

        BigDecimal finalPricePerNight = rule.getBasePrice()
                .multiply(multiplier)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalPrice = finalPricePerNight
                .multiply(BigDecimal.valueOf(totalNights))
                .setScale(2, RoundingMode.HALF_UP);

        log.info("Price calculated for property {}: {} × {} nights = {}",
                request.getPropertyId(), finalPricePerNight,
                totalNights, totalPrice);

        return PriceCalculationResponse.builder()
                .propertyId(request.getPropertyId())
                .basePrice(rule.getBasePrice())
                .finalPricePerNight(finalPricePerNight)
                .totalPrice(totalPrice)
                .totalNights(totalNights)
                .weekendMultiplier(rule.getWeekendMultiplier())
                .peakSeasonMultiplier(rule.getPeakSeasonMultiplier())
                .longStayDiscount(rule.getLongStayDiscount())
                .appliedRules(appliedRules)
                .build();
    }

    public PricingRuleResponse getRuleByProperty(String propertyId) {
        return mapToResponse(pricingRuleRepository
                .findByPropertyIdAndStatus(
                        propertyId, PricingRule.RuleStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException(
                        "No pricing rule found for property: " + propertyId)));
    }

    private boolean hasWeekendNight(LocalDate checkIn, LocalDate checkOut) {
        LocalDate date = checkIn;
        while (date.isBefore(checkOut)) {
            DayOfWeek day = date.getDayOfWeek();
            if (day == DayOfWeek.FRIDAY || day == DayOfWeek.SATURDAY) {
                return true;
            }
            date = date.plusDays(1);
        }
        return false;
    }

    private boolean isPeakSeason(LocalDate checkIn, LocalDate checkOut,
                                 LocalDate peakStart, LocalDate peakEnd) {
        if (peakStart == null || peakEnd == null) return false;
        return checkIn.isBefore(peakEnd) && checkOut.isAfter(peakStart);
    }

    private PricingRuleResponse mapToResponse(PricingRule r) {
        return PricingRuleResponse.builder()
                .id(r.getId())
                .propertyId(r.getPropertyId())
                .basePrice(r.getBasePrice())
                .weekendMultiplier(r.getWeekendMultiplier())
                .peakSeasonMultiplier(r.getPeakSeasonMultiplier())
                .peakSeasonStart(r.getPeakSeasonStart())
                .peakSeasonEnd(r.getPeakSeasonEnd())
                .minimumStay(r.getMinimumStay())
                .longStayDiscount(r.getLongStayDiscount())
                .longStayThresholdNights(r.getLongStayThresholdNights())
                .status(r.getStatus().name())
                .build();
    }
}