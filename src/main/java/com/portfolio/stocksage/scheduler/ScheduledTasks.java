package com.portfolio.stocksage.scheduler;

import com.portfolio.stocksage.entity.Product;
import com.portfolio.stocksage.entity.Transaction;
import com.portfolio.stocksage.report.ReportType;
import com.portfolio.stocksage.service.InventoryService;
import com.portfolio.stocksage.service.NotificationService;
import com.portfolio.stocksage.service.ProductService;
import com.portfolio.stocksage.service.ReportService;
import com.portfolio.stocksage.service.FileStorageService;
import com.portfolio.stocksage.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    private final ProductService productService;
    private final InventoryService inventoryService;
    private final NotificationService notificationService;
    private final ReportService reportService;
    private final FileStorageService fileStorageService;
    private final TransactionService transactionService;

    @Value("${app.scheduler.temp-file-cleanup-days:7}")
    private int tempFileCleanupDays;

    @Value("${app.scheduler.low-stock-threshold:10}")
    private int lowStockThreshold;

    /**
     * Check for low stock items - runs daily at 8:00 AM
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void checkLowStockItems() {
        log.info("Running scheduled task: Check Low Stock Items");

        try {
            List<Product> lowStockProducts = productService.getLowStockProducts(lowStockThreshold);

            if (!lowStockProducts.isEmpty()) {
                // Create title with count
                String title = "Low Stock Alert: " + lowStockProducts.size() + " products below threshold";

                // Build detailed message
                StringBuilder message = new StringBuilder();
                message.append("The following products are running low on stock:\n\n");

                for (Product product : lowStockProducts) {
                    message.append("• ")
                            .append(product.getName())
                            .append(" (")
                            .append(product.getSku())
                            .append("): ")
                            .append(product.getUnitsInStock())
                            .append(" in stock\n");

                    // Also send individual notifications for severely low stock (less than half of threshold)
                    if (product.getUnitsInStock() < lowStockThreshold / 2) {
                        notificationService.sendLowStockAlert(
                                product.getId(),
                                product.getName(),
                                product.getUnitsInStock(),
                                lowStockThreshold
                        );
                    }
                }

                // Send a summary notification to inventory managers
                notificationService.createNotificationForRole(
                        title,
                        message.toString(),
                        "INVENTORY_ALERT",
                        "INVENTORY_MANAGER"
                );

                log.info("Low stock notification sent for {} products", lowStockProducts.size());
            } else {
                log.info("No low stock items found");
            }
        } catch (Exception e) {
            log.error("Error in low stock check scheduled task", e);
        }
    }

    /**
     * Check for out-of-stock items - runs every 4 hours
     */
    @Scheduled(fixedRate = 4 * 60 * 60 * 1000)
    public void checkOutOfStockItems() {
        log.info("Running scheduled task: Check Out of Stock Items");

        try {
            List<Product> outOfStockProducts = productService.getLowStockProducts(0);

            for (Product product : outOfStockProducts) {
                if (product.getUnitsInStock() <= 0) {
                    notificationService.sendStockOutAlert(product.getId(), product.getName());
                }
            }

            if (!outOfStockProducts.isEmpty()) {
                log.info("Stock out alerts sent for {} products", outOfStockProducts.size());
            } else {
                log.info("No out-of-stock items found");
            }
        } catch (Exception e) {
            log.error("Error in out-of-stock check scheduled task", e);
        }
    }

    /**
     * Generate daily inventory report - runs daily at 11:00 PM
     */
    @Scheduled(cron = "0 0 23 * * ?")
    public void generateDailyInventoryReport() {
        log.info("Running scheduled task: Generate Daily Inventory Report");

        try {
            // Generate inventory report
            reportService.generateInventoryReport(null, null, null);

            log.info("Daily inventory report generated successfully");
        } catch (Exception e) {
            log.error("Error generating daily inventory report", e);
        }
    }

    /**
     * Generate weekly sales report - runs every Sunday at 11:30 PM
     */
    @Scheduled(cron = "0 30 23 * * SUN")
    public void generateWeeklySalesReport() {
        log.info("Running scheduled task: Generate Weekly Sales Report");

        try {
            // Set date range for the past week
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusDays(7);

            // Generate sales report
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("startDate", startDate);
            parameters.put("endDate", endDate);
            parameters.put("groupBy", "day");

            // Schedule the report with email notification to admin and inventory managers
            reportService.scheduleReport(
                    ReportType.SALES,
                    parameters,
                    "0 30 23 * * SUN", // Same schedule as this method
                    new String[]{"admin@stocksage.com", "inventory@stocksage.com"}
            );

            log.info("Weekly sales report generated successfully");
        } catch (Exception e) {
            log.error("Error generating weekly sales report", e);
        }
    }

    /**
     * Clean up temporary files older than the configured number of days - runs daily at 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupTempFiles() {
        log.info("Running scheduled task: Clean Up Temp Files");

        try {
            // Get all files in the temp directory
            fileStorageService.loadAllFromSubdirectory("temp").forEach(file -> {
                try {
                    Path fullPath = Paths.get(fileStorageService.toString(), "temp", file.toString());
                    BasicFileAttributes attrs = Files.readAttributes(fullPath, BasicFileAttributes.class);

                    // Check if file is older than the configured days
                    LocalDateTime createTime = LocalDateTime.ofInstant(
                            attrs.creationTime().toInstant(), ZoneId.systemDefault());
                    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(tempFileCleanupDays);

                    if (createTime.isBefore(cutoffDate)) {
                        if (Files.deleteIfExists(fullPath)) {
                            log.debug("Deleted old temp file: {}", file);
                        }
                    }
                } catch (IOException e) {
                    log.error("Error cleaning up temp file: {}", file, e);
                }
            });

            log.info("Temp files cleanup completed");
        } catch (Exception e) {
            log.error("Error in temp files cleanup task", e);
        }
    }

    /**
     * Update inventory statistics - runs every hour
     */
    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void updateInventoryStatistics() {
        log.info("Running scheduled task: Update Inventory Statistics");

        try {
            // This could involve updating cache, pre-calculating inventory stats, etc.
            // For now, just get some basic inventory data
            List<Product> lowStockProducts = productService.getLowStockProducts(lowStockThreshold);
            log.info("Current low stock products count: {}", lowStockProducts.size());

            // Rest of implementation would depend on what statistics need to be updated
        } catch (Exception e) {
            log.error("Error updating inventory statistics", e);
        }
    }

    /**
     * Send a test notification - runs at 5:00 AM on the first day of each month
     * This is to verify the notification system is working
     */
    @Scheduled(cron = "0 0 5 1 * ?")
    public void sendTestNotification() {
        log.info("Running scheduled task: Send Test Notification");

        try {
            String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            notificationService.createNotificationForRole(
                    "Monthly System Health Check",
                    "This is an automated test notification to verify the notification system is working properly. Date: " + currentDate,
                    "SYSTEM_TEST",
                    "ADMIN"
            );

            log.info("Test notification sent successfully");
        } catch (Exception e) {
            log.error("Error sending test notification", e);
        }
    }

    /**
     * Auto-cancel pending transactions older than 7 days at midnight
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void cancelOldPendingTransactions() {
        log.info("Running scheduled task: Cancel Old Pending Transactions");

        try {
            // Get pending transactions older than 7 days
            List<Transaction> pendingTransactions = transactionService.getOldPendingTransactions(7);

            if (!pendingTransactions.isEmpty()) {
                log.info("Found {} old pending transactions to cancel", pendingTransactions.size());

                // Cancel each transaction
                for (Transaction transaction : pendingTransactions) {
                    try {
                        transactionService.updateTransactionStatus(
                                transaction.getId(),
                                Transaction.TransactionStatus.CANCELLED
                        );

                        log.info("Cancelled transaction: {}", transaction.getTransactionNumber());
                    } catch (Exception e) {
                        log.error("Error cancelling transaction {}: {}",
                                transaction.getTransactionNumber(), e.getMessage());
                    }
                }
            } else {
                log.info("No old pending transactions found");
            }
        } catch (Exception e) {
            log.error("Error in cancel old pending transactions task", e);
        }
    }
}