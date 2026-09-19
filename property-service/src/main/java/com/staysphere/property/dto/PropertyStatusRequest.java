package com.staysphere.property.dto;

import lombok.Data;

@Data
public class PropertyStatusRequest {
    /** ACTIVE or INACTIVE */
    private String status;
}
