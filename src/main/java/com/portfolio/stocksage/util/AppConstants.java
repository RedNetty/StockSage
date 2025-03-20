package com.portfolio.stocksage.util;

import java.util.Arrays;
import java.util.List;

/**
 * Application constants used throughout the application.
 */
public final class AppConstants {

    // Pagination and Sorting defaults
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final String DEFAULT_SORT_BY = "id";
    public static final String DEFAULT_SORT_DIRECTION = "asc";

    // Date format patterns
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    // File paths
    public static final String UPLOAD_DIR = "uploads";
    public static final String REPORT_DIR = "reports";

    // Roles
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_INVENTORY_MANAGER = "INVENTORY_MANAGER";
    public static final String ROLE_USER = "USER";

    // Default roles to create when initializing the database
    public static final List<String> DEFAULT_ROLES = Arrays.asList(
            ROLE_ADMIN, ROLE_INVENTORY_MANAGER, ROLE_USER
    );

    // Transaction types
    public static final String TRANSACTION_TYPE_PURCHASE = "PURCHASE";
    public static final String TRANSACTION_TYPE_SALE = "SALE";
    public static final String TRANSACTION_TYPE_ADJUSTMENT = "ADJUSTMENT";
    public static final String TRANSACTION_TYPE_TRANSFER = "TRANSFER";

    // Transaction statuses
    public static final String TRANSACTION_STATUS_PENDING = "PENDING";
    public static final String TRANSACTION_STATUS_COMPLETED = "COMPLETED";
    public static final String TRANSACTION_STATUS_CANCELLED = "CANCELLED";

    // Export types
    public static final String EXPORT_TYPE_EXCEL = "excel";
    public static final String EXPORT_TYPE_PDF = "pdf";
    public static final String EXPORT_TYPE_CSV = "csv";

    // Content types for export
    public static final String CONTENT_TYPE_EXCEL = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final String CONTENT_TYPE_PDF = "application/pdf";
    public static final String CONTENT_TYPE_CSV = "text/csv";

    // Low stock threshold
    public static final int LOW_STOCK_THRESHOLD = 10;

    // Security constants
    public static final long JWT_EXPIRATION_TIME = 86400000; // 24 hours
    public static final String JWT_SECRET_ENV_VARIABLE = "JWT_SECRET";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";

    // Prevent instantiation
    private AppConstants() {
        throw new AssertionError("Cannot create instances of AppConstants");
    }
}