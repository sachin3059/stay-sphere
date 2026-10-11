package com.staysphere.property.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PresignedUploadItem {
    private String uploadUrl;
    private String objectKey;
    private String publicUrl;
    private String contentType;
}
