package com.portfolio.stocksage.util;

/**
 * Utility class for string operations
 */
public class StringUtils {

    private StringUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Check if a string is null or empty
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Check if a string is not null and not empty
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * Get a substring of a string, handling null and index bounds safely
     */
    public static String safeSubstring(String str, int start, int end) {
        if (isEmpty(str)) {
            return "";
        }

        int length = str.length();

        // Adjust start and end indices to be within bounds
        start = Math.max(0, Math.min(start, length));
        end = Math.max(start, Math.min(end, length));

        return str.substring(start, end);
    }

    /**
     * Truncate a string to a maximum length, appending ellipsis if truncated
     */
    public static String truncate(String str, int maxLength) {
        if (isEmpty(str) || str.length() <= maxLength) {
            return str;
        }

        return str.substring(0, maxLength) + "...";
    }

    /**
     * Convert a string to title case (capitalize first letter of each word)
     */
    public static String toTitleCase(String str) {
        if (isEmpty(str)) {
            return str;
        }

        StringBuilder result = new StringBuilder(str.length());
        boolean capitalize = true;

        for (char c : str.toCharArray()) {
            if (Character.isWhitespace(c)) {
                capitalize = true;
                result.append(c);
            } else if (capitalize) {
                result.append(Character.toUpperCase(c));
                capitalize = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }

        return result.toString();
    }

    /**
     * Convert a camelCase string to a human-readable format with spaces
     * e.g., "camelCaseString" -> "Camel Case String"
     */
    public static String camelCaseToHuman(String str) {
        if (isEmpty(str)) {
            return str;
        }

        // Add a space before each uppercase letter, then capitalize the first letter
        String result = str.replaceAll("([A-Z])", " $1");
        if (result.length() > 0) {
            result = Character.toUpperCase(result.charAt(0)) + result.substring(1);
        }

        return result;
    }

    /**
     * Generate a slug from a string (lowercase, remove special characters, replace spaces with hyphens)
     */
    public static String generateSlug(String str) {
        if (isEmpty(str)) {
            return str;
        }

        // Convert to lowercase
        String result = str.toLowerCase();

        // Replace non-alphanumeric characters with hyphens
        result = result.replaceAll("[^a-zA-Z0-9\\s]", "");

        // Replace spaces with hyphens
        result = result.replaceAll("\\s+", "-");

        // Remove consecutive hyphens
        result = result.replaceAll("-+", "-");

        // Remove leading and trailing hyphens
        return result.replaceAll("^-|-$", "");
    }

    /**
     * Format a number as a string with a specified number of digits and leading zeros
     */
    public static String formatNumber(int number, int digits) {
        return String.format("%0" + digits + "d", number);
    }

    /**
     * Check if a string contains only digits
     */
    public static boolean isNumeric(String str) {
        if (isEmpty(str)) {
            return false;
        }

        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Convert a string to CamelCase (remove spaces and capitalize each word except the first)
     */
    public static String toCamelCase(String str) {
        if (isEmpty(str)) {
            return str;
        }

        // Split the string by any non-alphanumeric character
        String[] words = str.split("[^a-zA-Z0-9]");
        StringBuilder result = new StringBuilder();

        // First word starts with lowercase
        result.append(words[0].toLowerCase());

        // Subsequent words start with uppercase
        for (int i = 1; i < words.length; i++) {
            if (!words[i].isEmpty()) {
                result.append(Character.toUpperCase(words[i].charAt(0)));
                result.append(words[i].substring(1).toLowerCase());
            }
        }

        return result.toString();
    }
}