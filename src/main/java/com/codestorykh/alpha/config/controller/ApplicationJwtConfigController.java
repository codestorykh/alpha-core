package com.codestorykh.alpha.config.controller;

import com.codestorykh.alpha.config.dto.ApplicationJwtConfigDTO;
import com.codestorykh.alpha.config.dto.CreateApplicationJwtConfigRequest;
import com.codestorykh.alpha.config.dto.UpdateApplicationJwtConfigRequest;
import com.codestorykh.alpha.config.service.ApplicationJwtConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/jwt-configs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Application JWT Configuration", description = "API endpoints for managing JWT configurations per application")
public class ApplicationJwtConfigController {

    private final ApplicationJwtConfigService applicationJwtConfigService;

    @PostMapping
    @Operation(summary = "Create JWT configuration", description = "Create a new JWT configuration for an application")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApplicationJwtConfigDTO> createConfiguration(
            @Valid @RequestBody CreateApplicationJwtConfigRequest request) {
        log.info("Creating JWT configuration for application: {}", request.getApplicationName());
        ApplicationJwtConfigDTO config = applicationJwtConfigService.createConfiguration(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(config);
    }

    @GetMapping
    @Operation(summary = "Get all JWT configurations", description = "Retrieve all JWT configurations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ApplicationJwtConfigDTO>> getAllConfigurations() {
        List<ApplicationJwtConfigDTO> configs = applicationJwtConfigService.findAll();
        return ResponseEntity.ok(configs);
    }

    @GetMapping("/enabled")
    @Operation(summary = "Get enabled JWT configurations", description = "Retrieve all enabled JWT configurations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ApplicationJwtConfigDTO>> getEnabledConfigurations() {
        List<ApplicationJwtConfigDTO> configs = applicationJwtConfigService.findAllEnabled();
        return ResponseEntity.ok(configs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get JWT configuration by ID", description = "Retrieve a JWT configuration by its ID")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApplicationJwtConfigDTO> getConfigurationById(
            @Parameter(description = "Configuration ID") @PathVariable Long id) {
        return applicationJwtConfigService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/application/{applicationName}")
    @Operation(summary = "Get JWT configuration by application name", description = "Retrieve JWT configuration for a specific application")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApplicationJwtConfigDTO> getConfigurationByApplicationName(
            @Parameter(description = "Application name") @PathVariable String applicationName) {
        return applicationJwtConfigService.findByApplicationName(applicationName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/application/{applicationName}/environment/{environment}")
    @Operation(summary = "Get JWT configuration by application and environment", description = "Retrieve JWT configuration for a specific application and environment")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApplicationJwtConfigDTO> getConfigurationByApplicationAndEnvironment(
            @Parameter(description = "Application name") @PathVariable String applicationName,
            @Parameter(description = "Environment name") @PathVariable String environment) {
        return applicationJwtConfigService.findByApplicationNameAndEnvironment(applicationName, environment)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/environment/{environment}")
    @Operation(summary = "Get JWT configurations by environment", description = "Retrieve all JWT configurations for a specific environment")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ApplicationJwtConfigDTO>> getConfigurationsByEnvironment(
            @Parameter(description = "Environment name") @PathVariable String environment) {
        List<ApplicationJwtConfigDTO> configs = applicationJwtConfigService.findByEnvironment(environment);
        return ResponseEntity.ok(configs);
    }

    @GetMapping("/search")
    @Operation(summary = "Search JWT configurations", description = "Search JWT configurations with filters")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ApplicationJwtConfigDTO>> searchConfigurations(
            @Parameter(description = "Application name filter") @RequestParam(required = false) String applicationName,
            @Parameter(description = "Environment filter") @RequestParam(required = false) String environment,
            @Parameter(description = "Enabled filter") @RequestParam(required = false) Boolean enabled,
            @Parameter(description = "System filter") @RequestParam(required = false) Boolean system) {
        List<ApplicationJwtConfigDTO> configs = applicationJwtConfigService.searchConfigurations(applicationName, environment, enabled, system);
        return ResponseEntity.ok(configs);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update JWT configuration by ID", description = "Update an existing JWT configuration by its ID")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApplicationJwtConfigDTO> updateConfiguration(
            @Parameter(description = "Configuration ID") @PathVariable Long id,
            @Valid @RequestBody UpdateApplicationJwtConfigRequest request) {
        try {
            ApplicationJwtConfigDTO config = applicationJwtConfigService.updateConfiguration(id, request);
            return ResponseEntity.ok(config);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/application/{applicationName}/environment/{environment}")
    @Operation(summary = "Update JWT configuration by application and environment", description = "Update JWT configuration for a specific application and environment")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApplicationJwtConfigDTO> updateConfigurationByApplicationAndEnvironment(
            @Parameter(description = "Application name") @PathVariable String applicationName,
            @Parameter(description = "Environment name") @PathVariable String environment,
            @Valid @RequestBody UpdateApplicationJwtConfigRequest request) {
        try {
            ApplicationJwtConfigDTO config = applicationJwtConfigService.updateConfiguration(applicationName, environment, request);
            return ResponseEntity.ok(config);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete JWT configuration by ID", description = "Delete a JWT configuration by its ID")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteConfiguration(
            @Parameter(description = "Configuration ID") @PathVariable Long id) {
        try {
            applicationJwtConfigService.deleteConfiguration(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/application/{applicationName}/environment/{environment}")
    @Operation(summary = "Delete JWT configuration by application and environment", description = "Delete JWT configuration for a specific application and environment")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteConfigurationByApplicationAndEnvironment(
            @Parameter(description = "Application name") @PathVariable String applicationName,
            @Parameter(description = "Environment name") @PathVariable String environment) {
        try {
            applicationJwtConfigService.deleteConfiguration(applicationName, environment);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/application/{applicationName}/environment/{environment}/enable")
    @Operation(summary = "Enable JWT configuration", description = "Enable a JWT configuration for a specific application and environment")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> enableConfiguration(
            @Parameter(description = "Application name") @PathVariable String applicationName,
            @Parameter(description = "Environment name") @PathVariable String environment) {
        try {
            applicationJwtConfigService.enableConfiguration(applicationName, environment);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/application/{applicationName}/environment/{environment}/disable")
    @Operation(summary = "Disable JWT configuration", description = "Disable a JWT configuration for a specific application and environment")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> disableConfiguration(
            @Parameter(description = "Application name") @PathVariable String applicationName,
            @Parameter(description = "Environment name") @PathVariable String environment) {
        try {
            applicationJwtConfigService.disableConfiguration(applicationName, environment);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/application/{applicationName}/environment/{environment}/set-default")
    @Operation(summary = "Set default JWT configuration", description = "Set a JWT configuration as the default for a specific application and environment")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> setDefaultConfiguration(
            @Parameter(description = "Application name") @PathVariable String applicationName,
            @Parameter(description = "Environment name") @PathVariable String environment) {
        try {
            applicationJwtConfigService.setDefaultConfiguration(applicationName, environment);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/applications")
    @Operation(summary = "Get all application names", description = "Retrieve all application names that have JWT configurations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> getAllApplicationNames() {
        List<String> applicationNames = applicationJwtConfigService.getAllApplicationNames();
        return ResponseEntity.ok(applicationNames);
    }

    @GetMapping("/environments")
    @Operation(summary = "Get all environments", description = "Retrieve all environment names that have JWT configurations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> getAllEnvironments() {
        List<String> environments = applicationJwtConfigService.getAllEnvironments();
        return ResponseEntity.ok(environments);
    }

    // Public endpoints for JWT configuration retrieval (used by other services)
    @GetMapping("/public/application/{applicationName}/secret")
    @Operation(summary = "Get JWT secret for application", description = "Retrieve JWT secret for a specific application (public endpoint)")
    public ResponseEntity<String> getJwtSecret(
            @Parameter(description = "Application name") @PathVariable String applicationName) {
        try {
            String secret = applicationJwtConfigService.getJwtSecret(applicationName);
            return ResponseEntity.ok(secret);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/public/application/{applicationName}/environment/{environment}/secret")
    @Operation(summary = "Get JWT secret for application and environment", description = "Retrieve JWT secret for a specific application and environment (public endpoint)")
    public ResponseEntity<String> getJwtSecretByEnvironment(
            @Parameter(description = "Application name") @PathVariable String applicationName,
            @Parameter(description = "Environment name") @PathVariable String environment) {
        try {
            String secret = applicationJwtConfigService.getJwtSecret(applicationName, environment);
            return ResponseEntity.ok(secret);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/public/application/{applicationName}/config")
    @Operation(summary = "Get JWT configuration for application", description = "Retrieve complete JWT configuration for a specific application (public endpoint)")
    public ResponseEntity<ApplicationJwtConfigDTO> getJwtConfig(
            @Parameter(description = "Application name") @PathVariable String applicationName) {
        try {
            ApplicationJwtConfigDTO config = applicationJwtConfigService.findByApplicationName(applicationName)
                    .orElseThrow(() -> new IllegalArgumentException("Configuration not found"));
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/public/application/{applicationName}/environment/{environment}/config")
    @Operation(summary = "Get JWT configuration for application and environment", description = "Retrieve complete JWT configuration for a specific application and environment (public endpoint)")
    public ResponseEntity<ApplicationJwtConfigDTO> getJwtConfigByEnvironment(
            @Parameter(description = "Application name") @PathVariable String applicationName,
            @Parameter(description = "Environment name") @PathVariable String environment) {
        try {
            ApplicationJwtConfigDTO config = applicationJwtConfigService.findByApplicationNameAndEnvironment(applicationName, environment)
                    .orElseThrow(() -> new IllegalArgumentException("Configuration not found"));
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
} 