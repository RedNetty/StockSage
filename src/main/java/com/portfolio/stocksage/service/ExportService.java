package com.portfolio.stocksage.service;

import com.portfolio.stocksage.export.ExportStrategy;
import com.portfolio.stocksage.dto.response.InventoryDTO;
import com.portfolio.stocksage.dto.response.ProductDTO;
import com.portfolio.stocksage.dto.response.SupplierDTO;
import com.portfolio.stocksage.dto.response.TransactionDTO;

import java.io.IOException;
import java.util.List;

public interface ExportService {

    byte[] exportProductsToExcel(List<ProductDTO> products) throws IOException;

    byte[] exportInventoryToExcel(List<InventoryDTO> inventory) throws IOException;

    byte[] exportTransactionsToExcel(List<TransactionDTO> transactions) throws IOException;

    byte[] exportSuppliersToExcel(List<SupplierDTO> suppliers) throws IOException;

    byte[] exportProductsToPdf(List<ProductDTO> products) throws IOException;

    byte[] exportInventoryToPdf(List<InventoryDTO> inventory) throws IOException;

    byte[] exportTransactionsToPdf(List<TransactionDTO> transactions) throws IOException;

    byte[] exportSuppliersToPdf(List<SupplierDTO> suppliers) throws IOException;

    byte[] exportProductsToCsv(List<ProductDTO> products) throws IOException;

    byte[] exportInventoryToCsv(List<InventoryDTO> inventory) throws IOException;

    byte[] exportTransactionsToCsv(List<TransactionDTO> transactions) throws IOException;

    byte[] exportSuppliersToCsv(List<SupplierDTO> suppliers) throws IOException;
}