package com.portfolio.stocksage.controller.api;

import com.portfolio.stocksage.entity.Notification;
import com.portfolio.stocksage.security.SecurityUtils;
import com.portfolio.stocksage.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification API", description = "Endpoints for managing notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final SecurityUtils securityUtils;

    @GetMapping
    @Operation(summary = "Get user notifications", description = "Returns a paginated list of notifications for the current user")
    public ResponseEntity<Page<Notification>> getUserNotifications(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Include only unread notifications")
            @RequestParam(defaultValue = "false") boolean unreadOnly) {

        Long currentUserId = securityUtils.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Notification> notifications;
        if (unreadOnly) {
            notifications = notificationService.getUnreadUserNotifications(currentUserId, pageable);
        } else {
            notifications = notificationService.getUserNotifications(currentUserId, pageable);
        }

        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/system")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get system notifications", description = "Returns a paginated list of system-wide notifications")
    public ResponseEntity<Page<Notification>> getSystemNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Notification> notifications = notificationService.getSystemNotifications(pageable);

        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/count")
    @Operation(summary = "Count unread notifications", description = "Returns the count of unread notifications for the current user")
    public ResponseEntity<Long> countUnreadNotifications() {
        Long currentUserId = securityUtils.getCurrentUserId();
        long count = notificationService.countUnreadNotifications(currentUserId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get notification by ID", description = "Returns a specific notification if it belongs to the current user")
    public ResponseEntity<Notification> getNotificationById(
            @Parameter(description = "Notification ID", required = true)
            @PathVariable Long id) {

        Notification notification = notificationService.getNotificationById(id);
        Long currentUserId = securityUtils.getCurrentUserId();

        // Check if notification belongs to current user or is a system notification
        if (notification.getUser() == null || notification.getUser().getId().equals(currentUserId)) {
            return ResponseEntity.ok(notification);
        } else {
            return ResponseEntity.status(403).build();
        }
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark notification as read", description = "Marks a notification as read if it belongs to the current user")
    public ResponseEntity<Notification> markAsRead(
            @Parameter(description = "Notification ID", required = true)
            @PathVariable Long id) {

        Notification notification = notificationService.getNotificationById(id);
        Long currentUserId = securityUtils.getCurrentUserId();

        // Check if notification belongs to current user
        if (notification.getUser() != null && notification.getUser().getId().equals(currentUserId)) {
            Notification updatedNotification = notificationService.markAsRead(id);
            return ResponseEntity.ok(updatedNotification);
        } else {
            return ResponseEntity.status(403).build();
        }
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read", description = "Marks all notifications for the current user as read")
    public ResponseEntity<Void> markAllAsRead() {
        Long currentUserId = securityUtils.getCurrentUserId();
        notificationService.markAllAsRead(currentUserId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete notification", description = "Deletes a notification if it belongs to the current user")
    public ResponseEntity<Void> deleteNotification(
            @Parameter(description = "Notification ID", required = true)
            @PathVariable Long id) {

        Notification notification = notificationService.getNotificationById(id);
        Long currentUserId = securityUtils.getCurrentUserId();

        // Check if notification belongs to current user or user is admin
        if ((notification.getUser() != null && notification.getUser().getId().equals(currentUserId))
                || securityUtils.isCurrentUserAdmin()) {
            notificationService.deleteNotification(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(403).build();
        }
    }

    @PostMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create test notification", description = "Creates a test notification for the current user (admin only)")
    public ResponseEntity<Notification> createTestNotification(
            @Parameter(description = "Notification title")
            @RequestParam(defaultValue = "Test Notification") String title,
            @Parameter(description = "Notification message")
            @RequestParam(defaultValue = "This is a test notification") String message) {

        Long currentUserId = securityUtils.getCurrentUserId();
        Notification notification = notificationService.createNotification(title, message, "TEST", currentUserId);
        return ResponseEntity.ok(notification);
    }
}