package com.portfolio.stocksage.controller.api;

import com.portfolio.stocksage.dto.request.WarehouseCreateDTO;
import com.portfolio.stocksage.dto.response.WarehouseDTO;
import com.portfolio.stocksage.service.WarehouseService;
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

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
@Tag(name = "Warehouse API", description = "Endpoints for managing warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    @Operation(summary = "Create a new warehouse", description = "Creates a new warehouse location")
    public ResponseEntity<WarehouseDTO> createWarehouse(@Valid @RequestBody WarehouseCreateDTO warehouseCreateDTO) {
        WarehouseDTO createdWarehouse = warehouseService.createWarehouse(warehouseCreateDTO);
        return new ResponseEntity<>(createdWarehouse, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get warehouse by ID", description = "Returns a warehouse based on the provided ID")
    public ResponseEntity<WarehouseDTO> getWarehouseById(
            @Parameter(description = "Warehouse ID", required = true)
            @PathVariable Long id) {
        WarehouseDTO warehouse = warehouseService.getWarehouseById(id);
        return ResponseEntity.ok(warehouse);
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Get warehouse by name", description = "Returns a warehouse based on the provided name")
    public ResponseEntity<WarehouseDTO> getWarehouseByName(
            @Parameter(description = "Warehouse name", required = true)
            @PathVariable String name) {
        WarehouseDTO warehouse = warehouseService.getWarehouseByName(name);
        return ResponseEntity.ok(warehouse);
    }

    @GetMapping
    @Operation(summary = "Get all warehouses", description = "Returns a paginated list of all warehouses")
    public ResponseEntity<Page<WarehouseDTO>> getAllWarehouses(
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

        Page<WarehouseDTO> warehouses = warehouseService.getAllWarehouses(pageable);
        return ResponseEntity.ok(warehouses);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active warehouses", description = "Returns a paginated list of active warehouses")
    public ResponseEntity<Page<WarehouseDTO>> getActiveWarehouses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<WarehouseDTO> warehouses = warehouseService.getActiveWarehouses(pageable);
        return ResponseEntity.ok(warehouses);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    @Operation(summary = "Update a warehouse", description = "Updates an existing warehouse with the provided information")
    public ResponseEntity<WarehouseDTO> updateWarehouse(
            @Parameter(description = "Warehouse ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody WarehouseCreateDTO warehouseCreateDTO) {

        WarehouseDTO updatedWarehouse = warehouseService.updateWarehouse(id, warehouseCreateDTO);
        return ResponseEntity.ok(updatedWarehouse);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a warehouse", description = "Deletes a warehouse with the specified ID")
    public ResponseEntity<Void> deleteWarehouse(
            @Parameter(description = "Warehouse ID", required = true)
            @PathVariable Long id) {

        warehouseService.deleteWarehouse(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/location")
    @Operation(summary = "Find warehouses by location", description = "Returns warehouses matching the location search term")
    public ResponseEntity<List<WarehouseDTO>> findByLocation(
            @Parameter(description = "Location search term", required = true)
            @RequestParam String location) {

        List<WarehouseDTO> warehouses = warehouseService.findByLocation(location);
        return ResponseEntity.ok(warehouses);
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get warehouses with product", description = "Returns warehouses that have the specified product in stock")
    public ResponseEntity<List<WarehouseDTO>> getWarehousesWithProduct(
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long productId) {

        List<WarehouseDTO> warehouses = warehouseService.getWarehousesWithProduct(productId);
        return ResponseEntity.ok(warehouses);
    }

    @GetMapping("/check-name")
    @Operation(summary = "Check if warehouse name is unique", description = "Returns whether the provided name is available")
    public ResponseEntity<Boolean> isWarehouseNameUnique(
            @Parameter(description = "Warehouse name to check", required = true)
            @RequestParam String name) {

        boolean isUnique = warehouseService.isWarehouseNameUnique(name);
        return ResponseEntity.ok(isUnique);
    }

    @GetMapping("/{id}/stats")
    @Operation(summary = "Get warehouse with inventory statistics", description = "Returns warehouse information with inventory statistics")
    public ResponseEntity<WarehouseDTO> getWarehouseWithInventoryStats(
            @Parameter(description = "Warehouse ID", required = true)
            @PathVariable Long id) {

        WarehouseDTO warehouse = warehouseService.getWarehouseWithInventoryStats(id);
        return ResponseEntity.ok(warehouse);
    }
}