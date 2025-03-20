package com.portfolio.stocksage.util;

import java.util.regex.Pattern;

/**
 * Utility class for common validation operations
 */
public class ValidationUtils {

    // Regular expression for a valid email format
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    // Regular expression for a valid phone number
    private static final String PHONE_REGEX = "^\\+?[0-9]{8,15}$";
    private static final Pattern PHONE_PATTERN = Pattern.compile(PHONE_REGEX);

    // SKU validation pattern - alphanumeric with hyphens, underscores
    private static final String SKU_REGEX = "^[a-zA-Z0-9_-]{3,50}$";
    private static final Pattern SKU_PATTERN = Pattern.compile(SKU_REGEX);

    // Username validation pattern - alphanumeric with underscores
    private static final String USERNAME_REGEX = "^[a-zA-Z0-9_]{3,50}$";
    private static final Pattern USERNAME_PATTERN = Pattern.compile(USERNAME_REGEX);

    // Private constructor to prevent instantiation
    private ValidationUtils() {
        throw new AssertionError("ValidationUtils is a utility class and should not be instantiated");
    }

    /**
     * Validates if the given email is in a correct format
     *
     * @param email the email to validate
     * @return true if the email is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validates if the given phone number is in a correct format
     *
     * @param phone the phone number to validate
     * @return true if the phone number is valid, false otherwise
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validates if the given SKU matches the required pattern
     *
     * @param sku the SKU to validate
     * @return true if the SKU is valid, false otherwise
     */
    public static boolean isValidSku(String sku) {
        if (sku == null || sku.isEmpty()) {
            return false;
        }
        return SKU_PATTERN.matcher(sku).matches();
    }

    /**
     * Validates if the given username matches the required pattern
     *
     * @param username the username to validate
     * @return true if the username is valid, false otherwise
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.isEmpty()) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * Validates if the given number is positive
     *
     * @param number the number to validate
     * @return true if the number is positive, false otherwise
     */
    public static boolean isPositive(Number number) {
        if (number == null) {
            return false;
        }
        return number.doubleValue() > 0;
    }

    /**
     * Validates if the given number is positive or zero
     *
     * @param number the number to validate
     * @return true if the number is positive or zero, false otherwise
     */
    public static boolean isPositiveOrZero(Number number) {
        if (number == null) {
            return false;
        }
        return number.doubleValue() >= 0;
    }

    /**
     * Validates if the given string is null or empty
     *
     * @param str the string to validate
     * @return true if the string is null or empty, false otherwise
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}