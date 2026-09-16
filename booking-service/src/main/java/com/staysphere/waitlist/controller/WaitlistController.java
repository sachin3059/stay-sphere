package com.staysphere.waitlist.controller;

import com.staysphere.common.ApiResponse;
import com.staysphere.waitlist.dto.WaitlistRequest;
import com.staysphere.waitlist.dto.WaitlistResponse;
import com.staysphere.waitlist.service.WaitlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/waitlist")
@RequiredArgsConstructor
public class WaitlistController {

    private final WaitlistService waitlistService;

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext()
                .getAuthentication().getName();
    }

    @PostMapping("/join")
    public ResponseEntity<ApiResponse<WaitlistResponse>> joinWaitlist(
            @RequestBody WaitlistRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Joined waitlist successfully",
                        waitlistService.joinWaitlist(
                                request, getCurrentUserId())));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<WaitlistResponse>> cancelWaitlist(
            @PathVariable String id) {
        return ResponseEntity.ok(
                ApiResponse.success("Waitlist entry cancelled successfully",
                        waitlistService.cancelWaitlist(
                                id, getCurrentUserId())));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<WaitlistResponse>>> getMyWaitlist() {
        return ResponseEntity.ok(
                ApiResponse.success("Waitlist fetched successfully",
                        waitlistService.getMyWaitlist(
                                getCurrentUserId())));
    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<ApiResponse<List<WaitlistResponse>>> getPropertyWaitlist(
            @PathVariable String propertyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate checkOut) {
        return ResponseEntity.ok(
                ApiResponse.success("Property waitlist fetched successfully",
                        waitlistService.getPropertyWaitlist(
                                propertyId, checkIn, checkOut)));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
                ApiResponse.success("Waitlist service is running"));
    }
}