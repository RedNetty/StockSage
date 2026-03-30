package com.portfolio.stocksage.export;

import com.portfolio.stocksage.dto.response.InventoryDTO;
import com.portfolio.stocksage.dto.response.ProductDTO;
import com.portfolio.stocksage.dto.response.SupplierDTO;
import com.portfolio.stocksage.dto.response.TransactionDTO;
import com.portfolio.stocksage.util.CsvExporter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * Service for exporting data to CSV format
 */
@Service
@Slf4j
public class CsvExportService implements ExportStrategy {

    @Override
    public byte[] exportProducts(List<ProductDTO> products) throws IOException {
        log.info("Exporting {} products to CSV", products.size());
        return CsvExporter.exportProducts(products);
    }

    @Override
    public byte[] exportInventory(List<InventoryDTO> inventory) throws IOException {
        log.info("Exporting {} inventory items to CSV", inventory.size());
        return CsvExporter.exportInventory(inventory);
    }

    @Override
    public byte[] exportTransactions(List<TransactionDTO> transactions) throws IOException {
        log.info("Exporting {} transactions to CSV", transactions.size());
        return CsvExporter.exportTransactions(transactions);
    }

    @Override
    public byte[] exportSuppliers(List<SupplierDTO> suppliers) throws IOException {
        log.info("Exporting {} suppliers to CSV", suppliers.size());
        return CsvExporter.exportSuppliers(suppliers);
    }
}
