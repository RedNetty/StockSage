package com.portfolio.stocksage.controller.web;

import com.portfolio.stocksage.dto.request.CategoryCreateDTO;
import com.portfolio.stocksage.dto.response.CategoryDTO;
import com.portfolio.stocksage.service.CategoryService;
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
@RequestMapping("/categories")
@RequiredArgsConstructor
public class WebCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public String getAllCategories(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String dir) {

        // Create pageable request
        Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sort));

        // Get categories
        Page<CategoryDTO> categories = categoryService.getAllCategories(pageRequest);

        // Get root categories for sidebar navigation
        List<CategoryDTO> rootCategories = categoryService.getRootCategories();

        // Add attributes to model
        model.addAttribute("categories", categories);
        model.addAttribute("rootCategories", rootCategories);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", categories.getTotalPages());
        model.addAttribute("totalItems", categories.getTotalElements());
        model.addAttribute("sortField", sort);
        model.addAttribute("sortDir", dir);
        model.addAttribute("reverseSortDir", "asc".equals(dir) ? "desc" : "asc");

        return "category/list";
    }

    @GetMapping("/{id}")
    public String getCategoryDetails(@PathVariable Long id, Model model) {
        CategoryDTO category = categoryService.getCategoryWithHierarchy(id);
        model.addAttribute("category", category);

        // Get product count for category
        long productCount = categoryService.getProductCount(id);
        model.addAttribute("productCount", productCount);

        // Get subcategories
        List<CategoryDTO> subcategories = categoryService.getSubcategories(id);
        model.addAttribute("subcategories", subcategories);

        return "category/details";
    }

    @GetMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String showCreateForm(Model model) {
        model.addAttribute("category", new CategoryCreateDTO());

        // Get all categories for parent selection
        Page<CategoryDTO> allCategories = categoryService.getAllCategories(null);
        model.addAttribute("allCategories", allCategories.getContent());

        return "category/create";
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String createCategory(
            @Valid @ModelAttribute("category") CategoryCreateDTO category,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Check for validation errors
        if (result.hasErrors()) {
            Page<CategoryDTO> allCategories = categoryService.getAllCategories(null);
            model.addAttribute("allCategories", allCategories.getContent());
            return "category/create";
        }

        // Check if name is unique
        if (!categoryService.isCategoryNameUnique(category.getName())) {
            result.rejectValue("name", "error.category", "Category name must be unique");
            Page<CategoryDTO> allCategories = categoryService.getAllCategories(null);
            model.addAttribute("allCategories", allCategories.getContent());
            return "category/create";
        }

        // Create category
        CategoryDTO savedCategory = categoryService.createCategory(category);

        // Add success message
        redirectAttributes.addFlashAttribute("successMessage", "Category created successfully");

        return "redirect:/categories/" + savedCategory.getId();
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String showEditForm(@PathVariable Long id, Model model) {
        CategoryDTO category = categoryService.getCategoryById(id);

        // Convert to CategoryCreateDTO for the form
        CategoryCreateDTO categoryForm = new CategoryCreateDTO();
        categoryForm.setName(category.getName());
        categoryForm.setDescription(category.getDescription());
        categoryForm.setActive(category.isActive());
        if (category.getParent() != null) {
            categoryForm.setParentId(category.getParent().getId());
        }

        model.addAttribute("category", categoryForm);
        model.addAttribute("categoryId", id);

        // Get all categories for parent selection (excluding the current one and its children)
        Page<CategoryDTO> allCategories = categoryService.getAllCategories(null);
        model.addAttribute("allCategories", allCategories.getContent().stream()
                .filter(c -> !c.getId().equals(id))
                .toList());

        return "category/edit";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    public String updateCategory(
            @PathVariable Long id,
            @Valid @ModelAttribute("category") CategoryCreateDTO category,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Check for validation errors
        if (result.hasErrors()) {
            model.addAttribute("categoryId", id);
            Page<CategoryDTO> allCategories = categoryService.getAllCategories(null);
            model.addAttribute("allCategories", allCategories.getContent().stream()
                    .filter(c -> !c.getId().equals(id))
                    .toList());
            return "category/edit";
        }

        // Update category
        try {
            CategoryDTO updatedCategory = categoryService.updateCategory(id, category);
            redirectAttributes.addFlashAttribute("successMessage", "Category updated successfully");
            return "redirect:/categories/" + updatedCategory.getId();
        } catch (IllegalArgumentException e) {
            // Handle errors like circular references
            result.rejectValue("parentId", "error.category", e.getMessage());
            model.addAttribute("categoryId", id);
            Page<CategoryDTO> allCategories = categoryService.getAllCategories(null);
            model.addAttribute("allCategories", allCategories.getContent().stream()
                    .filter(c -> !c.getId().equals(id))
                    .toList());
            return "category/edit";
        }
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showDeleteConfirmation(@PathVariable Long id, Model model) {
        CategoryDTO category = categoryService.getCategoryById(id);
        model.addAttribute("category", category);

        // Check if category has products
        long productCount = categoryService.getProductCount(id);
        model.addAttribute("productCount", productCount);

        // Check if category has subcategories
        List<CategoryDTO> subcategories = categoryService.getSubcategories(id);
        model.addAttribute("hasSubcategories", !subcategories.isEmpty());

        return "category/delete";
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteCategory(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Category deleted successfully");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/categories";
    }

    @GetMapping("/tree")
    public String getCategoryTree(Model model) {
        List<CategoryDTO> rootCategories = categoryService.getRootCategories();
        model.addAttribute("rootCategories", rootCategories);
        return "category/tree";
    }

    @GetMapping("/check-name")
    @ResponseBody
    public boolean checkNameUnique(@RequestParam String name, @RequestParam(required = false) Long id) {
        // If ID is provided, we're checking for an update operation
        if (id != null) {
            CategoryDTO existing = categoryService.getCategoryById(id);
            // If the name hasn't changed, it's valid
            if (existing.getName().equals(name)) {
                return true;
            }
        }
        return categoryService.isCategoryNameUnique(name);
    }
}