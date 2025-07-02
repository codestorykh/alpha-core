package com.codestorykh.alpha.security.service;

import com.codestorykh.alpha.config.properties.JwtConfigurationProperties;
import com.codestorykh.alpha.config.service.ConfigurationService;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {

    private final ConfigurationService configurationService;
    private final JwtConfigurationProperties jwtConfigurationProperties;
    private final SecretKey signingKey;

    public JwtService(ConfigurationService configurationService, JwtConfigurationProperties jwtConfigurationProperties, SecretKey signingKey) {
        this.configurationService = configurationService;
        this.jwtConfigurationProperties = jwtConfigurationProperties;
        this.signingKey = signingKey;
        log.info("🔐 JWT Service initialized with configuration: {}", jwtConfigurationProperties.getJwtConfigSummary());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.error("❌ JWT token is expired: {}", e.getMessage());
            throw new JwtException("Token is expired", e);
        } catch (UnsupportedJwtException e) {
            log.error("❌ JWT token is unsupported: {}", e.getMessage());
            throw new JwtException("Token is unsupported", e);
        } catch (MalformedJwtException e) {
            log.error("❌ JWT token is malformed: {}", e.getMessage());
            throw new JwtException("Token is malformed", e);
        } catch (SecurityException e) {
            log.error("❌ JWT signature verification failed: {}", e.getMessage());
            log.error("🔐 Current signing key algorithm: {}", signingKey.getAlgorithm());
            log.error("🔐 Current signing key format: {}", signingKey.getFormat());
            throw new JwtException("Invalid token signature", e);
        } catch (IllegalArgumentException e) {
            log.error("❌ JWT token is empty or null: {}", e.getMessage());
            throw new JwtException("Token is empty or null", e);
        } catch (Exception e) {
            log.error("❌ Unexpected error while parsing JWT token: {}", e.getMessage());
            throw new JwtException("Token parsing error", e);
        }
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        String token = buildToken(extraClaims, userDetails, jwtConfigurationProperties.getJwtExpiration());
        log.debug("✅ Generated JWT token for user: {}", userDetails.getUsername());
        return token;
    }

    public String generateRefreshToken(UserDetails userDetails) {
        String token = buildToken(new HashMap<>(), userDetails, jwtConfigurationProperties.getJwtRefreshExpiration());
        log.debug("✅ Generated refresh token for user: {}", userDetails.getUsername());
        return token;
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        log.debug("🔐 Building JWT token with expiration: {}ms", expiration);
        log.debug("🔐 User details: username={}, authorities={}", userDetails.getUsername(), userDetails.getAuthorities());
        
        try {
            // Validate signing key before use
            validateSigningKeyForUse();
            
            String token = Jwts.builder()
                    .setClaims(extraClaims)
                    .setSubject(userDetails.getUsername())
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + expiration))
                    .setIssuer(jwtConfigurationProperties.getJwtIssuer())
                    .setAudience(jwtConfigurationProperties.getJwtAudience())
                    .claim("authorities", userDetails.getAuthorities())
                    .signWith(signingKey, SignatureAlgorithm.HS256)
                    .compact();
            
            // Validate the generated token immediately
            validateGeneratedToken(token, userDetails.getUsername());
            
            log.debug("✅ JWT token generated and validated successfully for user: {}", userDetails.getUsername());
            return token;
        } catch (Exception e) {
            log.error("❌ Failed to generate JWT token for user: {}", userDetails.getUsername(), e);
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }

    /**
     * Validate the signing key before using it
     */
    private void validateSigningKeyForUse() {
        if (signingKey == null) {
            throw new RuntimeException("Signing key is null");
        }
        
        try {
            // Quick validation by generating a test token
            String testToken = Jwts.builder()
                    .setSubject("validation-test")
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 1000))
                    .signWith(signingKey, SignatureAlgorithm.HS256)
                    .compact();
            
            // Parse it back to ensure it works
            Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(testToken);
                    
            log.debug("✅ Signing key validation successful");
        } catch (Exception e) {
            log.error("❌ Signing key validation failed", e);
            throw new RuntimeException("Signing key is invalid", e);
        }
    }

    /**
     * Validate the generated token immediately after creation
     */
    private void validateGeneratedToken(String token, String expectedUsername) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            if (!expectedUsername.equals(claims.getSubject())) {
                throw new RuntimeException("Generated token subject mismatch");
            }
            
            log.debug("✅ Generated token validation successful for user: {}", expectedUsername);
        } catch (Exception e) {
            log.error("❌ Generated token validation failed for user: {}", expectedUsername, e);
            throw new RuntimeException("Generated token validation failed", e);
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            boolean isValid = (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
            log.debug("🔐 Token validation for user {}: {}", username, isValid);
            return isValid;
        } catch (Exception e) {
            log.error("❌ Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token);
            log.debug("✅ Token validation successful");
            return true;
        } catch (ExpiredJwtException e) {
            log.error("❌ Token is expired: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.error("❌ Token is unsupported: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.error("❌ Token is malformed: {}", e.getMessage());
            return false;
        } catch (SecurityException e) {
            log.error("❌ Token signature verification failed: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.error("❌ Token is empty or null: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("❌ Unexpected error during token validation: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get detailed token validation information for debugging
     */
    public Map<String, Object> getTokenValidationInfo(String token) {
        Map<String, Object> info = new HashMap<>();
        
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            info.put("valid", true);
            info.put("subject", claims.getSubject());
            info.put("issuer", claims.getIssuer());
            info.put("audience", claims.getAudience());
            info.put("issuedAt", claims.getIssuedAt());
            info.put("expiration", claims.getExpiration());
            info.put("expired", claims.getExpiration().before(new Date()));
            
        } catch (ExpiredJwtException e) {
            info.put("valid", false);
            info.put("error", "Token is expired");
            info.put("expiredAt", e.getClaims().getExpiration());
        } catch (SecurityException e) {
            info.put("valid", false);
            info.put("error", "Invalid signature");
            info.put("signatureError", e.getMessage());
            info.put("signingKeyAlgorithm", signingKey.getAlgorithm());
            info.put("signingKeyFormat", signingKey.getFormat());
        } catch (Exception e) {
            info.put("valid", false);
            info.put("error", e.getMessage());
        }
        
        return info;
    }

    /**
     * Get JWT signing key information for debugging
     */
    public Map<String, Object> getSigningKeyInfo() {
        Map<String, Object> info = new HashMap<>();
        
        try {
            info.put("algorithm", signingKey.getAlgorithm());
            info.put("format", signingKey.getFormat());
            info.put("encoded", signingKey.getEncoded() != null ? "present" : "null");
            info.put("encodedLength", signingKey.getEncoded() != null ? signingKey.getEncoded().length : 0);
            
            // Test key generation
            String testToken = Jwts.builder()
                    .setSubject("test")
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 60000))
                    .signWith(signingKey, SignatureAlgorithm.HS256)
                    .compact();
            
            info.put("testTokenGenerated", true);
            info.put("testTokenLength", testToken.length());
            
            // Test token validation
            try {
                Jwts.parserBuilder()
                        .setSigningKey(signingKey)
                        .build()
                        .parseClaimsJws(testToken);
                info.put("testTokenValid", true);
            } catch (Exception e) {
                info.put("testTokenValid", false);
                info.put("testTokenError", e.getMessage());
            }
            
        } catch (Exception e) {
            info.put("error", e.getMessage());
        }
        
        return info;
    }

    /**
     * Get current JWT configuration for debugging
     */
    public Map<String, Object> getJwtConfigurationInfo() {
        Map<String, Object> info = new HashMap<>();
        
        try {
            info.put("secretLength", configurationService.getJwtSecret().length());
            info.put("expiration", jwtConfigurationProperties.getJwtExpiration());
            info.put("refreshExpiration", jwtConfigurationProperties.getJwtRefreshExpiration());
            info.put("issuer", jwtConfigurationProperties.getJwtIssuer());
            info.put("audience", jwtConfigurationProperties.getJwtAudience());
            info.put("signingKeyAlgorithm", signingKey.getAlgorithm());
            info.put("signingKeyFormat", signingKey.getFormat());
            
        } catch (Exception e) {
            info.put("error", e.getMessage());
        }
        
        return info;
    }
} 