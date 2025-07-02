package com.codestorykh.alpha.security.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * DTO for storing comprehensive token information in Redis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenInfo {

    // Token Identification
    private String tokenId;
    private String tokenHash;
    private String tokenType; // "ACCESS" or "REFRESH"
    
    // User Information
    private String username;
    private String userId;
    private String email;
    
    // Token Details
    private String token;
    private String issuer;
    private String audience;
    
    // Expiration Information
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime issuedAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime expiresAt;
    private Long expiresIn; // seconds
    
    // Scope and Permissions
    private Set<String> scopes;
    private List<String> roles;
    private List<String> permissions;
    private List<String> authorities;
    
    // Security Information
    private String clientId;
    private String clientName;
    private String userAgent;
    private String ipAddress;
    private String deviceInfo;
    
    // Token Status
    private TokenStatus status;
    private String statusReason;
    
    // Refresh Token Association (for access tokens)
    private String refreshTokenId;
    private String refreshTokenHash;
    
    // Parent Token Association (for refresh tokens)
    private String parentTokenId;
    private String parentTokenHash;
    
    // Usage Tracking
    private Integer usageCount;
    private Integer maxUsage;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime lastUsedAt;
    
    // Metadata
    private String sessionId;
    private String correlationId;
    private String requestId;
    
    // Audit Information
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    
    // Custom Claims
    private String customClaims;
    
    public enum TokenStatus {
        ACTIVE,
        EXPIRED,
        REVOKED,
        SUSPENDED,
        BLACKLISTED
    }
    
    /**
     * Check if token is expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    /**
     * Check if token is active
     */
    public boolean isActive() {
        return TokenStatus.ACTIVE.equals(status) && !isExpired();
    }
    
    /**
     * Check if token can be used (active and within usage limits)
     */
    public boolean canBeUsed() {
        return isActive() && (maxUsage == null || usageCount == null || usageCount < maxUsage);
    }
    
    /**
     * Increment usage count
     */
    public void incrementUsage() {
        this.usageCount = (this.usageCount == null) ? 1 : this.usageCount + 1;
        this.lastUsedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Get remaining time in seconds
     */
    public Long getRemainingTimeInSeconds() {
        if (expiresAt == null) {
            return null;
        }
        long remaining = java.time.Duration.between(LocalDateTime.now(), expiresAt).getSeconds();
        return remaining > 0 ? remaining : 0L;
    }
    
    /**
     * Get token age in seconds
     */
    public Long getTokenAgeInSeconds() {
        if (issuedAt == null) {
            return null;
        }
        return java.time.Duration.between(issuedAt, LocalDateTime.now()).getSeconds();
    }
    
    /**
     * Check if token has specific scope
     */
    public boolean hasScope(String scope) {
        return scopes != null && scopes.contains(scope);
    }
    
    /**
     * Check if token has any of the specified scopes
     */
    public boolean hasAnyScope(Set<String> requiredScopes) {
        return scopes != null && scopes.stream().anyMatch(requiredScopes::contains);
    }
    
    /**
     * Check if token has all specified scopes
     */
    public boolean hasAllScopes(Set<String> requiredScopes) {
        return scopes != null && scopes.containsAll(requiredScopes);
    }
    
    /**
     * Check if token has specific role
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
    
    /**
     * Check if token has specific permission
     */
    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }
    
    /**
     * Create a copy with updated timestamps
     */
    public TokenInfo withUpdatedTimestamps() {
        this.updatedAt = LocalDateTime.now();
        return this;
    }
    
    /**
     * Create a copy for refresh token
     */
    public TokenInfo asRefreshToken() {
        return TokenInfo.builder()
            .tokenType("REFRESH")
            .username(this.username)
            .userId(this.userId)
            .email(this.email)
            .issuer(this.issuer)
            .audience(this.audience)
            .scopes(this.scopes)
            .roles(this.roles)
            .permissions(this.permissions)
            .authorities(this.authorities)
            .clientId(this.clientId)
            .clientName(this.clientName)
            .userAgent(this.userAgent)
            .ipAddress(this.ipAddress)
            .deviceInfo(this.deviceInfo)
            .status(TokenStatus.ACTIVE)
            .parentTokenId(this.tokenId)
            .parentTokenHash(this.tokenHash)
            .sessionId(this.sessionId)
            .correlationId(this.correlationId)
            .requestId(this.requestId)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
} 