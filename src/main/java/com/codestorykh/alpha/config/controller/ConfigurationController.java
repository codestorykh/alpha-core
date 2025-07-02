package com.codestorykh.alpha.config.controller;

import com.codestorykh.alpha.config.domain.Configuration;
import com.codestorykh.alpha.config.service.ConfigurationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/configurations")
@RequiredArgsConstructor
@Slf4j
public class ConfigurationController {

    private final ConfigurationService configurationService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Configuration>> getAllConfigurations() {
        List<Configuration> configurations = configurationService.findByCategory(null);
        return ResponseEntity.ok(configurations);
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Configuration>> getConfigurationsByCategory(@PathVariable String category) {
        List<Configuration> configurations = configurationService.findByCategory(category);
        return ResponseEntity.ok(configurations);
    }

    @GetMapping("/jwt")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getJwtConfigurations() {
        Map<String, Object> jwtConfig = Map.of(
            "secret", configurationService.getJwtSecret().substring(0, 10) + "...",
            "expiration", configurationService.getJwtExpiration(),
            "refreshExpiration", configurationService.getJwtRefreshExpiration(),
            "issuer", configurationService.getJwtIssuer(),
            "audience", configurationService.getJwtAudience()
        );
        return ResponseEntity.ok(jwtConfig);
    }

    @GetMapping("/oauth2/scopes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getOAuth2ScopesConfig() {
        Map<String, Object> oauth2Config = Map.of(
            "defaultScopes", configurationService.getOAuth2Scopes(),
            "availableScopes", configurationService.getAvailableOAuth2Scopes()
        );
        return ResponseEntity.ok(oauth2Config);
    }

    @GetMapping("/oauth2/scopes/available")
    public ResponseEntity<List<String>> getAvailableOAuth2Scopes() {
        return ResponseEntity.ok(configurationService.getAvailableOAuth2Scopes());
    }

    @GetMapping("/oauth2/scopes/default")
    public ResponseEntity<List<String>> getDefaultOAuth2Scopes() {
        return ResponseEntity.ok(configurationService.getOAuth2Scopes());
    }

    @GetMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Configuration> getConfigurationByKey(@PathVariable String key) {
        return configurationService.findByKey(key)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Configuration> createConfiguration(@RequestBody Configuration configuration) {
        if (configurationService.existsByKey(configuration.getKey())) {
            return ResponseEntity.badRequest().build();
        }
        Configuration savedConfig = configurationService.save(configuration);
        return ResponseEntity.ok(savedConfig);
    }

    @PutMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Configuration> updateConfiguration(@PathVariable String key, @RequestBody Configuration configuration) {
        if (!configurationService.existsByKey(key)) {
            return ResponseEntity.notFound().build();
        }
        configuration.setKey(key);
        Configuration updatedConfig = configurationService.save(configuration);
        return ResponseEntity.ok(updatedConfig);
    }

    @PatchMapping("/{key}/value")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Configuration> updateConfigurationValue(@PathVariable String key, @RequestBody Map<String, String> request) {
        String value = request.get("value");
        if (value == null) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            Configuration updatedConfig = configurationService.updateValue(key, value);
            return ResponseEntity.ok(updatedConfig);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteConfiguration(@PathVariable String key) {
        if (!configurationService.existsByKey(key)) {
            return ResponseEntity.notFound().build();
        }
        configurationService.deleteByKey(key);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/initialize")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> initializeDefaultConfigurations() {
        try {
            configurationService.initializeDefaultConfigurations();
            return ResponseEntity.ok("Default configurations initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize default configurations", e);
            return ResponseEntity.internalServerError().body("Failed to initialize configurations: " + e.getMessage());
        }
    }

    @PostMapping("/jwt/generate-secret")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> generateJwtSecret() {
        try {
            String newSecret = configurationService.generateSecureJwtSecret();
            configurationService.updateValue("jwt.secret", newSecret);
            configurationService.refreshJwtConfiguration();
            
            Map<String, String> response = Map.of(
                "message", "JWT secret generated and updated successfully",
                "secret", newSecret.substring(0, 10) + "...",
                "length", String.valueOf(newSecret.length())
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to generate JWT secret", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to generate JWT secret: " + e.getMessage()));
        }
    }
} 