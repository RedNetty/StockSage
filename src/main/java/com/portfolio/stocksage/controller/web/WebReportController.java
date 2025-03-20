package com.portfolio.stocksage.controller.web;

import com.portfolio.stocksage.dto.response.ReportDTO;
import com.portfolio.stocksage.report.ReportType;
import com.portfolio.stocksage.service.CategoryService;
import com.portfolio.stocksage.service.ProductService;
import com.portfolio.stocksage.service.ReportService;
import com.portfolio.stocksage.service.SupplierService;
import com.portfolio.stocksage.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class WebReportController {

    private final ReportService reportService;
    private final CategoryService categoryService;
    private final WarehouseService warehouseService;
    private final SupplierService supplierService;
    private final ProductService productService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String getReportDashboard(Model model) {
        // Populate model with necessary data for the report dashboard
        model.addAttribute("reportTypes", ReportType.values());

        // Get current date and first day of month for default date range
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstDayOfMonth = now.with(TemporalAdjusters.firstDayOfMonth());

        model.addAttribute("startDate", firstDayOfMonth);
        model.addAttribute("endDate", now);

        return "report/dashboard";
    }

    @GetMapping("/inventory")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String getInventoryReportForm(Model model) {
        // Add data for form dropdowns
        model.addAttribute("warehouses", warehouseService.getAllWarehouses(null));
        model.addAttribute("categories", categoryService.getAllCategories(null));

        return "report/inventory-form";
    }

    @PostMapping("/inventory")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String generateInventoryReport(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean lowStockOnly,
            Model model) {

        ReportDTO report = reportService.generateInventoryReport(warehouseId, categoryId, lowStockOnly);
        model.addAttribute("report", report);

        return "report/view";
    }

    @GetMapping("/sales")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String getSalesReportForm(Model model) {
        // Add data for form dropdowns
        model.addAttribute("products", productService.getAllProducts(null));
        model.addAttribute("warehouses", warehouseService.getAllWarehouses(null));

        // Get current date and first day of month for default date range
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstDayOfMonth = now.with(TemporalAdjusters.firstDayOfMonth());

        model.addAttribute("startDate", firstDayOfMonth);
        model.addAttribute("endDate", now);

        // Add grouping options
        Map<String, String> groupByOptions = new HashMap<>();
        groupByOptions.put("day", "Day");
        groupByOptions.put("week", "Week");
        groupByOptions.put("month", "Month");
        model.addAttribute("groupByOptions", groupByOptions);

        return "report/sales-form";
    }

    @PostMapping("/sales")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String generateSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false, defaultValue = "day") String groupBy,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long warehouseId,
            Model model) {

        ReportDTO report = reportService.generateSalesReport(startDate, endDate, groupBy, productId, warehouseId);
        model.addAttribute("report", report);

        return "report/view";
    }

    @GetMapping("/purchases")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String getPurchaseReportForm(Model model) {
        // Add data for form dropdowns
        model.addAttribute("products", productService.getAllProducts(null));
        model.addAttribute("suppliers", supplierService.getAllSuppliers(null));

        // Get current date and first day of month for default date range
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstDayOfMonth = now.with(TemporalAdjusters.firstDayOfMonth());

        model.addAttribute("startDate", firstDayOfMonth);
        model.addAttribute("endDate", now);

        // Add grouping options
        Map<String, String> groupByOptions = new HashMap<>();
        groupByOptions.put("day", "Day");
        groupByOptions.put("week", "Week");
        groupByOptions.put("month", "Month");
        model.addAttribute("groupByOptions", groupByOptions);

        return "report/purchases-form";
    }

    @PostMapping("/purchases")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String generatePurchaseReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false, defaultValue = "day") String groupBy,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long supplierId,
            Model model) {

        ReportDTO report = reportService.generatePurchaseReport(startDate, endDate, groupBy, productId, supplierId);
        model.addAttribute("report", report);

        return "report/view";
    }

    @GetMapping("/product-performance")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String getProductPerformanceReportForm(Model model) {
        // Add data for form dropdowns
        model.addAttribute("categories", categoryService.getAllCategories(null));

        // Get current date and first day of month for default date range
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstDayOfMonth = now.with(TemporalAdjusters.firstDayOfMonth());

        model.addAttribute("startDate", firstDayOfMonth);
        model.addAttribute("endDate", now);

        return "report/product-performance-form";
    }

    @PostMapping("/product-performance")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String generateProductPerformanceReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, defaultValue = "10") Integer topN,
            Model model) {

        ReportDTO report = reportService.generateProductPerformanceReport(startDate, endDate, categoryId, topN);
        model.addAttribute("report", report);

        return "report/view";
    }

    @GetMapping("/supplier-performance")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String getSupplierPerformanceReportForm(Model model) {
        // Add data for form dropdowns
        model.addAttribute("suppliers", supplierService.getAllSuppliers(null));

        // Get current date and first day of month for default date range
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstDayOfMonth = now.with(TemporalAdjusters.firstDayOfMonth());

        model.addAttribute("startDate", firstDayOfMonth);
        model.addAttribute("endDate", now);

        return "report/supplier-performance-form";
    }

    @PostMapping("/supplier-performance")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String generateSupplierPerformanceReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Long supplierId,
            Model model) {

        ReportDTO report = reportService.generateSupplierPerformanceReport(startDate, endDate, supplierId);
        model.addAttribute("report", report);

        return "report/view";
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String getDownloadOptions(@PathVariable Long id, Model model) {
        model.addAttribute("reportId", id);

        // Add export format options
        Map<String, String> formatOptions = new HashMap<>();
        formatOptions.put("pdf", "PDF");
        formatOptions.put("excel", "Excel");
        formatOptions.put("csv", "CSV");
        model.addAttribute("formatOptions", formatOptions);

        return "report/download-options";
    }

    @GetMapping("/schedules")
    @PreAuthorize("hasRole('ADMIN')")
    public String getScheduledReports(Model model) {
        Map<Long, Map<String, Object>> scheduledReports = reportService.getScheduledReports();
        model.addAttribute("scheduledReports", scheduledReports);

        return "report/schedules";
    }
}