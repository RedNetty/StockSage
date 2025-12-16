package com.portfolio.stocksage.controller.web;

import com.portfolio.stocksage.dto.request.InventoryCreateDTO;
import com.portfolio.stocksage.dto.response.InventoryDTO;
import com.portfolio.stocksage.service.InventoryService;
import com.portfolio.stocksage.service.ProductService;
import com.portfolio.stocksage.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class WebInventoryController {

    private final InventoryService inventoryService;
    private final ProductService productService;
    private final WarehouseService warehouseService;

    @GetMapping
    public String getAllInventory(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long categoryId) {

        // Create pageable request
        Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sort));

        // Get inventory based on filters
        Page<InventoryDTO> inventory;
        if (warehouseId != null) {
            inventory = inventoryService.getInventoryByWarehouse(warehouseId, pageRequest);
            model.addAttribute("selectedWarehouse", warehouseService.getWarehouseById(warehouseId));
        } else if (categoryId != null) {
            inventory = inventoryService.getInventoryByCategory(categoryId, pageRequest);
            model.addAttribute("selectedCategory", categoryId);
        } else {
            inventory = inventoryService.getAllInventory(pageRequest);
        }

        // Add attributes to model
        model.addAttribute("inventory", inventory);
        model.addAttribute("warehouses", warehouseService.getAllWarehouses(null));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", inventory.getTotalPages());
        model.addAttribute("totalItems", inventory.getTotalElements());
        model.addAttribute("sortField", sort);
        model.addAttribute("sortDir", dir);
        model.addAttribute("reverseSortDir", "asc".equals(dir) ? "desc" : "asc");
        model.addAttribute("warehouseId", warehouseId);
        model.addAttribute("categoryId", categoryId);

        return "inventory/list";
    }

    @GetMapping("/low")
    public String getLowInventory(Model model, @RequestParam(defaultValue = "10") int threshold) {
        List<InventoryDTO> lowInventory = inventoryService.getLowInventory(threshold);
        model.addAttribute("inventory", lowInventory);
        model.addAttribute("threshold", threshold);
        model.addAttribute("warehouses", warehouseService.getAllWarehouses(null));
        return "inventory/low-inventory";
    }

    @GetMapping("/out-of-stock")
    public String getOutOfStockItems(Model model) {
        List<InventoryDTO> outOfStockItems = inventoryService.getOutOfStockItems();
        model.addAttribute("inventory", outOfStockItems);
        model.addAttribute("warehouses", warehouseService.getAllWarehouses(null));
        return "inventory/out-of-stock";
    }

    @GetMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String showCreateForm(Model model) {
        model.addAttribute("inventory", new InventoryCreateDTO());
        model.addAttribute("products", productService.getAllProducts(null));
        model.addAttribute("warehouses", warehouseService.getAllWarehouses(null));
        return "inventory/create";
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String createInventory(
            @Valid @ModelAttribute("inventory") InventoryCreateDTO inventory,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Check for validation errors
        if (result.hasErrors()) {
            model.addAttribute("products", productService.getAllProducts(null));
            model.addAttribute("warehouses", warehouseService.getAllWarehouses(null));
            return "inventory/create";
        }

        try {
            // Create inventory
            InventoryDTO createdInventory = inventoryService.createInventory(inventory);
            redirectAttributes.addFlashAttribute("successMessage", "Inventory record created successfully");
            return "redirect:/inventory";
        } catch (IllegalArgumentException e) {
            result.rejectValue("productId", "error.inventory", e.getMessage());
            model.addAttribute("products", productService.getAllProducts(null));
            model.addAttribute("warehouses", warehouseService.getAllWarehouses(null));
            return "inventory/create";
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String showEditForm(@PathVariable Long id, Model model) {
        InventoryDTO inventory = inventoryService.getInventoryById(id);

        // Convert to InventoryCreateDTO for the form
        InventoryCreateDTO inventoryForm = new InventoryCreateDTO();
        inventoryForm.setProductId(inventory.getProduct().getId());
        inventoryForm.setWarehouseId(inventory.getWarehouse().getId());
        inventoryForm.setQuantity(inventory.getQuantity());

        model.addAttribute("inventory", inventoryForm);
        model.addAttribute("inventoryId", id);
        model.addAttribute("products", productService.getAllProducts(null));
        model.addAttribute("warehouses", warehouseService.getAllWarehouses(null));

        // Add original data for display purposes
        model.addAttribute("originalInventory", inventory);

        return "inventory/edit";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String updateInventory(
            @PathVariable Long id,
            @Valid @ModelAttribute("inventory") InventoryCreateDTO inventory,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Check for validation errors
        if (result.hasErrors()) {
            model.addAttribute("inventoryId", id);
            model.addAttribute("products", productService.getAllProducts(null));
            model.addAttribute("warehouses", warehouseService.getAllWarehouses(null));
            model.addAttribute("originalInventory", inventoryService.getInventoryById(id));
            return "inventory/edit";
        }

        try {
            // Update inventory
            InventoryDTO updatedInventory = inventoryService.updateInventory(id, inventory);
            redirectAttributes.addFlashAttribute("successMessage", "Inventory record updated successfully");
            return "redirect:/inventory";
        } catch (IllegalArgumentException e) {
            result.rejectValue("productId", "error.inventory", e.getMessage());
            model.addAttribute("inventoryId", id);
            model.addAttribute("products", productService.getAllProducts(null));
            model.addAttribute("warehouses", warehouseService.getAllWarehouses(null));
            model.addAttribute("originalInventory", inventoryService.getInventoryById(id));
            return "inventory/edit";
        }
    }

    @GetMapping("/adjust/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String showAdjustForm(@PathVariable Long id, Model model) {
        InventoryDTO inventory = inventoryService.getInventoryById(id);
        model.addAttribute("inventory", inventory);
        model.addAttribute("inventoryId", id);
        return "inventory/adjust";
    }

    @PostMapping("/adjust/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String adjustInventory(
            @PathVariable Long id,
            @RequestParam Integer quantityChange,
            RedirectAttributes redirectAttributes) {

        InventoryDTO inventory = inventoryService.getInventoryById(id);

        // Call adjust method for inventory
        inventoryService.adjustInventory(
                inventory.getProduct().getId(),
                inventory.getWarehouse().getId(),
                quantityChange);

        redirectAttributes.addFlashAttribute("successMessage",
                "Inventory quantity adjusted by " + quantityChange);

        return "redirect:/inventory";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String showDeleteConfirmation(@PathVariable Long id, Model model) {
        InventoryDTO inventory = inventoryService.getInventoryById(id);
        model.addAttribute("inventory", inventory);
        return "inventory/delete";
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String deleteInventory(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        inventoryService.deleteInventory(id);
        redirectAttributes.addFlashAttribute("successMessage", "Inventory record deleted successfully");
        return "redirect:/inventory";
    }
}