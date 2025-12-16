package com.portfolio.stocksage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDTO {

    private InventorySummary inventorySummary;
    private TransactionSummary transactionSummary;
    private List<TopProductDTO> topSellingProducts;
    private List<TopProductDTO> lowStockProducts;
    private List<WarehouseSummaryDTO> warehouseSummaries;
    private List<ChartDataDTO> salesTrend;
    private List<ChartDataDTO> purchaseTrend;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InventorySummary {
        private int totalProducts;
        private int totalCategories;
        private int totalWarehouses;
        private int totalSuppliers;
        private int lowStockItems;
        private int outOfStockItems;
        private BigDecimal totalInventoryValue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TransactionSummary {
        private int totalTransactions;
        private int pendingTransactions;
        private int completedTransactions;
        private int todaySales;
        private int todayPurchases;
        private BigDecimal todaySalesValue;
        private BigDecimal todayPurchasesValue;
        private BigDecimal monthlySalesValue;
        private BigDecimal monthlyPurchasesValue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopProductDTO {
        private Long id;
        private String sku;
        private String name;
        private String category;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalValue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChartDataDTO {
        private String label;
        private BigDecimal value;
    }
}