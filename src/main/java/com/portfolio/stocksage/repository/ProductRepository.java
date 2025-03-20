package com.portfolio.stocksage.repository;

import com.portfolio.stocksage.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    Page<Product> findByActive(boolean active, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword% OR p.sku LIKE %:keyword% OR p.description LIKE %:keyword%")
    Page<Product> search(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId")
    Page<Product> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.unitsInStock < :minStock")
    List<Product> findLowStockProducts(@Param("minStock") int minStock);

    @Query(value = "SELECT p.* FROM products p JOIN inventory i ON p.id = i.product_id WHERE i.warehouse_id = :warehouseId", nativeQuery = true)
    Page<Product> findByWarehouseId(@Param("warehouseId") Long warehouseId, Pageable pageable);

    @Query("SELECT p FROM Product p JOIN p.suppliers s WHERE s.id = :supplierId")
    Page<Product> findBySupplierId(@Param("supplierId") Long supplierId, Pageable pageable);
}