package com.portfolio.stocksage.controller.api;

import com.portfolio.stocksage.dto.request.SupplierCreateDTO;
import com.portfolio.stocksage.dto.response.SupplierDTO;
import com.portfolio.stocksage.service.SupplierService;
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
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
@Tag(name = "Supplier API", description = "Endpoints for managing suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    @Operation(summary = "Create a new supplier", description = "Creates a new supplier with the provided information")
    public ResponseEntity<SupplierDTO> createSupplier(@Valid @RequestBody SupplierCreateDTO supplierCreateDTO) {
        SupplierDTO createdSupplier = supplierService.createSupplier(supplierCreateDTO);
        return new ResponseEntity<>(createdSupplier, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get supplier by ID", description = "Returns a supplier based on the provided ID")
    public ResponseEntity<SupplierDTO> getSupplierById(
            @Parameter(description = "Supplier ID", required = true)
            @PathVariable Long id) {
        SupplierDTO supplier = supplierService.getSupplierById(id);
        return ResponseEntity.ok(supplier);
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Get supplier by name", description = "Returns a supplier based on the provided name")
    public ResponseEntity<SupplierDTO> getSupplierByName(
            @Parameter(description = "Supplier name", required = true)
            @PathVariable String name) {
        SupplierDTO supplier = supplierService.getSupplierByName(name);
        return ResponseEntity.ok(supplier);
    }

    @GetMapping
    @Operation(summary = "Get all suppliers", description = "Returns a paginated list of all suppliers")
    public ResponseEntity<Page<SupplierDTO>> getAllSuppliers(
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

        Page<SupplierDTO> suppliers = supplierService.getAllSuppliers(pageable);
        return ResponseEntity.ok(suppliers);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active suppliers", description = "Returns a paginated list of active suppliers")
    public ResponseEntity<Page<SupplierDTO>> getActiveSuppliers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<SupplierDTO> suppliers = supplierService.getActiveSuppliers(pageable);
        return ResponseEntity.ok(suppliers);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    @Operation(summary = "Update a supplier", description = "Updates an existing supplier with the provided information")
    public ResponseEntity<SupplierDTO> updateSupplier(
            @Parameter(description = "Supplier ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody SupplierCreateDTO supplierCreateDTO) {

        SupplierDTO updatedSupplier = supplierService.updateSupplier(id, supplierCreateDTO);
        return ResponseEntity.ok(updatedSupplier);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a supplier", description = "Deletes a supplier with the specified ID")
    public ResponseEntity<Void> deleteSupplier(
            @Parameter(description = "Supplier ID", required = true)
            @PathVariable Long id) {

        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search suppliers", description = "Returns suppliers matching the search criteria")
    public ResponseEntity<List<SupplierDTO>> searchSuppliers(
            @Parameter(description = "Search keyword", required = true)
            @RequestParam String keyword) {

        List<SupplierDTO> suppliers = supplierService.searchSuppliers(keyword);
        return ResponseEntity.ok(suppliers);
    }

    @PostMapping("/{supplierId}/products/{productId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    @Operation(summary = "Add product to supplier", description = "Associates a product with a supplier")
    public ResponseEntity<Void> addProductToSupplier(
            @Parameter(description = "Supplier ID", required = true)
            @PathVariable Long supplierId,
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long productId) {

        supplierService.addProductToSupplier(supplierId, productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{supplierId}/products/{productId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    @Operation(summary = "Remove product from supplier", description = "Removes the association between a product and a supplier")
    public ResponseEntity<Void> removeProductFromSupplier(
            @Parameter(description = "Supplier ID", required = true)
            @PathVariable Long supplierId,
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long productId) {

        supplierService.removeProductFromSupplier(supplierId, productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/check-name")
    @Operation(summary = "Check if supplier name is unique", description = "Returns whether the provided name is available")
    public ResponseEntity<Boolean> isSupplierNameUnique(
            @Parameter(description = "Supplier name to check", required = true)
            @RequestParam String name) {

        boolean isUnique = supplierService.isSupplierNameUnique(name);
        return ResponseEntity.ok(isUnique);
    }
}