package com.codestorykh.alpha.cache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DynamicCache {
    
    /**
     * Cache name to use
     */
    String cacheName() default "default";
    
    /**
     * Key prefix for the cache entry
     */
    String keyPrefix() default "";
    
    /**
     * Key expression in SpEL format
     */
    String key() default "";
    
    /**
     * Time to live for the cache entry
     */
    long ttl() default 30;
    
    /**
     * Time unit for TTL
     */
    TimeUnit timeUnit() default TimeUnit.MINUTES;
    
    /**
     * Whether to cache null values
     */
    boolean cacheNull() default false;
    
    /**
     * Whether to refresh the cache entry if it exists
     */
    boolean refresh() default false;
} 