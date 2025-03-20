
package com.portfolio.stocksage.service.impl;

import com.portfolio.stocksage.dto.mapper.SupplierMapper;
import com.portfolio.stocksage.dto.request.SupplierCreateDTO;
import com.portfolio.stocksage.dto.response.SupplierDTO;
import com.portfolio.stocksage.entity.Product;
import com.portfolio.stocksage.entity.Supplier;
import com.portfolio.stocksage.exception.ResourceNotFoundException;
import com.portfolio.stocksage.repository.ProductRepository;
import com.portfolio.stocksage.repository.SupplierRepository;
import com.portfolio.stocksage.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final SupplierMapper supplierMapper;

    @Override
    @Transactional
    public SupplierDTO createSupplier(SupplierCreateDTO supplierCreateDTO) {
        if (supplierRepository.existsByName(supplierCreateDTO.getName())) {
            throw new IllegalArgumentException("Supplier with name " + supplierCreateDTO.getName() + " already exists");
        }

        Supplier supplier = supplierMapper.toEntity(supplierCreateDTO);
        Supplier savedSupplier = supplierRepository.save(supplier);

        return supplierMapper.toDto(savedSupplier);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierDTO getSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        return supplierMapper.toDto(supplier);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierDTO getSupplierByName(String name) {
        Supplier supplier = supplierRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with name: " + name));

        return supplierMapper.toDto(supplier);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupplierDTO> getAllSuppliers(Pageable pageable) {
        return supplierRepository.findAll(pageable)
                .map(supplierMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupplierDTO> getActiveSuppliers(Pageable pageable) {
        return supplierRepository.findByActive(true, pageable)
                .map(supplierMapper::toDto);
    }

    @Override
    @Transactional
    public SupplierDTO updateSupplier(Long id, SupplierCreateDTO supplierCreateDTO) {
        Supplier existingSupplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        // Check if name is being changed and if the new name already exists
        if (!existingSupplier.getName().equals(supplierCreateDTO.getName()) &&
                supplierRepository.existsByName(supplierCreateDTO.getName())) {
            throw new IllegalArgumentException("Supplier with name " + supplierCreateDTO.getName() + " already exists");
        }

        // Update the supplier fields
        supplierMapper.updateEntityFromDto(supplierCreateDTO, existingSupplier);

        Supplier updatedSupplier = supplierRepository.save(existingSupplier);
        return supplierMapper.toDto(updatedSupplier);
    }

    @Override
    @Transactional
    public void deleteSupplier(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        // Remove associations with products to avoid orphaned relationships
        for (Product product : supplier.getProducts()) {
            product.getSuppliers().remove(supplier);
            productRepository.save(product);
        }

        supplierRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierDTO> searchSuppliers(String keyword) {
        return supplierRepository.findByNameContainingIgnoreCase(keyword).stream()
                .map(supplierMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addProductToSupplier(Long supplierId, Long productId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + supplierId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        product.addSupplier(supplier);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void removeProductFromSupplier(Long supplierId, Long productId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + supplierId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        product.removeSupplier(supplier);
        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSupplierNameUnique(String name) {
        return !supplierRepository.existsByName(name);
    }
}