package com.staysphere.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingExpiryScheduler {

    private final BookingService bookingService;

    @Scheduled(fixedRateString = "${booking.expiry-check-ms:300000}")
    public void expireStalePending() {
        bookingService.expirePendingBookings();
    }
}
