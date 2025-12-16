package com.portfolio.stocksage.util;

import com.opencsv.CSVWriter;
import com.portfolio.stocksage.dto.response.InventoryDTO;
import com.portfolio.stocksage.dto.response.ProductDTO;
import com.portfolio.stocksage.dto.response.SupplierDTO;
import com.portfolio.stocksage.dto.response.TransactionDTO;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility for exporting data to CSV format
 */
@Slf4j
public class CsvExporter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private CsvExporter() {
        // Private constructor to prevent instantiation
        throw new AssertionError("CsvExporter is a utility class and should not be instantiated");
    }

    /**
     * Export products to CSV
     *
     * @param products List of products to export
     * @return CSV data as byte array
     */
    public static byte[] exportProducts(List<ProductDTO> products) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             CSVWriter csvWriter = new CSVWriter(writer)) {

            // Write header
            String[] header = {"ID", "SKU", "Name", "Description", "Unit Price", "Category", "Stock", "Status", "Created At", "Updated At"};
            csvWriter.writeNext(header);

            // Write data rows
            for (ProductDTO product : products) {
                String[] row = {
                        product.getId().toString(),
                        product.getSku(),
                        product.getName(),
                        product.getDescription() != null ? product.getDescription() : "",
                        product.getUnitPrice().toString(),
                        product.getCategory().getName(),
                        product.getUnitsInStock().toString(),
                        product.isActive() ? "Active" : "Inactive",
                        product.getCreatedAt() != null ? product.getCreatedAt().format(DATE_FORMATTER) : "",
                        product.getUpdatedAt() != null ? product.getUpdatedAt().format(DATE_FORMATTER) : ""
                };
                csvWriter.writeNext(row);
            }

            csvWriter.flush();
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Error generating CSV for products", e);
            throw new RuntimeException("Failed to generate CSV for products", e);
        }
    }

    /**
     * Export inventory to CSV
     *
     * @param inventory List of inventory items to export
     * @return CSV data as byte array
     */
    public static byte[] exportInventory(List<InventoryDTO> inventory) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             CSVWriter csvWriter = new CSVWriter(writer)) {

            // Write header
            String[] header = {"ID", "Product SKU", "Product Name", "Warehouse", "Quantity", "Unit Price", "Total Value", "Created At", "Updated At"};
            csvWriter.writeNext(header);

            // Write data rows
            for (InventoryDTO item : inventory) {
                String[] row = {
                        item.getId().toString(),
                        item.getProduct().getSku(),
                        item.getProduct().getName(),
                        item.getWarehouse().getName(),
                        item.getQuantity().toString(),
                        item.getProduct().getUnitPrice().toString(),
                        item.getTotalValue().toString(),
                        item.getCreatedAt() != null ? item.getCreatedAt().format(DATE_FORMATTER) : "",
                        item.getUpdatedAt() != null ? item.getUpdatedAt().format(DATE_FORMATTER) : ""
                };
                csvWriter.writeNext(row);
            }

            csvWriter.flush();
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Error generating CSV for inventory", e);
            throw new RuntimeException("Failed to generate CSV for inventory", e);
        }
    }

    /**
     * Export transactions to CSV
     *
     * @param transactions List of transactions to export
     * @return CSV data as byte array
     */
    public static byte[] exportTransactions(List<TransactionDTO> transactions) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             CSVWriter csvWriter = new CSVWriter(writer)) {

            // Write header
            String[] header = {
                    "ID", "Transaction Number", "Date", "Type", "Status", "Product SKU", "Product Name",
                    "Quantity", "Unit Price", "Total Amount", "Warehouse", "Reference Number",
                    "Created By", "Created At", "Updated At"
            };
            csvWriter.writeNext(header);

            // Write data rows
            for (TransactionDTO transaction : transactions) {
                String[] row = {
                        transaction.getId().toString(),
                        transaction.getTransactionNumber(),
                        transaction.getTransactionDate().format(DATE_FORMATTER),
                        transaction.getTransactionType().toString(),
                        transaction.getStatus().toString(),
                        transaction.getProduct().getSku(),
                        transaction.getProduct().getName(),
                        transaction.getQuantity().toString(),
                        transaction.getUnitPrice().toString(),
                        transaction.getTotalAmount().toString(),
                        transaction.getWarehouse().getName(),
                        transaction.getReferenceNumber() != null ? transaction.getReferenceNumber() : "",
                        transaction.getCreatedBy().getFullName(),
                        transaction.getCreatedAt() != null ? transaction.getCreatedAt().format(DATE_FORMATTER) : "",
                        transaction.getUpdatedAt() != null ? transaction.getUpdatedAt().format(DATE_FORMATTER) : ""
                };
                csvWriter.writeNext(row);
            }

            csvWriter.flush();
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Error generating CSV for transactions", e);
            throw new RuntimeException("Failed to generate CSV for transactions", e);
        }
    }

    /**
     * Export suppliers to CSV
     *
     * @param suppliers List of suppliers to export
     * @return CSV data as byte array
     */
    public static byte[] exportSuppliers(List<SupplierDTO> suppliers) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             CSVWriter csvWriter = new CSVWriter(writer)) {

            // Write header
            String[] header = {
                    "ID", "Name", "Contact Name", "Email", "Phone", "Address", "Tax ID",
                    "Notes", "Status", "Product Count", "Created At", "Updated At"
            };
            csvWriter.writeNext(header);

            // Write data rows
            for (SupplierDTO supplier : suppliers) {
                String[] row = {
                        supplier.getId().toString(),
                        supplier.getName(),
                        supplier.getContactName() != null ? supplier.getContactName() : "",
                        supplier.getEmail() != null ? supplier.getEmail() : "",
                        supplier.getPhone() != null ? supplier.getPhone() : "",
                        supplier.getAddress() != null ? supplier.getAddress() : "",
                        supplier.getTaxId() != null ? supplier.getTaxId() : "",
                        supplier.getNotes() != null ? supplier.getNotes() : "",
                        supplier.isActive() ? "Active" : "Inactive",
                        String.valueOf(supplier.getProductCount()),
                        supplier.getCreatedAt() != null ? supplier.getCreatedAt().format(DATE_FORMATTER) : "",
                        supplier.getUpdatedAt() != null ? supplier.getUpdatedAt().format(DATE_FORMATTER) : ""
                };
                csvWriter.writeNext(row);
            }

            csvWriter.flush();
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Error generating CSV for suppliers", e);
            throw new RuntimeException("Failed to generate CSV for suppliers", e);
        }
    }
}