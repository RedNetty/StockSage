package com.portfolio.stocksage.service;

import com.portfolio.stocksage.dto.request.ProductCreateDTO;
import com.portfolio.stocksage.dto.response.ProductDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

    ProductDTO createProduct(ProductCreateDTO productCreateDTO);

    ProductDTO getProductById(Long id);

    ProductDTO getProductBySku(String sku);

    Page<ProductDTO> getAllProducts(Pageable pageable);

    Page<ProductDTO> getActiveProducts(Pageable pageable);

    Page<ProductDTO> searchProducts(String keyword, Pageable pageable);

    ProductDTO updateProduct(Long id, ProductCreateDTO productCreateDTO);

    void deleteProduct(Long id);

    Page<ProductDTO> getProductsByCategory(Long categoryId, Pageable pageable);

    List<ProductDTO> getLowStockProducts(int minStock);

    Page<ProductDTO> getProductsByWarehouse(Long warehouseId, Pageable pageable);

    Page<ProductDTO> getProductsBySupplier(Long supplierId, Pageable pageable);

    boolean isSkuUnique(String sku);
}