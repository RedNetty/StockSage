package com.portfolio.stocksage.report;

import com.portfolio.stocksage.dto.response.ProductDTO;
import com.portfolio.stocksage.dto.response.ReportDTO;
import com.portfolio.stocksage.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Component for generating product reports
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductReport {

    private final ProductService productService;

    /**
     * Generate a product performance report
     *
     * @param categoryId Optional category ID to filter products
     * @param active Filter by active status
     * @param lowStockOnly Whether to include only low stock items
     * @return ReportDTO containing the product report data
     */
    public ReportDTO generateProductReport(Long categoryId, Boolean active, Boolean lowStockOnly) {
        log.info("Generating product report - categoryId: {}, active: {}, lowStockOnly: {}",
                categoryId, active, lowStockOnly);

        // Create report object
        ReportDTO report = new ReportDTO();
        report.setTitle("Products Report");
        report.setReportType(ReportType.PRODUCT_PERFORMANCE);
        report.setGeneratedAt(LocalDateTime.now());

        // Set parameters
        report.getParameters().put("categoryId", categoryId);
        report.getParameters().put("active", active != null ? active : true);
        report.getParameters().put("lowStockOnly", lowStockOnly != null && lowStockOnly);

        // Set columns
        List<String> columns = List.of(
                "ID", "SKU", "Name", "Category", "Unit Price", "Stock", "Status"
        );
        report.setColumns(columns);

        // Build description
        StringBuilder description = new StringBuilder("Products Report");
        if (categoryId != null) {
            description.append(" for Category ID: ").append(categoryId);
        }
        if (active != null) {
            description.append(active ? " (Active Only)" : " (Including Inactive)");
        }
        if (lowStockOnly != null && lowStockOnly) {
            description.append(" (Low Stock Items Only)");
        }
        report.setDescription(description.toString());

        // Generate report data
        List<Map<String, Object>> reportData = new ArrayList<>();

        // Get products data based on parameters
        List<ProductDTO> productList;

        if (lowStockOnly != null && lowStockOnly) {
            productList = productService.getLowStockProducts(10); // Assuming threshold of 10
        } else {
            // Set up pageable for all products
            Pageable pageable = PageRequest.of(0, 1000); // Assuming max 1000 products

            // Get products by category or all products
            Page<ProductDTO> productsPage;
            if (categoryId != null) {
                productsPage = productService.getProductsByCategory(categoryId, pageable);
            } else if (active != null && active) {
                productsPage = productService.getActiveProducts(pageable);
            } else {
                productsPage = productService.getAllProducts(pageable);
            }

            productList = productsPage.getContent();
        }

        // Transform products to report rows
        for (ProductDTO product : productList) {
            Map<String, Object> row = new HashMap<>();

            row.put("ID", product.getId());
            row.put("SKU", product.getSku());
            row.put("Name", product.getName());
            row.put("Category", product.getCategory().getName());
            row.put("Unit Price", product.getUnitPrice());
            row.put("Stock", product.getUnitsInStock());
            row.put("Status", product.isActive() ? "Active" : "Inactive");

            reportData.add(row);
        }

        report.setData(reportData);

        // Calculate summary statistics
        Map<String, Object> summary = new HashMap<>();

        int totalProducts = reportData.size();
        int activeProducts = (int) reportData.stream()
                .filter(row -> "Active".equals(row.get("Status")))
                .count();
        int inactiveProducts = totalProducts - activeProducts;

        int outOfStock = (int) reportData.stream()
                .filter(row -> (Integer) row.get("Stock") == 0)
                .count();

        int lowStock = (int) reportData.stream()
                .filter(row -> {
                    Integer stock = (Integer) row.get("Stock");
                    return stock > 0 && stock <= 10; // Assuming threshold of 10
                })
                .count();

        BigDecimal totalValue = reportData.stream()
                .map(row -> {
                    BigDecimal price = (BigDecimal) row.get("Unit Price");
                    Integer stock = (Integer) row.get("Stock");
                    return price.multiply(new BigDecimal(stock));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        summary.put("Total Products", totalProducts);
        summary.put("Active Products", activeProducts);
        summary.put("Inactive Products", inactiveProducts);
        summary.put("Out of Stock", outOfStock);
        summary.put("Low Stock", lowStock);
        summary.put("Total Inventory Value", totalValue);

        report.setSummary(summary);

        // Create chart data for product status
        ReportDTO.ChartData statusChart = new ReportDTO.ChartData();
        statusChart.setChartType("pie");
        statusChart.setTitle("Product Status");

        List<String> statusLabels = List.of("Active", "Inactive");
        statusChart.setLabels(statusLabels);

        ReportDTO.DataSeries statusSeries = new ReportDTO.DataSeries();
        statusSeries.setName("Products");
        statusSeries.setData(List.of(activeProducts, inactiveProducts));

        statusChart.getSeries().add(statusSeries);
        report.getCharts().add(statusChart);

        // Create chart data for stock status
        ReportDTO.ChartData stockChart = new ReportDTO.ChartData();
        stockChart.setChartType("pie");
        stockChart.setTitle("Stock Status");

        List<String> stockLabels = List.of("Normal Stock", "Low Stock", "Out of Stock");
        stockChart.setLabels(stockLabels);

        ReportDTO.DataSeries stockSeries = new ReportDTO.DataSeries();
        stockSeries.setName("Products");
        stockSeries.setData(List.of(
                totalProducts - lowStock - outOfStock,
                lowStock,
                outOfStock
        ));

        stockChart.getSeries().add(stockSeries);
        report.getCharts().add(stockChart);

        log.info("Product report generated with {} data rows", reportData.size());
        return report;
    }
}