package com.staysphere.property.dto;

import lombok.Data;

@Data
public class MediaFileDescriptor {
    private String fileName;
    private String contentType;
    private long sizeBytes;
}
