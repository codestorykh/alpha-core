package com.codestorykh.alpha.config.service;

import com.codestorykh.alpha.config.domain.ApplicationJwtConfig;
import com.codestorykh.alpha.config.dto.ApplicationJwtConfigDTO;
import com.codestorykh.alpha.config.dto.CreateApplicationJwtConfigRequest;
import com.codestorykh.alpha.config.dto.UpdateApplicationJwtConfigRequest;

import java.util.List;
import java.util.Optional;

public interface ApplicationJwtConfigService {

    // CRUD operations
    ApplicationJwtConfigDTO createConfiguration(CreateApplicationJwtConfigRequest request);

    ApplicationJwtConfigDTO updateConfiguration(Long id, UpdateApplicationJwtConfigRequest request);

    ApplicationJwtConfigDTO updateConfiguration(String applicationName, String environment, UpdateApplicationJwtConfigRequest request);

    void deleteConfiguration(Long id);

    void deleteConfiguration(String applicationName, String environment);

    Optional<ApplicationJwtConfigDTO> findById(Long id);

    Optional<ApplicationJwtConfigDTO> findByApplicationName(String applicationName);

    Optional<ApplicationJwtConfigDTO> findByApplicationNameAndEnvironment(String applicationName, String environment);

    List<ApplicationJwtConfigDTO> findAll();

    List<ApplicationJwtConfigDTO> findAllEnabled();

    List<ApplicationJwtConfigDTO> findByEnvironment(String environment);

    // Configuration retrieval for JWT operations
    ApplicationJwtConfig getConfigurationForApplication(String applicationName);

    ApplicationJwtConfig getConfigurationForApplication(String applicationName, String environment);

    ApplicationJwtConfig getDefaultConfiguration();

    // Utility methods
    boolean existsByApplicationName(String applicationName);

    boolean existsByApplicationNameAndEnvironment(String applicationName, String environment);

    List<String> getAllApplicationNames();

    List<String> getAllEnvironments();

    // Search and filter
    List<ApplicationJwtConfigDTO> searchConfigurations(String applicationName, String environment, Boolean enabled, Boolean system);

    // System operations
    void initializeDefaultConfigurations();

    void setDefaultConfiguration(String applicationName, String environment);

    void enableConfiguration(String applicationName, String environment);

    void disableConfiguration(String applicationName, String environment);

    // JWT-specific methods
    String getJwtSecret(String applicationName);

    String getJwtSecret(String applicationName, String environment);

    long getJwtExpiration(String applicationName);

    long getJwtExpiration(String applicationName, String environment);

    long getJwtRefreshExpiration(String applicationName);

    long getJwtRefreshExpiration(String applicationName, String environment);

    String getJwtIssuer(String applicationName);

    String getJwtIssuer(String applicationName, String environment);

    String getJwtAudience(String applicationName);

    String getJwtAudience(String applicationName, String environment);

    ApplicationJwtConfig.JwtAlgorithm getJwtAlgorithm(String applicationName);

    ApplicationJwtConfig.JwtAlgorithm getJwtAlgorithm(String applicationName, String environment);
} 