package com.portfolio.stocksage.controller.api;

import com.portfolio.stocksage.dto.request.CategoryCreateDTO;
import com.portfolio.stocksage.dto.response.CategoryDTO;
import com.portfolio.stocksage.service.CategoryService;
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
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Category API", description = "Endpoints for managing product categories")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    @Operation(summary = "Create a new category", description = "Creates a new product category")
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryCreateDTO categoryCreateDTO) {
        CategoryDTO createdCategory = categoryService.createCategory(categoryCreateDTO);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Returns a category based on the provided ID")
    public ResponseEntity<CategoryDTO> getCategoryById(
            @Parameter(description = "Category ID", required = true)
            @PathVariable Long id) {
        CategoryDTO category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Get category by name", description = "Returns a category based on the provided name")
    public ResponseEntity<CategoryDTO> getCategoryByName(
            @Parameter(description = "Category name", required = true)
            @PathVariable String name) {
        CategoryDTO category = categoryService.getCategoryByName(name);
        return ResponseEntity.ok(category);
    }

    @GetMapping
    @Operation(summary = "Get all categories", description = "Returns a paginated list of all categories")
    public ResponseEntity<Page<CategoryDTO>> getAllCategories(
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

        Page<CategoryDTO> categories = categoryService.getAllCategories(pageable);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active categories", description = "Returns a paginated list of active categories")
    public ResponseEntity<Page<CategoryDTO>> getActiveCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<CategoryDTO> categories = categoryService.getActiveCategories(pageable);
        return ResponseEntity.ok(categories);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    @Operation(summary = "Update a category", description = "Updates an existing category with the provided information")
    public ResponseEntity<CategoryDTO> updateCategory(
            @Parameter(description = "Category ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody CategoryCreateDTO categoryCreateDTO) {

        CategoryDTO updatedCategory = categoryService.updateCategory(id, categoryCreateDTO);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a category", description = "Deletes a category with the specified ID")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Category ID", required = true)
            @PathVariable Long id) {

        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/root")
    @Operation(summary = "Get root categories", description = "Returns all top-level categories (with no parent)")
    public ResponseEntity<List<CategoryDTO>> getRootCategories() {
        List<CategoryDTO> rootCategories = categoryService.getRootCategories();
        return ResponseEntity.ok(rootCategories);
    }

    @GetMapping("/{id}/subcategories")
    @Operation(summary = "Get subcategories", description = "Returns all subcategories of the specified category")
    public ResponseEntity<List<CategoryDTO>> getSubcategories(
            @Parameter(description = "Parent category ID", required = true)
            @PathVariable Long id) {

        List<CategoryDTO> subcategories = categoryService.getSubcategories(id);
        return ResponseEntity.ok(subcategories);
    }

    @GetMapping("/check-name")
    @Operation(summary = "Check if category name is unique", description = "Returns whether the provided name is available")
    public ResponseEntity<Boolean> isCategoryNameUnique(
            @Parameter(description = "Category name to check", required = true)
            @RequestParam String name) {

        boolean isUnique = categoryService.isCategoryNameUnique(name);
        return ResponseEntity.ok(isUnique);
    }

    @GetMapping("/{id}/product-count")
    @Operation(summary = "Get product count", description = "Returns the number of products in the specified category")
    public ResponseEntity<Long> getProductCount(
            @Parameter(description = "Category ID", required = true)
            @PathVariable Long id) {

        long productCount = categoryService.getProductCount(id);
        return ResponseEntity.ok(productCount);
    }

    @GetMapping("/{id}/hierarchy")
    @Operation(summary = "Get category with hierarchy", description = "Returns a category with its parent and subcategories")
    public ResponseEntity<CategoryDTO> getCategoryWithHierarchy(
            @Parameter(description = "Category ID", required = true)
            @PathVariable Long id) {

        CategoryDTO category = categoryService.getCategoryWithHierarchy(id);
        return ResponseEntity.ok(category);
    }
}