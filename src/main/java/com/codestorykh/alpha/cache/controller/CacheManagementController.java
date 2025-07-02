package com.codestorykh.alpha.cache.controller;

import com.codestorykh.alpha.cache.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class CacheManagementController {

    private final CacheService cacheService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Map<String, Object>>> getCacheStats() {
        Map<String, Map<String, Object>> stats = cacheService.getAllCacheStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/{cacheName}")
    public ResponseEntity<Map<String, Object>> getCacheStats(@PathVariable String cacheName) {
        Map<String, Object> stats = cacheService.getCacheStats(cacheName);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/keys")
    public ResponseEntity<Set<String>> getKeys(@RequestParam String pattern) {
        Set<String> keys = cacheService.getKeys(pattern);
        return ResponseEntity.ok(keys);
    }

    @GetMapping("/keys/{cacheName}")
    public ResponseEntity<Set<String>> getKeys(@PathVariable String cacheName, @RequestParam String pattern) {
        Set<String> keys = cacheService.getKeys(cacheName + ":" + pattern);
        return ResponseEntity.ok(keys);
    }

    @GetMapping("/exists/{key}")
    public ResponseEntity<Boolean> keyExists(@PathVariable String key) {
        boolean exists = cacheService.exists(key);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/exists/{cacheName}/{key}")
    public ResponseEntity<Boolean> keyExists(@PathVariable String cacheName, @PathVariable String key) {
        boolean exists = cacheService.exists(cacheName, key);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/ttl/{key}")
    public ResponseEntity<Duration> getTtl(@PathVariable String key) {
        Duration ttl = cacheService.getTtl(key);
        return ResponseEntity.ok(ttl);
    }

    @PostMapping("/set")
    public ResponseEntity<Void> setValue(@RequestParam String key, @RequestBody Object value) {
        cacheService.set(key, value);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/set/{cacheName}")
    public ResponseEntity<Void> setValue(@PathVariable String cacheName, 
                                       @RequestParam String key, 
                                       @RequestBody Object value,
                                       @RequestParam(defaultValue = "30") long ttlMinutes) {
        cacheService.set(cacheName, key, value, Duration.ofMinutes(ttlMinutes));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{key}")
    public ResponseEntity<Void> deleteKey(@PathVariable String key) {
        cacheService.delete(key);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{cacheName}/{key}")
    public ResponseEntity<Void> deleteKey(@PathVariable String cacheName, @PathVariable String key) {
        cacheService.delete(cacheName, key);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete-multiple")
    public ResponseEntity<Void> deleteMultipleKeys(@RequestBody List<String> keys) {
        cacheService.deleteMultiple(keys);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/clear/{cacheName}")
    public ResponseEntity<Void> clearCache(@PathVariable String cacheName) {
        cacheService.clearCache(cacheName);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/clear-all")
    public ResponseEntity<Void> clearAllCaches() {
        cacheService.clearAllCaches();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh/{key}")
    public ResponseEntity<Void> refreshKey(@PathVariable String key, @RequestBody Object value) {
        cacheService.refresh(key, value);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh/{cacheName}/{key}")
    public ResponseEntity<Void> refreshKey(@PathVariable String cacheName, 
                                         @PathVariable String key, 
                                         @RequestBody Object value) {
        cacheService.refresh(cacheName, key, value);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/ttl/{key}")
    public ResponseEntity<Void> setTtl(@PathVariable String key, @RequestParam long ttlMinutes) {
        cacheService.setTtl(key, Duration.ofMinutes(ttlMinutes));
        return ResponseEntity.ok().build();
    }
} 