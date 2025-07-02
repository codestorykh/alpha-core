package com.codestorykh.alpha.validation.annotation;

import com.codestorykh.alpha.validation.validator.ValidUsernameValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidUsernameValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUsername {
    String message() default "Username must be 3-50 characters long and contain only letters, numbers, underscores, and hyphens";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
    int minLength() default 3;
    int maxLength() default 50;
    boolean allowUnderscore() default true;
    boolean allowHyphen() default true;
} 