package com.portfolio.stocksage.service;

import com.portfolio.stocksage.dto.request.InventoryCreateDTO;
import com.portfolio.stocksage.dto.response.InventoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface InventoryService {

    InventoryDTO createInventory(InventoryCreateDTO inventoryCreateDTO);

    InventoryDTO getInventoryById(Long id);

    InventoryDTO getInventoryByProductAndWarehouse(Long productId, Long warehouseId);

    Page<InventoryDTO> getAllInventory(Pageable pageable);

    Page<InventoryDTO> getInventoryByWarehouse(Long warehouseId, Pageable pageable);

    Page<InventoryDTO> getInventoryByCategory(Long categoryId, Pageable pageable);

    List<InventoryDTO> getInventoryByProduct(Long productId);

    InventoryDTO updateInventory(Long id, InventoryCreateDTO inventoryCreateDTO);

    void deleteInventory(Long id);

    List<InventoryDTO> getLowInventory(int threshold);

    List<InventoryDTO> getOutOfStockItems();

    Map<String, Integer> getTopStockedProducts(int limit);

    Integer getTotalQuantityByProduct(Long productId);

    void adjustInventory(Long productId, Long warehouseId, Integer quantityChange);
}
