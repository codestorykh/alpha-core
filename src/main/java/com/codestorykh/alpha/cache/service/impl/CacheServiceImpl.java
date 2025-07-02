package com.codestorykh.alpha.cache.service.impl;

import com.codestorykh.alpha.cache.service.CacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheServiceImpl implements CacheService {

    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper redisObjectMapper;
    
    // Cache statistics tracking
    private final Map<String, CacheStats> cacheStatsMap = new ConcurrentHashMap<>();

    @Override
    public <T> void set(String key, T value) {
        set("default", key, value, Duration.ofMinutes(30));
    }

    @Override
    public <T> void set(String key, T value, Duration ttl) {
        set("default", key, value, ttl);
    }

    @Override
    public <T> void set(String cacheName, String key, T value, Duration ttl) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.put(key, value);
                updateStats(cacheName, "set", true);
                log.debug("Cached value for key: {} in cache: {}", key, cacheName);
            } else {
                log.warn("Cache '{}' not found", cacheName);
                updateStats(cacheName, "set", false);
            }
        } catch (Exception e) {
            log.error("Error setting cache value for key: {} in cache: {}", key, cacheName, e);
            updateStats(cacheName, "set", false);
        }
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        return get("default", key, type);
    }

    @Override
    public <T> Optional<T> get(String cacheName, String key, Class<T> type) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                Cache.ValueWrapper wrapper = cache.get(key);
                if (wrapper != null) {
                    Object value = wrapper.get();
                    
                    // Handle direct type match
                    if (type.isInstance(value)) {
                        updateStats(cacheName, "hit", true);
                        log.debug("Cache hit for key: {} in cache: {}", key, cacheName);
                        return Optional.of(type.cast(value));
                    }
                    
                    // Handle LinkedHashMap (deserialized JSON)
                    if (value instanceof Map) {
                        try {
                            T convertedValue = redisObjectMapper.convertValue(value, type);
                            updateStats(cacheName, "hit", true);
                            log.debug("Cache hit for key: {} in cache: {} (converted from Map)", key, cacheName);
                            return Optional.of(convertedValue);
                        } catch (Exception e) {
                            log.warn("Failed to convert cached Map to type {} for key: {}", type.getSimpleName(), key, e);
                        }
                    }
                }
                updateStats(cacheName, "miss", true);
                log.debug("Cache miss for key: {} in cache: {}", key, cacheName);
            } else {
                log.warn("Cache '{}' not found", cacheName);
                updateStats(cacheName, "miss", false);
            }
        } catch (Exception e) {
            log.error("Error getting cache value for key: {} in cache: {}", key, cacheName, e);
            updateStats(cacheName, "miss", false);
        }
        return Optional.empty();
    }

    @Override
    public <T> Map<String, T> getMultiple(List<String> keys, Class<T> type) {
        Map<String, T> result = new HashMap<>();
        for (String key : keys) {
            get(key, type).ifPresent(value -> result.put(key, value));
        }
        return result;
    }

    @Override
    public void delete(String key) {
        delete("default", key);
    }

    @Override
    public void delete(String cacheName, String key) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.evict(key);
                updateStats(cacheName, "delete", true);
                log.debug("Deleted cache key: {} from cache: {}", key, cacheName);
            } else {
                log.warn("Cache '{}' not found", cacheName);
                updateStats(cacheName, "delete", false);
            }
        } catch (Exception e) {
            log.error("Error deleting cache key: {} from cache: {}", key, cacheName, e);
            updateStats(cacheName, "delete", false);
        }
    }

    @Override
    public void deleteMultiple(List<String> keys) {
        for (String key : keys) {
            delete(key);
        }
    }

    @Override
    public void clearCache(String cacheName) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                updateStats(cacheName, "clear", true);
                log.info("Cleared cache: {}", cacheName);
            } else {
                log.warn("Cache '{}' not found", cacheName);
                updateStats(cacheName, "clear", false);
            }
        } catch (Exception e) {
            log.error("Error clearing cache: {}", cacheName, e);
            updateStats(cacheName, "clear", false);
        }
    }

    @Override
    public void clearAllCaches() {
        cacheManager.getCacheNames().forEach(this::clearCache);
        log.info("Cleared all caches");
    }

    @Override
    public boolean exists(String key) {
        return exists("default", key);
    }

    @Override
    public boolean exists(String cacheName, String key) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            return cache != null && cache.get(key) != null;
        } catch (Exception e) {
            log.error("Error checking existence for key: {} in cache: {}", key, cacheName, e);
            return false;
        }
    }

    @Override
    public Duration getTtl(String key) {
        try {
            Long ttl = redisTemplate.getExpire(key);
            return ttl != null && ttl > 0 ? Duration.ofSeconds(ttl) : Duration.ZERO;
        } catch (Exception e) {
            log.error("Error getting TTL for key: {}", key, e);
            return Duration.ZERO;
        }
    }

    @Override
    public void setTtl(String key, Duration ttl) {
        try {
            redisTemplate.expire(key, ttl);
            log.debug("Set TTL for key: {} to {}", key, ttl);
        } catch (Exception e) {
            log.error("Error setting TTL for key: {}", key, e);
        }
    }

    @Override
    public Set<String> getKeys(String pattern) {
        try {
            return redisTemplate.keys(pattern);
        } catch (Exception e) {
            log.error("Error getting keys for pattern: {}", pattern, e);
            return new HashSet<>();
        }
    }

    @Override
    public Map<String, Object> getCacheStats(String cacheName) {
        CacheStats stats = cacheStatsMap.get(cacheName);
        if (stats != null) {
            Map<String, Object> result = new HashMap<>();
            result.put("cacheName", cacheName);
            result.put("hits", stats.getHits());
            result.put("misses", stats.getMisses());
            result.put("sets", stats.getSets());
            result.put("deletes", stats.getDeletes());
            result.put("clears", stats.getClears());
            result.put("errors", stats.getErrors());
            
            long totalRequests = stats.getHits() + stats.getMisses();
            double hitRate = totalRequests > 0 ? (double) stats.getHits() / totalRequests : 0.0;
            result.put("hitRate", String.format("%.2f%%", hitRate * 100));
            
            return result;
        }
        return new HashMap<>();
    }

    @Override
    public Map<String, Map<String, Object>> getAllCacheStats() {
        Map<String, Map<String, Object>> allStats = new HashMap<>();
        cacheManager.getCacheNames().forEach(cacheName -> 
            allStats.put(cacheName, getCacheStats(cacheName)));
        return allStats;
    }

    @Override
    public <T> void refresh(String key, T value) {
        refresh("default", key, value);
    }

    @Override
    public <T> void refresh(String cacheName, String key, T value) {
        delete(cacheName, key);
        set(cacheName, key, value, Duration.ofMinutes(30));
        log.debug("Refreshed cache entry for key: {} in cache: {}", key, cacheName);
    }

    @Override
    public ObjectMapper getRedisObjectMapper() {
        return redisObjectMapper;
    }

    private void updateStats(String cacheName, String operation, boolean success) {
        CacheStats stats = cacheStatsMap.computeIfAbsent(cacheName, k -> new CacheStats());
        
        switch (operation) {
            case "hit" -> stats.incrementHits();
            case "miss" -> stats.incrementMisses();
            case "set" -> stats.incrementSets();
            case "delete" -> stats.incrementDeletes();
            case "clear" -> stats.incrementClears();
        }
        
        if (!success) {
            stats.incrementErrors();
        }
    }

    private static class CacheStats {
        private final AtomicLong hits = new AtomicLong(0);
        private final AtomicLong misses = new AtomicLong(0);
        private final AtomicLong sets = new AtomicLong(0);
        private final AtomicLong deletes = new AtomicLong(0);
        private final AtomicLong clears = new AtomicLong(0);
        private final AtomicLong errors = new AtomicLong(0);

        public long getHits() { return hits.get(); }
        public long getMisses() { return misses.get(); }
        public long getSets() { return sets.get(); }
        public long getDeletes() { return deletes.get(); }
        public long getClears() { return clears.get(); }
        public long getErrors() { return errors.get(); }

        public void incrementHits() { hits.incrementAndGet(); }
        public void incrementMisses() { misses.incrementAndGet(); }
        public void incrementSets() { sets.incrementAndGet(); }
        public void incrementDeletes() { deletes.incrementAndGet(); }
        public void incrementClears() { clears.incrementAndGet(); }
        public void incrementErrors() { errors.incrementAndGet(); }
    }
} 