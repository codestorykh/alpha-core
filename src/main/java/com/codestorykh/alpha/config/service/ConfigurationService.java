package com.codestorykh.alpha.config.service;

import com.codestorykh.alpha.config.domain.Configuration;

import java.util.List;
import java.util.Optional;

public interface ConfigurationService {

    Optional<Configuration> findByKey(String key);

    Optional<String> getValue(String key);

    Optional<String> getValue(String key, String defaultValue);

    Optional<Long> getLongValue(String key);

    Optional<Long> getLongValue(String key, Long defaultValue);

    Optional<Integer> getIntValue(String key);

    Optional<Integer> getIntValue(String key, Integer defaultValue);

    Optional<Boolean> getBooleanValue(String key);

    Optional<Boolean> getBooleanValue(String key, Boolean defaultValue);

    Optional<Double> getDoubleValue(String key);

    Optional<Double> getDoubleValue(String key, Double defaultValue);

    List<Configuration> findByCategory(String category);

    Configuration save(Configuration configuration);

    Configuration updateValue(String key, String value);

    void deleteByKey(String key);

    boolean existsByKey(String key);

    // JWT specific methods
    String getJwtSecret();

    long getJwtExpiration();

    long getJwtRefreshExpiration();

    String getJwtIssuer();

    String getJwtAudience();

    // Initialize default configurations
    void initializeDefaultConfigurations();

    // Refresh JWT configuration (useful after updating JWT settings)
    void refreshJwtConfiguration();

    // Generate a secure JWT secret
    String generateSecureJwtSecret();

    // OAuth2 specific methods
    List<String> getOAuth2Scopes();
    
    List<String> getAvailableOAuth2Scopes();
} 