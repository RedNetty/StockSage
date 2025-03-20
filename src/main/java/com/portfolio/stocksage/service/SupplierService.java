package com.portfolio.stocksage.service;

import com.portfolio.stocksage.dto.request.SupplierCreateDTO;
import com.portfolio.stocksage.dto.response.SupplierDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SupplierService {

    SupplierDTO createSupplier(SupplierCreateDTO supplierCreateDTO);

    SupplierDTO getSupplierById(Long id);

    SupplierDTO getSupplierByName(String name);

    Page<SupplierDTO> getAllSuppliers(Pageable pageable);

    Page<SupplierDTO> getActiveSuppliers(Pageable pageable);

    SupplierDTO updateSupplier(Long id, SupplierCreateDTO supplierCreateDTO);

    void deleteSupplier(Long id);

    List<SupplierDTO> searchSuppliers(String keyword);

    void addProductToSupplier(Long supplierId, Long productId);

    void removeProductFromSupplier(Long supplierId, Long productId);

    boolean isSupplierNameUnique(String name);
}
