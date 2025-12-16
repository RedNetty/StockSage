package com.portfolio.stocksage.export;

import com.portfolio.stocksage.dto.response.InventoryDTO;
import com.portfolio.stocksage.dto.response.ProductDTO;
import com.portfolio.stocksage.dto.response.SupplierDTO;
import com.portfolio.stocksage.dto.response.TransactionDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelExportService implements ExportStrategy {

    @Override
    public byte[] exportProducts(List<ProductDTO> products) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Products");

            // Create header row
            Row headerRow = sheet.createRow(0);
            createHeaderCell(workbook, headerRow, 0, "ID");
            createHeaderCell(workbook, headerRow, 1, "SKU");
            createHeaderCell(workbook, headerRow, 2, "Name");
            createHeaderCell(workbook, headerRow, 3, "Description");
            createHeaderCell(workbook, headerRow, 4, "Unit Price");
            createHeaderCell(workbook, headerRow, 5, "Category");
            createHeaderCell(workbook, headerRow, 6, "Stock");
            createHeaderCell(workbook, headerRow, 7, "Status");

            // Create data rows
            int rowNum = 1;
            for (ProductDTO product : products) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(product.getId());
                row.createCell(1).setCellValue(product.getSku());
                row.createCell(2).setCellValue(product.getName());
                row.createCell(3).setCellValue(product.getDescription() != null ? product.getDescription() : "");
                row.createCell(4).setCellValue(product.getUnitPrice().doubleValue());
                row.createCell(5).setCellValue(product.getCategory().getName());
                row.createCell(6).setCellValue(product.getUnitsInStock());
                row.createCell(7).setCellValue(product.isActive() ? "Active" : "Inactive");
            }

            // Adjust column width
            for (int i = 0; i < 8; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to output stream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    @Override
    public byte[] exportInventory(List<InventoryDTO> inventory) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Inventory");

            // Create header row
            Row headerRow = sheet.createRow(0);
            createHeaderCell(workbook, headerRow, 0, "ID");
            createHeaderCell(workbook, headerRow, 1, "Product SKU");
            createHeaderCell(workbook, headerRow, 2, "Product Name");
            createHeaderCell(workbook, headerRow, 3, "Warehouse");
            createHeaderCell(workbook, headerRow, 4, "Quantity");
            createHeaderCell(workbook, headerRow, 5, "Unit Price");
            createHeaderCell(workbook, headerRow, 6, "Total Value");

            // Create data rows
            int rowNum = 1;
            for (InventoryDTO item : inventory) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(item.getId());
                row.createCell(1).setCellValue(item.getProduct().getSku());
                row.createCell(2).setCellValue(item.getProduct().getName());
                row.createCell(3).setCellValue(item.getWarehouse().getName());
                row.createCell(4).setCellValue(item.getQuantity());
                row.createCell(5).setCellValue(item.getProduct().getUnitPrice().doubleValue());
                row.createCell(6).setCellValue(item.getTotalValue().doubleValue());
            }

            // Adjust column width
            for (int i = 0; i < 7; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to output stream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    @Override
    public byte[] exportTransactions(List<TransactionDTO> transactions) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Transactions");

            // Create header row
            Row headerRow = sheet.createRow(0);
            createHeaderCell(workbook, headerRow, 0, "ID");
            createHeaderCell(workbook, headerRow, 1, "Transaction #");
            createHeaderCell(workbook, headerRow, 2, "Date");
            createHeaderCell(workbook, headerRow, 3, "Type");
            createHeaderCell(workbook, headerRow, 4, "Status");
            createHeaderCell(workbook, headerRow, 5, "Product");
            createHeaderCell(workbook, headerRow, 6, "Quantity");
            createHeaderCell(workbook, headerRow, 7, "Unit Price");
            createHeaderCell(workbook, headerRow, 8, "Total Amount");
            createHeaderCell(workbook, headerRow, 9, "Warehouse");
            createHeaderCell(workbook, headerRow, 10, "Created By");

            // Create data rows
            int rowNum = 1;
            for (TransactionDTO transaction : transactions) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(transaction.getId());
                row.createCell(1).setCellValue(transaction.getTransactionNumber());
                row.createCell(2).setCellValue(transaction.getTransactionDate().toString());
                row.createCell(3).setCellValue(transaction.getTransactionType().toString());
                row.createCell(4).setCellValue(transaction.getStatus().toString());
                row.createCell(5).setCellValue(transaction.getProduct().getName());
                row.createCell(6).setCellValue(transaction.getQuantity());
                row.createCell(7).setCellValue(transaction.getUnitPrice().doubleValue());
                row.createCell(8).setCellValue(transaction.getTotalAmount().doubleValue());
                row.createCell(9).setCellValue(transaction.getWarehouse().getName());
                row.createCell(10).setCellValue(transaction.getCreatedBy().getFullName());
            }

            // Adjust column width
            for (int i = 0; i < 11; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to output stream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    @Override
    public byte[] exportSuppliers(List<SupplierDTO> suppliers) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Suppliers");

            // Create header row
            Row headerRow = sheet.createRow(0);
            createHeaderCell(workbook, headerRow, 0, "ID");
            createHeaderCell(workbook, headerRow, 1, "Name");
            createHeaderCell(workbook, headerRow, 2, "Contact Name");
            createHeaderCell(workbook, headerRow, 3, "Email");
            createHeaderCell(workbook, headerRow, 4, "Phone");
            createHeaderCell(workbook, headerRow, 5, "Address");
            createHeaderCell(workbook, headerRow, 6, "Tax ID");
            createHeaderCell(workbook, headerRow, 7, "Status");
            createHeaderCell(workbook, headerRow, 8, "Products Count");

            // Create data rows
            int rowNum = 1;
            for (SupplierDTO supplier : suppliers) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(supplier.getId());
                row.createCell(1).setCellValue(supplier.getName());
                row.createCell(2).setCellValue(supplier.getContactName() != null ? supplier.getContactName() : "");
                row.createCell(3).setCellValue(supplier.getEmail() != null ? supplier.getEmail() : "");
                row.createCell(4).setCellValue(supplier.getPhone() != null ? supplier.getPhone() : "");
                row.createCell(5).setCellValue(supplier.getAddress() != null ? supplier.getAddress() : "");
                row.createCell(6).setCellValue(supplier.getTaxId() != null ? supplier.getTaxId() : "");
                row.createCell(7).setCellValue(supplier.isActive() ? "Active" : "Inactive");
                row.createCell(8).setCellValue(supplier.getProductCount());
            }

            // Adjust column width
            for (int i = 0; i < 9; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to output stream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void createHeaderCell(Workbook workbook, Row row, int column, String value) {
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = ((XSSFWorkbook) workbook).createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
}