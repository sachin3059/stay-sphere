package com.staysphere.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminUserResponse {
    private String id;
    private String email;
    private String fullName;
    private String role;
    private boolean socialOnly;
    private LocalDateTime createdAt;
}
