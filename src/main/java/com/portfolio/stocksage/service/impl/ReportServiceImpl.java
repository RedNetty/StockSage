package com.portfolio.stocksage.service.impl;

import com.portfolio.stocksage.dto.response.ReportDTO;
import com.portfolio.stocksage.entity.Product;
import com.portfolio.stocksage.entity.Transaction;
import com.portfolio.stocksage.entity.Transaction.TransactionStatus;
import com.portfolio.stocksage.entity.Transaction.TransactionType;
import com.portfolio.stocksage.exception.ResourceNotFoundException;
import com.portfolio.stocksage.repository.CategoryRepository;
import com.portfolio.stocksage.repository.InventoryRepository;
import com.portfolio.stocksage.repository.ProductRepository;
import com.portfolio.stocksage.repository.SupplierRepository;
import com.portfolio.stocksage.repository.TransactionRepository;
import com.portfolio.stocksage.repository.WarehouseRepository;
import com.portfolio.stocksage.report.ReportType;
import com.portfolio.stocksage.service.ExportService;
import com.portfolio.stocksage.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final TransactionRepository transactionRepository;
    private final WarehouseRepository warehouseRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final ExportService exportService;
    private final TaskScheduler taskScheduler;

    // Store for generated reports
    private final Map<Long, ReportDTO> reportStore = new ConcurrentHashMap<>();
    // Store for scheduled reports
    private final Map<Long, ScheduledFuture<?>> scheduledReports = new ConcurrentHashMap<>();
    // Counter for report IDs
    private long nextReportId = 1;

    @PostConstruct
    public void init() {
        log.info("Initializing Report Service");
    }

    @Override
    @Transactional(readOnly = true)
    public ReportDTO generateInventoryReport(Long warehouseId, Long categoryId, Boolean lowStockOnly) {
        // Create the report
        ReportDTO report = new ReportDTO();
        report.setId(getNextReportId());
        report.setTitle("Inventory Report");
        report.setReportType(ReportType.INVENTORY);
        report.setGeneratedAt(LocalDateTime.now());

        // Set parameters
        report.getParameters().put("warehouseId", warehouseId);
        report.getParameters().put("categoryId", categoryId);
        report.getParameters().put("lowStockOnly", lowStockOnly != null && lowStockOnly);

        // Set columns
        List<String> columns = Arrays.asList(
                "Product ID", "SKU", "Name", "Category", "Warehouse",
                "Quantity", "Unit Price", "Total Value", "Status"
        );
        report.setColumns(columns);

        // Build description
        StringBuilder description = new StringBuilder("Inventory Report");
        if (warehouseId != null) {
            String warehouseName = warehouseRepository.findById(warehouseId)
                    .map(w -> w.getName())
                    .orElse("Unknown");
            description.append(" for Warehouse: ").append(warehouseName);
        }
        if (categoryId != null) {
            String categoryName = categoryRepository.findById(categoryId)
                    .map(c -> c.getName())
                    .orElse("Unknown");
            description.append(" in Category: ").append(categoryName);
        }
        if (lowStockOnly != null && lowStockOnly) {
            description.append(" (Low Stock Items Only)");
        }
        report.setDescription(description.toString());

        // Fetch data based on parameters
        List<Map<String, Object>> reportData = new ArrayList<>();

        // Get inventory data
        inventoryRepository.findAll().forEach(inventory -> {
            // Apply filters
            if (warehouseId != null && !inventory.getWarehouse().getId().equals(warehouseId)) {
                return;
            }

            if (categoryId != null && !inventory.getProduct().getCategory().getId().equals(categoryId)) {
                return;
            }

            if (lowStockOnly != null && lowStockOnly && inventory.getQuantity() > 10) { // Assuming 10 is low stock threshold
                return;
            }

            Map<String, Object> row = new HashMap<>();
            Product product = inventory.getProduct();

            row.put("Product ID", product.getId());
            row.put("SKU", product.getSku());
            row.put("Name", product.getName());
            row.put("Category", product.getCategory().getName());
            row.put("Warehouse", inventory.getWarehouse().getName());
            row.put("Quantity", inventory.getQuantity());
            row.put("Unit Price", product.getUnitPrice());

            // Calculate total value
            BigDecimal totalValue = product.getUnitPrice().multiply(new BigDecimal(inventory.getQuantity()));
            row.put("Total Value", totalValue);

            // Determine status
            String status = "Normal";
            if (inventory.getQuantity() <= 0) {
                status = "Out of Stock";
            } else if (inventory.getQuantity() <= 10) { // Assuming 10 is low stock threshold
                status = "Low Stock";
            }
            row.put("Status", status);

            reportData.add(row);
        });

        report.setData(reportData);

        // Calculate summary statistics
        Map<String, Object> summary = new HashMap<>();
        int totalProducts = reportData.size();
        int outOfStock = (int) reportData.stream()
                .filter(row -> "Out of Stock".equals(row.get("Status")))
                .count();
        int lowStock = (int) reportData.stream()
                .filter(row -> "Low Stock".equals(row.get("Status")))
                .count();
        BigDecimal totalValue = reportData.stream()
                .map(row -> (BigDecimal) row.get("Total Value"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        summary.put("Total Products", totalProducts);
        summary.put("Out of Stock", outOfStock);
        summary.put("Low Stock", lowStock);
        summary.put("Total Value", totalValue);
        report.setSummary(summary);

        // Create chart data
        ReportDTO.ChartData statusChart = new ReportDTO.ChartData();
        statusChart.setChartType("pie");
        statusChart.setTitle("Inventory Status");

        List<String> labels = Arrays.asList("Normal", "Low Stock", "Out of Stock");
        statusChart.setLabels(labels);

        ReportDTO.DataSeries statusSeries = new ReportDTO.DataSeries();
        statusSeries.setName("Products");
        statusSeries.setData(Arrays.asList(
                totalProducts - lowStock - outOfStock,
                lowStock,
                outOfStock
        ));
        statusChart.getSeries().add(statusSeries);

        report.getCharts().add(statusChart);

        // Store the report
        reportStore.put(report.getId(), report);

        return report;
    }

    @Override
    @Transactional(readOnly = true)
    public ReportDTO generateSalesReport(LocalDateTime startDate, LocalDateTime endDate,
                                         String groupBy, Long productId, Long warehouseId) {
        // Create the report
        ReportDTO report = new ReportDTO();
        report.setId(getNextReportId());
        report.setTitle("Sales Report");
        report.setReportType(ReportType.SALES);
        report.setGeneratedAt(LocalDateTime.now());
        report.setStartDate(startDate);
        report.setEndDate(endDate);

        // Set parameters
        report.getParameters().put("startDate", startDate);
        report.getParameters().put("endDate", endDate);
        report.getParameters().put("groupBy", groupBy);
        report.getParameters().put("productId", productId);
        report.getParameters().put("warehouseId", warehouseId);

        // Set columns
        List<String> columns = Arrays.asList(
                "Date", "Product", "Quantity", "Unit Price", "Total Amount"
        );
        report.setColumns(columns);

        // Build description
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        StringBuilder description = new StringBuilder("Sales Report from ");
        description.append(startDate.format(formatter))
                .append(" to ")
                .append(endDate.format(formatter));

        if (productId != null) {
            String productName = productRepository.findById(productId)
                    .map(Product::getName)
                    .orElse("Unknown");
            description.append(" for Product: ").append(productName);
        }

        if (warehouseId != null) {
            String warehouseName = warehouseRepository.findById(warehouseId)
                    .map(w -> w.getName())
                    .orElse("Unknown");
            description.append(" at Warehouse: ").append(warehouseName);
        }

        report.setDescription(description.toString());

        // Fetch sales data
        List<Transaction> transactions = transactionRepository.findByDateRangeAndType(
                startDate, endDate, TransactionType.SALE);

        // Apply filters
        if (productId != null) {
            transactions = transactions.stream()
                    .filter(t -> t.getProduct().getId().equals(productId))
                    .collect(Collectors.toList());
        }

        if (warehouseId != null) {
            transactions = transactions.stream()
                    .filter(t -> t.getWarehouse().getId().equals(warehouseId))
                    .collect(Collectors.toList());
        }

        // Keep only completed transactions
        transactions = transactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.COMPLETED)
                .collect(Collectors.toList());

        // Prepare data
        List<Map<String, Object>> reportData = new ArrayList<>();
        Map<String, Object> aggregatedData = new HashMap<>();

        // Group by timeframe if specified
        if ("day".equalsIgnoreCase(groupBy)) {
            aggregatedData = groupTransactionsByDay(transactions);
        } else if ("week".equalsIgnoreCase(groupBy)) {
            aggregatedData = groupTransactionsByWeek(transactions);
        } else if ("month".equalsIgnoreCase(groupBy)) {
            aggregatedData = groupTransactionsByMonth(transactions);
        } else {
            // No grouping, show individual transactions
            for (Transaction transaction : transactions) {
                Map<String, Object> row = new HashMap<>();

                row.put("Date", transaction.getTransactionDate());
                row.put("Product", transaction.getProduct().getName());
                row.put("Quantity", transaction.getQuantity());
                row.put("Unit Price", transaction.getUnitPrice());

                // Calculate total amount
                BigDecimal totalAmount = transaction.getUnitPrice()
                        .multiply(new BigDecimal(transaction.getQuantity()));
                row.put("Total Amount", totalAmount);

                reportData.add(row);
            }
        }

        // If grouped, transform aggregated data to report rows
        if (!aggregatedData.isEmpty()) {
            for (Map.Entry<String, Object> entry : aggregatedData.entrySet()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> groupData = (Map<String, Object>) entry.getValue();

                Map<String, Object> row = new HashMap<>();
                row.put("Date", entry.getKey());
                row.put("Product", groupData.getOrDefault("productName", "Various"));
                row.put("Quantity", groupData.get("totalQuantity"));

                // For grouped data, we calculate average unit price
                row.put("Unit Price", groupData.getOrDefault("averageUnitPrice", BigDecimal.ZERO));
                row.put("Total Amount", groupData.get("totalAmount"));

                reportData.add(row);
            }
        }

        report.setData(reportData);

        // Calculate summary statistics
        Map<String, Object> summary = new HashMap<>();

        int totalTransactions = transactions.size();
        int totalQuantity = transactions.stream()
                .mapToInt(Transaction::getQuantity)
                .sum();

        BigDecimal totalAmount = transactions.stream()
                .map(t -> t.getUnitPrice().multiply(new BigDecimal(t.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageTransactionValue = totalTransactions > 0
                ? totalAmount.divide(new BigDecimal(totalTransactions), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        summary.put("Total Transactions", totalTransactions);
        summary.put("Total Quantity", totalQuantity);
        summary.put("Total Amount", totalAmount);
        summary.put("Average Transaction Value", averageTransactionValue);

        report.setSummary(summary);

        // Create chart data for sales trend
        if ("day".equalsIgnoreCase(groupBy) || "week".equalsIgnoreCase(groupBy) || "month".equalsIgnoreCase(groupBy)) {
            ReportDTO.ChartData trendChart = new ReportDTO.ChartData();
            trendChart.setChartType("line");
            trendChart.setTitle("Sales Trend");
            trendChart.setXAxisLabel("Time Period");
            trendChart.setYAxisLabel("Sales Amount");

            // Sort the aggregated data by date
            List<String> sortedDates = new ArrayList<>(aggregatedData.keySet());
            sortedDates.sort(Comparator.naturalOrder());

            trendChart.setLabels(sortedDates);

            ReportDTO.DataSeries amountSeries = new ReportDTO.DataSeries();
            amountSeries.setName("Sales Amount");
            amountSeries.setColor("#4CAF50"); // Green color for sales

            for (String date : sortedDates) {
                @SuppressWarnings("unchecked")
                Map<String, Object> groupData = (Map<String, Object>) aggregatedData.get(date);
                amountSeries.getData().add(groupData.get("totalAmount"));
            }

            trendChart.getSeries().add(amountSeries);
            report.getCharts().add(trendChart);
        }

        // Store the report
        reportStore.put(report.getId(), report);

        return report;
    }

    @Override
    @Transactional(readOnly = true)
    public ReportDTO generatePurchaseReport(LocalDateTime startDate, LocalDateTime endDate,
                                            String groupBy, Long productId, Long supplierId) {
        // Create the report
        ReportDTO report = new ReportDTO();
        report.setId(getNextReportId());
        report.setTitle("Purchase Report");
        report.setReportType(ReportType.PURCHASE);
        report.setGeneratedAt(LocalDateTime.now());
        report.setStartDate(startDate);
        report.setEndDate(endDate);

        // Set parameters
        report.getParameters().put("startDate", startDate);
        report.getParameters().put("endDate", endDate);
        report.getParameters().put("groupBy", groupBy);
        report.getParameters().put("productId", productId);
        report.getParameters().put("supplierId", supplierId);

        // Set columns
        List<String> columns = Arrays.asList(
                "Date", "Product", "Supplier", "Quantity", "Unit Price", "Total Amount"
        );
        report.setColumns(columns);

        // Build description
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        StringBuilder description = new StringBuilder("Purchase Report from ");
        description.append(startDate.format(formatter))
                .append(" to ")
                .append(endDate.format(formatter));

        if (productId != null) {
            String productName = productRepository.findById(productId)
                    .map(Product::getName)
                    .orElse("Unknown");
            description.append(" for Product: ").append(productName);
        }

        if (supplierId != null) {
            String supplierName = supplierRepository.findById(supplierId)
                    .map(s -> s.getName())
                    .orElse("Unknown");
            description.append(" from Supplier: ").append(supplierName);
        }

        report.setDescription(description.toString());

        // Fetch purchase data
        List<Transaction> transactions = transactionRepository.findByDateRangeAndType(
                startDate, endDate, TransactionType.PURCHASE);

        // Apply filters
        if (productId != null) {
            transactions = transactions.stream()
                    .filter(t -> t.getProduct().getId().equals(productId))
                    .collect(Collectors.toList());
        }

        // Filter by supplier - need to join with product.suppliers
        if (supplierId != null) {
            transactions = transactions.stream()
                    .filter(t -> t.getProduct().getSuppliers().stream()
                            .anyMatch(s -> s.getId().equals(supplierId)))
                    .collect(Collectors.toList());
        }

        // Keep only completed transactions
        transactions = transactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.COMPLETED)
                .collect(Collectors.toList());

        // Prepare data
        List<Map<String, Object>> reportData = new ArrayList<>();
        Map<String, Object> aggregatedData = new HashMap<>();

        // Group by timeframe if specified
        if ("day".equalsIgnoreCase(groupBy)) {
            aggregatedData = groupTransactionsByDay(transactions);
        } else if ("week".equalsIgnoreCase(groupBy)) {
            aggregatedData = groupTransactionsByWeek(transactions);
        } else if ("month".equalsIgnoreCase(groupBy)) {
            aggregatedData = groupTransactionsByMonth(transactions);
        } else {
            // No grouping, show individual transactions
            for (Transaction transaction : transactions) {
                Map<String, Object> row = new HashMap<>();

                row.put("Date", transaction.getTransactionDate());
                row.put("Product", transaction.getProduct().getName());

                // Get supplier name - we'll use the first supplier for simplicity
                String supplierName = transaction.getProduct().getSuppliers().stream()
                        .findFirst()
                        .map(s -> s.getName())
                        .orElse("Unknown");

                row.put("Supplier", supplierName);
                row.put("Quantity", transaction.getQuantity());
                row.put("Unit Price", transaction.getUnitPrice());

                // Calculate total amount
                BigDecimal totalAmount = transaction.getUnitPrice()
                        .multiply(new BigDecimal(transaction.getQuantity()));
                row.put("Total Amount", totalAmount);

                reportData.add(row);
            }
        }

        // If grouped, transform aggregated data to report rows
        if (!aggregatedData.isEmpty()) {
            for (Map.Entry<String, Object> entry : aggregatedData.entrySet()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> groupData = (Map<String, Object>) entry.getValue();

                Map<String, Object> row = new HashMap<>();
                row.put("Date", entry.getKey());
                row.put("Product", groupData.getOrDefault("productName", "Various"));
                row.put("Supplier", groupData.getOrDefault("supplierName", "Various"));
                row.put("Quantity", groupData.get("totalQuantity"));

                // For grouped data, we calculate average unit price
                row.put("Unit Price", groupData.getOrDefault("averageUnitPrice", BigDecimal.ZERO));
                row.put("Total Amount", groupData.get("totalAmount"));

                reportData.add(row);
            }
        }

        report.setData(reportData);

        // Calculate summary statistics
        Map<String, Object> summary = new HashMap<>();

        int totalTransactions = transactions.size();
        int totalQuantity = transactions.stream()
                .mapToInt(Transaction::getQuantity)
                .sum();

        BigDecimal totalAmount = transactions.stream()
                .map(t -> t.getUnitPrice().multiply(new BigDecimal(t.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageTransactionValue = totalTransactions > 0
                ? totalAmount.divide(new BigDecimal(totalTransactions), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        summary.put("Total Transactions", totalTransactions);
        summary.put("Total Quantity", totalQuantity);
        summary.put("Total Amount", totalAmount);
        summary.put("Average Transaction Value", averageTransactionValue);

        report.setSummary(summary);

        // Create chart data for purchase trend
        if ("day".equalsIgnoreCase(groupBy) || "week".equalsIgnoreCase(groupBy) || "month".equalsIgnoreCase(groupBy)) {
            ReportDTO.ChartData trendChart = new ReportDTO.ChartData();
            trendChart.setChartType("line");
            trendChart.setTitle("Purchase Trend");
            trendChart.setXAxisLabel("Time Period");
            trendChart.setYAxisLabel("Purchase Amount");

            // Sort the aggregated data by date
            List<String> sortedDates = new ArrayList<>(aggregatedData.keySet());
            sortedDates.sort(Comparator.naturalOrder());

            trendChart.setLabels(sortedDates);

            ReportDTO.DataSeries amountSeries = new ReportDTO.DataSeries();
            amountSeries.setName("Purchase Amount");
            amountSeries.setColor("#2196F3"); // Blue color for purchases

            for (String date : sortedDates) {
                @SuppressWarnings("unchecked")
                Map<String, Object> groupData = (Map<String, Object>) aggregatedData.get(date);
                amountSeries.getData().add(groupData.get("totalAmount"));
            }

            trendChart.getSeries().add(amountSeries);
            report.getCharts().add(trendChart);
        }

        // Store the report
        reportStore.put(report.getId(), report);

        return report;
    }

    @Override
    @Transactional(readOnly = true)
    public ReportDTO generateProductPerformanceReport(LocalDateTime startDate, LocalDateTime endDate,
                                                      Long categoryId, Integer topN) {
        // Create the report
        ReportDTO report = new ReportDTO();
        report.setId(getNextReportId());
        report.setTitle("Product Performance Report");
        report.setReportType(ReportType.PRODUCT_PERFORMANCE);
        report.setGeneratedAt(LocalDateTime.now());
        report.setStartDate(startDate);
        report.setEndDate(endDate);

        // Set parameters
        report.getParameters().put("startDate", startDate);
        report.getParameters().put("endDate", endDate);
        report.getParameters().put("categoryId", categoryId);
        report.getParameters().put("topN", topN != null ? topN : 10);

        // Set columns
        List<String> columns = Arrays.asList(
                "Product ID", "SKU", "Name", "Category",
                "Units Sold", "Sales Value", "Profit Margin", "Performance Score"
        );
        report.setColumns(columns);

        // Build description
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        StringBuilder description = new StringBuilder("Product Performance Report from ");
        description.append(startDate.format(formatter))
                .append(" to ")
                .append(endDate.format(formatter));

        if (categoryId != null) {
            String categoryName = categoryRepository.findById(categoryId)
                    .map(c -> c.getName())
                    .orElse("Unknown");
            description.append(" for Category: ").append(categoryName);
        }

        report.setDescription(description.toString());

        // Fetch completed sales transactions in the date range
        List<Transaction> salesTransactions = transactionRepository.findByDateRangeAndType(
                startDate, endDate, TransactionType.SALE);

        salesTransactions = salesTransactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.COMPLETED)
                .collect(Collectors.toList());

        // Group transactions by product
        Map<Long, List<Transaction>> transactionsByProduct = salesTransactions.stream()
                .collect(Collectors.groupingBy(t -> t.getProduct().getId()));

        // Calculate performance metrics for each product
        List<Map<String, Object>> productPerformance = new ArrayList<>();

        for (Map.Entry<Long, List<Transaction>> entry : transactionsByProduct.entrySet()) {
            Long productId = entry.getKey();
            List<Transaction> productTransactions = entry.getValue();

            // Get product details
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

            // Skip products not in the specified category
            if (categoryId != null && !product.getCategory().getId().equals(categoryId)) {
                continue;
            }

            Map<String, Object> performanceData = new HashMap<>();
            performanceData.put("Product ID", productId);
            performanceData.put("SKU", product.getSku());
            performanceData.put("Name", product.getName());
            performanceData.put("Category", product.getCategory().getName());

            // Calculate units sold
            int unitsSold = productTransactions.stream()
                    .mapToInt(Transaction::getQuantity)
                    .sum();
            performanceData.put("Units Sold", unitsSold);

            // Calculate sales value
            BigDecimal salesValue = productTransactions.stream()
                    .map(t -> t.getUnitPrice().multiply(new BigDecimal(t.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            performanceData.put("Sales Value", salesValue);

            // Calculate average profit margin (simplified)
            // Assuming cost is 70% of sale price for this example
            BigDecimal costFactor = new BigDecimal("0.7");
            BigDecimal totalCost = salesValue.multiply(costFactor);
            BigDecimal profit = salesValue.subtract(totalCost);
            BigDecimal profitMargin = salesValue.compareTo(BigDecimal.ZERO) > 0
                    ? profit.divide(salesValue, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100))
                    : BigDecimal.ZERO;

            performanceData.put("Profit Margin", profitMargin);

            // Calculate performance score (simplified)
            // Score is based on units sold and profit margin
            double unitsScore = Math.min(unitsSold / 10.0, 5.0); // Max 5 points for units
            double marginScore = profitMargin.doubleValue() / 10.0; // 10% margin = 1 point
            double performanceScore = unitsScore + marginScore;

            performanceData.put("Performance Score", performanceScore);

            productPerformance.add(performanceData);
        }

        // Sort by performance score (descending)
        productPerformance.sort((a, b) ->
                Double.compare((Double)b.get("Performance Score"), (Double)a.get("Performance Score")));

        // Limit to top N if specified
        if (topN != null && topN > 0 && productPerformance.size() > topN) {
            productPerformance = productPerformance.subList(0, topN);
        }

        report.setData(productPerformance);

        // Calculate summary statistics
        Map<String, Object> summary = new HashMap<>();

        int totalProducts = productPerformance.size();
        BigDecimal totalSalesValue = productPerformance.stream()
                .map(p -> (BigDecimal) p.get("Sales Value"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgPerformanceScore = productPerformance.stream()
                .mapToDouble(p -> (Double) p.get("Performance Score"))
                .average()
                .orElse(0.0);

        summary.put("Total Products", totalProducts);
        summary.put("Total Sales Value", totalSalesValue);
        summary.put("Average Performance Score", avgPerformanceScore);

        report.setSummary(summary);

        // Create chart data for top products
        if (!productPerformance.isEmpty()) {
            // Bar chart for sales value
            ReportDTO.ChartData salesChart = new ReportDTO.ChartData();
            salesChart.setChartType("bar");
            salesChart.setTitle("Top Products by Sales Value");
            salesChart.setXAxisLabel("Product");
            salesChart.setYAxisLabel("Sales Value");

            List<String> productNames = productPerformance.stream()
                    .map(p -> (String) p.get("Name"))
                    .collect(Collectors.toList());
            salesChart.setLabels(productNames);

            ReportDTO.DataSeries salesSeries = new ReportDTO.DataSeries();
            salesSeries.setName("Sales Value");
            salesSeries.setColor("#4CAF50"); // Green

            List<Object> salesValues = productPerformance.stream()
                    .map(p -> p.get("Sales Value"))
                    .collect(Collectors.toList());
            salesSeries.setData(salesValues);

            salesChart.getSeries().add(salesSeries);
            report.getCharts().add(salesChart);

            // Bar chart for performance score
            ReportDTO.ChartData scoreChart = new ReportDTO.ChartData();
            scoreChart.setChartType("bar");
            scoreChart.setTitle("Top Products by Performance Score");
            scoreChart.setXAxisLabel("Product");
            scoreChart.setYAxisLabel("Performance Score");

            scoreChart.setLabels(productNames);

            ReportDTO.DataSeries scoreSeries = new ReportDTO.DataSeries();
            scoreSeries.setName("Performance Score");
            scoreSeries.setColor("#2196F3"); // Blue

            List<Object> scoreValues = productPerformance.stream()
                    .map(p -> p.get("Performance Score"))
                    .collect(Collectors.toList());
            scoreSeries.setData(scoreValues);

            scoreChart.getSeries().add(scoreSeries);
            report.getCharts().add(scoreChart);
        }

        // Store the report
        reportStore.put(report.getId(), report);

        return report;
    }

    @Override
    @Transactional(readOnly = true)
    public ReportDTO generateSupplierPerformanceReport(LocalDateTime startDate, LocalDateTime endDate,
                                                       Long supplierId) {
        // For this example, we'll implement a simplified version of the supplier performance report
        // In a real application, you would have more detailed metrics and calculations

        // Create the report
        ReportDTO report = new ReportDTO();
        report.setId(getNextReportId());
        report.setTitle("Supplier Performance Report");
        report.setReportType(ReportType.SUPPLIER_PERFORMANCE);
        report.setGeneratedAt(LocalDateTime.now());
        report.setStartDate(startDate);
        report.setEndDate(endDate);

        // Set parameters
        report.getParameters().put("startDate", startDate);
        report.getParameters().put("endDate", endDate);
        report.getParameters().put("supplierId", supplierId);

        // Build description
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        StringBuilder description = new StringBuilder("Supplier Performance Report from ");
        description.append(startDate.format(formatter))
                .append(" to ")
                .append(endDate.format(formatter));

        if (supplierId != null) {
            String supplierName = supplierRepository.findById(supplierId)
                    .map(s -> s.getName())
                    .orElse("Unknown");
            description.append(" for Supplier: ").append(supplierName);
        }

        report.setDescription(description.toString());

        // In a real implementation, this would involve complex joins and calculations
        // For this example, we'll return a placeholder report
        report.setColumns(Arrays.asList(
                "Supplier ID", "Supplier Name", "Products Supplied", "On-Time Delivery",
                "Quality Rating", "Price Competitiveness", "Overall Score"
        ));

        List<Map<String, Object>> performanceData = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();

        row.put("Supplier ID", supplierId);
        row.put("Supplier Name", "Example Supplier");
        row.put("Products Supplied", 25);
        row.put("On-Time Delivery", 92);
        row.put("Quality Rating", 4.3);
        row.put("Price Competitiveness", 3.8);
        row.put("Overall Score", 4.0);

        performanceData.add(row);
        report.setData(performanceData);

        // Add a summary
        Map<String, Object> summary = new HashMap<>();
        summary.put("Total Suppliers", 1);
        summary.put("Average Score", 4.0);
        report.setSummary(summary);

        // Store the report
        reportStore.put(report.getId(), report);

        return report;
    }

    @Override
    public Long scheduleReport(ReportType reportType, Map<String, Object> parameters,
                               String schedule, String[] emailRecipients) {
        // Generate a unique ID for the scheduled report
        long scheduleId = getNextReportId();

        // Create a runnable task that will generate the report and send it to recipients
        Runnable reportTask = () -> {
            try {
                ReportDTO report = null;

                // Determine which report to generate based on type
                switch (reportType) {
                    case INVENTORY:
                        Long warehouseId = (Long) parameters.get("warehouseId");
                        Long categoryId = (Long) parameters.get("categoryId");
                        Boolean lowStockOnly = (Boolean) parameters.get("lowStockOnly");

                        report = generateInventoryReport(warehouseId, categoryId, lowStockOnly);
                        break;

                    case SALES:
                        LocalDateTime salesStartDate = (LocalDateTime) parameters.get("startDate");
                        LocalDateTime salesEndDate = (LocalDateTime) parameters.get("endDate");
                        String salesGroupBy = (String) parameters.get("groupBy");
                        Long salesProductId = (Long) parameters.get("productId");
                        Long salesWarehouseId = (Long) parameters.get("warehouseId");

                        report = generateSalesReport(salesStartDate, salesEndDate, salesGroupBy,
                                salesProductId, salesWarehouseId);
                        break;

                    // Add additional cases for other report types

                    default:
                        log.warn("Unsupported report type for scheduling: {}", reportType);
                        return;
                }

                if (report != null) {
                    // TODO: Send the report to email recipients
                    log.info("Generated scheduled report: {} (ID: {})", report.getTitle(), report.getId());
                }

            } catch (Exception e) {
                log.error("Error generating scheduled report: {}", e.getMessage(), e);
            }
        };

        // Schedule the task with the provided cron expression
        ScheduledFuture<?> future = taskScheduler.schedule(reportTask, new CronTrigger(schedule));

        // Store the scheduled task
        scheduledReports.put(scheduleId, future);

        return scheduleId;
    }

    @Override
    public void cancelScheduledReport(Long scheduleId) {
        ScheduledFuture<?> future = scheduledReports.get(scheduleId);

        if (future != null) {
            future.cancel(false);
            scheduledReports.remove(scheduleId);
            log.info("Cancelled scheduled report with ID: {}", scheduleId);
        } else {
            throw new ResourceNotFoundException("Scheduled report not found with ID: " + scheduleId);
        }
    }

    @Override
    public Map<Long, Map<String, Object>> getScheduledReports() {
        Map<Long, Map<String, Object>> result = new HashMap<>();

        for (Map.Entry<Long, ScheduledFuture<?>> entry : scheduledReports.entrySet()) {
            Long id = entry.getKey();
            ScheduledFuture<?> future = entry.getValue();

            Map<String, Object> details = new HashMap<>();
            details.put("id", id);
            details.put("active", !future.isCancelled() && !future.isDone());

            result.put(id, details);
        }

        return result;
    }

    @Override
    public byte[] exportReport(Long reportId, String format) {
        ReportDTO report = reportStore.get(reportId);

        if (report == null) {
            throw new ResourceNotFoundException("Report not found with ID: " + reportId);
        }

        try {
            // Convert report data to a list of appropriate DTOs based on report type
            switch (format.toLowerCase()) {
                case "excel":
                    // This is a simplified implementation
                    // In a real application, you would convert the report data to appropriate DTOs
                    return new byte[0]; // Placeholder

                case "pdf":
                    // This is a simplified implementation
                    return new byte[0]; // Placeholder

                case "csv":
                    // This is a simplified implementation
                    return new byte[0]; // Placeholder

                default:
                    throw new IllegalArgumentException("Unsupported export format: " + format);
            }
        } catch (Exception e) {
            log.error("Error exporting report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to export report", e);
        }
    }

    /**
     * Helper method to group transactions by day
     */
    private Map<String, Object> groupTransactionsByDay(List<Transaction> transactions) {
        Map<String, Object> result = new HashMap<>();

        // Group transactions by day
        Map<String, List<Transaction>> groupedByDay = transactions.stream()
                .collect(Collectors.groupingBy(t ->
                        t.getTransactionDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));

        // Calculate aggregates for each day
        for (Map.Entry<String, List<Transaction>> entry : groupedByDay.entrySet()) {
            String day = entry.getKey();
            List<Transaction> dayTransactions = entry.getValue();

            Map<String, Object> dayData = new HashMap<>();

            // Total quantity for the day
            int totalQuantity = dayTransactions.stream()
                    .mapToInt(Transaction::getQuantity)
                    .sum();
            dayData.put("totalQuantity", totalQuantity);

            // Total amount for the day
            BigDecimal totalAmount = dayTransactions.stream()
                    .map(t -> t.getUnitPrice().multiply(new BigDecimal(t.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            dayData.put("totalAmount", totalAmount);

            // Average unit price for the day
            if (!dayTransactions.isEmpty()) {
                BigDecimal totalUnitPrice = dayTransactions.stream()
                        .map(Transaction::getUnitPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal averageUnitPrice = totalUnitPrice.divide(
                        new BigDecimal(dayTransactions.size()), 2, RoundingMode.HALF_UP);

                dayData.put("averageUnitPrice", averageUnitPrice);
            }

            // If all transactions are for the same product, include product name
            boolean sameProduct = dayTransactions.stream()
                    .map(t -> t.getProduct().getId())
                    .distinct()
                    .count() == 1;

            if (sameProduct) {
                dayData.put("productName", dayTransactions.get(0).getProduct().getName());
            } else {
                dayData.put("productName", "Various");
            }

            result.put(day, dayData);
        }

        return result;
    }

    /**
     * Helper method to group transactions by week
     */
    private Map<String, Object> groupTransactionsByWeek(List<Transaction> transactions) {
        Map<String, Object> result = new HashMap<>();

        // Group transactions by week
        Map<String, List<Transaction>> groupedByWeek = transactions.stream()
                .collect(Collectors.groupingBy(t -> {
                    LocalDateTime date = t.getTransactionDate();
                    return date.getYear() + "-W" + date.get(ChronoUnit.WEEKS.ordinal());
                }));

        // Calculate aggregates for each week
        for (Map.Entry<String, List<Transaction>> entry : groupedByWeek.entrySet()) {
            String week = entry.getKey();
            List<Transaction> weekTransactions = entry.getValue();

            Map<String, Object> weekData = new HashMap<>();

            // Total quantity for the week
            int totalQuantity = weekTransactions.stream()
                    .mapToInt(Transaction::getQuantity)
                    .sum();
            weekData.put("totalQuantity", totalQuantity);

            // Total amount for the week
            BigDecimal totalAmount = weekTransactions.stream()
                    .map(t -> t.getUnitPrice().multiply(new BigDecimal(t.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            weekData.put("totalAmount", totalAmount);

            // Average unit price for the week
            if (!weekTransactions.isEmpty()) {
                BigDecimal totalUnitPrice = weekTransactions.stream()
                        .map(Transaction::getUnitPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal averageUnitPrice = totalUnitPrice.divide(
                        new BigDecimal(weekTransactions.size()), 2, RoundingMode.HALF_UP);

                weekData.put("averageUnitPrice", averageUnitPrice);
            }

            weekData.put("productName", "Various");

            result.put(week, weekData);
        }

        return result;
    }

    /**
     * Helper method to group transactions by month
     */
    private Map<String, Object> groupTransactionsByMonth(List<Transaction> transactions) {
        Map<String, Object> result = new HashMap<>();

        // Group transactions by month
        Map<String, List<Transaction>> groupedByMonth = transactions.stream()
                .collect(Collectors.groupingBy(t ->
                        t.getTransactionDate().format(DateTimeFormatter.ofPattern("yyyy-MM"))));

        // Calculate aggregates for each month
        for (Map.Entry<String, List<Transaction>> entry : groupedByMonth.entrySet()) {
            String month = entry.getKey();
            List<Transaction> monthTransactions = entry.getValue();

            Map<String, Object> monthData = new HashMap<>();

            // Total quantity for the month
            int totalQuantity = monthTransactions.stream()
                    .mapToInt(Transaction::getQuantity)
                    .sum();
            monthData.put("totalQuantity", totalQuantity);

            // Total amount for the month
            BigDecimal totalAmount = monthTransactions.stream()
                    .map(t -> t.getUnitPrice().multiply(new BigDecimal(t.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            monthData.put("totalAmount", totalAmount);

            // Average unit price for the month
            if (!monthTransactions.isEmpty()) {
                BigDecimal totalUnitPrice = monthTransactions.stream()
                        .map(Transaction::getUnitPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal averageUnitPrice = totalUnitPrice.divide(
                        new BigDecimal(monthTransactions.size()), 2, RoundingMode.HALF_UP);

                monthData.put("averageUnitPrice", averageUnitPrice);
            }

            monthData.put("productName", "Various");

            result.put(month, monthData);
        }

        return result;
    }

    /**
     * Get the next report ID
     */
    private synchronized long getNextReportId() {
        return nextReportId++;
    }
}