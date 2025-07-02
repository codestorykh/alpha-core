package com.codestorykh.alpha.config;

import com.codestorykh.alpha.config.properties.JwtConfigurationProperties;
import com.codestorykh.alpha.config.service.ConfigurationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
@Slf4j
public class JwtConfigurationValidator {

    private final JwtConfigurationProperties jwtConfigurationProperties;
    private final ConfigurationService configurationService;
    private final SecretKey signingKey;

    public JwtConfigurationValidator(JwtConfigurationProperties jwtConfigurationProperties,
                                     ConfigurationService configurationService,
                                     SecretKey signingKey) {
        this.jwtConfigurationProperties = jwtConfigurationProperties;
        this.configurationService = configurationService;
        this.signingKey = signingKey;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void validateJwtConfiguration() {
        log.info("🔐 Starting JWT configuration validation...");
        
        try {
            // 1. Validate JWT secret
            validateJwtSecret();
            
            // 2. Validate signing key
            validateSigningKey();
            
            // 3. Validate configuration consistency
            validateConfigurationConsistency();
            
            // 4. Test token generation and validation
            testTokenGenerationAndValidation();
            
            log.info("✅ JWT configuration validation completed successfully");
            
        } catch (Exception e) {
            log.error("❌ JWT configuration validation failed", e);
            // Don't throw the exception to allow the application to start
            // but log it clearly for debugging
        }
    }

    private void validateJwtSecret() {
        log.info("🔐 Validating JWT secret...");
        
        String secret = jwtConfigurationProperties.getJwtSecret();
        
        if (secret == null || secret.trim().isEmpty()) {
            log.error("❌ JWT secret is null or empty");
            return;
        }
        
        if (secret.length() < 32) {
            log.warn("⚠️ JWT secret is too short ({} characters). Minimum recommended: 32 characters", secret.length());
        }
        
        if (secret.matches("[0-9a-fA-F]+")) {
            log.info("✅ JWT secret is valid hex string ({} characters)", secret.length());
        } else {
            log.info("✅ JWT secret is valid string ({} characters)", secret.length());
        }
    }

    private void validateSigningKey() {
        log.info("🔐 Validating signing key...");
        
        if (signingKey == null) {
            log.error("❌ Signing key is null");
            return;
        }
        
        log.info("✅ Signing key algorithm: {}", signingKey.getAlgorithm());
        log.info("✅ Signing key format: {}", signingKey.getFormat());
        
        if (signingKey.getEncoded() != null) {
            log.info("✅ Signing key encoded length: {} bytes", signingKey.getEncoded().length);
            
            if (signingKey.getEncoded().length < 32) {
                log.warn("⚠️ Signing key is shorter than recommended ({} bytes). Minimum: 32 bytes", signingKey.getEncoded().length);
            }
        } else {
            log.warn("⚠️ Signing key encoded bytes are null");
        }
    }

    private void validateConfigurationConsistency() {
        log.info("🔐 Validating configuration consistency...");
        
        // Check database vs properties secret
        try {
            String dbSecret = configurationService.getJwtSecret();
            String propSecret = jwtConfigurationProperties.getJwtSecret();
            
            if (!dbSecret.equals(propSecret)) {
                log.warn("⚠️ Database and properties JWT secrets do not match!");
                log.warn("⚠️ Database secret length: {}", dbSecret.length());
                log.warn("⚠️ Properties secret length: {}", propSecret.length());
            } else {
                log.info("✅ Database and properties JWT secrets match");
            }
        } catch (Exception e) {
            log.warn("⚠️ Could not compare database and properties secrets: {}", e.getMessage());
        }
        
        // Check expiration times
        long expiration = jwtConfigurationProperties.getJwtExpiration();
        long refreshExpiration = jwtConfigurationProperties.getJwtRefreshExpiration();
        
        if (expiration <= 0) {
            log.error("❌ JWT expiration time is invalid: {}", expiration);
        } else {
            log.info("✅ JWT expiration time: {}ms ({} minutes)", expiration, expiration / 60000);
        }
        
        if (refreshExpiration <= 0) {
            log.error("❌ JWT refresh expiration time is invalid: {}", refreshExpiration);
        } else {
            log.info("✅ JWT refresh expiration time: {}ms ({} hours)", refreshExpiration, refreshExpiration / 3600000);
        }
        
        if (refreshExpiration <= expiration) {
            log.warn("⚠️ Refresh expiration time should be longer than access token expiration");
        }
    }

    private void testTokenGenerationAndValidation() {
        log.info("🔐 Testing token generation and validation...");
        
        try {
            // Generate a test token
            String testToken = io.jsonwebtoken.Jwts.builder()
                    .setSubject("validation-test")
                    .setIssuedAt(new java.util.Date())
                    .setExpiration(new java.util.Date(System.currentTimeMillis() + 60000))
                    .signWith(signingKey, io.jsonwebtoken.SignatureAlgorithm.HS256)
                    .compact();
            
            log.info("✅ Test token generated successfully (length: {})", testToken.length());
            
            // Validate the test token
            io.jsonwebtoken.Claims claims = io.jsonwebtoken.Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(testToken)
                    .getBody();
            
            log.info("✅ Test token validated successfully");
            log.info("✅ Token subject: {}", claims.getSubject());
            log.info("✅ Token expiration: {}", claims.getExpiration());
            
        } catch (Exception e) {
            log.error("❌ Token generation and validation test failed", e);
        }
    }

    /**
     * Get JWT configuration summary for monitoring
     */
    public String getJwtConfigurationSummary() {
        try {
            return String.format(
                "JWT Config - Secret: %d chars, Expiration: %dms, Refresh: %dms, Key: %s/%s",
                jwtConfigurationProperties.getJwtSecret().length(),
                jwtConfigurationProperties.getJwtExpiration(),
                jwtConfigurationProperties.getJwtRefreshExpiration(),
                signingKey.getAlgorithm(),
                signingKey.getFormat()
            );
        } catch (Exception e) {
            return "JWT Config - Error: " + e.getMessage();
        }
    }
} 