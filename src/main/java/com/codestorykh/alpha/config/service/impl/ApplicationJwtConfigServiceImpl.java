package com.codestorykh.alpha.config.service.impl;

import com.codestorykh.alpha.config.domain.ApplicationJwtConfig;
import com.codestorykh.alpha.config.dto.ApplicationJwtConfigDTO;
import com.codestorykh.alpha.config.dto.CreateApplicationJwtConfigRequest;
import com.codestorykh.alpha.config.dto.UpdateApplicationJwtConfigRequest;
import com.codestorykh.alpha.config.repository.ApplicationJwtConfigRepository;
import com.codestorykh.alpha.config.service.ApplicationJwtConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ApplicationJwtConfigServiceImpl implements ApplicationJwtConfigService {

    private final ApplicationJwtConfigRepository applicationJwtConfigRepository;

    @Override
    public ApplicationJwtConfigDTO createConfiguration(CreateApplicationJwtConfigRequest request) {
        log.info("Creating JWT configuration for application: {} in environment: {}", 
                request.getApplicationName(), request.getEnvironment());

        // Check if configuration already exists
        if (existsByApplicationNameAndEnvironment(request.getApplicationName(), request.getEnvironment())) {
            throw new IllegalArgumentException("JWT configuration already exists for application: " + 
                    request.getApplicationName() + " in environment: " + request.getEnvironment());
        }

        // If this is set as default, unset other defaults
        if (request.isDefault()) {
            unsetOtherDefaults();
        }

        ApplicationJwtConfig config = ApplicationJwtConfig.builder()
                .applicationName(request.getApplicationName())
                .environment(request.getEnvironment())
                .secret(request.getSecret())
                .expirationMs(request.getExpirationMs())
                .refreshExpirationMs(request.getRefreshExpirationMs())
                .issuer(request.getIssuer())
                .audience(request.getAudience())
                .enabled(request.isEnabled())
                .description(request.getDescription())
                .isDefault(request.isDefault())
                .system(request.isSystem())
                .readonly(request.isReadonly())
                .algorithm(request.getAlgorithm())
                .keyId(request.getKeyId())
                .additionalClaims(request.getAdditionalClaims())
                .build();

        ApplicationJwtConfig savedConfig = applicationJwtConfigRepository.save(config);
        log.info("Created JWT configuration with ID: {} for application: {}", 
                savedConfig.getId(), savedConfig.getApplicationName());

        return convertToDTO(savedConfig);
    }

    @Override
    public ApplicationJwtConfigDTO updateConfiguration(Long id, UpdateApplicationJwtConfigRequest request) {
        log.info("Updating JWT configuration with ID: {}", id);

        ApplicationJwtConfig config = applicationJwtConfigRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("JWT configuration not found with ID: " + id));

        return updateConfigurationInternal(config, request);
    }

    @Override
    public ApplicationJwtConfigDTO updateConfiguration(String applicationName, String environment, UpdateApplicationJwtConfigRequest request) {
        log.info("Updating JWT configuration for application: {} in environment: {}", applicationName, environment);

        ApplicationJwtConfig config = applicationJwtConfigRepository
                .findByApplicationNameAndEnvironmentAndActiveTrue(applicationName, environment)
                .orElseThrow(() -> new IllegalArgumentException("JWT configuration not found for application: " + 
                        applicationName + " in environment: " + environment));

        return updateConfigurationInternal(config, request);
    }

    private ApplicationJwtConfigDTO updateConfigurationInternal(ApplicationJwtConfig config, UpdateApplicationJwtConfigRequest request) {
        // Check if configuration is readonly
        if (config.isReadonlyConfig()) {
            throw new IllegalStateException("Cannot update readonly JWT configuration");
        }

        // Update fields if provided
        if (request.getSecret() != null) {
            config.setSecret(request.getSecret());
        }
        if (request.getExpirationMs() != null) {
            config.setExpirationMs(request.getExpirationMs());
        }
        if (request.getRefreshExpirationMs() != null) {
            config.setRefreshExpirationMs(request.getRefreshExpirationMs());
        }
        if (request.getIssuer() != null) {
            config.setIssuer(request.getIssuer());
        }
        if (request.getAudience() != null) {
            config.setAudience(request.getAudience());
        }
        if (request.getEnabled() != null) {
            config.setEnabled(request.getEnabled());
        }
        if (request.getDescription() != null) {
            config.setDescription(request.getDescription());
        }
        if (request.getIsDefault() != null) {
            if (request.getIsDefault()) {
                unsetOtherDefaults();
            }
            config.setDefault(request.getIsDefault());
        }
        if (request.getSystem() != null) {
            config.setSystem(request.getSystem());
        }
        if (request.getReadonly() != null) {
            config.setReadonly(request.getReadonly());
        }
        if (request.getAlgorithm() != null) {
            config.setAlgorithm(request.getAlgorithm());
        }
        if (request.getKeyId() != null) {
            config.setKeyId(request.getKeyId());
        }
        if (request.getAdditionalClaims() != null) {
            config.setAdditionalClaims(request.getAdditionalClaims());
        }

        ApplicationJwtConfig savedConfig = applicationJwtConfigRepository.save(config);
        log.info("Updated JWT configuration for application: {}", savedConfig.getApplicationName());

        return convertToDTO(savedConfig);
    }

    @Override
    public void deleteConfiguration(Long id) {
        log.info("Deleting JWT configuration with ID: {}", id);

        ApplicationJwtConfig config = applicationJwtConfigRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("JWT configuration not found with ID: " + id));

        if (config.isReadonlyConfig()) {
            throw new IllegalStateException("Cannot delete readonly JWT configuration");
        }

        applicationJwtConfigRepository.delete(config);
        log.info("Deleted JWT configuration with ID: {}", id);
    }

    @Override
    public void deleteConfiguration(String applicationName, String environment) {
        log.info("Deleting JWT configuration for application: {} in environment: {}", applicationName, environment);

        ApplicationJwtConfig config = applicationJwtConfigRepository
                .findByApplicationNameAndEnvironmentAndActiveTrue(applicationName, environment)
                .orElseThrow(() -> new IllegalArgumentException("JWT configuration not found for application: " + 
                        applicationName + " in environment: " + environment));

        if (config.isReadonlyConfig()) {
            throw new IllegalStateException("Cannot delete readonly JWT configuration");
        }

        applicationJwtConfigRepository.delete(config);
        log.info("Deleted JWT configuration for application: {} in environment: {}", applicationName, environment);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ApplicationJwtConfigDTO> findById(Long id) {
        return applicationJwtConfigRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ApplicationJwtConfigDTO> findByApplicationName(String applicationName) {
        return applicationJwtConfigRepository.findByApplicationNameAndActiveTrue(applicationName)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ApplicationJwtConfigDTO> findByApplicationNameAndEnvironment(String applicationName, String environment) {
        return applicationJwtConfigRepository.findByApplicationNameAndEnvironmentAndActiveTrue(applicationName, environment)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationJwtConfigDTO> findAll() {
        return applicationJwtConfigRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationJwtConfigDTO> findAllEnabled() {
        return applicationJwtConfigRepository.findAllEnabled().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationJwtConfigDTO> findByEnvironment(String environment) {
        return applicationJwtConfigRepository.findEnabledByEnvironment(environment).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationJwtConfig getConfigurationForApplication(String applicationName) {
        return applicationJwtConfigRepository.findEnabledByApplicationName(applicationName)
                .orElseGet(this::getDefaultConfiguration);
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationJwtConfig getConfigurationForApplication(String applicationName, String environment) {
        return applicationJwtConfigRepository.findEnabledByApplicationNameAndEnvironment(applicationName, environment)
                .orElseGet(this::getDefaultConfiguration);
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationJwtConfig getDefaultConfiguration() {
        return applicationJwtConfigRepository.findDefaultConfiguration()
                .orElseThrow(() -> new IllegalStateException("No default JWT configuration found"));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByApplicationName(String applicationName) {
        return applicationJwtConfigRepository.existsByApplicationName(applicationName);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByApplicationNameAndEnvironment(String applicationName, String environment) {
        return applicationJwtConfigRepository.existsByApplicationNameAndEnvironment(applicationName, environment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllApplicationNames() {
        return applicationJwtConfigRepository.findAllApplicationNames();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllEnvironments() {
        return applicationJwtConfigRepository.findAllEnvironments();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationJwtConfigDTO> searchConfigurations(String applicationName, String environment, Boolean enabled, Boolean system) {
        return applicationJwtConfigRepository.searchConfigurations(applicationName, environment, enabled, system).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void initializeDefaultConfigurations() {
        log.info("Initializing default JWT configurations...");

        // Create default configuration if it doesn't exist
        if (!applicationJwtConfigRepository.findDefaultConfiguration().isPresent()) {
            CreateApplicationJwtConfigRequest defaultRequest = CreateApplicationJwtConfigRequest.builder()
                    .applicationName("default")
                    .environment("default")
                    .secret("5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437")
                    .expirationMs(3600000L)
                    .refreshExpirationMs(86400000L)
                    .issuer("alpha-identity-server")
                    .audience("alpha-clients")
                    .enabled(true)
                    .description("Default JWT configuration for all applications")
                    .isDefault(true)
                    .system(true)
                    .readonly(false)
                    .algorithm(ApplicationJwtConfig.JwtAlgorithm.HS256)
                    .build();

            createConfiguration(defaultRequest);
            log.info("Created default JWT configuration");
        }
    }

    @Override
    public void setDefaultConfiguration(String applicationName, String environment) {
        log.info("Setting default JWT configuration for application: {} in environment: {}", applicationName, environment);

        // Unset current defaults
        unsetOtherDefaults();

        // Set new default
        ApplicationJwtConfig config = applicationJwtConfigRepository
                .findByApplicationNameAndEnvironmentAndActiveTrue(applicationName, environment)
                .orElseThrow(() -> new IllegalArgumentException("JWT configuration not found for application: " + 
                        applicationName + " in environment: " + environment));

        config.setDefault(true);
        applicationJwtConfigRepository.save(config);
        log.info("Set default JWT configuration for application: {} in environment: {}", applicationName, environment);
    }

    @Override
    public void enableConfiguration(String applicationName, String environment) {
        log.info("Enabling JWT configuration for application: {} in environment: {}", applicationName, environment);

        ApplicationJwtConfig config = applicationJwtConfigRepository
                .findByApplicationNameAndEnvironmentAndActiveTrue(applicationName, environment)
                .orElseThrow(() -> new IllegalArgumentException("JWT configuration not found for application: " + 
                        applicationName + " in environment: " + environment));

        config.setEnabled(true);
        applicationJwtConfigRepository.save(config);
        log.info("Enabled JWT configuration for application: {} in environment: {}", applicationName, environment);
    }

    @Override
    public void disableConfiguration(String applicationName, String environment) {
        log.info("Disabling JWT configuration for application: {} in environment: {}", applicationName, environment);

        ApplicationJwtConfig config = applicationJwtConfigRepository
                .findByApplicationNameAndEnvironmentAndActiveTrue(applicationName, environment)
                .orElseThrow(() -> new IllegalArgumentException("JWT configuration not found for application: " + 
                        applicationName + " in environment: " + environment));

        config.setEnabled(false);
        applicationJwtConfigRepository.save(config);
        log.info("Disabled JWT configuration for application: {} in environment: {}", applicationName, environment);
    }

    // JWT-specific methods
    @Override
    @Transactional(readOnly = true)
    public String getJwtSecret(String applicationName) {
        return getConfigurationForApplication(applicationName).getSecret();
    }

    @Override
    @Transactional(readOnly = true)
    public String getJwtSecret(String applicationName, String environment) {
        return getConfigurationForApplication(applicationName, environment).getSecret();
    }

    @Override
    @Transactional(readOnly = true)
    public long getJwtExpiration(String applicationName) {
        return getConfigurationForApplication(applicationName).getExpirationMs();
    }

    @Override
    @Transactional(readOnly = true)
    public long getJwtExpiration(String applicationName, String environment) {
        return getConfigurationForApplication(applicationName, environment).getExpirationMs();
    }

    @Override
    @Transactional(readOnly = true)
    public long getJwtRefreshExpiration(String applicationName) {
        return getConfigurationForApplication(applicationName).getRefreshExpirationMs();
    }

    @Override
    @Transactional(readOnly = true)
    public long getJwtRefreshExpiration(String applicationName, String environment) {
        return getConfigurationForApplication(applicationName, environment).getRefreshExpirationMs();
    }

    @Override
    @Transactional(readOnly = true)
    public String getJwtIssuer(String applicationName) {
        return getConfigurationForApplication(applicationName).getIssuer();
    }

    @Override
    @Transactional(readOnly = true)
    public String getJwtIssuer(String applicationName, String environment) {
        return getConfigurationForApplication(applicationName, environment).getIssuer();
    }

    @Override
    @Transactional(readOnly = true)
    public String getJwtAudience(String applicationName) {
        return getConfigurationForApplication(applicationName).getAudience();
    }

    @Override
    @Transactional(readOnly = true)
    public String getJwtAudience(String applicationName, String environment) {
        return getConfigurationForApplication(applicationName, environment).getAudience();
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationJwtConfig.JwtAlgorithm getJwtAlgorithm(String applicationName) {
        return getConfigurationForApplication(applicationName).getAlgorithm();
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationJwtConfig.JwtAlgorithm getJwtAlgorithm(String applicationName, String environment) {
        return getConfigurationForApplication(applicationName, environment).getAlgorithm();
    }

    // Private helper methods
    private void unsetOtherDefaults() {
        applicationJwtConfigRepository.findAll().stream()
                .filter(ApplicationJwtConfig::isDefaultConfig)
                .forEach(config -> {
                    config.setDefault(false);
                    applicationJwtConfigRepository.save(config);
                });
    }

    private ApplicationJwtConfigDTO convertToDTO(ApplicationJwtConfig config) {
        var app = ApplicationJwtConfigDTO.builder()
                .applicationName(config.getApplicationName())
                .environment(config.getEnvironment())
                .secret(config.getSecret())
                .expirationMs(config.getExpirationMs())
                .refreshExpirationMs(config.getRefreshExpirationMs())
                .issuer(config.getIssuer())
                .audience(config.getAudience())
                .enabled(config.isEnabled())
                .description(config.getDescription())
                .isDefault(config.isDefaultConfig())
                .system(config.isSystemConfig())
                .readonly(config.isReadonlyConfig())
                .algorithm(config.getAlgorithm())
                .keyId(config.getKeyId())
                .additionalClaims(config.getAdditionalClaims())
                .build();
        app.setId(config.getId());
        app.setCreatedAt(config.getCreatedAt());
        app.setUpdatedAt(config.getUpdatedAt());
        app.setActive(config.isActive());
        return app;
    }
} 