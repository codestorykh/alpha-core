package com.codestorykh.alpha.config.properties;

import com.codestorykh.alpha.config.service.ConfigurationService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.Date;

@Configuration
@Slf4j
public class JwtConfigurationProperties {

    @Value("${jwt.secret:}")
    private String jwtSecretFromProperties;

    @Value("${jwt.expiration:3600000}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration:86400000}")
    private long jwtRefreshExpiration;

    @Value("${jwt.issuer:alpha-identity-server}")
    private String jwtIssuer;

    @Value("${jwt.audience:alpha-clients}")
    private String jwtAudience;

    private final ConfigurationService configurationService;

    public JwtConfigurationProperties(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @Bean
    @Primary
    public SecretKey jwtSigningKey() {
        String secret = getJwtSecret();
        log.info("🔐 Initializing JWT signing key with secret length: {}", secret.length());
        log.debug("🔐 JWT secret (first 16 chars): {}", secret.substring(0, Math.min(16, secret.length())));
        
        try {
            byte[] keyBytes = convertSecretToBytes(secret);
            SecretKey signingKey = Keys.hmacShaKeyFor(keyBytes);
            
            // Validate the key immediately
            validateSigningKey(signingKey);
            
            log.info("✅ JWT signing key created and validated successfully");
            return signingKey;
        } catch (Exception e) {
            log.error("❌ Error creating JWT signing key, using fallback", e);
            return generateSecureFallbackKey();
        }
    }

    /**
     * Convert JWT secret to byte array with proper validation
     */
    private byte[] convertSecretToBytes(String secret) {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT secret cannot be null or empty");
        }
        
        secret = secret.trim();
        byte[] keyBytes;
        
        if (secret.matches("[0-9a-fA-F]+")) {
            // It's a hex string, convert to bytes
            keyBytes = hexStringToByteArray(secret);
            log.debug("🔐 Converted hex JWT secret to {} bytes", keyBytes.length);
        } else {
            // It's a regular string, use as-is
            keyBytes = secret.getBytes();
            log.debug("🔐 Using JWT secret as string, {} bytes", keyBytes.length);
        }
        
        // Ensure a minimum key length for HS256 (256 bits = 32 bytes)
        if (keyBytes.length < 32) {
            log.warn("⚠️ JWT secret is too short ({} bytes), padding to 32 bytes", keyBytes.length);
            byte[] paddedKey = new byte[32];
            System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, 32));
            keyBytes = paddedKey;
        }
        
        return keyBytes;
    }

    /**
     * Validate the signing key by generating and parsing a test token
     */
    private void validateSigningKey(SecretKey signingKey) {
        try {
            // Generate test token
            String testToken = Jwts.builder()
                    .setSubject("test-validation")
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 60000))
                    .signWith(signingKey, SignatureAlgorithm.HS256)
                    .compact();
            
            // Parse and validate the test token
            Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(testToken);
            
            log.debug("✅ JWT signing key validation successful");
        } catch (Exception e) {
            log.error("❌ JWT signing key validation failed", e);
            throw new RuntimeException("JWT signing key validation failed", e);
        }
    }

    private byte[] hexStringToByteArray(String s) {
        if (s == null || s.isEmpty()) {
            throw new IllegalArgumentException("Hex string cannot be null or empty");
        }
        s = s.trim();
        if (!s.matches("[0-9a-fA-F]+")) {
            throw new IllegalArgumentException("Invalid hex string format: " + s);
        }
        if (s.length() % 2 != 0) {
            s = "0" + s;
        }
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public String getJwtSecret() {
        // Priority: 1. Database configuration, 2. Properties, 3. Use consistent fallback
        try {
            if (configurationService != null) {
                String dbSecret = configurationService.getJwtSecret();
                if (dbSecret != null && !dbSecret.trim().isEmpty()) {
                    log.debug("🔐 Using JWT secret from database configuration");
                    return dbSecret;
                }
            }
        } catch (Exception e) {
            log.warn("⚠️ Could not retrieve JWT secret from database: {}", e.getMessage());
        }

        if (jwtSecretFromProperties != null && !jwtSecretFromProperties.trim().isEmpty()) {
            log.debug("🔐 Using JWT secret from application properties");
            return jwtSecretFromProperties;
        }

        // Use a consistent fallback secret
        String consistentSecret = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";
        log.info("🔐 Using consistent JWT secret fallback (length: {})", consistentSecret.length());
        return consistentSecret;
    }

    private SecretKey generateSecureFallbackKey() {
        try {
            SecureRandom random = new SecureRandom();
            byte[] bytes = new byte[32]; // 256 bits
            random.nextBytes(bytes);
            SecretKey key = Keys.hmacShaKeyFor(bytes);
            
            // Validate the fallback key
            validateSigningKey(key);
            
            log.warn("⚠️ Generated secure fallback JWT key");
            return key;
        } catch (Exception e) {
            log.error("❌ Failed to generate secure fallback key", e);
            // Last resort - use a hardcoded key (not recommended for production)
            return Keys.hmacShaKeyFor("fallback-secret-key-for-development-only".getBytes());
        }
    }

    public long getJwtExpiration() {
        try {
            if (configurationService != null) {
                return configurationService.getJwtExpiration();
            }
        } catch (Exception e) {
            log.warn("⚠️ Could not retrieve JWT expiration from database: {}", e.getMessage());
        }
        return jwtExpiration;
    }

    public long getJwtRefreshExpiration() {
        try {
            if (configurationService != null) {
                return configurationService.getJwtRefreshExpiration();
            }
        } catch (Exception e) {
            log.warn("⚠️ Could not retrieve JWT refresh expiration from database: {}", e.getMessage());
        }
        return jwtRefreshExpiration;
    }

    public String getJwtIssuer() {
        try {
            if (configurationService != null) {
                return configurationService.getJwtIssuer();
            }
        } catch (Exception e) {
            log.warn("⚠️ Could not retrieve JWT issuer from database: {}", e.getMessage());
        }
        return jwtIssuer;
    }

    public String getJwtAudience() {
        try {
            if (configurationService != null) {
                return configurationService.getJwtAudience();
            }
        } catch (Exception e) {
            log.warn("⚠️ Could not retrieve JWT audience from database: {}", e.getMessage());
        }
        return jwtAudience;
    }

    /**
     * Get JWT configuration summary for debugging
     */
    public String getJwtConfigSummary() {
        return String.format(
            "JWT Config - Secret Length: %d, Expiration: %dms, Refresh: %dms, Issuer: %s, Audience: %s",
            getJwtSecret().length(),
            getJwtExpiration(),
            getJwtRefreshExpiration(),
            getJwtIssuer(),
            getJwtAudience()
        );
    }
} 