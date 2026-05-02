package com.staysphere.notification.service;

import com.staysphere.notification.dto.NotificationResponse;
import com.staysphere.notification.entity.Notification;
import com.staysphere.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    // Consume booking events
    @KafkaListener(topics = "booking-events",
            groupId = "notification-group")
    @Transactional
    public void onBookingEvent(Map<String, Object> event) {
        String eventType = (String) event.get("event");
        log.info("Received booking event: {}", eventType);

        switch (eventType) {
            case "booking_created" -> handleBookingCreated(event);
            case "booking_confirmed" -> handleBookingConfirmed(event);
            case "booking_cancelled" -> handleBookingCancelled(event);
        }
    }

    // Consume payment events
    @KafkaListener(topics = "payment-events",
            groupId = "notification-group")
    @Transactional
    public void onPaymentEvent(Map<String, Object> event) {
        String eventType = (String) event.get("event");
        log.info("Received payment event: {}", eventType);

        switch (eventType) {
            case "payment_success" -> handlePaymentSuccess(event);
            case "payment_failed" -> handlePaymentFailed(event);
            case "payment_refunded" -> handlePaymentRefunded(event);
        }
    }

    // Consume waitlist events
    @KafkaListener(topics = "waitlist-events",
            groupId = "notification-group")
    @Transactional
    public void onWaitlistEvent(Map<String, Object> event) {
        String eventType = (String) event.get("event");
        log.info("Received waitlist event: {}", eventType);

        if ("slot_offered".equals(eventType)) {
            handleSlotOffered(event);
        }
    }

    private void handleBookingCreated(Map<String, Object> event) {
        String guestId = (String) event.get("guestId");
        String bookingId = (String) event.get("bookingId");
        String checkIn = (String) event.get("checkIn");
        String checkOut = (String) event.get("checkOut");
        String totalPrice = (String) event.get("totalPrice");

        String subject = "Booking Request Received — StaySphere";
        String message = String.format(
                "Hi,\n\nYour booking request has been received!\n\n" +
                        "Booking ID: %s\nCheck-in: %s\nCheck-out: %s\n" +
                        "Total Price: ₹%s\nStatus: PENDING\n\n" +
                        "Please complete payment to confirm your booking.\n\n" +
                        "StaySphere Team",
                bookingId, checkIn, checkOut, totalPrice);

        saveAndSend(guestId, guestId, subject, message,
                Notification.NotificationType.BOOKING_CREATED, bookingId);
    }

    private void handleBookingConfirmed(Map<String, Object> event) {
        String guestId = (String) event.get("guestId");
        String hostId = (String) event.get("hostId");
        String bookingId = (String) event.get("bookingId");
        String checkIn = (String) event.get("checkIn");
        String checkOut = (String) event.get("checkOut");

        String subject = "Booking Confirmed! — StaySphere";
        String guestMsg = String.format(
                "Hi,\n\nGreat news! Your booking is CONFIRMED!\n\n" +
                        "Booking ID: %s\nCheck-in: %s\nCheck-out: %s\n\n" +
                        "Enjoy your stay!\n\nStaySphere Team",
                bookingId, checkIn, checkOut);

        String hostMsg = String.format(
                "Hi,\n\nA booking has been confirmed for your property!\n\n" +
                        "Booking ID: %s\nCheck-in: %s\nCheck-out: %s\n\n" +
                        "StaySphere Team",
                bookingId, checkIn, checkOut);

        saveAndSend(guestId, guestId, subject, guestMsg,
                Notification.NotificationType.BOOKING_CONFIRMED, bookingId);
        saveAndSend(hostId, hostId, "New Booking Confirmed — StaySphere",
                hostMsg, Notification.NotificationType.BOOKING_CONFIRMED,
                bookingId);
    }

    private void handleBookingCancelled(Map<String, Object> event) {
        String guestId = (String) event.get("guestId");
        String bookingId = (String) event.get("bookingId");

        String subject = "Booking Cancelled — StaySphere";
        String message = String.format(
                "Hi,\n\nYour booking has been cancelled.\n\n" +
                        "Booking ID: %s\n\n" +
                        "If you have any questions, contact our support.\n\n" +
                        "StaySphere Team", bookingId);

        saveAndSend(guestId, guestId, subject, message,
                Notification.NotificationType.BOOKING_CANCELLED, bookingId);
    }

    private void handlePaymentSuccess(Map<String, Object> event) {
        String guestId = (String) event.get("guestId");
        String paymentId = (String) event.get("paymentId");
        String amount = (String) event.get("amount");
        String transactionId = (String) event.get("transactionId");

        String subject = "Payment Successful — StaySphere";
        String message = String.format(
                "Hi,\n\nYour payment was successful!\n\n" +
                        "Payment ID: %s\nTransaction ID: %s\nAmount: ₹%s\n\n" +
                        "StaySphere Team",
                paymentId, transactionId, amount);

        saveAndSend(guestId, guestId, subject, message,
                Notification.NotificationType.PAYMENT_SUCCESS, paymentId);
    }

    private void handlePaymentFailed(Map<String, Object> event) {
        String guestId = (String) event.get("guestId");
        String paymentId = (String) event.get("paymentId");

        String subject = "Payment Failed — StaySphere";
        String message = String.format(
                "Hi,\n\nUnfortunately your payment failed.\n\n" +
                        "Payment ID: %s\n\n" +
                        "Please try again or use a different payment method.\n\n" +
                        "StaySphere Team", paymentId);

        saveAndSend(guestId, guestId, subject, message,
                Notification.NotificationType.PAYMENT_FAILED, paymentId);
    }

    private void handlePaymentRefunded(Map<String, Object> event) {
        String guestId = (String) event.get("guestId");
        String paymentId = (String) event.get("paymentId");
        String amount = (String) event.get("amount");

        String subject = "Refund Processed — StaySphere";
        String message = String.format(
                "Hi,\n\nYour refund of ₹%s has been processed.\n\n" +
                        "Payment ID: %s\n\n" +
                        "It may take 3-5 business days to reflect.\n\n" +
                        "StaySphere Team", amount, paymentId);

        saveAndSend(guestId, guestId, subject, message,
                Notification.NotificationType.PAYMENT_REFUNDED, paymentId);
    }

    private void handleSlotOffered(Map<String, Object> event) {
        String guestId = (String) event.get("guestId");
        String propertyId = (String) event.get("propertyId");
        String checkIn = (String) event.get("checkIn");
        String checkOut = (String) event.get("checkOut");
        String expiresAt = (String) event.get("expiresAt");

        String subject = "Your Waitlist Slot is Available! — StaySphere";
        String message = String.format(
                "Hi,\n\nGreat news! A slot has opened up for your waitlisted property!\n\n" +
                        "Property ID: %s\nCheck-in: %s\nCheck-out: %s\n\n" +
                        "You have 30 minutes to complete your booking before: %s\n\n" +
                        "Book now before someone else takes it!\n\n" +
                        "StaySphere Team",
                propertyId, checkIn, checkOut, expiresAt);

        saveAndSend(guestId, guestId, subject, message,
                Notification.NotificationType.SLOT_OFFERED, propertyId);
    }

    private void saveAndSend(String recipientId, String recipientEmail,
                             String subject, String message,
                             Notification.NotificationType type,
                             String referenceId) {
        Notification notification = Notification.builder()
                .recipientId(recipientId)
                .recipientEmail(recipientEmail)
                .subject(subject)
                .message(message)
                .notificationType(type)
                .referenceId(referenceId)
                .status(Notification.NotificationStatus.PENDING)
                .build();

        Notification saved = notificationRepository.save(notification);

        // Try to send email — log is used as fallback in dev
        // In prod this calls real SMTP/SendGrid
        boolean sent = emailService.sendEmail(
                recipientEmail, subject, message);

        saved.setStatus(sent
                ? Notification.NotificationStatus.SENT
                : Notification.NotificationStatus.FAILED);
        saved.setSentAt(LocalDateTime.now());

        if (!sent) {
            saved.setErrorMessage("Email delivery failed");
        }

        notificationRepository.save(saved);
        log.info("Notification {} for recipient {} — status: {}",
                type, recipientId, saved.getStatus());
    }

    public List<NotificationResponse> getMyNotifications(
            String recipientId) {
        return notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(recipientId)
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<NotificationResponse> getNotificationsByReference(
            String referenceId) {
        return notificationRepository.findByReferenceId(referenceId)
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .recipientId(n.getRecipientId())
                .recipientEmail(n.getRecipientEmail())
                .subject(n.getSubject())
                .message(n.getMessage())
                .notificationType(n.getNotificationType().name())
                .status(n.getStatus().name())
                .referenceId(n.getReferenceId())
                .createdAt(n.getCreatedAt())
                .sentAt(n.getSentAt())
                .build();
    }
}