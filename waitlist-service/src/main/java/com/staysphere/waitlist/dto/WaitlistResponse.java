package com.staysphere.waitlist.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class WaitlistResponse {
    private String id;
    private String propertyId;
    private String guestId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer totalGuests;
    private Integer queuePosition;
    private String status;
    private LocalDateTime slotOfferedAt;
    private LocalDateTime slotExpiresAt;
    private LocalDateTime createdAt;
}