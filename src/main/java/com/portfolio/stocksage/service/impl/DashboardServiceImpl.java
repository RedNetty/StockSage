package com.portfolio.stocksage.service.impl;

import com.portfolio.stocksage.dto.response.DashboardDTO;
import com.portfolio.stocksage.entity.Product;
import com.portfolio.stocksage.entity.Transaction.TransactionType;
import com.portfolio.stocksage.repository.*;
import com.portfolio.stocksage.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final SupplierRepository supplierRepository;
    private final InventoryRepository inventoryRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardDTO getDashboardSummary() {
        // Get today's date range
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        // Get month's date range
        LocalDateTime startOfMonth = LocalDateTime.of(LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()), LocalTime.MIN);
        LocalDateTime endOfMonth = LocalDateTime.of(LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()), LocalTime.MAX);

        return getDashboardSummaryForDateRange(startOfMonth, endOfMonth);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardDTO getDashboardSummaryForDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        // Today's date range for daily metrics
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        // Build inventory summary
        DashboardDTO.InventorySummary inventorySummary = buildInventorySummary();

        // Build transaction summary
        DashboardDTO.TransactionSummary transactionSummary = buildTransactionSummary(startOfDay, endOfDay, startDate, endDate);

        // Get top selling products
        List<DashboardDTO.TopProductDTO> topSellingProducts = getTopSellingProducts();

        // Get low stock products
        List<DashboardDTO.TopProductDTO> lowStockProducts = getLowStockProducts();

        // Get warehouse summaries
        List<DashboardDTO.WarehouseSummaryDTO> warehouseSummaries = getWarehouseSummaries();

        // Get sales trend
        List<DashboardDTO.ChartDataDTO> salesTrend = getSalesTrend(startDate, endDate);

        // Get purchase trend
        List<DashboardDTO.ChartDataDTO> purchaseTrend = getPurchaseTrend(startDate, endDate);

        // Build the complete dashboard DTO
        return DashboardDTO.builder()
                .inventorySummary(inventorySummary)
                .transactionSummary(transactionSummary)
                .topSellingProducts(topSellingProducts)
                .lowStockProducts(lowStockProducts)
                .warehouseSummaries(warehouseSummaries)
                .salesTrend(salesTrend)
                .purchaseTrend(purchaseTrend)
                .build();
    }

    private DashboardDTO.InventorySummary buildInventorySummary() {
        long totalProducts = productRepository.count();
        long totalCategories = categoryRepository.count();
        long totalWarehouses = warehouseRepository.count();
        long totalSuppliers = supplierRepository.count();

        List<Product> lowStockProducts = productRepository.findLowStockProducts(10);
        List<Product> outOfStockProducts = productRepository.findAll().stream()
                .filter(p -> p.getUnitsInStock() == 0)
                .collect(Collectors.toList());

        // Calculate total inventory value (simplified version)
        BigDecimal totalValue = BigDecimal.ZERO;
        List<Product> allProducts = productRepository.findAll();
        for (Product product : allProducts) {
            if (product.getUnitsInStock() > 0) {
                totalValue = totalValue.add(product.getUnitPrice().multiply(new BigDecimal(product.getUnitsInStock())));
            }
        }

        return DashboardDTO.InventorySummary.builder()
                .totalProducts((int) totalProducts)
                .totalCategories((int) totalCategories)
                .totalWarehouses((int) totalWarehouses)
                .totalSuppliers((int) totalSuppliers)
                .lowStockItems(lowStockProducts.size())
                .outOfStockItems(outOfStockProducts.size())
                .totalInventoryValue(totalValue)
                .build();
    }

    private DashboardDTO.TransactionSummary buildTransactionSummary(
            LocalDateTime startOfDay, LocalDateTime endOfDay,
            LocalDateTime startOfPeriod, LocalDateTime endOfPeriod) {

        // Count transactions
        long totalTransactions = transactionRepository.count();
        long pendingTransactions = transactionRepository.findByStatus(
                Transaction.TransactionStatus.PENDING, null).getTotalElements();
        long completedTransactions = transactionRepository.findByStatus(
                Transaction.TransactionStatus.COMPLETED, null).getTotalElements();

        // Count today's transactions
        long todaySales = transactionRepository.countTransactionsSince(
                startOfDay, TransactionType.SALE);
        long todayPurchases = transactionRepository.countTransactionsSince(
                startOfDay, TransactionType.PURCHASE);

        // Calculate values (simplified version)
        BigDecimal todaySalesValue = calculateTransactionValue(
                startOfDay, endOfDay, TransactionType.SALE);
        BigDecimal todayPurchasesValue = calculateTransactionValue(
                startOfDay, endOfDay, TransactionType.PURCHASE);
        BigDecimal periodSalesValue = calculateTransactionValue(
                startOfPeriod, endOfPeriod, TransactionType.SALE);
        BigDecimal periodPurchasesValue = calculateTransactionValue(
                startOfPeriod, endOfPeriod, TransactionType.PURCHASE);

        return DashboardDTO.TransactionSummary.builder()
                .totalTransactions((int) totalTransactions)
                .pendingTransactions((int) pendingTransactions)
                .completedTransactions((int) completedTransactions)
                .todaySales((int) todaySales)
                .todayPurchases((int) todayPurchases)
                .todaySalesValue(todaySalesValue)
                .todayPurchasesValue(todayPurchasesValue)
                .monthlySalesValue(periodSalesValue)
                .monthlyPurchasesValue(periodPurchasesValue)
                .build();
    }

    private BigDecimal calculateTransactionValue(
            LocalDateTime startDate, LocalDateTime endDate, TransactionType type) {
        // This is a simplified implementation
        // In a real application, you would query the database more efficiently
        BigDecimal total = BigDecimal.ZERO;

        // Get all transactions in the date range and of the specified type
        List<Transaction> transactions = transactionRepository.findByDateRangeAndType(
                startDate, endDate, type);

        // Calculate the total value
        for (Transaction transaction : transactions) {
            if (transaction.getStatus() == Transaction.TransactionStatus.COMPLETED) {
                BigDecimal transactionValue = transaction.getUnitPrice()
                        .multiply(new BigDecimal(transaction.getQuantity()));
                total = total.add(transactionValue);
            }
        }

        return total;
    }

    private List<DashboardDTO.TopProductDTO> getTopSellingProducts() {
        // This is a simplified implementation
        // In a real application, you would use a more efficient query
        List<DashboardDTO.TopProductDTO> result = new ArrayList<>();

        // Get all products and sort by sales
        List<Product> allProducts = productRepository.findAll();

        for (Product product : allProducts) {
            // Get the total quantity sold for this product
            Integer quantitySold = transactionRepository.sumQuantityByProductAndType(
                    product.getId(), TransactionType.SALE);

            if (quantitySold != null && quantitySold > 0) {
                result.add(DashboardDTO.TopProductDTO.builder()
                        .id(product.getId())
                        .sku(product.getSku())
                        .name(product.getName())
                        .category(product.getCategory().getName())
                        .quantity(quantitySold)
                        .unitPrice(product.getUnitPrice())
                        .totalValue(product.getUnitPrice().multiply(new BigDecimal(quantitySold)))
                        .build());
            }
        }

        // Sort by quantity sold (descending) and limit to top 5
        result.sort((a, b) -> b.getQuantity() - a.getQuantity());
        if (result.size() > 5) {
            result = result.subList(0, 5);
        }

        return result;
    }

    private List<DashboardDTO.TopProductDTO> getLowStockProducts() {
        List<DashboardDTO.TopProductDTO> result = new ArrayList<>();

        // Get low stock products
        List<Product> lowStockProducts = productRepository.findLowStockProducts(10);

        for (Product product : lowStockProducts) {
            result.add(DashboardDTO.TopProductDTO.builder()
                    .id(product.getId())
                    .sku(product.getSku())
                    .name(product.getName())
                    .category(product.getCategory().getName())
                    .quantity(product.getUnitsInStock())
                    .unitPrice(product.getUnitPrice())
                    .totalValue(product.getUnitPrice().multiply(new BigDecimal(product.getUnitsInStock())))
                    .build());
        }

        // Sort by stock quantity (ascending)
        result.sort((a, b) -> a.getQuantity() - b.getQuantity());

        return result;
    }

    private List<DashboardDTO.WarehouseSummaryDTO> getWarehouseSummaries() {
        // This is a simplified implementation
        List<DashboardDTO.WarehouseSummaryDTO> result = new ArrayList<>();

        // In a real implementation, you would join tables and calculate metrics in a query

        return result;
    }

    private List<DashboardDTO.ChartDataDTO> getSalesTrend(LocalDateTime startDate, LocalDateTime endDate) {
        // This is a simplified implementation
        List<DashboardDTO.ChartDataDTO> result = new ArrayList<>();

        // In a real implementation, you would aggregate sales by day/week/month

        return result;
    }

    private List<DashboardDTO.ChartDataDTO> getPurchaseTrend(LocalDateTime startDate, LocalDateTime endDate) {
        // This is a simplified implementation
        List<DashboardDTO.ChartDataDTO> result = new ArrayList<>();

        // In a real implementation, you would aggregate purchases by day/week/month

        return result;
    }
}