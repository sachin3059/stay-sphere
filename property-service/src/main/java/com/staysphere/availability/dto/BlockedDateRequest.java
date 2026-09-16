package com.staysphere.availability.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class BlockedDateRequest {
    private String propertyId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
}