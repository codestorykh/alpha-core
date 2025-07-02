package com.codestorykh.alpha.validation;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

@Slf4j
public class ValidationUtils {

    // Common regex patterns
    public static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    
    public static final Pattern ALPHANUMERIC_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9]+$");
    
    public static final Pattern ALPHANUMERIC_WITH_UNDERSCORE_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9_]+$");
    
    public static final Pattern ALPHANUMERIC_WITH_HYPHEN_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9-]+$");
    
    public static final Pattern ALPHANUMERIC_WITH_UNDERSCORE_AND_HYPHEN_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9_-]+$");

    /**
     * Validates email format
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validates alphanumeric string
     */
    public static boolean isAlphanumeric(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        return ALPHANUMERIC_PATTERN.matcher(value.trim()).matches();
    }

    /**
     * Validates alphanumeric string with underscores
     */
    public static boolean isAlphanumericWithUnderscore(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        return ALPHANUMERIC_WITH_UNDERSCORE_PATTERN.matcher(value.trim()).matches();
    }

    /**
     * Validates alphanumeric string with hyphens
     */
    public static boolean isAlphanumericWithHyphen(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        return ALPHANUMERIC_WITH_HYPHEN_PATTERN.matcher(value.trim()).matches();
    }

    /**
     * Validates alphanumeric string with underscores and hyphens
     */
    public static boolean isAlphanumericWithUnderscoreAndHyphen(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        return ALPHANUMERIC_WITH_UNDERSCORE_AND_HYPHEN_PATTERN.matcher(value.trim()).matches();
    }

    /**
     * Validates string length
     */
    public static boolean isValidLength(String value, int minLength, int maxLength) {
        if (value == null) {
            return minLength == 0;
        }
        int length = value.trim().length();
        return length >= minLength && length <= maxLength;
    }

    /**
     * Validates that string contains only allowed characters
     */
    public static boolean containsOnlyAllowedCharacters(String value, String allowedCharacters) {
        if (value == null || allowedCharacters == null) {
            return false;
        }
        return value.chars().allMatch(ch -> allowedCharacters.indexOf(ch) != -1);
    }

    /**
     * Validates that string doesn't contain forbidden characters
     */
    public static boolean containsForbiddenCharacters(String value, String forbiddenCharacters) {
        if (value == null || forbiddenCharacters == null) {
            return false;
        }
        return value.chars().anyMatch(ch -> forbiddenCharacters.indexOf(ch) != -1);
    }

    /**
     * Sanitizes input by removing potentially dangerous characters
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        
        // Remove null bytes and control characters
        String sanitized = input.replaceAll("[\\x00-\\x1F\\x7F]", "");
        
        // Remove script tags and common XSS patterns
        sanitized = sanitized.replaceAll("(?i)<script[^>]*>.*?</script>", "");
        sanitized = sanitized.replaceAll("(?i)javascript:", "");
        sanitized = sanitized.replaceAll("(?i)on\\w+\\s*=", "");
        
        return sanitized.trim();
    }

    /**
     * Validates OAuth2 redirect URI format
     */
    public static boolean isValidOAuth2RedirectUri(String redirectUri) {
        if (redirectUri == null || redirectUri.trim().isEmpty()) {
            return false;
        }
        
        // Must be a valid URL
        if (!isValidUrl(redirectUri)) {
            return false;
        }
        
        // Must use HTTPS (except for localhost)
        return !redirectUri.toLowerCase().startsWith("http://") ||
                redirectUri.toLowerCase().contains("localhost");
    }

    /**
     * Validates URL format
     */
    public static boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        
        try {
            new java.net.URL(url);
            return true;
        } catch (java.net.MalformedURLException e) {
            return false;
        }
    }

    /**
     * Validates OAuth2 scope format
     */
    public static boolean isValidOAuth2Scope(String scope) {
        if (scope == null || scope.trim().isEmpty()) {
            return false;
        }
        
        // Scope should contain only alphanumeric characters, dots, and colons
        return scope.matches("^[a-zA-Z0-9.:]+$");
    }

    /**
     * Validates OAuth2 grant type
     */
    public static boolean isValidOAuth2GrantType(String grantType) {
        if (grantType == null || grantType.trim().isEmpty()) {
            return false;
        }
        
        String[] validGrantTypes = {
            "authorization_code",
            "client_credentials", 
            "password",
            "refresh_token",
            "implicit"
        };
        
        return java.util.Arrays.asList(validGrantTypes).contains(grantType.toLowerCase());
    }
} 