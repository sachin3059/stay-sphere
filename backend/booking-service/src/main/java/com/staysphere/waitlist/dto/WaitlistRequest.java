package com.staysphere.waitlist.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class WaitlistRequest {
    private String propertyId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer totalGuests;
}