package com.portfolio.stocksage.service;

import com.portfolio.stocksage.dto.request.TransactionCreateDTO;
import com.portfolio.stocksage.dto.response.TransactionDTO;
import com.portfolio.stocksage.entity.Transaction;
import com.portfolio.stocksage.entity.Transaction.TransactionStatus;
import com.portfolio.stocksage.entity.Transaction.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface TransactionService {

    TransactionDTO createTransaction(TransactionCreateDTO transactionCreateDTO, String username);

    TransactionDTO getTransactionById(Long id);

    TransactionDTO getTransactionByNumber(String transactionNumber);

    Page<TransactionDTO> getAllTransactions(Pageable pageable);

    Page<TransactionDTO> getTransactionsByStatus(TransactionStatus status, Pageable pageable);

    Page<TransactionDTO> getTransactionsByType(TransactionType type, Pageable pageable);

    Page<TransactionDTO> getTransactionsByProduct(Long productId, Pageable pageable);

    Page<TransactionDTO> getTransactionsByWarehouse(Long warehouseId, Pageable pageable);

    Page<TransactionDTO> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<TransactionDTO> getTransactionsByUser(Long userId, Pageable pageable);

    Page<TransactionDTO> searchTransactions(String keyword, Pageable pageable);

    TransactionDTO updateTransaction(Long id, TransactionCreateDTO transactionCreateDTO);

    TransactionDTO updateTransactionStatus(Long id, TransactionStatus status);

    void deleteTransaction(Long id);

    List<TransactionDTO> getRecentTransactions(int limit);

    BigDecimal getSalesTotal(LocalDateTime startDate, LocalDateTime endDate);

    BigDecimal getPurchasesTotal(LocalDateTime startDate, LocalDateTime endDate);

    Map<String, BigDecimal> getMonthlySales(int year);

    Map<String, BigDecimal> getMonthlyPurchases(int year);

    boolean isTransactionNumberUnique(String transactionNumber);

    String generateTransactionNumber(TransactionType type);

    /**
     * Get a list of pending transactions older than the specified number of days
     *
     * @param days Number of days
     * @return List of old pending transactions
     */
    List<Transaction> getOldPendingTransactions(int days);

    /**
     * Get the count of transactions for a specific date range and type
     *
     * @param startDate Start date
     * @param endDate End date
     * @param type Transaction type
     * @return Count of transactions
     */
    int getDailyTransactionCount(LocalDateTime startDate, LocalDateTime endDate, TransactionType type);
}