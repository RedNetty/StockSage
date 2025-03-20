package com.portfolio.stocksage.export;

import com.portfolio.stocksage.dto.response.InventoryDTO;
import com.portfolio.stocksage.dto.response.ProductDTO;
import com.portfolio.stocksage.dto.response.SupplierDTO;
import com.portfolio.stocksage.dto.response.TransactionDTO;

import java.io.IOException;
import java.util.List;

public interface ExportStrategy {

    byte[] exportProducts(List<ProductDTO> products) throws IOException;

    byte[] exportInventory(List<InventoryDTO> inventory) throws IOException;

    byte[] exportTransactions(List<TransactionDTO> transactions) throws IOException;

    byte[] exportSuppliers(List<SupplierDTO> suppliers) throws IOException;
}