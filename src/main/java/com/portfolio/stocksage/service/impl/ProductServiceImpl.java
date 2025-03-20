package com.portfolio.stocksage.service.impl;

import com.portfolio.stocksage.dto.mapper.ProductMapper;
import com.portfolio.stocksage.dto.request.ProductCreateDTO;
import com.portfolio.stocksage.dto.response.ProductDTO;
import com.portfolio.stocksage.entity.Category;
import com.portfolio.stocksage.entity.Product;
import com.portfolio.stocksage.exception.ResourceNotFoundException;
import com.portfolio.stocksage.repository.CategoryRepository;
import com.portfolio.stocksage.repository.ProductRepository;
import com.portfolio.stocksage.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductDTO createProduct(ProductCreateDTO productCreateDTO) {
        if (productRepository.existsBySku(productCreateDTO.getSku())) {
            throw new IllegalArgumentException("Product with SKU " + productCreateDTO.getSku() + " already exists");
        }

        Product product = productMapper.toEntity(productCreateDTO);

        Category category = categoryRepository.findById(productCreateDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + productCreateDTO.getCategoryId()));
        product.setCategory(category);

        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return productMapper.toDto(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with SKU: " + sku));
        return productMapper.toDto(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(productMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getActiveProducts(Pageable pageable) {
        return productRepository.findByActive(true, pageable)
                .map(productMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> searchProducts(String keyword, Pageable pageable) {
        return productRepository.search(keyword, pageable)
                .map(productMapper::toDto);
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(Long id, ProductCreateDTO productCreateDTO) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        // Check if SKU is being changed and if the new SKU already exists
        if (!existingProduct.getSku().equals(productCreateDTO.getSku()) &&
                productRepository.existsBySku(productCreateDTO.getSku())) {
            throw new IllegalArgumentException("Product with SKU " + productCreateDTO.getSku() + " already exists");
        }

        // Update the product fields
        productMapper.updateEntityFromDto(productCreateDTO, existingProduct);

        // Update category if changed
        if (!existingProduct.getCategory().getId().equals(productCreateDTO.getCategoryId())) {
            Category category = categoryRepository.findById(productCreateDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + productCreateDTO.getCategoryId()));
            existingProduct.setCategory(category);
        }

        Product updatedProduct = productRepository.save(existingProduct);
        return productMapper.toDto(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductsByCategory(Long categoryId, Pageable pageable) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category not found with id: " + categoryId);
        }
        return productRepository.findByCategoryId(categoryId, pageable)
                .map(productMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getLowStockProducts(int minStock) {
        return productRepository.findLowStockProducts(minStock).stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductsByWarehouse(Long warehouseId, Pageable pageable) {
        return productRepository.findByWarehouseId(warehouseId, pageable)
                .map(productMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductsBySupplier(Long supplierId, Pageable pageable) {
        return productRepository.findBySupplierId(supplierId, pageable)
                .map(productMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSkuUnique(String sku) {
        return !productRepository.existsBySku(sku);
    }
}