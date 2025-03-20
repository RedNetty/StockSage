package com.portfolio.stocksage.service.impl;

import com.portfolio.stocksage.entity.Notification;
import com.portfolio.stocksage.entity.User;
import com.portfolio.stocksage.exception.ResourceNotFoundException;
import com.portfolio.stocksage.repository.NotificationRepository;
import com.portfolio.stocksage.repository.UserRepository;
import com.portfolio.stocksage.service.NotificationService;
import com.portfolio.stocksage.util.EmailUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EmailUtils emailUtils;
    private final boolean emailEnabled;

    @Autowired
    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            EmailUtils emailUtils,
            @org.springframework.beans.factory.annotation.Value("${application.email.enabled:false}") boolean emailEnabled) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.emailUtils = emailUtils;
        this.emailEnabled = emailEnabled;
    }

    @Override
    @Transactional
    public Notification createNotification(String title, String message, String type, Long userId) {
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        }

        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setUser(user);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);

        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public Notification createSystemNotification(String title, String message, String type) {
        return createNotification(title, message, type, null);
    }

    @Override
    @Transactional
    public void createNotificationForAllUsers(String title, String message, String type) {
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            createNotification(title, message, type, user.getId());
        }
    }

    @Override
    @Transactional
    public void createNotificationForRole(String title, String message, String type, String roleName) {
        List<User> usersWithRole = userRepository.findByRoleName(roleName);
        for (User user : usersWithRole) {
            createNotification(title, message, type, user.getId());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Notification getNotificationById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
    }

    @Override
    @Transactional
    public Notification markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndReadFalse(userId);
        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    @Override
    @Transactional
    public void deleteNotification(Long id) {
        if (!notificationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Notification not found with id: " + id);
        }
        notificationRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> getUserNotifications(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> getUnreadUserNotifications(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        return notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> getSystemNotifications(Pageable pageable) {
        return notificationRepository.findByUserIsNullOrderByCreatedAtDesc(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnreadNotifications(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Override
    @Async
    public CompletableFuture<Void> sendLowStockAlert(Long productId, String productName, int currentStock, int threshold) {
        String title = "Low Stock Alert";
        String message = String.format("Product '%s' (ID: %d) is running low on stock. Current stock: %d (Threshold: %d)",
                productName, productId, currentStock, threshold);

        // Create in-app notification for inventory managers
        createNotificationForRole(title, message, "INVENTORY_ALERT", "INVENTORY_MANAGER");

        // Send email to inventory managers if email is enabled
        if (emailEnabled) {
            List<User> inventoryManagers = userRepository.findByRoleName("INVENTORY_MANAGER");
            for (User manager : inventoryManagers) {
                if (manager.getEmail() != null && !manager.getEmail().isEmpty()) {
                    emailUtils.sendSimpleEmail(manager.getEmail(), title, message);
                }
            }
        } else {
            log.info("Email notifications disabled. Would have sent low stock alert emails.");
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> sendTransactionNotification(String transactionNumber, String transactionType,
                                                               String status, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        String title = "Transaction " + status;
        String message = String.format("%s %s has been %s",
                transactionType, transactionNumber, status.toLowerCase());

        // Create in-app notification
        createNotification(title, message, "TRANSACTION", userId);

        // Send email if enabled
        if (emailEnabled && user.getEmail() != null && !user.getEmail().isEmpty()) {
            emailUtils.sendSimpleEmail(user.getEmail(), title, message);
        } else {
            log.info("Email notifications disabled. Would have sent transaction notification email to: {}",
                    user.getEmail() != null ? user.getEmail() : "unknown");
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> sendStockOutAlert(Long productId, String productName) {
        String title = "STOCK OUT ALERT";
        String message = String.format("Product '%s' (ID: %d) is out of stock!", productName, productId);

        // Create in-app notification for inventory managers and admins
        createNotificationForRole(title, message, "INVENTORY_ALERT", "INVENTORY_MANAGER");
        createNotificationForRole(title, message, "INVENTORY_ALERT", "ADMIN");

        // Send urgent email to inventory managers and admins if email is enabled
        if (emailEnabled) {
            List<User> inventoryManagers = userRepository.findByRoleName("INVENTORY_MANAGER");
            List<User> admins = userRepository.findByRoleName("ADMIN");

            List<String> emails = inventoryManagers.stream()
                    .map(User::getEmail)
                    .filter(email -> email != null && !email.isEmpty())
                    .collect(Collectors.toList());

            emails.addAll(admins.stream()
                    .map(User::getEmail)
                    .filter(email -> email != null && !email.isEmpty())
                    .collect(Collectors.toList()));

            for (String email : emails) {
                emailUtils.sendSimpleEmail(email, title, message);
            }
        } else {
            log.info("Email notifications disabled. Would have sent stock out alert emails.");
        }

        return CompletableFuture.completedFuture(null);
    }
}