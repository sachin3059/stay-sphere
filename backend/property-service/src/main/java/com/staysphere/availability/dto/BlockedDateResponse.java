package com.staysphere.availability.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class BlockedDateResponse {
    private String id;
    private String propertyId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private String referenceId;
    private LocalDateTime createdAt;
}