package com.portfolio.stocksage.controller.api;

import com.portfolio.stocksage.dto.response.ReportDTO;
import com.portfolio.stocksage.report.ReportType;
import com.portfolio.stocksage.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Report API", description = "Endpoints for generating and managing reports")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/inventory")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    @Operation(summary = "Generate inventory report", description = "Generates a report of current inventory status")
    public ResponseEntity<ReportDTO> generateInventoryReport(
            @Parameter(description = "Warehouse ID to filter by")
            @RequestParam(required = false) Long warehouseId,
            @Parameter(description = "Category ID to filter by")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Show only low stock items")
            @RequestParam(required = false) Boolean lowStockOnly) {

        ReportDTO report = reportService.generateInventoryReport(warehouseId, categoryId, lowStockOnly);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/sales")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    @Operation(summary = "Generate sales report", description = "Generates a report of sales for the specified period")
    public ResponseEntity<ReportDTO> generateSalesReport(
            @Parameter(description = "Start date", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "How to group results (day, week, month)")
            @RequestParam(required = false, defaultValue = "day") String groupBy,
            @Parameter(description = "Product ID to filter by")
            @RequestParam(required = false) Long productId,
            @Parameter(description = "Warehouse ID to filter by")
            @RequestParam(required = false) Long warehouseId) {

        ReportDTO report = reportService.generateSalesReport(startDate, endDate, groupBy, productId, warehouseId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/purchases")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    @Operation(summary = "Generate purchase report", description = "Generates a report of purchases for the specified period")
    public ResponseEntity<ReportDTO> generatePurchaseReport(
            @Parameter(description = "Start date", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "How to group results (day, week, month)")
            @RequestParam(required = false, defaultValue = "day") String groupBy,
            @Parameter(description = "Product ID to filter by")
            @RequestParam(required = false) Long productId,
            @Parameter(description = "Supplier ID to filter by")
            @RequestParam(required = false) Long supplierId) {

        ReportDTO report = reportService.generatePurchaseReport(startDate, endDate, groupBy, productId, supplierId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/product-performance")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    @Operation(summary = "Generate product performance report", description = "Generates a report of product performance metrics")
    public ResponseEntity<ReportDTO> generateProductPerformanceReport(
            @Parameter(description = "Start date", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "Category ID to filter by")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Number of top products to include")
            @RequestParam(required = false, defaultValue = "10") Integer topN) {

        ReportDTO report = reportService.generateProductPerformanceReport(startDate, endDate, categoryId, topN);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/supplier-performance")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    @Operation(summary = "Generate supplier performance report", description = "Generates a report of supplier performance metrics")
    public ResponseEntity<ReportDTO> generateSupplierPerformanceReport(
            @Parameter(description = "Start date", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "Supplier ID to filter by")
            @RequestParam(required = false) Long supplierId) {

        ReportDTO report = reportService.generateSupplierPerformanceReport(startDate, endDate, supplierId);
        return ResponseEntity.ok(report);
    }

    @PostMapping("/schedule")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Schedule a report", description = "Schedules a report to be generated periodically")
    public ResponseEntity<Map<String, Object>> scheduleReport(
            @Parameter(description = "Report type", required = true)
            @RequestParam ReportType reportType,
            @Parameter(description = "Report parameters", required = true)
            @RequestBody Map<String, Object> parameters,
            @Parameter(description = "Cron expression for schedule", required = true)
            @RequestParam String schedule,
            @Parameter(description = "Email recipients", required = true)
            @RequestParam String[] emailRecipients) {

        Long scheduleId = reportService.scheduleReport(reportType, parameters, schedule, emailRecipients);

        Map<String, Object> response = new HashMap<>();
        response.put("scheduleId", scheduleId);
        response.put("message", "Report scheduled successfully");

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/schedule/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cancel scheduled report", description = "Cancels a scheduled report")
    public ResponseEntity<Map<String, Object>> cancelScheduledReport(
            @Parameter(description = "Schedule ID", required = true)
            @PathVariable Long id) {

        reportService.cancelScheduledReport(id);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Scheduled report canceled successfully");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/schedules")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get scheduled reports", description = "Returns a list of all scheduled reports")
    public ResponseEntity<Map<Long, Map<String, Object>>> getScheduledReports() {
        Map<Long, Map<String, Object>> scheduledReports = reportService.getScheduledReports();
        return ResponseEntity.ok(scheduledReports);
    }

    @GetMapping("/{id}/export")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    @Operation(summary = "Export report", description = "Exports a report in the specified format")
    public ResponseEntity<byte[]> exportReport(
            @Parameter(description = "Report ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Export format (pdf, excel, csv)", required = true)
            @RequestParam String format) {

        byte[] reportBytes = reportService.exportReport(id, format);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(getMediaType(format));
        headers.setContentDispositionFormData("attachment", "report." + getFileExtension(format));

        return ResponseEntity.ok()
                .headers(headers)
                .body(reportBytes);
    }

    /**
     * Helper method to get media type based on format
     */
    private MediaType getMediaType(String format) {
        switch (format.toLowerCase()) {
            case "pdf":
                return MediaType.APPLICATION_PDF;
            case "excel":
                return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case "csv":
                return MediaType.parseMediaType("text/csv");
            default:
                return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    /**
     * Helper method to get file extension based on format
     */
    private String getFileExtension(String format) {
        switch (format.toLowerCase()) {
            case "pdf":
                return "pdf";
            case "excel":
                return "xlsx";
            case "csv":
                return "csv";
            default:
                return "bin";
        }
    }
}