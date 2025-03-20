package com.portfolio.stocksage.config;

import com.portfolio.stocksage.dto.response.InventoryDTO;
import com.portfolio.stocksage.entity.Transaction;
import com.portfolio.stocksage.service.InventoryService;
import com.portfolio.stocksage.service.NotificationService;
import com.portfolio.stocksage.service.ProductService;
import com.portfolio.stocksage.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasksConfig {

    private final InventoryService inventoryService;
    private final NotificationService notificationService;
    private final ProductService productService;
    private final TransactionService transactionService;

    private static final int LOW_STOCK_THRESHOLD = 10;

    /**
     * Daily check for low stock items at 7:00 AM
     */
    @Scheduled(cron = "0 0 7 * * ?")
    public void checkLowStockItems() {
        log.info("Running scheduled low stock check");
        List<InventoryDTO> lowStockItems = inventoryService.getLowInventory(LOW_STOCK_THRESHOLD);

        if (!lowStockItems.isEmpty()) {
            log.info("Found {} low stock items", lowStockItems.size());

            // Send notifications for each low stock item
            for (InventoryDTO item : lowStockItems) {
                notificationService.sendLowStockAlert(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        LOW_STOCK_THRESHOLD
                );
            }
        } else {
            log.info("No low stock items found");
        }
    }

    /**
     * Check for out of stock items every 4 hours
     */
    @Scheduled(fixedRate = 4 * 60 * 60 * 1000)
    public void checkOutOfStockItems() {
        log.info("Running scheduled out of stock check");
        List<InventoryDTO> outOfStockItems = inventoryService.getOutOfStockItems();

        if (!outOfStockItems.isEmpty()) {
            log.info("Found {} out of stock items", outOfStockItems.size());

            // Send notifications for each out of stock item
            for (InventoryDTO item : outOfStockItems) {
                notificationService.sendStockOutAlert(
                        item.getProduct().getId(),
                        item.getProduct().getName()
                );
            }
        } else {
            log.info("No out of stock items found");
        }
    }

    /**
     * Auto-cancel pending transactions older than 7 days at midnight
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void cancelOldPendingTransactions() {
        log.info("Running scheduled check for old pending transactions");

        // Get pending transactions
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
    }

    /**
     * Generate and send daily transaction summary at 11:55 PM
     */
    @Scheduled(cron = "0 55 23 * * ?")
    public void generateDailySummary() {
        log.info("Generating daily transaction summary");

        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        try {
            // Get sales and purchases totals
            int todaySales = transactionService.getDailyTransactionCount(
                    startOfDay, endOfDay, Transaction.TransactionType.SALE);
            int todayPurchases = transactionService.getDailyTransactionCount(
                    startOfDay, endOfDay, Transaction.TransactionType.PURCHASE);

            // Create a system notification with the summary
            String title = "Daily Transaction Summary";
            String message = String.format(
                    "Today's activity: %d sales and %d purchases completed.",
                    todaySales, todayPurchases);

            notificationService.createSystemNotification(title, message, "SYSTEM");
            log.info("Generated daily summary: {}", message);
        } catch (Exception e) {
            log.error("Error generating daily summary: {}", e.getMessage());
        }
    }
}