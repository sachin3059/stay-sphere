package com.staysphere.notification.repository;

import com.staysphere.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, String> {

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(
            String recipientId);

    List<Notification> findByRecipientIdAndStatus(
            String recipientId, Notification.NotificationStatus status);

    List<Notification> findByReferenceId(String referenceId);
}