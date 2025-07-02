package com.codestorykh.alpha.validation.validator;

import com.codestorykh.alpha.validation.annotation.ValidUsername;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidUsernameValidator implements ConstraintValidator<ValidUsername, String> {

    private int minLength;
    private int maxLength;
    private boolean allowUnderscore;
    private boolean allowHyphen;

    @Override
    public void initialize(ValidUsername constraintAnnotation) {
        this.minLength = constraintAnnotation.minLength();
        this.maxLength = constraintAnnotation.maxLength();
        this.allowUnderscore = constraintAnnotation.allowUnderscore();
        this.allowHyphen = constraintAnnotation.allowHyphen();
    }

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        if (username == null || username.isEmpty()) {
            return false;
        }

        // Check length
        if (username.length() < minLength || username.length() > maxLength) {
            return false;
        }

        // Build regex pattern based on configuration
        StringBuilder pattern = new StringBuilder("^[a-zA-Z0-9");
        if (allowUnderscore) {
            pattern.append("_");
        }
        if (allowHyphen) {
            pattern.append("-");
        }
        pattern.append("]+$");

        // Check if username matches the pattern
        if (!username.matches(pattern.toString())) {
            return false;
        }

        // Username cannot start or end with underscore or hyphen
        if (username.startsWith("_") || username.startsWith("-") ||
            username.endsWith("_") || username.endsWith("-")) {
            return false;
        }

        // Username cannot contain consecutive underscores or hyphens
        return !username.contains("__") && !username.contains("--") && !username.contains("_-") && !username.contains("-_");
    }
} 