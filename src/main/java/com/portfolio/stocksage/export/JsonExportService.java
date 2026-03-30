package com.portfolio.stocksage.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.portfolio.stocksage.dto.response.InventoryDTO;
import com.portfolio.stocksage.dto.response.ProductDTO;
import com.portfolio.stocksage.dto.response.SupplierDTO;
import com.portfolio.stocksage.dto.response.TransactionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * Service for exporting data to JSON format
 */
@Service
@Slf4j
public class JsonExportService implements ExportStrategy {

    private final ObjectMapper objectMapper;

    public JsonExportService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public byte[] exportProducts(List<ProductDTO> products) throws IOException {
        log.info("Exporting {} products to JSON", products.size());
        return objectMapper.writeValueAsBytes(products);
    }

    @Override
    public byte[] exportInventory(List<InventoryDTO> inventory) throws IOException {
        log.info("Exporting {} inventory items to JSON", inventory.size());
        return objectMapper.writeValueAsBytes(inventory);
    }

    @Override
    public byte[] exportTransactions(List<TransactionDTO> transactions) throws IOException {
        log.info("Exporting {} transactions to JSON", transactions.size());
        return objectMapper.writeValueAsBytes(transactions);
    }

    @Override
    public byte[] exportSuppliers(List<SupplierDTO> suppliers) throws IOException {
        log.info("Exporting {} suppliers to JSON", suppliers.size());
        return objectMapper.writeValueAsBytes(suppliers);
    }
}
