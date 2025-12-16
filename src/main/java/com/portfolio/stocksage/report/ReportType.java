package com.portfolio.stocksage.report;

public enum ReportType {
    INVENTORY("Inventory Report"),
    SALES("Sales Report"),
    PURCHASE("Purchase Report"),
    PRODUCT_PERFORMANCE("Product Performance Report"),
    SUPPLIER_PERFORMANCE("Supplier Performance Report"),
    LOW_STOCK("Low Stock Report"),
    STOCK_MOVEMENT("Stock Movement Report"),
    WAREHOUSE_USAGE("Warehouse Usage Report"),
    TRANSACTION_SUMMARY("Transaction Summary Report"),
    CUSTOM("Custom Report");

    private final String displayName;

    ReportType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}