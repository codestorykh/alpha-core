package com.codestorykh.alpha.validation.validator;

import com.codestorykh.alpha.validation.annotation.StrongPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    private int minLength;
    private boolean requireUppercase;
    private boolean requireLowercase;
    private boolean requireDigit;
    private boolean requireSpecialChar;

    @Override
    public void initialize(StrongPassword constraintAnnotation) {
        this.minLength = constraintAnnotation.minLength();
        this.requireUppercase = constraintAnnotation.requireUppercase();
        this.requireLowercase = constraintAnnotation.requireLowercase();
        this.requireDigit = constraintAnnotation.requireDigit();
        this.requireSpecialChar = constraintAnnotation.requireSpecialChar();
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isEmpty()) {
            return false;
        }

        // Check minimum length
        if (password.length() < minLength) {
            return false;
        }

        // Check for uppercase letters
        if (requireUppercase && !password.matches(".*[A-Z].*")) {
            return false;
        }

        // Check for lowercase letters
        if (requireLowercase && !password.matches(".*[a-z].*")) {
            return false;
        }

        // Check for digits
        if (requireDigit && !password.matches(".*\\d.*")) {
            return false;
        }

        // Check for special characters
        return !requireSpecialChar || password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
    }
} 