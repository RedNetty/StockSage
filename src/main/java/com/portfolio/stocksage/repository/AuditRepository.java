package com.portfolio.stocksage.repository;

import com.portfolio.stocksage.entity.Audit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Audit entity
 */
@Repository
public interface AuditRepository extends JpaRepository<Audit, Long> {

    /**
     * Find audit logs by user ID
     */
    Page<Audit> findByUserId(Long userId, Pageable pageable);

    /**
     * Find audit logs by username
     */
    Page<Audit> findByUsername(String username, Pageable pageable);

    /**
     * Find audit logs by action
     */
    Page<Audit> findByAction(String action, Pageable pageable);

    /**
     * Find audit logs by entity type
     */
    Page<Audit> findByEntityType(String entityType, Pageable pageable);

    /**
     * Find audit logs by entity type and entity ID
     */
    Page<Audit> findByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);

    /**
     * Find audit logs by date range
     */
    @Query("SELECT a FROM Audit a WHERE a.createdAt BETWEEN :startDate AND :endDate")
    Page<Audit> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Find audit logs by action and date range
     */
    @Query("SELECT a FROM Audit a WHERE a.action = :action AND a.createdAt BETWEEN :startDate AND :endDate")
    Page<Audit> findByActionAndDateRange(
            @Param("action") String action,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Find latest audit logs
     */
    List<Audit> findTop100ByOrderByCreatedAtDesc();

    /**
     * Find latest audit logs by user ID
     */
    List<Audit> findTop50ByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find latest audit logs by entity type and entity ID
     */
    List<Audit> findTop50ByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId);

    /**
     * Count audit logs by action
     */
    long countByAction(String action);

    /**
     * Count audit logs by user ID
     */
    long countByUserId(Long userId);

    /**
     * Count audit logs by date range
     */
    @Query("SELECT COUNT(a) FROM Audit a WHERE a.createdAt BETWEEN :startDate AND :endDate")
    long countByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Search audit logs
     */
    @Query("SELECT a FROM Audit a WHERE " +
            "a.username LIKE %:keyword% OR " +
            "a.action LIKE %:keyword% OR " +
            "a.entityType LIKE %:keyword% OR " +
            "a.details LIKE %:keyword%")
    Page<Audit> search(@Param("keyword") String keyword, Pageable pageable);
}