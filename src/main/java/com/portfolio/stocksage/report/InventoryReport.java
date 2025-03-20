package com.portfolio.stocksage.report;

import com.portfolio.stocksage.dto.response.InventoryDTO;
import com.portfolio.stocksage.dto.response.ReportDTO;
import com.portfolio.stocksage.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Component for generating inventory reports
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryReport {

    private final InventoryService inventoryService;

    /**
     * Generate an inventory report
     *
     * @param warehouseId    Optional warehouse ID to filter inventory
     * @param categoryId     Optional category ID to filter inventory
     * @param lowStockOnly   Whether to include only low stock items
     * @return ReportDTO containing the inventory report data
     */
    public ReportDTO generateReport(Long warehouseId, Long categoryId, Boolean lowStockOnly) {
        log.info("Generating inventory report - warehouseId: {}, categoryId: {}, lowStockOnly: {}",
                warehouseId, categoryId, lowStockOnly);

        // Create report object
        ReportDTO report = new ReportDTO();
        report.setTitle("Inventory Report");
        report.setReportType(ReportType.INVENTORY);
        report.setGeneratedAt(LocalDateTime.now());

        // Set parameters
        report.getParameters().put("warehouseId", warehouseId);
        report.getParameters().put("categoryId", categoryId);
        report.getParameters().put("lowStockOnly", lowStockOnly != null && lowStockOnly);

        // Set columns
        List<String> columns = List.of(
                "Product ID", "SKU", "Name", "Category", "Warehouse",
                "Quantity", "Unit Price", "Total Value", "Status"
        );
        report.setColumns(columns);

        // Build description
        StringBuilder description = new StringBuilder("Inventory Report");
        if (warehouseId != null) {
            description.append(" for Warehouse ID: ").append(warehouseId);
        }
        if (categoryId != null) {
            description.append(" in Category ID: ").append(categoryId);
        }
        if (lowStockOnly != null && lowStockOnly) {
            description.append(" (Low Stock Items Only)");
        }
        report.setDescription(description.toString());

        // Generate report data
        List<Map<String, Object>> reportData = new ArrayList<>();

        // Get inventory data based on parameters
        List<InventoryDTO> inventoryList;

        if (lowStockOnly != null && lowStockOnly) {
            inventoryList = inventoryService.getLowInventory(10); // Assuming threshold of 10
        } else {
            // Get all inventory and apply filters
            inventoryList = inventoryService.getAllInventory(null).getContent();
        }

        // Apply category filter if specified
        if (categoryId != null) {
            inventoryList = inventoryList.stream()
                    .filter(i -> i.getProduct().getCategory().getId().equals(categoryId))
                    .collect(Collectors.toList());
        }

        // Apply warehouse filter if specified
        if (warehouseId != null) {
            inventoryList = inventoryList.stream()
                    .filter(i -> i.getWarehouse().getId().equals(warehouseId))
                    .collect(Collectors.toList());
        }

        // Transform inventory items to report rows
        for (InventoryDTO inventory : inventoryList) {
            Map<String, Object> row = new HashMap<>();

            row.put("Product ID", inventory.getProduct().getId());
            row.put("SKU", inventory.getProduct().getSku());
            row.put("Name", inventory.getProduct().getName());
            row.put("Category", inventory.getProduct().getCategory());
            row.put("Warehouse", inventory.getWarehouse().getName());
            row.put("Quantity", inventory.getQuantity());
            row.put("Unit Price", inventory.getProduct().getUnitPrice());
            row.put("Total Value", inventory.getTotalValue());

            // Determine status
            String status = "Normal";
            if (inventory.getQuantity() <= 0) {
                status = "Out of Stock";
            } else if (inventory.getQuantity() <= 10) { // Assuming threshold of 10
                status = "Low Stock";
            }
            row.put("Status", status);

            reportData.add(row);
        }

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

        // Create chart data for inventory status
        ReportDTO.ChartData statusChart = new ReportDTO.ChartData();
        statusChart.setChartType("pie");
        statusChart.setTitle("Inventory Status");

        List<String> labels = List.of("Normal", "Low Stock", "Out of Stock");
        statusChart.setLabels(labels);

        ReportDTO.DataSeries statusSeries = new ReportDTO.DataSeries();
        statusSeries.setName("Products");
        statusSeries.setData(List.of(
                totalProducts - lowStock - outOfStock,
                lowStock,
                outOfStock
        ));

        statusChart.getSeries().add(statusSeries);
        report.getCharts().add(statusChart);

        log.info("Inventory report generated with {} data rows", reportData.size());
        return report;
    }
}