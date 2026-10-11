package com.staysphere.property.dto;

import lombok.Data;

import java.util.List;

@Data
public class ConfirmPropertyMediaRequest {
    private List<String> objectKeys;
}
