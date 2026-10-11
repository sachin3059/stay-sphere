package com.staysphere.property.dto;

import lombok.Data;

import java.util.List;

@Data
public class PresignPropertyMediaRequest {
    private List<MediaFileDescriptor> files;
}
