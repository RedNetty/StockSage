package com.portfolio.stocksage.controller.web;

import com.portfolio.stocksage.dto.response.DashboardDTO;
import com.portfolio.stocksage.service.DashboardService;
import com.portfolio.stocksage.service.ProductService;
import com.portfolio.stocksage.service.TransactionService;
import com.portfolio.stocksage.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Optional;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class WebDashboardController {

    private final DashboardService dashboardService;
    private final ProductService productService;
    private final WarehouseService warehouseService;
    private final TransactionService transactionService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String getDashboard(
            Model model,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        // If dates not provided, use current month
        if (startDate == null) {
            startDate = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        }

        if (endDate == null) {
            endDate = LocalDateTime.now().with(TemporalAdjusters.lastDayOfMonth())
                    .withHour(23).withMinute(59).withSecond(59);
        }

        // Get dashboard data
        DashboardDTO dashboardData = dashboardService.getDashboardSummaryForDateRange(startDate, endDate);

        // Add data to model
        model.addAttribute("dashboard", dashboardData);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        // Additional metrics for widgets
        model.addAttribute("lowStockCount", dashboardData.getInventorySummary().getLowStockItems());
        model.addAttribute("outOfStockCount", dashboardData.getInventorySummary().getOutOfStockItems());
        model.addAttribute("pendingTransactions", dashboardData.getTransactionSummary().getPendingTransactions());

        return "dashboard/index";
    }

    @GetMapping("/products")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String getProductsDashboard(Model model) {
        // Add product-specific metrics to model
        model.addAttribute("totalProducts", productService.getAllProducts(null).getTotalElements());

        // Get low stock products for alerts
        model.addAttribute("lowStockProducts", productService.getLowStockProducts(10));

        return "dashboard/products";
    }

    @GetMapping("/inventory")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String getInventoryDashboard(Model model) {
        // Add inventory-specific metrics to model
        model.addAttribute("warehouses", warehouseService.getAllWarehouses(null));

        return "dashboard/inventory";
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String getTransactionsDashboard(
            Model model,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        // If dates not provided, use current month
        if (startDate == null) {
            startDate = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        }

        if (endDate == null) {
            endDate = LocalDateTime.now().with(TemporalAdjusters.lastDayOfMonth())
                    .withHour(23).withMinute(59).withSecond(59);
        }

        // Add transaction-specific metrics to model
        model.addAttribute("recentTransactions", transactionService.getRecentTransactions(10));
        model.addAttribute("salesTotal", transactionService.getSalesTotal(startDate, endDate));
        model.addAttribute("purchasesTotal", transactionService.getPurchasesTotal(startDate, endDate));

        return "dashboard/transactions";
    }
}