package com.codestorykh.alpha.utils.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

@Slf4j
public class SecurityUtils {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Get current authenticated user's username
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                return ((UserDetails) principal).getUsername();
            } else if (principal instanceof String) {
                return (String) principal;
            }
        }
        return null;
    }

    /**
     * Get current authenticated user's UserDetails
     */
    public static UserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                return (UserDetails) principal;
            }
        }
        return null;
    }

    /**
     * Check if current user has a specific role
     */
    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }

    /**
     * Check if current user has any of the specified roles
     */
    public static boolean hasAnyRole(String... roles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        
        for (String role : roles) {
            if (authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if current user has all of the specified roles
     */
    public static boolean hasAllRoles(String... roles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        
        for (String role : roles) {
            if (authentication.getAuthorities().stream()
                    .noneMatch(authority -> authority.getAuthority().equals("ROLE_" + role))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if current user is authenticated
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    /**
     * Generate a secure random token
     */
    public static String generateSecureToken() {
        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.getEncoder().encodeToString(tokenBytes).replace("+", "-").replace("/", "_").replace("=", "");
    }

    /**
     * Generate a secure random token with specified length
     */
    public static String generateSecureToken(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }
        
        byte[] tokenBytes = new byte[length];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.getEncoder().encodeToString(tokenBytes).replace("+", "-").replace("/", "_").replace("=", "");
    }

    /**
     * Generate a UUID-based token
     */
    public static String generateUuidToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Generate a random numeric code
     */
    public static String generateNumericCode(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }
        
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(SECURE_RANDOM.nextInt(10));
        }
        return code.toString();
    }

    /**
     * Generate a random alphanumeric code
     */
    public static String generateAlphanumericCode(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }
        
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(chars.charAt(SECURE_RANDOM.nextInt(chars.length())));
        }
        return code.toString();
    }

    /**
     * Mask sensitive data (like email, phone, etc.)
     */
    public static String maskSensitiveData(String data, String type) {
        if (data == null || data.isEmpty()) {
            return data;
        }

        switch (type.toLowerCase()) {
            case "email":
                return maskEmail(data);
            case "phone":
                return maskPhone(data);
            case "ssn":
                return maskSSN(data);
            case "creditcard":
                return maskCreditCard(data);
            default:
                return maskGeneric(data);
        }
    }

    private static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        
        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];
        
        if (username.length() <= 2) {
            return email;
        }
        
        String maskedUsername = username.charAt(0) + "*".repeat(username.length() - 2) + username.charAt(username.length() - 1);
        return maskedUsername + "@" + domain;
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) {
            return phone;
        }
        
        return "*".repeat(phone.length() - 4) + phone.substring(phone.length() - 4);
    }

    private static String maskSSN(String ssn) {
        if (ssn == null || ssn.length() < 4) {
            return ssn;
        }
        
        return "***-**-" + ssn.substring(ssn.length() - 4);
    }

    private static String maskCreditCard(String card) {
        if (card == null || card.length() < 4) {
            return card;
        }
        
        return "*".repeat(card.length() - 4) + card.substring(card.length() - 4);
    }

    private static String maskGeneric(String data) {
        if (data == null || data.length() < 3) {
            return data;
        }
        
        return data.charAt(0) + "*".repeat(data.length() - 2) + data.charAt(data.length() - 1);
    }

    /**
     * Check if a string contains potentially dangerous content
     */
    public static boolean containsDangerousContent(String content) {
        if (content == null) {
            return false;
        }
        
        String lowerContent = content.toLowerCase();
        
        // Check for SQL injection patterns
        String[] sqlPatterns = {
            "select", "insert", "update", "delete", "drop", "create", "alter",
            "union", "exec", "execute", "script", "javascript", "vbscript"
        };
        
        for (String pattern : sqlPatterns) {
            if (lowerContent.contains(pattern)) {
                return true;
            }
        }
        
        // Check for XSS patterns
        String[] xssPatterns = {
            "<script", "javascript:", "onload", "onerror", "onclick", "onmouseover"
        };
        
        for (String pattern : xssPatterns) {
            if (lowerContent.contains(pattern)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Sanitize input to prevent XSS attacks
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        
        return input
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&#x27;")
                .replaceAll("&", "&amp;");
    }
} 