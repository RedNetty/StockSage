package com.portfolio.stocksage.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity to track audit logs for important system actions
 */
@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who performed the action
     */
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username")
    private String username;

    /**
     * Type of action (CREATE, UPDATE, DELETE, LOGIN, LOGOUT, etc.)
     */
    @Column(nullable = false)
    private String action;

    /**
     * Type of entity the action was performed on (Product, User, etc.)
     */
    @Column(name = "entity_type", nullable = false)
    private String entityType;

    /**
     * ID of the entity the action was performed on
     */
    @Column(name = "entity_id")
    private Long entityId;

    /**
     * Additional details about the action (can be JSON)
     */
    @Column(columnDefinition = "TEXT")
    private String details;

    /**
     * IP address of the user who performed the action
     */
    @Column(name = "ip_address")
    private String ipAddress;

    /**
     * User agent of the user who performed the action
     */
    @Column(name = "user_agent")
    private String userAgent;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}