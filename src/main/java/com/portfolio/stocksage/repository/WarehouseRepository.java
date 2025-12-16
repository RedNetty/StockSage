package com.portfolio.stocksage.repository;

import com.portfolio.stocksage.entity.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    Optional<Warehouse> findByName(String name);

    boolean existsByName(String name);

    Page<Warehouse> findByActive(boolean active, Pageable pageable);

    @Query("SELECT w FROM Warehouse w WHERE w.location LIKE %:location%")
    List<Warehouse> findByLocationContaining(@Param("location") String location);

    @Query(value = "SELECT w.* FROM warehouses w JOIN inventory i ON w.id = i.warehouse_id WHERE i.product_id = :productId", nativeQuery = true)
    List<Warehouse> findWarehousesWithProduct(@Param("productId") Long productId);
}