package com.staysphere.pricing.service;

import com.staysphere.common.exception.BadRequestException;
import com.staysphere.common.exception.ResourceNotFoundException;
import com.staysphere.pricing.dto.*;
import com.staysphere.pricing.entity.PricingRule;
import com.staysphere.pricing.repository.PricingRuleRepository;
import com.staysphere.property.repository.PropertyRepository;
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
    private final PropertyRepository propertyRepository;

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
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active pricing rule for property: "
                                + request.getPropertyId()));

        long totalNights = ChronoUnit.DAYS.between(
                request.getCheckIn(), request.getCheckOut());

        int minimumStay = rule.getMinimumStay() != null ? rule.getMinimumStay() : 1;
        if (totalNights < minimumStay) {
            throw new BadRequestException(
                    "Minimum stay is " + minimumStay + " nights");
        }

        BigDecimal weekendMultiplier = rule.getWeekendMultiplier() != null
                ? rule.getWeekendMultiplier() : BigDecimal.ONE;
        BigDecimal peakSeasonMultiplier = rule.getPeakSeasonMultiplier() != null
                ? rule.getPeakSeasonMultiplier() : BigDecimal.ONE;
        BigDecimal longStayDiscount = rule.getLongStayDiscount() != null
                ? rule.getLongStayDiscount() : BigDecimal.ONE;
        int longStayThreshold = rule.getLongStayThresholdNights() != null
                ? rule.getLongStayThresholdNights() : 7;

        List<String> appliedRules = new ArrayList<>();
        BigDecimal multiplier = BigDecimal.ONE;

        // Weekend multiplier — check if any night falls on Fri/Sat
        boolean hasWeekend = hasWeekendNight(
                request.getCheckIn(), request.getCheckOut());
        if (hasWeekend && weekendMultiplier.compareTo(BigDecimal.ONE) > 0) {
            multiplier = multiplier.multiply(weekendMultiplier);
            appliedRules.add("Weekend surcharge: ×" + weekendMultiplier);
        }

        // Peak season multiplier
        boolean isPeakSeason = isPeakSeason(
                request.getCheckIn(), request.getCheckOut(),
                rule.getPeakSeasonStart(), rule.getPeakSeasonEnd());
        if (isPeakSeason && peakSeasonMultiplier.compareTo(BigDecimal.ONE) > 0) {
            multiplier = multiplier.multiply(peakSeasonMultiplier);
            appliedRules.add("Peak season surcharge: ×" + peakSeasonMultiplier);
        }

        // Long stay discount
        if (totalNights >= longStayThreshold
                && longStayDiscount.compareTo(BigDecimal.ONE) < 0) {
            multiplier = multiplier.multiply(longStayDiscount);
            appliedRules.add("Long stay discount: ×" + longStayDiscount);
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
                .weekendMultiplier(weekendMultiplier)
                .peakSeasonMultiplier(peakSeasonMultiplier)
                .longStayDiscount(longStayDiscount)
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

    public PricingRuleResponse updateRule(
            String propertyId,
            PricingRuleRequest request,
            String hostId) {
        propertyRepository.findById(propertyId)
                .filter(p -> p.getHostId().equals(hostId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Not authorized to update pricing for this property"));

        PricingRule rule = pricingRuleRepository
                .findByPropertyIdAndStatus(
                        propertyId, PricingRule.RuleStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException(
                        "No pricing rule found for property: " + propertyId));

        if (request.getBasePrice() != null) {
            rule.setBasePrice(request.getBasePrice());
        }
        if (request.getMinimumStay() != null) {
            rule.setMinimumStay(request.getMinimumStay());
        }
        if (request.getWeekendMultiplier() != null) {
            rule.setWeekendMultiplier(request.getWeekendMultiplier());
        }
        if (request.getPeakSeasonMultiplier() != null) {
            rule.setPeakSeasonMultiplier(request.getPeakSeasonMultiplier());
        }
        if (request.getPeakSeasonStart() != null) {
            rule.setPeakSeasonStart(request.getPeakSeasonStart());
        }
        if (request.getPeakSeasonEnd() != null) {
            rule.setPeakSeasonEnd(request.getPeakSeasonEnd());
        }
        if (request.getLongStayDiscount() != null) {
            rule.setLongStayDiscount(request.getLongStayDiscount());
        }
        if (request.getLongStayThresholdNights() != null) {
            rule.setLongStayThresholdNights(
                    request.getLongStayThresholdNights());
        }

        return mapToResponse(pricingRuleRepository.save(rule));
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