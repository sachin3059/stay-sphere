package com.staysphere.property.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ImageUploadResponse {
    private String propertyId;
    private List<String> imageUrls;
    private int totalImages;
}