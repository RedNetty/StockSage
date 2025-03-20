package com.portfolio.stocksage.service.impl;

import com.portfolio.stocksage.dto.mapper.InventoryMapper;
import com.portfolio.stocksage.dto.request.InventoryCreateDTO;
import com.portfolio.stocksage.dto.response.InventoryDTO;
import com.portfolio.stocksage.entity.Inventory;
import com.portfolio.stocksage.entity.Product;
import com.portfolio.stocksage.entity.Warehouse;
import com.portfolio.stocksage.exception.ResourceNotFoundException;
import com.portfolio.stocksage.repository.InventoryRepository;
import com.portfolio.stocksage.repository.ProductRepository;
import com.portfolio.stocksage.repository.WarehouseRepository;
import com.portfolio.stocksage.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryMapper inventoryMapper;

    @Override
    @Transactional
    public InventoryDTO createInventory(InventoryCreateDTO inventoryCreateDTO) {
        Product product = productRepository.findById(inventoryCreateDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + inventoryCreateDTO.getProductId()));

        Warehouse warehouse = warehouseRepository.findById(inventoryCreateDTO.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + inventoryCreateDTO.getWarehouseId()));

        // Check if inventory already exists for this product-warehouse combination
        Optional<Inventory> existingInventory = inventoryRepository.findByProductIdAndWarehouseId(
                inventoryCreateDTO.getProductId(), inventoryCreateDTO.getWarehouseId());

        if (existingInventory.isPresent()) {
            throw new IllegalArgumentException("Inventory already exists for this product in the specified warehouse. Use update instead.");
        }

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setWarehouse(warehouse);
        inventory.setQuantity(inventoryCreateDTO.getQuantity());

        Inventory savedInventory = inventoryRepository.save(inventory);

        // Update product total stock
        updateProductStock(product);

        return inventoryMapper.toDto(savedInventory);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryDTO getInventoryById(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + id));

        return inventoryMapper.toDto(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryDTO getInventoryByProductAndWarehouse(Long productId, Long warehouseId) {
        Inventory inventory = inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product id: " +
                        productId + " and warehouse id: " + warehouseId));

        return inventoryMapper.toDto(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryDTO> getAllInventory(Pageable pageable) {
        return inventoryRepository.findAll(pageable)
                .map(inventoryMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryDTO> getInventoryByWarehouse(Long warehouseId, Pageable pageable) {
        if (!warehouseRepository.existsById(warehouseId)) {
            throw new ResourceNotFoundException("Warehouse not found with id: " + warehouseId);
        }

        Page<Inventory> inventoryPage = inventoryRepository.findAll(pageable);
        return inventoryPage
                .map(inventoryMapper::toDto)
                .filter(inventory -> inventory.getWarehouse().getId().equals(warehouseId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryDTO> getInventoryByCategory(Long categoryId, Pageable pageable) {
        return inventoryRepository.findByCategoryId(categoryId, pageable)
                .map(inventoryMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDTO> getInventoryByProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        return inventoryRepository.findByProductId(productId).stream()
                .map(inventoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InventoryDTO updateInventory(Long id, InventoryCreateDTO inventoryCreateDTO) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + id));

        // If product or warehouse is being changed, check if there's already an inventory record for the new combination
        if (!inventory.getProduct().getId().equals(inventoryCreateDTO.getProductId()) ||
                !inventory.getWarehouse().getId().equals(inventoryCreateDTO.getWarehouseId())) {

            Optional<Inventory> existingInventory = inventoryRepository.findByProductIdAndWarehouseId(
                    inventoryCreateDTO.getProductId(), inventoryCreateDTO.getWarehouseId());

            if (existingInventory.isPresent() && !existingInventory.get().getId().equals(id)) {
                throw new IllegalArgumentException("Inventory already exists for this product in the specified warehouse.");
            }

            // Get the new product and warehouse if they're being changed
            if (!inventory.getProduct().getId().equals(inventoryCreateDTO.getProductId())) {
                Product product = productRepository.findById(inventoryCreateDTO.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + inventoryCreateDTO.getProductId()));

                // Save the old product to update its stock later
                Product oldProduct = inventory.getProduct();

                inventory.setProduct(product);

                // Update both products' stock counts
                updateProductStock(oldProduct);
                updateProductStock(product);
            }

            if (!inventory.getWarehouse().getId().equals(inventoryCreateDTO.getWarehouseId())) {
                Warehouse warehouse = warehouseRepository.findById(inventoryCreateDTO.getWarehouseId())
                        .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + inventoryCreateDTO.getWarehouseId()));
                inventory.setWarehouse(warehouse);
            }
        } else {
            // If the product is the same, just update its stock count if quantity changed
            if (inventory.getQuantity() != inventoryCreateDTO.getQuantity()) {
                Product product = inventory.getProduct();
                inventory.setQuantity(inventoryCreateDTO.getQuantity());
                updateProductStock(product);
            } else {
                // Just update the quantity
                inventory.setQuantity(inventoryCreateDTO.getQuantity());
            }
        }

        Inventory updatedInventory = inventoryRepository.save(inventory);

        return inventoryMapper.toDto(updatedInventory);
    }

    @Override
    @Transactional
    public void deleteInventory(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + id));

        Product product = inventory.getProduct();

        inventoryRepository.deleteById(id);

        // Update product total stock
        updateProductStock(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDTO> getLowInventory(int threshold) {
        return inventoryRepository.findLowInventory(threshold).stream()
                .map(inventoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDTO> getOutOfStockItems() {
        return inventoryRepository.findOutOfStockItems().stream()
                .map(inventoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Integer> getTopStockedProducts(int limit) {
        List<Object[]> results = inventoryRepository.findTopStockedProducts(limit);

        Map<String, Integer> topProducts = new HashMap<>();
        for (Object[] result : results) {
            String productName = (String) result[0];
            Integer totalQuantity = ((Number) result[1]).intValue();
            topProducts.put(productName, totalQuantity);
        }

        return topProducts;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getTotalQuantityByProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        Integer totalQuantity = inventoryRepository.getTotalQuantityByProductId(productId);
        return totalQuantity != null ? totalQuantity : 0;
    }

    @Override
    @Transactional
    public void adjustInventory(Long productId, Long warehouseId, Integer quantityChange) {
        Inventory inventory = inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product id: " +
                        productId + " and warehouse id: " + warehouseId));

        // Calculate new quantity, ensuring it doesn't go below zero
        int newQuantity = Math.max(0, inventory.getQuantity() + quantityChange);
        inventory.setQuantity(newQuantity);

        inventoryRepository.save(inventory);

        // Update product total stock
        updateProductStock(inventory.getProduct());
    }

    // Helper method to update the total stock count on the product entity
    private void updateProductStock(Product product) {
        Integer totalStock = inventoryRepository.getTotalQuantityByProductId(product.getId());
        product.setUnitsInStock(totalStock != null ? totalStock : 0);
        productRepository.save(product);
    }
}