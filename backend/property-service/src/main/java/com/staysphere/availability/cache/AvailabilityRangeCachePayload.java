package com.staysphere.availability.cache;

import com.staysphere.availability.dto.AvailableRangeResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityRangeCachePayload {
    private List<AvailableRangeResponse> ranges;
}
