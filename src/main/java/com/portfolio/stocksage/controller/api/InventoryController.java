package com.portfolio.stocksage.controller.api;

import com.portfolio.stocksage.dto.request.InventoryCreateDTO;
import com.portfolio.stocksage.dto.response.InventoryDTO;
import com.portfolio.stocksage.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory API", description = "Endpoints for managing inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    @Operation(summary = "Create inventory record", description = "Creates a new inventory record for a product in a specific warehouse")
    public ResponseEntity<InventoryDTO> createInventory(@Valid @RequestBody InventoryCreateDTO inventoryCreateDTO) {
        InventoryDTO createdInventory = inventoryService.createInventory(inventoryCreateDTO);
        return new ResponseEntity<>(createdInventory, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get inventory by ID", description = "Returns an inventory record based on the provided ID")
    public ResponseEntity<InventoryDTO> getInventoryById(
            @Parameter(description = "Inventory ID", required = true)
            @PathVariable Long id) {
        InventoryDTO inventory = inventoryService.getInventoryById(id);
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/product/{productId}/warehouse/{warehouseId}")
    @Operation(summary = "Get inventory by product and warehouse", description = "Returns an inventory record for a specific product in a specific warehouse")
    public ResponseEntity<InventoryDTO> getInventoryByProductAndWarehouse(
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long productId,
            @Parameter(description = "Warehouse ID", required = true)
            @PathVariable Long warehouseId) {
        InventoryDTO inventory = inventoryService.getInventoryByProductAndWarehouse(productId, warehouseId);
        return ResponseEntity.ok(inventory);
    }

    @GetMapping
    @Operation(summary = "Get all inventory", description = "Returns a paginated list of all inventory records")
    public ResponseEntity<Page<InventoryDTO>> getAllInventory(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "id") String sort,
            @Parameter(description = "Sort direction (asc or desc)")
            @RequestParam(defaultValue = "asc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<InventoryDTO> inventory = inventoryService.getAllInventory(pageable);
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/warehouse/{warehouseId}")
    @Operation(summary = "Get inventory by warehouse", description = "Returns a paginated list of inventory records for a specific warehouse")
    public ResponseEntity<Page<InventoryDTO>> getInventoryByWarehouse(
            @Parameter(description = "Warehouse ID", required = true)
            @PathVariable Long warehouseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<InventoryDTO> inventory = inventoryService.getInventoryByWarehouse(warehouseId, pageable);
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get inventory by category", description = "Returns a paginated list of inventory records for products in a specific category")
    public ResponseEntity<Page<InventoryDTO>> getInventoryByCategory(
            @Parameter(description = "Category ID", required = true)
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<InventoryDTO> inventory = inventoryService.getInventoryByCategory(categoryId, pageable);
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get inventory by product", description = "Returns a list of inventory records for a specific product across all warehouses")
    public ResponseEntity<List<InventoryDTO>> getInventoryByProduct(
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long productId) {

        List<InventoryDTO> inventory = inventoryService.getInventoryByProduct(productId);
        return ResponseEntity.ok(inventory);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    @Operation(summary = "Update inventory", description = "Updates an existing inventory record with the provided information")
    public ResponseEntity<InventoryDTO> updateInventory(
            @Parameter(description = "Inventory ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody InventoryCreateDTO inventoryCreateDTO) {

        InventoryDTO updatedInventory = inventoryService.updateInventory(id, inventoryCreateDTO);
        return ResponseEntity.ok(updatedInventory);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    @Operation(summary = "Delete inventory", description = "Deletes an inventory record with the specified ID")
    public ResponseEntity<Void> deleteInventory(
            @Parameter(description = "Inventory ID", required = true)
            @PathVariable Long id) {

        inventoryService.deleteInventory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/low-inventory")
    @Operation(summary = "Get low inventory items", description = "Returns inventory items with quantity below the specified threshold")
    public ResponseEntity<List<InventoryDTO>> getLowInventory(
            @Parameter(description = "Threshold quantity")
            @RequestParam(defaultValue = "10") int threshold) {

        List<InventoryDTO> lowInventory = inventoryService.getLowInventory(threshold);
        return ResponseEntity.ok(lowInventory);
    }

    @GetMapping("/out-of-stock")
    @Operation(summary = "Get out of stock items", description = "Returns inventory items with zero quantity")
    public ResponseEntity<List<InventoryDTO>> getOutOfStockItems() {
        List<InventoryDTO> outOfStockItems = inventoryService.getOutOfStockItems();
        return ResponseEntity.ok(outOfStockItems);
    }

    @GetMapping("/top-stocked-products")
    @Operation(summary = "Get top stocked products", description = "Returns the top N products by inventory quantity")
    public ResponseEntity<Map<String, Integer>> getTopStockedProducts(
            @Parameter(description = "Limit (number of products to return)")
            @RequestParam(defaultValue = "5") int limit) {

        Map<String, Integer> topProducts = inventoryService.getTopStockedProducts(limit);
        return ResponseEntity.ok(topProducts);
    }

    @GetMapping("/product/{productId}/total-quantity")
    @Operation(summary = "Get total quantity for product", description = "Returns the total quantity of a product across all warehouses")
    public ResponseEntity<Integer> getTotalQuantityByProduct(
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long productId) {

        Integer totalQuantity = inventoryService.getTotalQuantityByProduct(productId);
        return ResponseEntity.ok(totalQuantity);
    }

    @PostMapping("/adjust/product/{productId}/warehouse/{warehouseId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    @Operation(summary = "Adjust inventory", description = "Adjusts the inventory quantity for a product in a specific warehouse")
    public ResponseEntity<Void> adjustInventory(
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long productId,
            @Parameter(description = "Warehouse ID", required = true)
            @PathVariable Long warehouseId,
            @Parameter(description = "Quantity change (positive or negative)")
            @RequestParam Integer quantityChange) {

        inventoryService.adjustInventory(productId, warehouseId, quantityChange);
        return ResponseEntity.ok().build();
    }
}