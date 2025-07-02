package com.codestorykh.alpha.validation.validator;

import com.codestorykh.alpha.validation.annotation.ValidPhoneNumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class ValidPhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {

    private String countryCode;
    private boolean allowInternational;
    private boolean allowExtensions;

    // International phone number pattern
    private static final Pattern INTERNATIONAL_PATTERN = 
        Pattern.compile("^\\+[1-9]\\d{1,14}$");
    
    // US phone number pattern
    private static final Pattern US_PATTERN = 
        Pattern.compile("^\\+?1?[-.\\s]?\\(?([0-9]{3})\\)?[-.\\s]?([0-9]{3})[-.\\s]?([0-9]{4})$");
    
    // General phone number pattern (7-15 digits)
    private static final Pattern GENERAL_PATTERN = 
        Pattern.compile("^[+]?[0-9]{7,15}$");

    @Override
    public void initialize(ValidPhoneNumber constraintAnnotation) {
        this.countryCode = constraintAnnotation.countryCode();
        this.allowInternational = constraintAnnotation.allowInternational();
        this.allowExtensions = constraintAnnotation.allowExtensions();
    }

    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }

        String cleanedNumber = phoneNumber.trim();

        // Handle extensions
        if (allowExtensions && cleanedNumber.contains("x")) {
            String[] parts = cleanedNumber.split("x", 2);
            if (parts.length != 2) {
                return false;
            }
            cleanedNumber = parts[0].trim();
            String extension = parts[1].trim();
            
            // Validate extension (1-5 digits)
            if (!extension.matches("^[0-9]{1,5}$")) {
                return false;
            }
        }

        // Check for specific country code
        if (!countryCode.isEmpty()) {
            if (countryCode.equals("US")) {
                return US_PATTERN.matcher(cleanedNumber).matches();
            }
            // Add more country-specific patterns as needed
        }

        // Check international format
        if (allowInternational && cleanedNumber.startsWith("+")) {
            return INTERNATIONAL_PATTERN.matcher(cleanedNumber).matches();
        }

        // Check general format
        return GENERAL_PATTERN.matcher(cleanedNumber).matches();
    }
} 