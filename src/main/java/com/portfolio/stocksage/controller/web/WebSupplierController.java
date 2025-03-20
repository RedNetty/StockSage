package com.portfolio.stocksage.controller.web;

import com.portfolio.stocksage.dto.request.SupplierCreateDTO;
import com.portfolio.stocksage.dto.response.ProductDTO;
import com.portfolio.stocksage.dto.response.SupplierDTO;
import com.portfolio.stocksage.service.ProductService;
import com.portfolio.stocksage.service.SupplierService;
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
@RequestMapping("/suppliers")
@RequiredArgsConstructor
public class WebSupplierController {

    private final SupplierService supplierService;
    private final ProductService productService;

    @GetMapping
    public String getAllSuppliers(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String dir) {

        // Create pageable request
        Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sort));

        // Get suppliers
        Page<SupplierDTO> suppliers = supplierService.getAllSuppliers(pageRequest);

        // Add attributes to model
        model.addAttribute("suppliers", suppliers);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", suppliers.getTotalPages());
        model.addAttribute("totalItems", suppliers.getTotalElements());
        model.addAttribute("sortField", sort);
        model.addAttribute("sortDir", dir);
        model.addAttribute("reverseSortDir", "asc".equals(dir) ? "desc" : "asc");

        return "supplier/list";
    }

    @GetMapping("/{id}")
    public String getSupplierDetails(@PathVariable Long id, Model model) {
        SupplierDTO supplier = supplierService.getSupplierById(id);
        model.addAttribute("supplier", supplier);
        return "supplier/details";
    }

    @GetMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String showCreateForm(Model model) {
        model.addAttribute("supplier", new SupplierCreateDTO());
        return "supplier/create";
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String createSupplier(
            @Valid @ModelAttribute("supplier") SupplierCreateDTO supplier,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        // Check for validation errors
        if (result.hasErrors()) {
            return "supplier/create";
        }

        // Check if name is unique
        if (!supplierService.isSupplierNameUnique(supplier.getName())) {
            result.rejectValue("name", "error.supplier", "Supplier name must be unique");
            return "supplier/create";
        }

        // Create supplier
        SupplierDTO savedSupplier = supplierService.createSupplier(supplier);

        // Add success message
        redirectAttributes.addFlashAttribute("successMessage", "Supplier created successfully");

        return "redirect:/suppliers/" + savedSupplier.getId();
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String showEditForm(@PathVariable Long id, Model model) {
        SupplierDTO supplier = supplierService.getSupplierById(id);

        // Convert to SupplierCreateDTO for the form
        SupplierCreateDTO supplierForm = new SupplierCreateDTO();
        supplierForm.setName(supplier.getName());
        supplierForm.setContactName(supplier.getContactName());
        supplierForm.setEmail(supplier.getEmail());
        supplierForm.setPhone(supplier.getPhone());
        supplierForm.setAddress(supplier.getAddress());
        supplierForm.setTaxId(supplier.getTaxId());
        supplierForm.setNotes(supplier.getNotes());
        supplierForm.setActive(supplier.isActive());

        model.addAttribute("supplier", supplierForm);
        model.addAttribute("supplierId", id);

        return "supplier/edit";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String updateSupplier(
            @PathVariable Long id,
            @Valid @ModelAttribute("supplier") SupplierCreateDTO supplier,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Check for validation errors
        if (result.hasErrors()) {
            model.addAttribute("supplierId", id);
            return "supplier/edit";
        }

        // Check if name is being changed and if the new name is already taken
        SupplierDTO existingSupplier = supplierService.getSupplierById(id);
        if (!existingSupplier.getName().equals(supplier.getName()) &&
                !supplierService.isSupplierNameUnique(supplier.getName())) {
            result.rejectValue("name", "error.supplier", "Supplier name must be unique");
            model.addAttribute("supplierId", id);
            return "supplier/edit";
        }

        // Update supplier
        SupplierDTO updatedSupplier = supplierService.updateSupplier(id, supplier);

        // Add success message
        redirectAttributes.addFlashAttribute("successMessage", "Supplier updated successfully");

        return "redirect:/suppliers/" + updatedSupplier.getId();
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showDeleteConfirmation(@PathVariable Long id, Model model) {
        SupplierDTO supplier = supplierService.getSupplierById(id);
        model.addAttribute("supplier", supplier);
        return "supplier/delete";
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteSupplier(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            supplierService.deleteSupplier(id);
            redirectAttributes.addFlashAttribute("successMessage", "Supplier deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/suppliers";
    }

    @GetMapping("/search")
    public String searchSuppliers(@RequestParam String keyword, Model model) {
        List<SupplierDTO> suppliers = supplierService.searchSuppliers(keyword);
        model.addAttribute("suppliers", suppliers);
        model.addAttribute("searchTerm", keyword);
        return "supplier/search-results";
    }

    @GetMapping("/{id}/products")
    public String getSupplierProducts(@PathVariable Long id, Model model) {
        SupplierDTO supplier = supplierService.getSupplierById(id);
        model.addAttribute("supplier", supplier);
        return "supplier/products";
    }

    @GetMapping("/{id}/add-product")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String showAddProductForm(@PathVariable Long id, Model model) {
        SupplierDTO supplier = supplierService.getSupplierById(id);
        model.addAttribute("supplier", supplier);

        // Get products not already associated with this supplier
        Page<ProductDTO> allProducts = productService.getAllProducts(null);
        List<ProductDTO> availableProducts = allProducts.getContent().stream()
                .filter(p -> supplier.getProducts().stream()
                        .noneMatch(sp -> sp.getId().equals(p.getId())))
                .toList();

        model.addAttribute("products", availableProducts);

        return "supplier/add-product";
    }

    @PostMapping("/{id}/add-product")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String addProductToSupplier(
            @PathVariable Long id,
            @RequestParam Long productId,
            RedirectAttributes redirectAttributes) {

        supplierService.addProductToSupplier(id, productId);
        redirectAttributes.addFlashAttribute("successMessage", "Product added to supplier successfully");

        return "redirect:/suppliers/" + id + "/products";
    }

    @PostMapping("/{supplierId}/remove-product/{productId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String removeProductFromSupplier(
            @PathVariable Long supplierId,
            @PathVariable Long productId,
            RedirectAttributes redirectAttributes) {

        supplierService.removeProductFromSupplier(supplierId, productId);
        redirectAttributes.addFlashAttribute("successMessage", "Product removed from supplier successfully");

        return "redirect:/suppliers/" + supplierId + "/products";
    }

    @GetMapping("/check-name")
    @ResponseBody
    public boolean checkNameUnique(@RequestParam String name, @RequestParam(required = false) Long id) {
        // If ID is provided, we're checking for an update operation
        if (id != null) {
            SupplierDTO existing = supplierService.getSupplierById(id);
            // If the name hasn't changed, it's valid
            if (existing.getName().equals(name)) {
                return true;
            }
        }
        return supplierService.isSupplierNameUnique(name);
    }
}