package com.portfolio.stocksage.service.impl;

import com.portfolio.stocksage.dto.mapper.WarehouseMapper;
import com.portfolio.stocksage.dto.request.WarehouseCreateDTO;
import com.portfolio.stocksage.dto.response.WarehouseDTO;
import com.portfolio.stocksage.entity.Inventory;
import com.portfolio.stocksage.entity.Warehouse;
import com.portfolio.stocksage.exception.ResourceNotFoundException;
import com.portfolio.stocksage.repository.InventoryRepository;
import com.portfolio.stocksage.repository.WarehouseRepository;
import com.portfolio.stocksage.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final InventoryRepository inventoryRepository;
    private final WarehouseMapper warehouseMapper;

    @Override
    @Transactional
    public WarehouseDTO createWarehouse(WarehouseCreateDTO warehouseCreateDTO) {
        if (warehouseRepository.existsByName(warehouseCreateDTO.getName())) {
            throw new IllegalArgumentException("Warehouse with name " + warehouseCreateDTO.getName() + " already exists");
        }

        Warehouse warehouse = warehouseMapper.toEntity(warehouseCreateDTO);
        Warehouse savedWarehouse = warehouseRepository.save(warehouse);

        return warehouseMapper.toDto(savedWarehouse);
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseDTO getWarehouseById(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + id));

        WarehouseDTO dto = warehouseMapper.toDto(warehouse);
        enrichWarehouseWithStats(dto, warehouse);

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseDTO getWarehouseByName(String name) {
        Warehouse warehouse = warehouseRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with name: " + name));

        WarehouseDTO dto = warehouseMapper.toDto(warehouse);
        enrichWarehouseWithStats(dto, warehouse);

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WarehouseDTO> getAllWarehouses(Pageable pageable) {
        return warehouseRepository.findAll(pageable)
                .map(warehouse -> {
                    WarehouseDTO dto = warehouseMapper.toDto(warehouse);
                    enrichWarehouseWithStats(dto, warehouse);
                    return dto;
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WarehouseDTO> getActiveWarehouses(Pageable pageable) {
        return warehouseRepository.findByActive(true, pageable)
                .map(warehouse -> {
                    WarehouseDTO dto = warehouseMapper.toDto(warehouse);
                    enrichWarehouseWithStats(dto, warehouse);
                    return dto;
                });
    }

    @Override
    @Transactional
    public WarehouseDTO updateWarehouse(Long id, WarehouseCreateDTO warehouseCreateDTO) {
        Warehouse existingWarehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + id));

        // Check if name is being changed and if the new name already exists
        if (!existingWarehouse.getName().equals(warehouseCreateDTO.getName()) &&
                warehouseRepository.existsByName(warehouseCreateDTO.getName())) {
            throw new IllegalArgumentException("Warehouse with name " + warehouseCreateDTO.getName() + " already exists");
        }

        // Update the warehouse fields
        warehouseMapper.updateEntityFromDto(warehouseCreateDTO, existingWarehouse);

        Warehouse updatedWarehouse = warehouseRepository.save(existingWarehouse);

        WarehouseDTO dto = warehouseMapper.toDto(updatedWarehouse);
        enrichWarehouseWithStats(dto, updatedWarehouse);

        return dto;
    }

    @Override
    @Transactional
    public void deleteWarehouse(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + id));

        // Check if warehouse has inventory
        List<Inventory> inventories = inventoryRepository.findByWarehouseId(id);
        if (!inventories.isEmpty()) {
            throw new IllegalStateException("Cannot delete warehouse with inventory items. Warehouse has " +
                    inventories.size() + " inventory records.");
        }

        warehouseRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseDTO> findByLocation(String location) {
        return warehouseRepository.findByLocationContaining(location).stream()
                .map(warehouse -> {
                    WarehouseDTO dto = warehouseMapper.toDto(warehouse);
                    enrichWarehouseWithStats(dto, warehouse);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseDTO> getWarehousesWithProduct(Long productId) {
        return warehouseRepository.findWarehousesWithProduct(productId).stream()
                .map(warehouse -> {
                    WarehouseDTO dto = warehouseMapper.toDto(warehouse);
                    enrichWarehouseWithStats(dto, warehouse);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isWarehouseNameUnique(String name) {
        return !warehouseRepository.existsByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseDTO getWarehouseWithInventoryStats(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + id));

        WarehouseDTO dto = warehouseMapper.toDto(warehouse);
        enrichWarehouseWithStats(dto, warehouse);

        return dto;
    }

    private void enrichWarehouseWithStats(WarehouseDTO dto, Warehouse warehouse) {
        // Get inventory statistics
        List<Inventory> inventories = inventoryRepository.findByWarehouseId(warehouse.getId());

        // Number of distinct products in this warehouse
        dto.setProductCount((int) inventories.stream()
                .map(inventory -> inventory.getProduct().getId())
                .distinct()
                .count());

        // Total quantity of all items in this warehouse
        dto.setTotalItems(inventories.stream()
                .mapToInt(Inventory::getQuantity)
                .sum());
    }
}