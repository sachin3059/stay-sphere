package com.staysphere.notification.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private String id;
    private String recipientId;
    private String recipientEmail;
    private String subject;
    private String message;
    private String notificationType;
    private String status;
    private String referenceId;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
}