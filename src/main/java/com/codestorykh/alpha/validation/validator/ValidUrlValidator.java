package com.codestorykh.alpha.validation.validator;

import com.codestorykh.alpha.validation.annotation.ValidUrl;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.Pattern;

public class ValidUrlValidator implements ConstraintValidator<ValidUrl, String> {

    private String[] allowedProtocols;
    private boolean allowLocalhost;
    private boolean allowIpAddresses;
    private int maxLength;

    // IP address pattern
    private static final Pattern IP_PATTERN = 
        Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");

    @Override
    public void initialize(ValidUrl constraintAnnotation) {
        this.allowedProtocols = constraintAnnotation.allowedProtocols();
        this.allowLocalhost = constraintAnnotation.allowLocalhost();
        this.allowIpAddresses = constraintAnnotation.allowIpAddresses();
        this.maxLength = constraintAnnotation.maxLength();
    }

    @Override
    public boolean isValid(String urlString, ConstraintValidatorContext context) {
        if (urlString == null || urlString.trim().isEmpty()) {
            return false;
        }

        // Check length
        if (urlString.length() > maxLength) {
            return false;
        }

        try {
            URL url = new URL(urlString);

            // Check protocol
            String protocol = url.getProtocol().toLowerCase();
            if (!Arrays.asList(allowedProtocols).contains(protocol)) {
                return false;
            }

            // Check host
            String host = url.getHost();
            if (host == null || host.isEmpty()) {
                return false;
            }

            // Check for localhost
            if (!allowLocalhost && "localhost".equalsIgnoreCase(host)) {
                return false;
            }

            // Check for IP addresses
            if (!allowIpAddresses && IP_PATTERN.matcher(host).matches()) {
                return false;
            }

            // Additional security checks
            return !host.contains("..") && !host.startsWith(".") && !host.endsWith(".");

        } catch (MalformedURLException e) {
            return false;
        }
    }
} 