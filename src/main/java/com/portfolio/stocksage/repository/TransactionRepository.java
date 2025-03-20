package com.portfolio.stocksage.repository;

import com.portfolio.stocksage.entity.Transaction;
import com.portfolio.stocksage.entity.Transaction.TransactionStatus;
import com.portfolio.stocksage.entity.Transaction.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionNumber(String transactionNumber);

    boolean existsByTransactionNumber(String transactionNumber);

    Page<Transaction> findByStatus(TransactionStatus status, Pageable pageable);

    Page<Transaction> findByTransactionType(TransactionType transactionType, Pageable pageable);

    Page<Transaction> findByProductId(Long productId, Pageable pageable);

    Page<Transaction> findByWarehouseId(Long warehouseId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.transactionDate BETWEEN :startDate AND :endDate")
    Page<Transaction> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.createdBy.id = :userId")
    Page<Transaction> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE " +
            "t.transactionNumber LIKE %:keyword% OR " +
            "t.product.name LIKE %:keyword% OR " +
            "t.product.sku LIKE %:keyword% OR " +
            "t.referenceNumber LIKE %:keyword%")
    Page<Transaction> search(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE " +
            "t.transactionDate BETWEEN :startDate AND :endDate AND " +
            "t.transactionType = :type")
    List<Transaction> findByDateRangeAndType(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("type") TransactionType type);

    @Query("SELECT SUM(t.quantity) FROM Transaction t WHERE " +
            "t.product.id = :productId AND " +
            "t.transactionType = :type AND " +
            "t.status = 'COMPLETED'")
    Integer sumQuantityByProductAndType(
            @Param("productId") Long productId,
            @Param("type") TransactionType type);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE " +
            "t.transactionDate >= :date AND " +
            "t.transactionType = :type")
    long countTransactionsSince(
            @Param("date") LocalDateTime date,
            @Param("type") TransactionType type);
}