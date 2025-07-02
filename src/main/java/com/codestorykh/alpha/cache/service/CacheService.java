package com.codestorykh.alpha.cache.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface CacheService {
    
    /**
     * Store a value in cache with default TTL
     */
    <T> void set(String key, T value);
    
    /**
     * Store a value in cache with custom TTL
     */
    <T> void set(String key, T value, Duration ttl);
    
    /**
     * Store a value in cache with custom TTL and cache name
     */
    <T> void set(String cacheName, String key, T value, Duration ttl);
    
    /**
     * Get a value from cache
     */
    <T> Optional<T> get(String key, Class<T> type);
    
    /**
     * Get a value from specific cache
     */
    <T> Optional<T> get(String cacheName, String key, Class<T> type);
    
    /**
     * Get multiple values from cache
     */
    <T> Map<String, T> getMultiple(List<String> keys, Class<T> type);
    
    /**
     * Delete a key from cache
     */
    void delete(String key);
    
    /**
     * Delete a key from specific cache
     */
    void delete(String cacheName, String key);
    
    /**
     * Delete multiple keys
     */
    void deleteMultiple(List<String> keys);
    
    /**
     * Clear entire cache
     */
    void clearCache(String cacheName);
    
    /**
     * Clear all caches
     */
    void clearAllCaches();
    
    /**
     * Check if key exists
     */
    boolean exists(String key);
    
    /**
     * Check if key exists in specific cache
     */
    boolean exists(String cacheName, String key);
    
    /**
     * Get TTL for a key
     */
    Duration getTtl(String key);
    
    /**
     * Set TTL for a key
     */
    void setTtl(String key, Duration ttl);
    
    /**
     * Get all keys matching pattern
     */
    Set<String> getKeys(String pattern);
    
    /**
     * Get cache statistics
     */
    Map<String, Object> getCacheStats(String cacheName);
    
    /**
     * Get all cache statistics
     */
    Map<String, Map<String, Object>> getAllCacheStats();
    
    /**
     * Refresh cache entry (delete and recreate)
     */
    <T> void refresh(String key, T value);
    
    /**
     * Refresh cache entry in specific cache
     */
    <T> void refresh(String cacheName, String key, T value);
    
    /**
     * Get the Redis ObjectMapper for type conversion
     */
    ObjectMapper getRedisObjectMapper();
} 