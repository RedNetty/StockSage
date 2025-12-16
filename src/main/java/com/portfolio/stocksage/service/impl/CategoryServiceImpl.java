package com.portfolio.stocksage.service.impl;

import com.portfolio.stocksage.dto.mapper.CategoryMapper;
import com.portfolio.stocksage.dto.request.CategoryCreateDTO;
import com.portfolio.stocksage.dto.response.CategoryDTO;
import com.portfolio.stocksage.entity.Category;
import com.portfolio.stocksage.exception.ResourceNotFoundException;
import com.portfolio.stocksage.repository.CategoryRepository;
import com.portfolio.stocksage.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryDTO createCategory(CategoryCreateDTO categoryCreateDTO) {
        if (categoryRepository.existsByName(categoryCreateDTO.getName())) {
            throw new IllegalArgumentException("Category with name " + categoryCreateDTO.getName() + " already exists");
        }

        Category category = categoryMapper.toEntity(categoryCreateDTO);

        // Set parent category if provided
        if (categoryCreateDTO.getParentId() != null) {
            Category parentCategory = categoryRepository.findById(categoryCreateDTO.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with id: " + categoryCreateDTO.getParentId()));
            category.setParent(parentCategory);
        }

        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toDto(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return categoryMapper.toDto(category);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryByName(String name) {
        Category category = categoryRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with name: " + name));
        return categoryMapper.toDto(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryDTO> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable)
                .map(category -> {
                    CategoryDTO dto = categoryMapper.toDto(category);
                    dto.setProductCount(categoryRepository.countProductsByCategoryId(category.getId()));
                    return dto;
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryDTO> getActiveCategories(Pageable pageable) {
        return categoryRepository.findByActive(true, pageable)
                .map(category -> {
                    CategoryDTO dto = categoryMapper.toDto(category);
                    dto.setProductCount(categoryRepository.countProductsByCategoryId(category.getId()));
                    return dto;
                });
    }

    @Override
    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryCreateDTO categoryCreateDTO) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        // Check if name is being changed and if the new name already exists
        if (!existingCategory.getName().equals(categoryCreateDTO.getName()) &&
                categoryRepository.existsByName(categoryCreateDTO.getName())) {
            throw new IllegalArgumentException("Category with name " + categoryCreateDTO.getName() + " already exists");
        }

        // Update the category fields
        categoryMapper.updateEntityFromDto(categoryCreateDTO, existingCategory);

        // Update parent if changed (and check for circular references)
        if (categoryCreateDTO.getParentId() != null) {
            if (categoryCreateDTO.getParentId().equals(id)) {
                throw new IllegalArgumentException("Category cannot be its own parent");
            }

            Category parentCategory = categoryRepository.findById(categoryCreateDTO.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with id: " + categoryCreateDTO.getParentId()));

            // Check for circular references in the category hierarchy
            Category current = parentCategory;
            while (current != null) {
                if (current.getId().equals(id)) {
                    throw new IllegalArgumentException("Circular reference detected in category hierarchy");
                }
                current = current.getParent();
            }

            existingCategory.setParent(parentCategory);
        } else {
            existingCategory.setParent(null);
        }

        Category updatedCategory = categoryRepository.save(existingCategory);
        return categoryMapper.toDto(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        // Check if category has products
        long productCount = categoryRepository.countProductsByCategoryId(id);
        if (productCount > 0) {
            throw new IllegalStateException("Cannot delete category with associated products. Category has " + productCount + " products.");
        }

        // Move subcategories to parent category or make them root categories
        if (!category.getSubCategories().isEmpty()) {
            Category parent = category.getParent();
            for (Category subCategory : category.getSubCategories()) {
                subCategory.setParent(parent);
                categoryRepository.save(subCategory);
            }
        }

        categoryRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getRootCategories() {
        return categoryRepository.findAllRootCategories().stream()
                .map(category -> {
                    CategoryDTO dto = categoryMapper.toDto(category);
                    dto.setProductCount(categoryRepository.countProductsByCategoryId(category.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getSubcategories(Long parentId) {
        if (!categoryRepository.existsById(parentId)) {
            throw new ResourceNotFoundException("Parent category not found with id: " + parentId);
        }

        return categoryRepository.findSubcategoriesByParentId(parentId).stream()
                .map(category -> {
                    CategoryDTO dto = categoryMapper.toDto(category);
                    dto.setProductCount(categoryRepository.countProductsByCategoryId(category.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCategoryNameUnique(String name) {
        return !categoryRepository.existsByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public long getProductCount(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category not found with id: " + categoryId);
        }
        return categoryRepository.countProductsByCategoryId(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryWithHierarchy(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        CategoryDTO categoryDTO = categoryMapper.toDto(category);
        categoryDTO.setProductCount(categoryRepository.countProductsByCategoryId(category.getId()));

        // Set parent if exists
        if (category.getParent() != null) {
            categoryDTO.setParent(categoryMapper.toDto(category.getParent()));
        }

        // Set subcategories
        List<CategoryDTO> subCategoryDTOs = categoryRepository.findSubcategoriesByParentId(id).stream()
                .map(subcategory -> {
                    CategoryDTO dto = categoryMapper.toDto(subcategory);
                    dto.setProductCount(categoryRepository.countProductsByCategoryId(subcategory.getId()));
                    return dto;
                })
                .collect(Collectors.toList());

        categoryDTO.setSubCategories(subCategoryDTOs);

        return categoryDTO;
    }
}