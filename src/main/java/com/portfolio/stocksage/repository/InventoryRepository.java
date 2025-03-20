package com.portfolio.stocksage.repository;

import com.portfolio.stocksage.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    List<Inventory> findByProductId(Long productId);

    List<Inventory> findByWarehouseId(Long warehouseId);

    @Query("SELECT i FROM Inventory i WHERE i.quantity <= :threshold")
    List<Inventory> findLowInventory(@Param("threshold") int threshold);

    @Query("SELECT SUM(i.quantity) FROM Inventory i WHERE i.product.id = :productId")
    Integer getTotalQuantityByProductId(@Param("productId") Long productId);

    @Query(value = "SELECT i.* FROM inventory i JOIN products p ON i.product_id = p.id WHERE p.category_id = :categoryId", nativeQuery = true)
    Page<Inventory> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT i FROM Inventory i WHERE i.quantity = 0")
    List<Inventory> findOutOfStockItems();

    @Query(value = "SELECT p.name, SUM(i.quantity) as total FROM inventory i JOIN products p ON i.product_id = p.id GROUP BY p.name ORDER BY total DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> findTopStockedProducts(@Param("limit") int limit);
}