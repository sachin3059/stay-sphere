package com.staysphere.booking.controller;

import com.staysphere.booking.dto.BookingRequest;
import com.staysphere.booking.dto.BookingResponse;
import com.staysphere.booking.service.BookingService;
import com.staysphere.common.exception.BadRequestException;
import com.staysphere.common.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final JwtUtil jwtUtil;

    private void requireVerifiedEmail(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.extractEmailVerified(token)) {
            throw new BadRequestException(
                    "Verify your email before creating a booking.");
        }
    }

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext()
                .getAuthentication().getName();
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            HttpServletRequest httpRequest,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody BookingRequest request) {
        requireVerifiedEmail(httpRequest);
        return ResponseEntity.ok(
                bookingService.createBooking(request, getCurrentUserId(), idempotencyKey));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<BookingResponse> confirmBooking(
            @PathVariable String id) {
        return ResponseEntity.ok(
                bookingService.confirmBooking(id, getCurrentUserId()));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable String id) {
        return ResponseEntity.ok(
                bookingService.cancelBooking(id, getCurrentUserId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBooking(
            @PathVariable String id) {
        return ResponseEntity.ok(
                bookingService.getBooking(id, getCurrentUserId()));
    }

    @GetMapping("/my")
    public ResponseEntity<List<BookingResponse>> getMyBookings() {
        return ResponseEntity.ok(
                bookingService.getGuestBookings(getCurrentUserId()));
    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<BookingResponse>> getPropertyBookings(
            @PathVariable String propertyId) {
        return ResponseEntity.ok(
                bookingService.getPropertyBookings(propertyId, getCurrentUserId()));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Booking service is running");
    }
}
