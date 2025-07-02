package com.codestorykh.alpha.utils.string;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class StringUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");
    private static final Pattern ALPHABETIC_PATTERN = Pattern.compile("^[a-zA-Z]+$");
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^[0-9]+$");

    /**
     * Check if string is null or empty
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Check if string is not null and not empty
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * Check if string is null, empty, or contains only whitespace
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Check if string is not null, not empty, and contains non-whitespace characters
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * Get default value if string is null or empty
     */
    public static String defaultIfEmpty(String str, String defaultValue) {
        return isEmpty(str) ? defaultValue : str;
    }

    /**
     * Get default value if string is null, empty, or blank
     */
    public static String defaultIfBlank(String str, String defaultValue) {
        return isBlank(str) ? defaultValue : str;
    }

    /**
     * Trim string and return null if empty
     */
    public static String trimToNull(String str) {
        if (str == null) {
            return null;
        }
        String trimmed = str.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Trim string and return empty string if null
     */
    public static String trimToEmpty(String str) {
        return str == null ? "" : str.trim();
    }

    /**
     * Capitalize first letter of string
     */
    public static String capitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    /**
     * Capitalize first letter of each word
     */
    public static String capitalizeWords(String str) {
        if (isEmpty(str)) {
            return str;
        }
        
        String[] words = str.split("\\s+");
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                result.append(" ");
            }
            result.append(capitalize(words[i]));
        }
        
        return result.toString();
    }

    /**
     * Convert string to camel case
     */
    public static String toCamelCase(String str) {
        if (isEmpty(str)) {
            return str;
        }
        
        String[] words = str.split("[\\s_-]+");
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < words.length; i++) {
            if (i == 0) {
                result.append(words[i].toLowerCase());
            } else {
                result.append(capitalize(words[i]));
            }
        }
        
        return result.toString();
    }

    /**
     * Convert string to snake case
     */
    public static String toSnakeCase(String str) {
        if (isEmpty(str)) {
            return str;
        }
        
        return str.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    /**
     * Convert string to kebab case
     */
    public static String toKebabCase(String str) {
        if (isEmpty(str)) {
            return str;
        }
        
        return str.replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase();
    }

    /**
     * Convert string to title case
     */
    public static String toTitleCase(String str) {
        if (isEmpty(str)) {
            return str;
        }
        
        String[] words = str.split("\\s+");
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                result.append(" ");
            }
            result.append(capitalize(words[i]));
        }
        
        return result.toString();
    }

    /**
     * Reverse a string
     */
    public static String reverse(String str) {
        if (str == null) {
            return null;
        }
        return new StringBuilder(str).reverse().toString();
    }

    /**
     * Check if string is palindrome
     */
    public static boolean isPalindrome(String str) {
        if (isEmpty(str)) {
            return true;
        }
        
        String cleaned = str.toLowerCase().replaceAll("[^a-z0-9]", "");
        return cleaned.equals(reverse(cleaned));
    }

    /**
     * Count occurrences of substring in string
     */
    public static int countOccurrences(String str, String substring) {
        if (isEmpty(str) || isEmpty(substring)) {
            return 0;
        }
        
        int count = 0;
        int lastIndex = 0;
        
        while (lastIndex != -1) {
            lastIndex = str.indexOf(substring, lastIndex);
            if (lastIndex != -1) {
                count++;
                lastIndex += substring.length();
            }
        }
        
        return count;
    }

    /**
     * Check if string contains only alphabetic characters
     */
    public static boolean isAlphabetic(String str) {
        return str != null && ALPHABETIC_PATTERN.matcher(str).matches();
    }

    /**
     * Check if string contains only numeric characters
     */
    public static boolean isNumeric(String str) {
        return str != null && NUMERIC_PATTERN.matcher(str).matches();
    }

    /**
     * Check if string contains only alphanumeric characters
     */
    public static boolean isAlphanumeric(String str) {
        return str != null && ALPHANUMERIC_PATTERN.matcher(str).matches();
    }

    /**
     * Check if string is a valid email
     */
    public static boolean isValidEmail(String str) {
        return str != null && EMAIL_PATTERN.matcher(str).matches();
    }

    /**
     * Check if string is a valid UUID
     */
    public static boolean isValidUuid(String str) {
        return str != null && UUID_PATTERN.matcher(str).matches();
    }

    /**
     * Generate random string
     */
    public static String generateRandomString(int length) {
        if (length <= 0) {
            return "";
        }
        
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder result = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < length; i++) {
            result.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return result.toString();
    }

    /**
     * Generate random alphanumeric string
     */
    public static String generateRandomAlphanumeric(int length) {
        if (length <= 0) {
            return "";
        }
        
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder result = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < length; i++) {
            result.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return result.toString();
    }

    /**
     * Generate random numeric string
     */
    public static String generateRandomNumeric(int length) {
        if (length <= 0) {
            return "";
        }
        
        StringBuilder result = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < length; i++) {
            result.append(random.nextInt(10));
        }
        
        return result.toString();
    }

    /**
     * Abbreviate string to specified length
     */
    public static String abbreviate(String str, int maxLength) {
        if (isEmpty(str) || str.length() <= maxLength) {
            return str;
        }
        
        if (maxLength <= 3) {
            return str.substring(0, maxLength);
        }
        
        return str.substring(0, maxLength - 3) + "...";
    }

    /**
     * Remove all whitespace from string
     */
    public static String removeWhitespace(String str) {
        if (str == null) {
            return null;
        }
        return str.replaceAll("\\s+", "");
    }

    /**
     * Normalize whitespace (replace multiple spaces with single space)
     */
    public static String normalizeWhitespace(String str) {
        if (str == null) {
            return null;
        }
        return str.replaceAll("\\s+", " ").trim();
    }

    /**
     * Split string and trim each element
     */
    public static List<String> splitAndTrim(String str, String delimiter) {
        if (isEmpty(str)) {
            return new ArrayList<>();
        }
        
        return Arrays.stream(str.split(delimiter))
                .map(String::trim)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toList());
    }

    /**
     * Join collection of strings with delimiter
     */
    public static String join(Collection<String> strings, String delimiter) {
        if (strings == null || strings.isEmpty()) {
            return "";
        }
        
        return strings.stream()
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.joining(delimiter));
    }

    /**
     * Join array of strings with delimiter
     */
    public static String join(String[] strings, String delimiter) {
        if (strings == null || strings.length == 0) {
            return "";
        }
        
        return Arrays.stream(strings)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.joining(delimiter));
    }

    /**
     * Extract numbers from string
     */
    public static String extractNumbers(String str) {
        if (str == null) {
            return "";
        }
        return str.replaceAll("[^0-9]", "");
    }

    /**
     * Extract letters from string
     */
    public static String extractLetters(String str) {
        if (str == null) {
            return "";
        }
        return str.replaceAll("[^a-zA-Z]", "");
    }

    /**
     * Extract alphanumeric characters from string
     */
    public static String extractAlphanumeric(String str) {
        if (str == null) {
            return "";
        }
        return str.replaceAll("[^a-zA-Z0-9]", "");
    }

    /**
     * Mask string (replace characters with asterisks)
     */
    public static String mask(String str, int visibleStart, int visibleEnd) {
        if (isEmpty(str) || str.length() <= visibleStart + visibleEnd) {
            return str;
        }
        
        String start = str.substring(0, visibleStart);
        String end = str.substring(str.length() - visibleEnd);
        String middle = "*".repeat(str.length() - visibleStart - visibleEnd);
        
        return start + middle + end;
    }

    /**
     * Check if string starts with any of the prefixes
     */
    public static boolean startsWithAny(String str, String... prefixes) {
        if (isEmpty(str) || prefixes == null) {
            return false;
        }
        
        for (String prefix : prefixes) {
            if (str.startsWith(prefix)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Check if string ends with any of the suffixes
     */
    public static boolean endsWithAny(String str, String... suffixes) {
        if (isEmpty(str) || suffixes == null) {
            return false;
        }
        
        for (String suffix : suffixes) {
            if (str.endsWith(suffix)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Check if string contains any of the substrings
     */
    public static boolean containsAny(String str, String... substrings) {
        if (isEmpty(str) || substrings == null) {
            return false;
        }
        
        for (String substring : substrings) {
            if (str.contains(substring)) {
                return true;
            }
        }
        
        return false;
    }
} 