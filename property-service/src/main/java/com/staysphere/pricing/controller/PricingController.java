package com.staysphere.pricing.controller;

import com.staysphere.common.ApiResponse;
import com.staysphere.pricing.dto.*;
import com.staysphere.pricing.service.PricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
public class PricingController {

    private final PricingService pricingService;

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
    }

    @PostMapping("/rules")
    @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<ApiResponse<PricingRuleResponse>> createRule(
            @RequestBody PricingRuleRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Pricing rule created successfully",
                        pricingService.createRule(request)));
    }

    @PutMapping("/rules/{propertyId}")
    @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<ApiResponse<PricingRuleResponse>> updateRule(
            @PathVariable String propertyId,
            @RequestBody PricingRuleRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Pricing rule updated successfully",
                        pricingService.updateRule(
                                propertyId, request, getCurrentUserId())));
    }

    @GetMapping("/rules/{propertyId}")
    public ResponseEntity<ApiResponse<PricingRuleResponse>> getRuleByProperty(
            @PathVariable String propertyId) {
        return ResponseEntity.ok(
                ApiResponse.success("Pricing rule fetched successfully",
                        pricingService.getRuleByProperty(propertyId)));
    }

    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<PriceCalculationResponse>> calculatePrice(
            @RequestBody PriceCalculationRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Price calculated successfully",
                        pricingService.calculatePrice(request)));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
                ApiResponse.success("Pricing service is running"));
    }
}