package com.portfolio.stocksage.dto.response;

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
public class InventoryDTO {

    private Long id;

    private ProductSummaryDTO product;

    private WarehouseSummaryDTO warehouse;

    private Integer quantity;

    private BigDecimal totalValue;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class ProductSummaryDTO {
    private Long id;
    private String sku;
    private String name;
    private BigDecimal unitPrice;
    private String category;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class WarehouseSummaryDTO {
    private Long id;
    private String name;
    private String location;
}