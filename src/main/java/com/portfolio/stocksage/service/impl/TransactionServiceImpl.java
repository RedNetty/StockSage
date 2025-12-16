package com.portfolio.stocksage.service.impl;

import com.portfolio.stocksage.dto.mapper.TransactionMapper;
import com.portfolio.stocksage.dto.request.TransactionCreateDTO;
import com.portfolio.stocksage.dto.response.TransactionDTO;
import com.portfolio.stocksage.entity.Inventory;
import com.portfolio.stocksage.entity.Product;
import com.portfolio.stocksage.entity.Transaction;
import com.portfolio.stocksage.entity.Transaction.TransactionStatus;
import com.portfolio.stocksage.entity.Transaction.TransactionType;
import com.portfolio.stocksage.entity.User;
import com.portfolio.stocksage.entity.Warehouse;
import com.portfolio.stocksage.exception.ResourceNotFoundException;
import com.portfolio.stocksage.repository.InventoryRepository;
import com.portfolio.stocksage.repository.ProductRepository;
import com.portfolio.stocksage.repository.TransactionRepository;
import com.portfolio.stocksage.repository.UserRepository;
import com.portfolio.stocksage.repository.WarehouseRepository;
import com.portfolio.stocksage.service.TransactionService;
import com.portfolio.stocksage.util.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;
    private final TransactionMapper transactionMapper;

    @Override
    @Transactional
    public TransactionDTO createTransaction(TransactionCreateDTO transactionCreateDTO, String username) {
        // Validate transaction number uniqueness if provided
        if (transactionCreateDTO.getTransactionNumber() != null &&
                !transactionCreateDTO.getTransactionNumber().isEmpty() &&
                !isTransactionNumberUnique(transactionCreateDTO.getTransactionNumber())) {
            throw new IllegalArgumentException("Transaction number already exists: " + transactionCreateDTO.getTransactionNumber());
        }

        // Generate transaction number if not provided
        if (transactionCreateDTO.getTransactionNumber() == null ||
                transactionCreateDTO.getTransactionNumber().isEmpty()) {
            transactionCreateDTO.setTransactionNumber(
                    generateTransactionNumber(transactionCreateDTO.getTransactionType()));
        }

        // Get the user who is creating the transaction
        User createdBy = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        // Get product and warehouse
        Product product = productRepository.findById(transactionCreateDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + transactionCreateDTO.getProductId()));

        Warehouse warehouse = warehouseRepository.findById(transactionCreateDTO.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + transactionCreateDTO.getWarehouseId()));

        // Create transaction entity
        Transaction transaction = new Transaction();
        transaction.setTransactionNumber(transactionCreateDTO.getTransactionNumber());
        transaction.setTransactionDate(transactionCreateDTO.getTransactionDate());
        transaction.setTransactionType(transactionCreateDTO.getTransactionType());
        transaction.setStatus(transactionCreateDTO.getStatus());
        transaction.setProduct(product);
        transaction.setQuantity(transactionCreateDTO.getQuantity());
        transaction.setUnitPrice(transactionCreateDTO.getUnitPrice());
        transaction.setWarehouse(warehouse);
        transaction.setReferenceNumber(transactionCreateDTO.getReferenceNumber());
        transaction.setNotes(transactionCreateDTO.getNotes());
        transaction.setCreatedBy(createdBy);

        // Handle transfer transaction
        if (TransactionType.TRANSFER.equals(transactionCreateDTO.getTransactionType())) {
            if (transactionCreateDTO.getSourceWarehouseId() == null ||
                    transactionCreateDTO.getDestinationWarehouseId() == null) {
                throw new IllegalArgumentException("Source and destination warehouses are required for transfer transactions");
            }

            Warehouse sourceWarehouse = warehouseRepository.findById(transactionCreateDTO.getSourceWarehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Source warehouse not found with id: " +
                            transactionCreateDTO.getSourceWarehouseId()));

            Warehouse destinationWarehouse = warehouseRepository.findById(transactionCreateDTO.getDestinationWarehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Destination warehouse not found with id: " +
                            transactionCreateDTO.getDestinationWarehouseId()));

            transaction.setSourceWarehouse(sourceWarehouse);
            transaction.setDestinationWarehouse(destinationWarehouse);
        }

        // Save the transaction
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Update inventory if transaction is COMPLETED
        if (TransactionStatus.COMPLETED.equals(transaction.getStatus())) {
            updateInventory(transaction);
        }

        // Return the transaction DTO
        return transactionMapper.toDto(savedTransaction);
    }
    /**
     * Get a list of pending transactions older than the specified number of days
     */
    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getOldPendingTransactions(int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        return transactionRepository.findByStatusAndTransactionDateBefore(
                TransactionStatus.PENDING, cutoffDate);
    }

    /**
     * Get the count of transactions for a specific date range and type
     */
    @Override
    @Transactional(readOnly = true)
    public int getDailyTransactionCount(LocalDateTime startDate, LocalDateTime endDate, TransactionType type) {
        List<Transaction> transactions = transactionRepository.findByDateRangeAndType(
                startDate, endDate, type);

        // Count only completed transactions
        return (int) transactions.stream()
                .filter(t -> t.getStatus() == TransactionStatus.COMPLETED)
                .count();
    }
    @Override
    @Transactional(readOnly = true)
    public TransactionDTO getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        return transactionMapper.toDto(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionDTO getTransactionByNumber(String transactionNumber) {
        Transaction transaction = transactionRepository.findByTransactionNumber(transactionNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with number: " + transactionNumber));

        return transactionMapper.toDto(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionDTO> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable)
                .map(transactionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionDTO> getTransactionsByStatus(TransactionStatus status, Pageable pageable) {
        return transactionRepository.findByStatus(status, pageable)
                .map(transactionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionDTO> getTransactionsByType(TransactionType type, Pageable pageable) {
        return transactionRepository.findByTransactionType(type, pageable)
                .map(transactionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionDTO> getTransactionsByProduct(Long productId, Pageable pageable) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }
        return transactionRepository.findByProductId(productId, pageable)
                .map(transactionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionDTO> getTransactionsByWarehouse(Long warehouseId, Pageable pageable) {
        if (!warehouseRepository.existsById(warehouseId)) {
            throw new ResourceNotFoundException("Warehouse not found with id: " + warehouseId);
        }
        return transactionRepository.findByWarehouseId(warehouseId, pageable)
                .map(transactionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionDTO> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return transactionRepository.findByDateRange(startDate, endDate, pageable)
                .map(transactionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionDTO> getTransactionsByUser(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        return transactionRepository.findByUserId(userId, pageable)
                .map(transactionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionDTO> searchTransactions(String keyword, Pageable pageable) {
        return transactionRepository.search(keyword, pageable)
                .map(transactionMapper::toDto);
    }

    @Override
    @Transactional
    public TransactionDTO updateTransaction(Long id, TransactionCreateDTO transactionCreateDTO) {
        Transaction existingTransaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        // Check if transaction is already completed and trying to modify it
        if (TransactionStatus.COMPLETED.equals(existingTransaction.getStatus()) &&
                !TransactionStatus.COMPLETED.equals(transactionCreateDTO.getStatus())) {
            throw new IllegalArgumentException("Cannot modify a completed transaction");
        }

        // Check transaction number uniqueness if changing
        if (!existingTransaction.getTransactionNumber().equals(transactionCreateDTO.getTransactionNumber()) &&
                !isTransactionNumberUnique(transactionCreateDTO.getTransactionNumber())) {
            throw new IllegalArgumentException("Transaction number already exists: " + transactionCreateDTO.getTransactionNumber());
        }

        // Save old status to detect changes
        TransactionStatus oldStatus = existingTransaction.getStatus();

        // Update transaction fields
        existingTransaction.setTransactionNumber(transactionCreateDTO.getTransactionNumber());
        existingTransaction.setTransactionDate(transactionCreateDTO.getTransactionDate());
        existingTransaction.setTransactionType(transactionCreateDTO.getTransactionType());
        existingTransaction.setStatus(transactionCreateDTO.getStatus());
        existingTransaction.setQuantity(transactionCreateDTO.getQuantity());
        existingTransaction.setUnitPrice(transactionCreateDTO.getUnitPrice());
        existingTransaction.setReferenceNumber(transactionCreateDTO.getReferenceNumber());
        existingTransaction.setNotes(transactionCreateDTO.getNotes());

        // Update product if changed
        if (!existingTransaction.getProduct().getId().equals(transactionCreateDTO.getProductId())) {
            Product product = productRepository.findById(transactionCreateDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + transactionCreateDTO.getProductId()));
            existingTransaction.setProduct(product);
        }

        // Update warehouse if changed
        if (!existingTransaction.getWarehouse().getId().equals(transactionCreateDTO.getWarehouseId())) {
            Warehouse warehouse = warehouseRepository.findById(transactionCreateDTO.getWarehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + transactionCreateDTO.getWarehouseId()));
            existingTransaction.setWarehouse(warehouse);
        }

        // Handle transfer transaction
        if (TransactionType.TRANSFER.equals(transactionCreateDTO.getTransactionType())) {
            if (transactionCreateDTO.getSourceWarehouseId() != null) {
                Warehouse sourceWarehouse = warehouseRepository.findById(transactionCreateDTO.getSourceWarehouseId())
                        .orElseThrow(() -> new ResourceNotFoundException("Source warehouse not found with id: " +
                                transactionCreateDTO.getSourceWarehouseId()));
                existingTransaction.setSourceWarehouse(sourceWarehouse);
            }

            if (transactionCreateDTO.getDestinationWarehouseId() != null) {
                Warehouse destinationWarehouse = warehouseRepository.findById(transactionCreateDTO.getDestinationWarehouseId())
                        .orElseThrow(() -> new ResourceNotFoundException("Destination warehouse not found with id: " +
                                transactionCreateDTO.getDestinationWarehouseId()));
                existingTransaction.setDestinationWarehouse(destinationWarehouse);
            }
        }

        // Save the updated transaction
        Transaction updatedTransaction = transactionRepository.save(existingTransaction);

        // Handle inventory updates if status changed to or from COMPLETED
        if (oldStatus != TransactionStatus.COMPLETED && updatedTransaction.getStatus() == TransactionStatus.COMPLETED) {
            // Transaction is now completed, update inventory
            updateInventory(updatedTransaction);
        } else if (oldStatus == TransactionStatus.COMPLETED && updatedTransaction.getStatus() != TransactionStatus.COMPLETED) {
            // Transaction was completed but now isn't, reverse inventory changes
            reverseInventoryChanges(updatedTransaction);
        } else if (oldStatus == TransactionStatus.COMPLETED && updatedTransaction.getStatus() == TransactionStatus.COMPLETED) {
            // Transaction was and still is completed, but may have changed details
            reverseInventoryChanges(updatedTransaction);
            updateInventory(updatedTransaction);
        }

        return transactionMapper.toDto(updatedTransaction);
    }

    @Override
    @Transactional
    public TransactionDTO updateTransactionStatus(Long id, TransactionStatus status) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        // Save old status to detect changes
        TransactionStatus oldStatus = transaction.getStatus();

        // Update status
        transaction.setStatus(status);

        // Save the updated transaction
        Transaction updatedTransaction = transactionRepository.save(transaction);

        // Handle inventory updates if status changed to or from COMPLETED
        if (oldStatus != TransactionStatus.COMPLETED && status == TransactionStatus.COMPLETED) {
            // Transaction is now completed, update inventory
            updateInventory(updatedTransaction);
        } else if (oldStatus == TransactionStatus.COMPLETED && status != TransactionStatus.COMPLETED) {
            // Transaction was completed but now isn't, reverse inventory changes
            reverseInventoryChanges(updatedTransaction);
        }

        return transactionMapper.toDto(updatedTransaction);
    }

    @Override
    @Transactional
    public void deleteTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        // Reverse inventory changes if transaction was completed
        if (TransactionStatus.COMPLETED.equals(transaction.getStatus())) {
            reverseInventoryChanges(transaction);
        }

        transactionRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDTO> getRecentTransactions(int limit) {
        Pageable pageable = Pageable.ofSize(limit);
        return transactionRepository.findAll(pageable).stream()
                .map(transactionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getSalesTotal(LocalDateTime startDate, LocalDateTime endDate) {
        List<Transaction> salesTransactions = transactionRepository.findByDateRangeAndType(
                startDate, endDate, TransactionType.SALE);

        return salesTransactions.stream()
                .filter(t -> TransactionStatus.COMPLETED.equals(t.getStatus()))
                .map(t -> t.getUnitPrice().multiply(new BigDecimal(t.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getPurchasesTotal(LocalDateTime startDate, LocalDateTime endDate) {
        List<Transaction> purchaseTransactions = transactionRepository.findByDateRangeAndType(
                startDate, endDate, TransactionType.PURCHASE);

        return purchaseTransactions.stream()
                .filter(t -> TransactionStatus.COMPLETED.equals(t.getStatus()))
                .map(t -> t.getUnitPrice().multiply(new BigDecimal(t.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getMonthlySales(int year) {
        Map<String, BigDecimal> monthlySales = new LinkedHashMap<>();

        for (int month = 1; month <= 12; month++) {
            LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
            LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);

            BigDecimal sales = getSalesTotal(startOfMonth, endOfMonth);
            monthlySales.put(Month.of(month).toString(), sales);
        }

        return monthlySales;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> getMonthlyPurchases(int year) {
        Map<String, BigDecimal> monthlyPurchases = new LinkedHashMap<>();

        for (int month = 1; month <= 12; month++) {
            LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
            LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);

            BigDecimal purchases = getPurchasesTotal(startOfMonth, endOfMonth);
            monthlyPurchases.put(Month.of(month).toString(), purchases);
        }

        return monthlyPurchases;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTransactionNumberUnique(String transactionNumber) {
        return !transactionRepository.existsByTransactionNumber(transactionNumber);
    }

    @Override
    public String generateTransactionNumber(TransactionType type) {
        // Generate a unique transaction number based on type, date, and a random number
        String prefix;
        switch (type) {
            case PURCHASE:
                prefix = "PO";
                break;
            case SALE:
                prefix = "SO";
                break;
            case ADJUSTMENT:
                prefix = "ADJ";
                break;
            case TRANSFER:
                prefix = "TRF";
                break;
            default:
                prefix = "TRX";
        }

        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String randomPart = String.format("%04d", new Random().nextInt(10000));

        String transactionNumber = prefix + "-" + datePart + "-" + randomPart;

        // Ensure uniqueness
        if (!isTransactionNumberUnique(transactionNumber)) {
            return generateTransactionNumber(type); // Recursively try again if not unique
        }

        return transactionNumber;
    }

    /**
     * Update inventory based on the transaction type and details
     */
    private void updateInventory(Transaction transaction) {
        Product product = transaction.getProduct();

        switch (transaction.getTransactionType()) {
            case PURCHASE:
                // Increase inventory in the destination warehouse
                updateWarehouseInventory(product, transaction.getWarehouse(), transaction.getQuantity());
                break;

            case SALE:
                // Decrease inventory in the source warehouse
                updateWarehouseInventory(product, transaction.getWarehouse(), -transaction.getQuantity());
                break;

            case ADJUSTMENT:
                // Adjust inventory (can be positive or negative)
                updateWarehouseInventory(product, transaction.getWarehouse(), transaction.getQuantity());
                break;

            case TRANSFER:
                if (transaction.getSourceWarehouse() != null && transaction.getDestinationWarehouse() != null) {
                    // Decrease in source warehouse
                    updateWarehouseInventory(product, transaction.getSourceWarehouse(), -transaction.getQuantity());
                    // Increase in destination warehouse
                    updateWarehouseInventory(product, transaction.getDestinationWarehouse(), transaction.getQuantity());
                }
                break;
        }
    }

    /**
     * Reverse inventory changes made by a completed transaction
     */
    private void reverseInventoryChanges(Transaction transaction) {
        Product product = transaction.getProduct();

        switch (transaction.getTransactionType()) {
            case PURCHASE:
                // Decrease inventory in the destination warehouse
                updateWarehouseInventory(product, transaction.getWarehouse(), -transaction.getQuantity());
                break;

            case SALE:
                // Increase inventory in the source warehouse
                updateWarehouseInventory(product, transaction.getWarehouse(), transaction.getQuantity());
                break;

            case ADJUSTMENT:
                // Reverse adjustment (invert the sign)
                updateWarehouseInventory(product, transaction.getWarehouse(), -transaction.getQuantity());
                break;

            case TRANSFER:
                if (transaction.getSourceWarehouse() != null && transaction.getDestinationWarehouse() != null) {
                    // Increase in source warehouse
                    updateWarehouseInventory(product, transaction.getSourceWarehouse(), transaction.getQuantity());
                    // Decrease in destination warehouse
                    updateWarehouseInventory(product, transaction.getDestinationWarehouse(), -transaction.getQuantity());
                }
                break;
        }
    }

    /**
     * Update inventory quantity for a product in a specific warehouse
     */
    private void updateWarehouseInventory(Product product, Warehouse warehouse, int quantityChange) {
        // Find existing inventory or create a new one
        Optional<Inventory> inventoryOptional = inventoryRepository.findByProductIdAndWarehouseId(
                product.getId(), warehouse.getId());

        Inventory inventory;
        if (inventoryOptional.isPresent()) {
            inventory = inventoryOptional.get();
            // Calculate new quantity, ensuring it doesn't go below zero
            int newQuantity = Math.max(0, inventory.getQuantity() + quantityChange);
            inventory.setQuantity(newQuantity);
        } else if (quantityChange > 0) {
            // Only create new inventory record if adding stock (not for negative adjustments)
            inventory = new Inventory();
            inventory.setProduct(product);
            inventory.setWarehouse(warehouse);
            inventory.setQuantity(quantityChange);
        } else {
            // Cannot remove from non-existent inventory
            return;
        }

        // Save inventory
        inventoryRepository.save(inventory);

        // Update product total stock
        updateProductStock(product);
    }

    /**
     * Update the total stock count on the product entity
     */
    private void updateProductStock(Product product) {
        Integer totalStock = inventoryRepository.getTotalQuantityByProductId(product.getId());
        product.setUnitsInStock(totalStock != null ? totalStock : 0);
        productRepository.save(product);
    }
}