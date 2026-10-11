package com.staysphere.property.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PresignPropertyMediaResponse {
    private String propertyId;
    private List<PresignedUploadItem> uploads;
}
