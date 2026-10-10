package com.staysphere.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications",
        indexes = {
                @Index(name = "idx_notification_recipient",
                        columnList = "recipient_id"),
                @Index(name = "idx_notification_type",
                        columnList = "notification_type")
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String recipientId;

    @Column(nullable = false)
    private String recipientEmail;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, length = 2000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    private String referenceId;
    private String errorMessage;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime sentAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = NotificationStatus.PENDING;
    }

    public enum NotificationType {
        BOOKING_CREATED,
        BOOKING_CONFIRMED,
        BOOKING_CANCELLED,
        PAYMENT_SUCCESS,
        PAYMENT_FAILED,
        PAYMENT_REFUNDED,
        SLOT_OFFERED,
        GENERAL
    }

    public enum NotificationStatus {
        PENDING, SENT, FAILED
    }
}