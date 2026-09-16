package com.staysphere.booking.controller;

import com.staysphere.booking.dto.BookingRequest;
import jakarta.validation.Valid;
import com.staysphere.booking.dto.BookingResponse;
import com.staysphere.booking.service.BookingService;
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

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext()
                .getAuthentication().getName();
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request) {
        return ResponseEntity.ok(
                bookingService.createBooking(request, getCurrentUserId()));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<BookingResponse> confirmBooking(
            @PathVariable String id) {
        return ResponseEntity.ok(bookingService.confirmBooking(id));
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
        return ResponseEntity.ok(bookingService.getBooking(id));
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
                bookingService.getPropertyBookings(propertyId));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Booking service is running");
    }
}