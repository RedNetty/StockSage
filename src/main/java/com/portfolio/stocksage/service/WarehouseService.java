package com.portfolio.stocksage.service;

import com.portfolio.stocksage.dto.request.WarehouseCreateDTO;
import com.portfolio.stocksage.dto.response.WarehouseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WarehouseService {

    WarehouseDTO createWarehouse(WarehouseCreateDTO warehouseCreateDTO);

    WarehouseDTO getWarehouseById(Long id);

    WarehouseDTO getWarehouseByName(String name);

    Page<WarehouseDTO> getAllWarehouses(Pageable pageable);

    Page<WarehouseDTO> getActiveWarehouses(Pageable pageable);

    WarehouseDTO updateWarehouse(Long id, WarehouseCreateDTO warehouseCreateDTO);

    void deleteWarehouse(Long id);

    List<WarehouseDTO> findByLocation(String location);

    List<WarehouseDTO> getWarehousesWithProduct(Long productId);

    boolean isWarehouseNameUnique(String name);

    WarehouseDTO getWarehouseWithInventoryStats(Long id);
}