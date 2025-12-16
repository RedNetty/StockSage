package com.portfolio.stocksage.repository;

import com.portfolio.stocksage.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findByName(String name);

    boolean existsByName(String name);

    Page<Supplier> findByActive(boolean active, Pageable pageable);

    List<Supplier> findByNameContainingIgnoreCase(String keyword);

    @Query("SELECT s FROM Supplier s JOIN s.products p WHERE p.id = :productId")
    List<Supplier> findByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(p) FROM Supplier s JOIN s.products p WHERE s.id = :supplierId")
    long countProductsBySupplierId(@Param("supplierId") Long supplierId);
}