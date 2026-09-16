package com.staysphere.availability.controller;

import com.staysphere.availability.dto.*;
import com.staysphere.availability.service.AvailabilityService;
import com.staysphere.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @GetMapping("/{propertyId}/ranges")
    public ResponseEntity<ApiResponse<List<AvailableRangeResponse>>> getAvailableRanges(
            @PathVariable String propertyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to) {
        return ResponseEntity.ok(
                ApiResponse.success("Available ranges fetched successfully",
                        availabilityService.getAvailableRanges(
                                propertyId, from, to)));
    }

    @GetMapping("/{propertyId}/check")
    public ResponseEntity<ApiResponse<AvailabilityCheckResponse>> checkAvailability(
            @PathVariable String propertyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate checkOut) {
        return ResponseEntity.ok(
                ApiResponse.success("Availability checked successfully",
                        availabilityService.checkAvailability(
                                propertyId, checkIn, checkOut)));
    }

    @PostMapping("/block")
    public ResponseEntity<ApiResponse<BlockedDateResponse>> blockDates(
            @RequestBody BlockedDateRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Dates blocked successfully",
                        availabilityService.blockDates(request)));
    }

    @GetMapping("/{propertyId}/blocked")
    public ResponseEntity<ApiResponse<List<BlockedDateResponse>>> getBlockedDates(
            @PathVariable String propertyId) {
        return ResponseEntity.ok(
                ApiResponse.success("Blocked dates fetched successfully",
                        availabilityService.getBlockedDates(propertyId)));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
                ApiResponse.success("Availability service is running"));
    }
}