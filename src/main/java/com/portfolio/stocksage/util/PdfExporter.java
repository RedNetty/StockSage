package com.portfolio.stocksage.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.portfolio.stocksage.dto.response.InventoryDTO;
import com.portfolio.stocksage.dto.response.ProductDTO;
import com.portfolio.stocksage.dto.response.SupplierDTO;
import com.portfolio.stocksage.dto.response.TransactionDTO;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility for exporting data to PDF format
 */
@Slf4j
public class PdfExporter {

    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
    private static final Font SMALL_FONT = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private PdfExporter() {
        // Private constructor to prevent instantiation
        throw new AssertionError("PdfExporter is a utility class and should not be instantiated");
    }

    /**
     * Export products to PDF
     *
     * @param products List of products to export
     * @return PDF document as byte array
     */
    public static byte[] exportProducts(List<ProductDTO> products) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Add title
            Paragraph title = new Paragraph("Products Report", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Add generation date
            Paragraph date = new Paragraph("Generated on: " + LocalDateTime.now().format(DATE_FORMATTER), SMALL_FONT);
            date.setAlignment(Element.ALIGN_RIGHT);
            date.setSpacingAfter(20);
            document.add(date);

            // Create products table
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            // Set column widths
            float[] columnWidths = {0.5f, 1.5f, 2f, 2f, 1f, 1.5f, 1f};
            table.setWidths(columnWidths);

            // Add table headers
            addTableHeader(table, new String[]{"ID", "SKU", "Name", "Category", "Price", "Stock", "Status"});

            // Add table rows
            for (ProductDTO product : products) {
                table.addCell(new Phrase(product.getId().toString(), NORMAL_FONT));
                table.addCell(new Phrase(product.getSku(), NORMAL_FONT));
                table.addCell(new Phrase(product.getName(), NORMAL_FONT));
                table.addCell(new Phrase(product.getCategory().getName(), NORMAL_FONT));
                table.addCell(new Phrase(product.getUnitPrice().toString(), NORMAL_FONT));
                table.addCell(new Phrase(product.getUnitsInStock().toString(), NORMAL_FONT));
                table.addCell(new Phrase(product.isActive() ? "Active" : "Inactive", NORMAL_FONT));
            }

            document.add(table);
            document.close();

            return outputStream.toByteArray();
        } catch (DocumentException | IOException e) {
            log.error("Error generating PDF for products", e);
            throw new RuntimeException("Failed to generate PDF for products", e);
        }
    }

    /**
     * Export inventory to PDF
     *
     * @param inventory List of inventory items to export
     * @return PDF document as byte array
     */
    public static byte[] exportInventory(List<InventoryDTO> inventory) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Add title
            Paragraph title = new Paragraph("Inventory Report", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Add generation date
            Paragraph date = new Paragraph("Generated on: " + LocalDateTime.now().format(DATE_FORMATTER), SMALL_FONT);
            date.setAlignment(Element.ALIGN_RIGHT);
            date.setSpacingAfter(20);
            document.add(date);

            // Create inventory table
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            // Set column widths
            float[] columnWidths = {0.5f, 1.5f, 2f, 2f, 1f, 1.5f};
            table.setWidths(columnWidths);

            // Add table headers
            addTableHeader(table, new String[]{"ID", "Product SKU", "Product Name", "Warehouse", "Quantity", "Total Value"});

            // Add table rows
            for (InventoryDTO item : inventory) {
                table.addCell(new Phrase(item.getId().toString(), NORMAL_FONT));
                table.addCell(new Phrase(item.getProduct().getSku(), NORMAL_FONT));
                table.addCell(new Phrase(item.getProduct().getName(), NORMAL_FONT));
                table.addCell(new Phrase(item.getWarehouse().getName(), NORMAL_FONT));
                table.addCell(new Phrase(item.getQuantity().toString(), NORMAL_FONT));
                table.addCell(new Phrase(item.getTotalValue().toString(), NORMAL_FONT));
            }

            document.add(table);
            document.close();

            return outputStream.toByteArray();
        } catch (DocumentException | IOException e) {
            log.error("Error generating PDF for inventory", e);
            throw new RuntimeException("Failed to generate PDF for inventory", e);
        }
    }

    /**
     * Export transactions to PDF
     *
     * @param transactions List of transactions to export
     * @return PDF document as byte array
     */
    public static byte[] exportTransactions(List<TransactionDTO> transactions) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate()); // Landscape for more columns
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Add title
            Paragraph title = new Paragraph("Transactions Report", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Add generation date
            Paragraph date = new Paragraph("Generated on: " + LocalDateTime.now().format(DATE_FORMATTER), SMALL_FONT);
            date.setAlignment(Element.ALIGN_RIGHT);
            date.setSpacingAfter(20);
            document.add(date);

            // Create transactions table
            PdfPTable table = new PdfPTable(10);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            // Set column widths
            float[] columnWidths = {0.5f, 1.5f, 1.5f, 1f, 1f, 1.5f, 1f, 1f, 1.5f, 1.5f};
            table.setWidths(columnWidths);

            // Add table headers
            addTableHeader(table, new String[]{
                    "ID", "Transaction #", "Date", "Type", "Status", "Product",
                    "Quantity", "Unit Price", "Total", "Created By"
            });

            // Add table rows
            for (TransactionDTO transaction : transactions) {
                table.addCell(new Phrase(transaction.getId().toString(), NORMAL_FONT));
                table.addCell(new Phrase(transaction.getTransactionNumber(), NORMAL_FONT));
                table.addCell(new Phrase(transaction.getTransactionDate().format(DATE_FORMATTER), NORMAL_FONT));
                table.addCell(new Phrase(transaction.getTransactionType().toString(), NORMAL_FONT));
                table.addCell(new Phrase(transaction.getStatus().toString(), NORMAL_FONT));
                table.addCell(new Phrase(transaction.getProduct().getName(), NORMAL_FONT));
                table.addCell(new Phrase(transaction.getQuantity().toString(), NORMAL_FONT));
                table.addCell(new Phrase(transaction.getUnitPrice().toString(), NORMAL_FONT));
                table.addCell(new Phrase(transaction.getTotalAmount().toString(), NORMAL_FONT));
                table.addCell(new Phrase(transaction.getCreatedBy().getFullName(), NORMAL_FONT));
            }

            document.add(table);
            document.close();

            return outputStream.toByteArray();
        } catch (DocumentException | IOException e) {
            log.error("Error generating PDF for transactions", e);
            throw new RuntimeException("Failed to generate PDF for transactions", e);
        }
    }

    /**
     * Export suppliers to PDF
     *
     * @param suppliers List of suppliers to export
     * @return PDF document as byte array
     */
    public static byte[] exportSuppliers(List<SupplierDTO> suppliers) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Add title
            Paragraph title = new Paragraph("Suppliers Report", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Add generation date
            Paragraph date = new Paragraph("Generated on: " + LocalDateTime.now().format(DATE_FORMATTER), SMALL_FONT);
            date.setAlignment(Element.ALIGN_RIGHT);
            date.setSpacingAfter(20);
            document.add(date);

            // Create suppliers table
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            // Set column widths
            float[] columnWidths = {0.5f, 1.5f, 1.5f, 1.5f, 1.5f, 0.8f, 1f};
            table.setWidths(columnWidths);

            // Add table headers
            addTableHeader(table, new String[]{
                    "ID", "Name", "Contact Name", "Email", "Phone", "Status", "Products"
            });

            // Add table rows
            for (SupplierDTO supplier : suppliers) {
                table.addCell(new Phrase(supplier.getId().toString(), NORMAL_FONT));
                table.addCell(new Phrase(supplier.getName(), NORMAL_FONT));
                table.addCell(new Phrase(supplier.getContactName() != null ? supplier.getContactName() : "", NORMAL_FONT));
                table.addCell(new Phrase(supplier.getEmail() != null ? supplier.getEmail() : "", NORMAL_FONT));
                table.addCell(new Phrase(supplier.getPhone() != null ? supplier.getPhone() : "", NORMAL_FONT));
                table.addCell(new Phrase(supplier.isActive() ? "Active" : "Inactive", NORMAL_FONT));
                table.addCell(new Phrase(String.valueOf(supplier.getProductCount()), NORMAL_FONT));
            }

            document.add(table);
            document.close();

            return outputStream.toByteArray();
        } catch (DocumentException | IOException e) {
            log.error("Error generating PDF for suppliers", e);
            throw new RuntimeException("Failed to generate PDF for suppliers", e);
        }
    }

    /**
     * Helper method to add table headers
     *
     * @param table The table to add headers to
     * @param headers Array of header titles
     */
    private static void addTableHeader(PdfPTable table, String[] headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell();
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setPadding(5);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPhrase(new Phrase(header, HEADER_FONT));
            table.addCell(cell);
        }
    }
}