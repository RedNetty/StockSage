package com.portfolio.stocksage.controller.web;

import com.portfolio.stocksage.dto.request.TransactionCreateDTO;
import com.portfolio.stocksage.dto.response.ProductDTO;
import com.portfolio.stocksage.dto.response.TransactionDTO;
import com.portfolio.stocksage.entity.Transaction.TransactionStatus;
import com.portfolio.stocksage.entity.Transaction.TransactionType;
import com.portfolio.stocksage.security.SecurityUtils;
import com.portfolio.stocksage.service.ProductService;
import com.portfolio.stocksage.service.TransactionService;
import com.portfolio.stocksage.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class WebTransactionController {

    private final TransactionService transactionService;
    private final ProductService productService;
    private final WarehouseService warehouseService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public String getAllTransactions(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "transactionDate") String sort,
            @RequestParam(defaultValue = "desc") String dir,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        // Create pageable request
        Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sort));

        // Get transactions based on filters
        Page<TransactionDTO> transactions;
        if (type != null) {
            transactions = transactionService.getTransactionsByType(type, pageRequest);
        } else if (status != null) {
            transactions = transactionService.getTransactionsByStatus(status, pageRequest);
        } else if (startDate != null && endDate != null) {
            transactions = transactionService.getTransactionsByDateRange(startDate, endDate, pageRequest);
        } else {
            transactions = transactionService.getAllTransactions(pageRequest);
        }

        // Add attributes to model
        model.addAttribute("transactions", transactions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", transactions.getTotalPages());
        model.addAttribute("totalItems", transactions.getTotalElements());
        model.addAttribute("sortField", sort);
        model.addAttribute("sortDir", dir);
        model.addAttribute("reverseSortDir", "asc".equals(dir) ? "desc" : "asc");
        model.addAttribute("transactionTypes", TransactionType.values());
        model.addAttribute("transactionStatuses", TransactionStatus.values());
        model.addAttribute("type", type);
        model.addAttribute("status", status);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "transaction/list";
    }

    @GetMapping("/{id}")
    public String getTransactionDetails(@PathVariable Long id, Model model) {
        TransactionDTO transaction = transactionService.getTransactionById(id);
        model.addAttribute("transaction", transaction);
        return "transaction/details";
    }

    @GetMapping("/create/{type}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String showCreateForm(@PathVariable TransactionType type, Model model) {
        TransactionCreateDTO transaction = new TransactionCreateDTO();
        transaction.setTransactionType(type);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setStatus(TransactionStatus.PENDING);

        // Generate transaction number
        transaction.setTransactionNumber(transactionService.generateTransactionNumber(type));

        model.addAttribute("transaction", transaction);
        model.addAttribute("products", productService.getAllProducts(null));
        model.addAttribute("warehouses", warehouseService.getAllWarehouses(null));
        model.addAttribute("transactionType", type);

        return "transaction/create";
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String createTransaction(
            @Valid @ModelAttribute("transaction") TransactionCreateDTO transaction,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Check for validation errors
        if (result.hasErrors()) {
            model.addAttribute("products", productService.getAllProducts(null));
            model.addAttribute("warehouses", warehouseService.getAllWarehouses(null));
            model.addAttribute("transactionType", transaction.getTransactionType());
            return "transaction/create";
        }

        // For transfer transactions, validate source and destination warehouses
        if (TransactionType.TRANSFER.equals(transaction.getTransactionType())) {
            if (transaction.getSourceWarehouseId() == null) {
                result.rejectValue("sourceWarehouseId", "error.transaction", "Source warehouse is required for transfers");
                model.addAttribute("products", productService.getAllProducts(null));
                model.addAttribute("warehouses", warehouseService.getAllWarehouses(null));
                model.addAttribute("transactionType", transaction.getTransactionType());
                return "transaction/create";
            }
            if (transaction.getDestinationWarehouseId() == null) {
                result.rejectValue("destinationWarehouseId", "error.transaction", "Destination warehouse is required for transfers");
                model.addAttribute("products", productService.getAllProducts(null));
                model.addAttribute("warehouses", warehouseService.getAllWarehouses(null));
                model.addAttribute("transactionType", transaction.getTransactionType());
                return "transaction/create";
            }
            if (transaction.getSourceWarehouseId().equals(transaction.getDestinationWarehouseId())) {
                result.rejectValue("destinationWarehouseId", "error.transaction", "Source and destination warehouses cannot be the same");
                model.addAttribute("products", productService.getAllProducts(null));
                model.addAttribute("warehouses", warehouseService.getAllWarehouses(null));
                model.addAttribute("transactionType", transaction.getTransactionType());
                return "transaction/create";
            }
        }

        try {
            // Get current username
            String username = securityUtils.getCurrentUsername();

            // Create transaction
            TransactionDTO createdTransaction = transactionService.createTransaction(transaction, username);

            redirectAttributes.addFlashAttribute("successMessage", "Transaction created successfully");
            return "redirect:/transactions/" + createdTransaction.getId();
        } catch (Exception e) {
            result.rejectValue("product", "error.transaction", e.getMessage());
            model.addAttribute("products", productService.getAllProducts(null));
            model.addAttribute("warehouses", warehouseService.getAllWarehouses(null));
            model.addAttribute("transactionType", transaction.getTransactionType());
            return "transaction/create";
        }
    }

    @GetMapping("/update-status/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String showUpdateStatusForm(@PathVariable Long id, Model model) {
        TransactionDTO transaction = transactionService.getTransactionById(id);
        model.addAttribute("transaction", transaction);
        model.addAttribute("transactionStatuses", TransactionStatus.values());
        return "transaction/update-status";
    }

    @PostMapping("/update-status/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String updateTransactionStatus(
            @PathVariable Long id,
            @RequestParam TransactionStatus status,
            RedirectAttributes redirectAttributes) {

        try {
            TransactionDTO updatedTransaction = transactionService.updateTransactionStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Transaction status updated successfully");
            return "redirect:/transactions/" + updatedTransaction.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/transactions/" + id;
        }
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showDeleteConfirmation(@PathVariable Long id, Model model) {
        TransactionDTO transaction = transactionService.getTransactionById(id);
        model.addAttribute("transaction", transaction);
        return "transaction/delete";
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteTransaction(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            transactionService.deleteTransaction(id);
            redirectAttributes.addFlashAttribute("successMessage", "Transaction deleted successfully");
            return "redirect:/transactions";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/transactions/" + id;
        }
    }

    @GetMapping("/product-price/{id}")
    @ResponseBody
    public Map<String, Object> getProductPrice(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        Map<String, Object> response = new HashMap<>();
        response.put("unitPrice", product.getUnitPrice());
        return response;
    }

    @GetMapping("/sales")
    public String getSalesTransactions(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "transactionDate"));
        Page<TransactionDTO> transactions = transactionService.getTransactionsByType(TransactionType.SALE, pageRequest);

        model.addAttribute("transactions", transactions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", transactions.getTotalPages());
        model.addAttribute("totalItems", transactions.getTotalElements());
        model.addAttribute("transactionType", "Sales");

        return "transaction/type-list";
    }

    @GetMapping("/purchases")
    public String getPurchaseTransactions(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "transactionDate"));
        Page<TransactionDTO> transactions = transactionService.getTransactionsByType(TransactionType.PURCHASE, pageRequest);

        model.addAttribute("transactions", transactions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", transactions.getTotalPages());
        model.addAttribute("totalItems", transactions.getTotalElements());
        model.addAttribute("transactionType", "Purchases");

        return "transaction/type-list";
    }

    @GetMapping("/transfers")
    public String getTransferTransactions(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "transactionDate"));
        Page<TransactionDTO> transactions = transactionService.getTransactionsByType(TransactionType.TRANSFER, pageRequest);

        model.addAttribute("transactions", transactions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", transactions.getTotalPages());
        model.addAttribute("totalItems", transactions.getTotalElements());
        model.addAttribute("transactionType", "Transfers");

        return "transaction/type-list";
    }

    @GetMapping("/adjustments")
    public String getAdjustmentTransactions(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "transactionDate"));
        Page<TransactionDTO> transactions = transactionService.getTransactionsByType(TransactionType.ADJUSTMENT, pageRequest);

        model.addAttribute("transactions", transactions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", transactions.getTotalPages());
        model.addAttribute("totalItems", transactions.getTotalElements());
        model.addAttribute("transactionType", "Adjustments");

        return "transaction/type-list";
    }
}