package com.portfolio.stocksage.controller.web;

import com.portfolio.stocksage.dto.request.ProductCreateDTO;
import com.portfolio.stocksage.dto.response.ProductDTO;
import com.portfolio.stocksage.service.CategoryService;
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

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class WebProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final SupplierService supplierService;

    @GetMapping
    public String getAllProducts(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            @RequestParam(required = false) String search) {

        // Create pageable request
        Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sort));

        // Get products (with or without search)
        Page<ProductDTO> products;
        if (search != null && !search.trim().isEmpty()) {
            products = productService.searchProducts(search, pageRequest);
            model.addAttribute("search", search);
        } else {
            products = productService.getActiveProducts(pageRequest);
        }

        // Add attributes to model
        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("totalItems", products.getTotalElements());
        model.addAttribute("sortField", sort);
        model.addAttribute("sortDir", dir);
        model.addAttribute("reverseSortDir", "asc".equals(dir) ? "desc" : "asc");

        return "product/list";
    }

    @GetMapping("/{id}")
    public String getProductDetails(@PathVariable Long id, Model model) {
        ProductDTO product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "product/details";
    }

    @GetMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new ProductCreateDTO());
        model.addAttribute("categories", categoryService.getAllCategories(null));
        model.addAttribute("suppliers", supplierService.getAllSuppliers(null));
        return "product/create";
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String createProduct(
            @Valid @ModelAttribute("product") ProductCreateDTO product,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Check for validation errors
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories(null));
            model.addAttribute("suppliers", supplierService.getAllSuppliers(null));
            return "product/create";
        }

        // Check if SKU is unique
        if (!productService.isSkuUnique(product.getSku())) {
            result.rejectValue("sku", "error.product", "SKU must be unique");
            model.addAttribute("categories", categoryService.getAllCategories(null));
            model.addAttribute("suppliers", supplierService.getAllSuppliers(null));
            return "product/create";
        }

        // Create product
        ProductDTO savedProduct = productService.createProduct(product);

        // Add success message
        redirectAttributes.addFlashAttribute("successMessage", "Product created successfully");

        return "redirect:/products/" + savedProduct.getId();
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String showEditForm(@PathVariable Long id, Model model) {
        ProductDTO product = productService.getProductById(id);

        // Convert to ProductCreateDTO for the form
        ProductCreateDTO productForm = new ProductCreateDTO();
        productForm.setSku(product.getSku());
        productForm.setName(product.getName());
        productForm.setDescription(product.getDescription());
        productForm.setUnitPrice(product.getUnitPrice());
        productForm.setCategoryId(product.getCategory().getId());
        productForm.setImageUrl(product.getImageUrl());
        productForm.setActive(product.isActive());
        productForm.setUnitsInStock(product.getUnitsInStock());

        model.addAttribute("product", productForm);
        model.addAttribute("productId", id);
        model.addAttribute("categories", categoryService.getAllCategories(null));
        model.addAttribute("suppliers", supplierService.getAllSuppliers(null));

        return "product/edit";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String updateProduct(
            @PathVariable Long id,
            @Valid @ModelAttribute("product") ProductCreateDTO product,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Check for validation errors
        if (result.hasErrors()) {
            model.addAttribute("productId", id);
            model.addAttribute("categories", categoryService.getAllCategories(null));
            model.addAttribute("suppliers", supplierService.getAllSuppliers(null));
            return "product/edit";
        }

        // Update product
        ProductDTO updatedProduct = productService.updateProduct(id, product);

        // Add success message
        redirectAttributes.addFlashAttribute("successMessage", "Product updated successfully");

        return "redirect:/products/" + updatedProduct.getId();
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showDeleteConfirmation(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.getProductById(id));
        return "product/delete";
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteProduct(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        productService.deleteProduct(id);

        // Add success message
        redirectAttributes.addFlashAttribute("successMessage", "Product deleted successfully");

        return "redirect:/products";
    }

    @GetMapping("/category/{categoryId}")
    public String getProductsByCategory(
            @PathVariable Long categoryId,
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<ProductDTO> products = productService.getProductsByCategory(categoryId, pageRequest);

        model.addAttribute("products", products);
        model.addAttribute("category", categoryService.getCategoryById(categoryId));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("totalItems", products.getTotalElements());

        return "product/by-category";
    }
}