package com.staysphere.notification.controller;

import com.staysphere.common.ApiResponse;
import com.staysphere.notification.dto.NotificationResponse;
import com.staysphere.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext()
                .getAuthentication().getName();
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications() {
        return ResponseEntity.ok(
                ApiResponse.success("Notifications fetched successfully",
                        notificationService.getMyNotifications(
                                getCurrentUserId())));
    }

    @GetMapping("/reference/{referenceId}")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getByReference(
            @PathVariable String referenceId) {
        return ResponseEntity.ok(
                ApiResponse.success("Notifications fetched successfully",
                        notificationService.getNotificationsByReference(
                                referenceId)));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
                ApiResponse.success("Notification service is running"));
    }
}