package com.codestorykh.alpha.exception;

/**
 * Exception thrown when password validation fails
 */
public class InvalidPasswordException extends RuntimeException {
    
    private final String reason;
    
    public InvalidPasswordException(String reason) {
        super(String.format("Invalid password: %s", reason));
        this.reason = reason;
    }
    
    public InvalidPasswordException(String message, String reason) {
        super(message);
        this.reason = reason;
    }
    
    public String getReason() {
        return reason;
    }
} 