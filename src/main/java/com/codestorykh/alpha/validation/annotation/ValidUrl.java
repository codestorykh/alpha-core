package com.codestorykh.alpha.validation.annotation;

import com.codestorykh.alpha.validation.validator.ValidUrlValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidUrlValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUrl {
    String message() default "Invalid URL format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
    String[] allowedProtocols() default {"http", "https"};
    boolean allowLocalhost() default false;
    boolean allowIpAddresses() default false;
    int maxLength() default 2048;
} 