
package com.portfolio.stocksage.service.impl;

import com.portfolio.stocksage.export.ExcelExportService;
import com.portfolio.stocksage.export.PdfExportService;
import com.portfolio.stocksage.export.CsvExportService;
import com.portfolio.stocksage.dto.response.InventoryDTO;
import com.portfolio.stocksage.dto.response.ProductDTO;
import com.portfolio.stocksage.dto.response.SupplierDTO;
import com.portfolio.stocksage.dto.response.TransactionDTO;
import com.portfolio.stocksage.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {

    private final ExcelExportService excelExportService;
    private final PdfExportService pdfExportService;
    private final CsvExportService csvExportService;

    @Override
    public byte[] exportProductsToExcel(List<ProductDTO> products) throws IOException {
        return excelExportService.exportProducts(products);
    }

    @Override
    public byte[] exportInventoryToExcel(List<InventoryDTO> inventory) throws IOException {
        return excelExportService.exportInventory(inventory);
    }

    @Override
    public byte[] exportTransactionsToExcel(List<TransactionDTO> transactions) throws IOException {
        return excelExportService.exportTransactions(transactions);
    }

    @Override
    public byte[] exportSuppliersToExcel(List<SupplierDTO> suppliers) throws IOException {
        return excelExportService.exportSuppliers(suppliers);
    }

    @Override
    public byte[] exportProductsToPdf(List<ProductDTO> products) throws IOException {
        return pdfExportService.exportProducts(products);
    }

    @Override
    public byte[] exportInventoryToPdf(List<InventoryDTO> inventory) throws IOException {
        return pdfExportService.exportInventory(inventory);
    }

    @Override
    public byte[] exportTransactionsToPdf(List<TransactionDTO> transactions) throws IOException {
        return pdfExportService.exportTransactions(transactions);
    }

    @Override
    public byte[] exportSuppliersToPdf(List<SupplierDTO> suppliers) throws IOException {
        return pdfExportService.exportSuppliers(suppliers);
    }

    @Override
    public byte[] exportProductsToCsv(List<ProductDTO> products) throws IOException {
        return csvExportService.exportProducts(products);
    }

    @Override
    public byte[] exportInventoryToCsv(List<InventoryDTO> inventory) throws IOException {
        return csvExportService.exportInventory(inventory);
    }

    @Override
    public byte[] exportTransactionsToCsv(List<TransactionDTO> transactions) throws IOException {
        return csvExportService.exportTransactions(transactions);
    }

    @Override
    public byte[] exportSuppliersToCsv(List<SupplierDTO> suppliers) throws IOException {
        return csvExportService.exportSuppliers(suppliers);
    }
}