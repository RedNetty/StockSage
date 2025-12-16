package com.portfolio.stocksage.export;

import com.portfolio.stocksage.dto.response.InventoryDTO;
import com.portfolio.stocksage.dto.response.ProductDTO;
import com.portfolio.stocksage.dto.response.SupplierDTO;
import com.portfolio.stocksage.dto.response.TransactionDTO;
import com.portfolio.stocksage.util.PdfExporter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * Service for exporting data to PDF format
 */
@Service
@Slf4j
public class PdfExportService implements ExportStrategy {

    @Override
    public byte[] exportProducts(List<ProductDTO> products) throws IOException {
        log.info("Exporting {} products to PDF", products.size());
        return PdfExporter.exportProducts(products);
    }

    @Override
    public byte[] exportInventory(List<InventoryDTO> inventory) throws IOException {
        log.info("Exporting {} inventory items to PDF", inventory.size());
        return PdfExporter.exportInventory(inventory);
    }

    @Override
    public byte[] exportTransactions(List<TransactionDTO> transactions) throws IOException {
        log.info("Exporting {} transactions to PDF", transactions.size());
        return PdfExporter.exportTransactions(transactions);
    }

    @Override
    public byte[] exportSuppliers(List<SupplierDTO> suppliers) throws IOException {
        log.info("Exporting {} suppliers to PDF", suppliers.size());
        return PdfExporter.exportSuppliers(suppliers);
    }
}