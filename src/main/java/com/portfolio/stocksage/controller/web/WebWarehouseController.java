package com.portfolio.stocksage.controller.web;

import com.portfolio.stocksage.dto.request.WarehouseCreateDTO;
import com.portfolio.stocksage.dto.response.WarehouseDTO;
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
@RequestMapping("/warehouses")
@RequiredArgsConstructor
public class WebWarehouseController {

    private final WarehouseService warehouseService;

    @GetMapping
    public String getAllWarehouses(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String dir) {

        // Create pageable request
        Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sort));

        // Get warehouses
        Page<WarehouseDTO> warehouses = warehouseService.getAllWarehouses(pageRequest);

        // Add attributes to model
        model.addAttribute("warehouses", warehouses);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", warehouses.getTotalPages());
        model.addAttribute("totalItems", warehouses.getTotalElements());
        model.addAttribute("sortField", sort);
        model.addAttribute("sortDir", dir);
        model.addAttribute("reverseSortDir", "asc".equals(dir) ? "desc" : "asc");

        return "warehouse/list";
    }

    @GetMapping("/{id}")
    public String getWarehouseDetails(@PathVariable Long id, Model model) {
        WarehouseDTO warehouse = warehouseService.getWarehouseWithInventoryStats(id);
        model.addAttribute("warehouse", warehouse);
        return "warehouse/details";
    }

    @GetMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String showCreateForm(Model model) {
        model.addAttribute("warehouse", new WarehouseCreateDTO());
        return "warehouse/create";
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String createWarehouse(
            @Valid @ModelAttribute("warehouse") WarehouseCreateDTO warehouse,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        // Check for validation errors
        if (result.hasErrors()) {
            return "warehouse/create";
        }

        // Check if name is unique
        if (!warehouseService.isWarehouseNameUnique(warehouse.getName())) {
            result.rejectValue("name", "error.warehouse", "Warehouse name must be unique");
            return "warehouse/create";
        }

        // Create warehouse
        WarehouseDTO savedWarehouse = warehouseService.createWarehouse(warehouse);

        // Add success message
        redirectAttributes.addFlashAttribute("successMessage", "Warehouse created successfully");

        return "redirect:/warehouses/" + savedWarehouse.getId();
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String showEditForm(@PathVariable Long id, Model model) {
        WarehouseDTO warehouse = warehouseService.getWarehouseById(id);

        // Convert to WarehouseCreateDTO for the form
        WarehouseCreateDTO warehouseForm = new WarehouseCreateDTO();
        warehouseForm.setName(warehouse.getName());
        warehouseForm.setLocation(warehouse.getLocation());
        warehouseForm.setDescription(warehouse.getDescription());
        warehouseForm.setCapacity(warehouse.getCapacity());
        warehouseForm.setActive(warehouse.getActive());

        model.addAttribute("warehouse", warehouseForm);
        model.addAttribute("warehouseId", id);

        return "warehouse/edit";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String updateWarehouse(
            @PathVariable Long id,
            @Valid @ModelAttribute("warehouse") WarehouseCreateDTO warehouse,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Check for validation errors
        if (result.hasErrors()) {
            model.addAttribute("warehouseId", id);
            return "warehouse/edit";
        }

        // Check if name is being changed and if the new name is already taken
        WarehouseDTO existingWarehouse = warehouseService.getWarehouseById(id);
        if (!existingWarehouse.getName().equals(warehouse.getName()) &&
                !warehouseService.isWarehouseNameUnique(warehouse.getName())) {
            result.rejectValue("name", "error.warehouse", "Warehouse name must be unique");
            model.addAttribute("warehouseId", id);
            return "warehouse/edit";
        }

        // Update warehouse
        WarehouseDTO updatedWarehouse = warehouseService.updateWarehouse(id, warehouse);

        // Add success message
        redirectAttributes.addFlashAttribute("successMessage", "Warehouse updated successfully");

        return "redirect:/warehouses/" + updatedWarehouse.getId();
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showDeleteConfirmation(@PathVariable Long id, Model model) {
        WarehouseDTO warehouse = warehouseService.getWarehouseById(id);
        model.addAttribute("warehouse", warehouse);
        return "warehouse/delete";
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteWarehouse(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            warehouseService.deleteWarehouse(id);
            redirectAttributes.addFlashAttribute("successMessage", "Warehouse deleted successfully");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/warehouses";
    }

    @GetMapping("/search")
    public String searchWarehouses(@RequestParam String location, Model model) {
        List<WarehouseDTO> warehouses = warehouseService.findByLocation(location);
        model.addAttribute("warehouses", warehouses);
        model.addAttribute("searchTerm", location);
        return "warehouse/search-results";
    }

    @GetMapping("/check-name")
    @ResponseBody
    public boolean checkNameUnique(@RequestParam String name, @RequestParam(required = false) Long id) {
        // If ID is provided, we're checking for an update operation
        if (id != null) {
            WarehouseDTO existing = warehouseService.getWarehouseById(id);
            // If the name hasn't changed, it's valid
            if (existing.getName().equals(name)) {
                return true;
            }
        }
        return warehouseService.isWarehouseNameUnique(name);
    }
}