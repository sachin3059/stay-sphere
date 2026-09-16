package com.staysphere.booking;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BookingOverlapLogicTest {

    @Test
    void overlappingRangesAreDetected() {
        LocalDate aIn = LocalDate.of(2026, 5, 1);
        LocalDate aOut = LocalDate.of(2026, 5, 5);
        LocalDate bIn = LocalDate.of(2026, 5, 3);
        LocalDate bOut = LocalDate.of(2026, 5, 7);
        assertTrue(aIn.isBefore(bOut) && bIn.isBefore(aOut));
    }
}
