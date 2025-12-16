package com.portfolio.stocksage.service;

import com.portfolio.stocksage.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.concurrent.CompletableFuture;

public interface NotificationService {

    /**
     * Create a new notification for a specific user
     */
    Notification createNotification(String title, String message, String type, Long userId);

    /**
     * Create a system-wide notification (not tied to a specific user)
     */
    Notification createSystemNotification(String title, String message, String type);

    /**
     * Create a notification for all users in the system
     */
    void createNotificationForAllUsers(String title, String message, String type);

    /**
     * Create a notification for all users with a specific role
     */
    void createNotificationForRole(String title, String message, String type, String roleName);

    /**
     * Get a notification by its ID
     */
    Notification getNotificationById(Long id);

    /**
     * Mark a notification as read
     */
    Notification markAsRead(Long id);

    /**
     * Mark all notifications for a user as read
     */
    void markAllAsRead(Long userId);

    /**
     * Delete a notification
     */
    void deleteNotification(Long id);

    /**
     * Get all notifications for a user, sorted by creation date
     */
    Page<Notification> getUserNotifications(Long userId, Pageable pageable);

    /**
     * Get unread notifications for a user, sorted by creation date
     */
    Page<Notification> getUnreadUserNotifications(Long userId, Pageable pageable);

    /**
     * Get system notifications
     */
    Page<Notification> getSystemNotifications(Pageable pageable);

    /**
     * Count unread notifications for a user
     */
    long countUnreadNotifications(Long userId);

    /**
     * Send a low stock alert for a product
     */
    CompletableFuture<Void> sendLowStockAlert(Long productId, String productName, int currentStock, int threshold);

    /**
     * Send a notification about a transaction status change
     */
    CompletableFuture<Void> sendTransactionNotification(String transactionNumber, String transactionType,
                                                        String status, Long userId);

    /**
     * Send an alert when a product is completely out of stock
     */
    CompletableFuture<Void> sendStockOutAlert(Long productId, String productName);
}