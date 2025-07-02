package com.codestorykh.alpha.config.service.impl;

import com.codestorykh.alpha.config.domain.Configuration;
import com.codestorykh.alpha.config.repository.ConfigurationRepository;
import com.codestorykh.alpha.config.service.ConfigurationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ConfigurationServiceImpl implements ConfigurationService {

    private final ConfigurationRepository configurationRepository;

    @Override
    public Optional<Configuration> findByKey(String key) {
        return configurationRepository.findActiveByKey(key);
    }

    @Override
    public Optional<String> getValue(String key) {
        return configurationRepository.findValueByKey(key);
    }

    @Override
    public Optional<String> getValue(String key, String defaultValue) {
        return getValue(key).or(() -> Optional.of(defaultValue));
    }

    @Override
    public Optional<Long> getLongValue(String key) {
        return getValue(key).map(Long::parseLong);
    }

    @Override
    public Optional<Long> getLongValue(String key, Long defaultValue) {
        return getLongValue(key).or(() -> Optional.of(defaultValue));
    }

    @Override
    public Optional<Integer> getIntValue(String key) {
        return getValue(key).map(Integer::parseInt);
    }

    @Override
    public Optional<Integer> getIntValue(String key, Integer defaultValue) {
        return getIntValue(key).or(() -> Optional.of(defaultValue));
    }

    @Override
    public Optional<Boolean> getBooleanValue(String key) {
        return getValue(key).map(Boolean::parseBoolean);
    }

    @Override
    public Optional<Boolean> getBooleanValue(String key, Boolean defaultValue) {
        return getBooleanValue(key).or(() -> Optional.of(defaultValue));
    }

    @Override
    public Optional<Double> getDoubleValue(String key) {
        return getValue(key).map(Double::parseDouble);
    }

    @Override
    public Optional<Double> getDoubleValue(String key, Double defaultValue) {
        return getDoubleValue(key).or(() -> Optional.of(defaultValue));
    }

    @Override
    public List<Configuration> findByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return configurationRepository.findAllActiveSystemConfigurations();
        }
        return configurationRepository.findActiveByCategory(category);
    }

    @Override
    public Configuration save(Configuration configuration) {
        return configurationRepository.save(configuration);
    }

    @Override
    public Configuration updateValue(String key, String value) {
        Optional<Configuration> existingConfig = findByKey(key);
        if (existingConfig.isPresent()) {
            Configuration config = existingConfig.get();
            config.setValue(value);
            return configurationRepository.save(config);
        } else {
            throw new IllegalArgumentException("Configuration with key '" + key + "' not found");
        }
    }

    @Override
    public void deleteByKey(String key) {
        Optional<Configuration> config = findByKey(key);
        if (config.isPresent()) {
            Configuration configuration = config.get();
            configuration.setActive(false);
            configurationRepository.save(configuration);
        }
    }

    @Override
    public boolean existsByKey(String key) {
        return configurationRepository.existsByKey(key);
    }

    // JWT specific methods
    @Override
    public String getJwtSecret() {
        String secret = getValue("jwt.secret", "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437").orElse("5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437");
        
        // Validate secret length (must be at least 256 bits = 32 bytes = 64 hex characters)
        if (secret.length() < 64) {
            log.warn("JWT secret is too short ({} characters). Minimum required: 64 characters for HS256", secret.length());
            // Use a secure fallback
            secret = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";
        }
        
        log.debug("Retrieved JWT secret from database, length: {}", secret.length());
        return secret;
    }

    @Override
    public long getJwtExpiration() {
        long expiration = getLongValue("jwt.expiration", 3600000L).orElse(3600000L); // 1 hour default
        log.debug("Retrieved JWT expiration from database: {}ms", expiration);
        return expiration;
    }

    @Override
    public long getJwtRefreshExpiration() {
        long expiration = getLongValue("jwt.refresh-expiration", 86400000L).orElse(86400000L); // 24 hours default
        log.debug("Retrieved JWT refresh expiration from database: {}ms", expiration);
        return expiration;
    }

    @Override
    public String getJwtIssuer() {
        String issuer = getValue("jwt.issuer", "alpha-identity-server").orElse("alpha-identity-server");
        log.debug("Retrieved JWT issuer from database: {}", issuer);
        return issuer;
    }

    @Override
    public String getJwtAudience() {
        String audience = getValue("jwt.audience", "alpha-clients").orElse("alpha-clients");
        log.debug("Retrieved JWT audience from database: {}", audience);
        return audience;
    }

    @Override
    public void initializeDefaultConfigurations() {
        log.info("Initializing default configurations...");

        // JWT Configurations - Use consistent secret instead of generating new one
        String consistentJwtSecret = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";
        createIfNotExists("jwt.secret", 
            consistentJwtSecret, 
            "JWT", 
            "JWT signing secret key (must be at least 256 bits)", 
            false, true, false, Configuration.ConfigurationValueType.ENCRYPTED);

        createIfNotExists("jwt.expiration", 
            "3600000", 
            "JWT", 
            "JWT access token expiration time in milliseconds (1 hour)", 
            false, true, false, Configuration.ConfigurationValueType.LONG);

        createIfNotExists("jwt.refresh-expiration", 
            "86400000", 
            "JWT", 
            "JWT refresh token expiration time in milliseconds (24 hours)", 
            false, true, false, Configuration.ConfigurationValueType.LONG);

        createIfNotExists("jwt.issuer", 
            "alpha-identity-server", 
            "JWT", 
            "JWT issuer claim", 
            false, true, false, Configuration.ConfigurationValueType.STRING);

        createIfNotExists("jwt.audience", 
            "alpha-clients", 
            "JWT", 
            "JWT audience claim", 
            false, true, false, Configuration.ConfigurationValueType.STRING);

        // Application Configurations
        createIfNotExists("app.name", 
            "alpha-oauth2", 
            "APPLICATION", 
            "Application name", 
            false, true, false, Configuration.ConfigurationValueType.STRING);

        createIfNotExists("app.version", 
            "1.0.0", 
            "APPLICATION", 
            "Application version", 
            false, true, false, Configuration.ConfigurationValueType.STRING);

        createIfNotExists("app.environment", 
            "dev", 
            "APPLICATION", 
            "Application environment", 
            false, true, false, Configuration.ConfigurationValueType.STRING);

        // OAuth2 Configurations
        createIfNotExists("oauth2.scopes", 
            "read,write,admin,user", 
            "OAUTH2", 
            "Comma-separated list of OAuth2 scopes available for tokens", 
            false, true, false, Configuration.ConfigurationValueType.STRING);

        createIfNotExists("oauth2.default-scopes", 
            "read,write", 
            "OAUTH2", 
            "Default OAuth2 scopes assigned to new tokens", 
            false, true, false, Configuration.ConfigurationValueType.STRING);

        log.info("Default configurations initialized successfully");
        log.info("Using consistent JWT secret: {}", consistentJwtSecret.substring(0, 16) + "...");
    }

    @Override
    public void refreshJwtConfiguration() {
        log.info("Refreshing JWT configuration...");
        // This method can be used to clear any cached JWT configuration
        // For now, we'll just log that the configuration should be refreshed
        log.info("JWT configuration refresh requested. New settings will be used on next JWT operation.");
    }

    @Override
    public List<String> getOAuth2Scopes() {
        String scopesValue = getValue("oauth2.default-scopes", "read,write").orElse("read,write");
        List<String> scopes = Arrays.stream(scopesValue.split(","))
            .map(String::trim)
            .filter(scope -> !scope.isEmpty())
            .collect(java.util.stream.Collectors.toList());
        
        log.debug("Retrieved OAuth2 scopes from database: {}", scopes);
        return scopes;
    }

    @Override
    public List<String> getAvailableOAuth2Scopes() {
        String scopesValue = getValue("oauth2.scopes", "read,write,admin,user").orElse("read,write,admin,user");
        List<String> scopes = Arrays.asList(scopesValue.split(","))
            .stream()
            .map(String::trim)
            .filter(scope -> !scope.isEmpty())
            .collect(java.util.stream.Collectors.toList());
        
        log.debug("Retrieved available OAuth2 scopes from database: {}", scopes);
        return scopes;
    }

    private void createIfNotExists(String key, String value, String category, String description, 
                                 boolean encrypted, boolean system, boolean readonly, 
                                 Configuration.ConfigurationValueType valueType) {
        if (!existsByKey(key)) {
            Configuration config = Configuration.builder()
                .key(key)
                .value(value)
                .category(category)
                .description(description)
                .encrypted(encrypted)
                .system(system)
                .readonly(readonly)
                .valueType(valueType)
                .build();
            
            configurationRepository.save(config);
            log.debug("Created configuration: {}", key);
        }
    }

    @Override
    public String generateSecureJwtSecret() {
        try {
            java.security.SecureRandom random = new java.security.SecureRandom();
            byte[] bytes = new byte[32]; // 256 bits
            random.nextBytes(bytes);
            
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            
            String secret = sb.toString();
            log.info("Generated new secure JWT secret (256 bits)");
            return secret;
        } catch (Exception e) {
            log.error("Failed to generate secure JWT secret, using fallback", e);
            // Fallback to a secure default
            return "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";
        }
    }
} 