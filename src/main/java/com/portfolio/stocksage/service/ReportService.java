package com.portfolio.stocksage.service;

import com.portfolio.stocksage.dto.response.ReportDTO;
import com.portfolio.stocksage.report.ReportType;

import java.time.LocalDateTime;
import java.util.Map;

public interface ReportService {

    /**
     * Generate an inventory report
     * @param warehouseId Optional warehouse ID to filter inventory
     * @param categoryId Optional category ID to filter inventory
     * @param lowStockOnly Whether to include only low stock items
     * @return ReportDTO containing the inventory report data
     */
    ReportDTO generateInventoryReport(Long warehouseId, Long categoryId, Boolean lowStockOnly);

    /**
     * Generate a sales report for a specific date range
     * @param startDate Start date for the report
     * @param endDate End date for the report
     * @param groupBy How to group the results (e.g., by day, week, month)
     * @param productId Optional product ID to filter sales
     * @param warehouseId Optional warehouse ID to filter sales
     * @return ReportDTO containing the sales report data
     */
    ReportDTO generateSalesReport(LocalDateTime startDate, LocalDateTime endDate,
                                  String groupBy, Long productId, Long warehouseId);

    /**
     * Generate a purchase report for a specific date range
     * @param startDate Start date for the report
     * @param endDate End date for the report
     * @param groupBy How to group the results (e.g., by day, week, month)
     * @param productId Optional product ID to filter purchases
     * @param supplierId Optional supplier ID to filter purchases
     * @return ReportDTO containing the purchase report data
     */
    ReportDTO generatePurchaseReport(LocalDateTime startDate, LocalDateTime endDate,
                                     String groupBy, Long productId, Long supplierId);

    /**
     * Generate a product performance report
     * @param startDate Start date for the report
     * @param endDate End date for the report
     * @param categoryId Optional category ID to filter products
     * @param topN Number of top products to include
     * @return ReportDTO containing the product performance report data
     */
    ReportDTO generateProductPerformanceReport(LocalDateTime startDate, LocalDateTime endDate,
                                               Long categoryId, Integer topN);

    /**
     * Generate a supplier performance report
     * @param startDate Start date for the report
     * @param endDate End date for the report
     * @param supplierId Optional supplier ID to filter
     * @return ReportDTO containing the supplier performance report data
     */
    ReportDTO generateSupplierPerformanceReport(LocalDateTime startDate, LocalDateTime endDate,
                                                Long supplierId);

    /**
     * Schedule a report for periodic generation
     * @param reportType Type of report to schedule
     * @param parameters Report parameters
     * @param schedule Cron expression for scheduling
     * @param emailRecipients List of email recipients
     * @return ID of the scheduled report
     */
    Long scheduleReport(ReportType reportType, Map<String, Object> parameters,
                        String schedule, String[] emailRecipients);

    /**
     * Cancel a scheduled report
     * @param scheduleId ID of the scheduled report
     */
    void cancelScheduledReport(Long scheduleId);

    /**
     * Get a list of all scheduled reports
     * @return Map of schedule IDs to report details
     */
    Map<Long, Map<String, Object>> getScheduledReports();

    /**
     * Export a report in the specified format
     * @param reportId Report ID to export
     * @param format Export format (PDF, Excel, CSV)
     * @return Byte array of the exported report
     */
    byte[] exportReport(Long reportId, String format);
}