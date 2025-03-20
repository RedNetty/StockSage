package com.portfolio.stocksage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    private Long id;
    private String title;
    private String message;
    private String type;
    private UserSummaryDTO user;
    private boolean read;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    /**
     * Returns a short preview of the message (first 100 characters)
     */
    public String getMessagePreview() {
        if (message == null || message.length() <= 100) {
            return message;
        }
        return message.substring(0, 97) + "...";
    }

    /**
     * Returns true if the notification is recent (less than 24 hours old)
     */
    public boolean isRecent() {
        return createdAt != null &&
                createdAt.isAfter(LocalDateTime.now().minusHours(24));
    }

    /**
     * Returns CSS class based on notification type
     */
    public String getTypeClass() {
        if (type == null) {
            return "info";
        }

        switch (type.toUpperCase()) {
            case "ERROR":
            case "DANGER":
                return "danger";
            case "WARNING":
                return "warning";
            case "SUCCESS":
                return "success";
            case "INVENTORY_ALERT":
                return "warning";
            case "TRANSACTION":
                return "info";
            default:
                return "info";
        }
    }

    /**
     * Returns icon based on notification type
     */
    public String getTypeIcon() {
        if (type == null) {
            return "fas fa-bell";
        }

        switch (type.toUpperCase()) {
            case "ERROR":
            case "DANGER":
                return "fas fa-exclamation-circle";
            case "WARNING":
                return "fas fa-exclamation-triangle";
            case "SUCCESS":
                return "fas fa-check-circle";
            case "INVENTORY_ALERT":
                return "fas fa-boxes";
            case "TRANSACTION":
                return "fas fa-exchange-alt";
            default:
                return "fas fa-bell";
        }
    }
}