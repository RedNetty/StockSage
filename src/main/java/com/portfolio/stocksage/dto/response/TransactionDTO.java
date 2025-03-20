package com.portfolio.stocksage.dto.response;

import com.portfolio.stocksage.entity.Transaction.TransactionStatus;
import com.portfolio.stocksage.entity.Transaction.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDTO {

    private Long id;
    private String transactionNumber;
    private LocalDateTime transactionDate;
    private TransactionType transactionType;
    private TransactionStatus status;
    private ProductSummaryDTO product;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private WarehouseSummaryDTO warehouse;
    private WarehouseSummaryDTO sourceWarehouse;
    private WarehouseSummaryDTO destinationWarehouse;
    private String referenceNumber;
    private String notes;
    private UserSummaryDTO createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummaryDTO {
    private Long id;
    private String username;
    private String fullName;
}