package com.portfolio.stocksage.service;

import com.portfolio.stocksage.dto.request.CategoryCreateDTO;
import com.portfolio.stocksage.dto.response.CategoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {

    CategoryDTO createCategory(CategoryCreateDTO categoryCreateDTO);

    CategoryDTO getCategoryById(Long id);

    CategoryDTO getCategoryByName(String name);

    Page<CategoryDTO> getAllCategories(Pageable pageable);

    Page<CategoryDTO> getActiveCategories(Pageable pageable);

    CategoryDTO updateCategory(Long id, CategoryCreateDTO categoryCreateDTO);

    void deleteCategory(Long id);

    List<CategoryDTO> getRootCategories();

    List<CategoryDTO> getSubcategories(Long parentId);

    boolean isCategoryNameUnique(String name);

    long getProductCount(Long categoryId);

    CategoryDTO getCategoryWithHierarchy(Long id);
}
