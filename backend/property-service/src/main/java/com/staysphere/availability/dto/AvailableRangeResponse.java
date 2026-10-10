package com.staysphere.availability.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class AvailableRangeResponse {
    private LocalDate from;
    private LocalDate to;
    private long nights;
}