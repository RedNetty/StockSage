package com.portfolio.stocksage.util;

import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Utility class for validation operations
 */
public final class ValidationUtils {

    // Email validation regular expression
    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" +
                    "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private static final Pattern PATTERN_EMAIL = Pattern.compile(EMAIL_PATTERN);

    // SKU validation pattern - alphanumeric with optional hyphens
    private static final String SKU_PATTERN = "^[A-Za-z0-9\\-]+$";
    private static final Pattern PATTERN_SKU = Pattern.compile(SKU_PATTERN);

    // Phone validation pattern - Allow international format
    private static final String PHONE_PATTERN = "^\\+?[0-9\\-\\s\\(\\)]{8,20}$";
    private static final Pattern PATTERN_PHONE = Pattern.compile(PHONE_PATTERN);

    private ValidationUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Validate email format
     *
     * @param email Email to validate
     * @return True if email format is valid
     */
    public static boolean isValidEmail(String email) {
        return email != null && PATTERN_EMAIL.matcher(email).matches();
    }

    /**
     * Validate SKU format
     *
     * @param sku SKU to validate
     * @return True if SKU format is valid
     */
    public static boolean isValidSku(String sku) {
        return sku != null && PATTERN_SKU.matcher(sku).matches();
    }

    /**
     * Validate phone number format
     *
     * @param phone Phone number to validate
     * @return True if phone format is valid
     */
    public static boolean isValidPhoneNumber(String phone) {
        return phone != null && PATTERN_PHONE.matcher(phone).matches();
    }

    /**
     * Validate price value
     *
     * @param price Price to validate
     * @return True if price is valid (non-negative)
     */
    public static boolean isValidPrice(BigDecimal price) {
        return price != null && price.compareTo(BigDecimal.ZERO) >= 0;
    }

    /**
     * Validate quantity value
     *
     * @param quantity Quantity to validate
     * @return True if quantity is valid (non-negative)
     */
    public static boolean isValidQuantity(Integer quantity) {
        return quantity != null && quantity >= 0;
    }

    /**
     * Validate string is not empty and within length constraints
     *
     * @param value String to validate
     * @param minLength Minimum length
     * @param maxLength Maximum length
     * @return True if string is valid
     */
    public static boolean isValidString(String value, int minLength, int maxLength) {
        return value != null && value.length() >= minLength && value.length() <= maxLength;
    }

    /**
     * Validate password strength
     *
     * @param password Password to validate
     * @return True if password is strong enough
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUppercase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowercase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else {
                hasSpecial = true;
            }
        }

        return hasUppercase && hasLowercase && (hasDigit || hasSpecial);
    }
}