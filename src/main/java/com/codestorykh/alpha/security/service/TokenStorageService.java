package com.codestorykh.alpha.security.service;

import com.codestorykh.alpha.security.dto.TokenInfo;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Service interface for managing token storage in Redis
 */
public interface TokenStorageService {

    // ==================== ACCESS TOKEN OPERATIONS ====================

    /**
     * Store access token in Redis
     */
    void storeAccessToken(String token, TokenInfo tokenInfo);

    /**
     * Store access token with custom TTL
     */
    void storeAccessToken(String token, TokenInfo tokenInfo, Duration ttl);

    /**
     * Retrieve access token information
     */
    Optional<TokenInfo> getAccessTokenInfo(String token);

    /**
     * Check if access token exists and is valid
     */
    boolean isAccessTokenValid(String token);

    /**
     * Invalidate access token (mark as revoked)
     */
    void invalidateAccessToken(String token, String reason);

    /**
     * Delete access token from storage
     */
    void deleteAccessToken(String token);

    /**
     * Get all access tokens for a user
     */
    List<TokenInfo> getUserAccessTokens(String username);

    /**
     * Get active access tokens for a user
     */
    List<TokenInfo> getUserActiveAccessTokens(String username);

    // ==================== REFRESH TOKEN OPERATIONS ====================

    /**
     * Store refresh token in Redis
     */
    void storeRefreshToken(String token, TokenInfo tokenInfo);

    /**
     * Store refresh token with custom TTL
     */
    void storeRefreshToken(String token, TokenInfo tokenInfo, Duration ttl);

    /**
     * Retrieve refresh token information
     */
    Optional<TokenInfo> getRefreshTokenInfo(String token);

    /**
     * Check if refresh token exists and is valid
     */
    boolean isRefreshTokenValid(String token);

    /**
     * Invalidate refresh token (mark as revoked)
     */
    void invalidateRefreshToken(String token, String reason);

    /**
     * Delete refresh token from storage
     */
    void deleteRefreshToken(String token);

    /**
     * Get all refresh tokens for a user
     */
    List<TokenInfo> getUserRefreshTokens(String username);

    /**
     * Get active refresh tokens for a user
     */
    List<TokenInfo> getUserActiveRefreshTokens(String username);

    // ==================== TOKEN PAIR OPERATIONS ====================

    /**
     * Store both access and refresh tokens as a pair
     */
    void storeTokenPair(String accessToken, TokenInfo accessTokenInfo, 
                       String refreshToken, TokenInfo refreshTokenInfo);

    /**
     * Store token pair with custom TTLs
     */
    void storeTokenPair(String accessToken, TokenInfo accessTokenInfo, Duration accessTokenTtl,
                       String refreshToken, TokenInfo refreshTokenInfo, Duration refreshTokenTtl);

    /**
     * Get token pair information
     */
    Optional<TokenPairInfo> getTokenPairInfo(String accessToken, String refreshToken);

    /**
     * Invalidate token pair (both access and refresh tokens)
     */
    void invalidateTokenPair(String accessToken, String refreshToken, String reason);

    /**
     * Delete token pair from storage
     */
    void deleteTokenPair(String accessToken, String refreshToken);

    // ==================== TOKEN VALIDATION OPERATIONS ====================

    /**
     * Validate token and return token info if valid
     */
    Optional<TokenInfo> validateToken(String token, String tokenType);

    /**
     * Validate token with scope requirements
     */
    Optional<TokenInfo> validateTokenWithScope(String token, String tokenType, Set<String> requiredScopes);

    /**
     * Validate token with role requirements
     */
    Optional<TokenInfo> validateTokenWithRole(String token, String tokenType, String requiredRole);

    /**
     * Validate token with permission requirements
     */
    Optional<TokenInfo> validateTokenWithPermission(String token, String tokenType, String requiredPermission);

    /**
     * Check if token is blacklisted
     */
    boolean isTokenBlacklisted(String token);

    /**
     * Add token to blacklist
     */
    void blacklistToken(String token, String reason, Duration ttl);

    // ==================== TOKEN USAGE TRACKING ====================

    /**
     * Increment token usage count
     */
    void incrementTokenUsage(String token, String tokenType);

    /**
     * Get token usage statistics
     */
    TokenUsageStats getTokenUsageStats(String username);

    /**
     * Get token usage statistics for all users
     */
    Map<String, TokenUsageStats> getAllTokenUsageStats();

    // ==================== TOKEN CLEANUP OPERATIONS ====================

    /**
     * Clean up expired tokens
     */
    void cleanupExpiredTokens();

    /**
     * Clean up expired tokens for a specific user
     */
    void cleanupExpiredTokensForUser(String username);

    /**
     * Clean up all tokens for a user (logout)
     */
    void cleanupAllTokensForUser(String username);

    /**
     * Clean up all tokens for a session
     */
    void cleanupTokensForSession(String sessionId);

    // ==================== TOKEN SEARCH OPERATIONS ====================

    /**
     * Search tokens by criteria
     */
    List<TokenInfo> searchTokens(TokenSearchCriteria criteria);

    /**
     * Get tokens by client ID
     */
    List<TokenInfo> getTokensByClientId(String clientId);

    /**
     * Get tokens by IP address
     */
    List<TokenInfo> getTokensByIpAddress(String ipAddress);

    /**
     * Get tokens by user agent
     */
    List<TokenInfo> getTokensByUserAgent(String userAgent);

    // ==================== TOKEN STATISTICS ====================

    /**
     * Get token statistics
     */
    TokenStatistics getTokenStatistics();

    /**
     * Get token statistics for a user
     */
    TokenStatistics getTokenStatisticsForUser(String username);

    /**
     * Get token statistics for a client
     */
    TokenStatistics getTokenStatisticsForClient(String clientId);

    // ==================== TOKEN MONITORING ====================

    /**
     * Get active token count
     */
    long getActiveTokenCount();

    /**
     * Get active token count for a user
     */
    long getActiveTokenCountForUser(String username);

    /**
     * Get token count by status
     */
    Map<TokenInfo.TokenStatus, Long> getTokenCountByStatus();

    /**
     * Get token count by type
     */
    Map<String, Long> getTokenCountByType();

    // ==================== TOKEN HEALTH CHECK ====================

    /**
     * Check token storage health
     */
    TokenStorageHealth checkTokenStorageHealth();

    /**
     * Get token storage metrics
     */
    TokenStorageMetrics getTokenStorageMetrics();

    // ==================== INNER CLASSES ====================

    /**
     * Token pair information
     */
    class TokenPairInfo {
        private final TokenInfo accessTokenInfo;
        private final TokenInfo refreshTokenInfo;

        public TokenPairInfo(TokenInfo accessTokenInfo, TokenInfo refreshTokenInfo) {
            this.accessTokenInfo = accessTokenInfo;
            this.refreshTokenInfo = refreshTokenInfo;
        }

        public TokenInfo getAccessTokenInfo() { return accessTokenInfo; }
        public TokenInfo getRefreshTokenInfo() { return refreshTokenInfo; }
    }

    /**
     * Token usage statistics
     */
    class TokenUsageStats {
        private final String username;
        private final long totalAccessTokens;
        private final long activeAccessTokens;
        private final long totalRefreshTokens;
        private final long activeRefreshTokens;
        private final long totalUsageCount;
        private final long averageUsagePerToken;

        public TokenUsageStats(String username, long totalAccessTokens, long activeAccessTokens,
                              long totalRefreshTokens, long activeRefreshTokens,
                              long totalUsageCount, long averageUsagePerToken) {
            this.username = username;
            this.totalAccessTokens = totalAccessTokens;
            this.activeAccessTokens = activeAccessTokens;
            this.totalRefreshTokens = totalRefreshTokens;
            this.activeRefreshTokens = activeRefreshTokens;
            this.totalUsageCount = totalUsageCount;
            this.averageUsagePerToken = averageUsagePerToken;
        }

        // Getters
        public String getUsername() { return username; }
        public long getTotalAccessTokens() { return totalAccessTokens; }
        public long getActiveAccessTokens() { return activeAccessTokens; }
        public long getTotalRefreshTokens() { return totalRefreshTokens; }
        public long getActiveRefreshTokens() { return activeRefreshTokens; }
        public long getTotalUsageCount() { return totalUsageCount; }
        public long getAverageUsagePerToken() { return averageUsagePerToken; }
    }

    /**
     * Token search criteria
     */
    class TokenSearchCriteria {
        private String username;
        private String clientId;
        private String ipAddress;
        private String userAgent;
        private TokenInfo.TokenStatus status;
        private String tokenType;
        private Set<String> scopes;
        private String sessionId;
        private LocalDateTime createdAfter;
        private LocalDateTime createdBefore;
        private LocalDateTime expiresAfter;
        private LocalDateTime expiresBefore;

        // Builder pattern
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private final TokenSearchCriteria criteria = new TokenSearchCriteria();

            public Builder username(String username) {
                criteria.username = username;
                return this;
            }

            public Builder clientId(String clientId) {
                criteria.clientId = clientId;
                return this;
            }

            public Builder ipAddress(String ipAddress) {
                criteria.ipAddress = ipAddress;
                return this;
            }

            public Builder userAgent(String userAgent) {
                criteria.userAgent = userAgent;
                return this;
            }

            public Builder status(TokenInfo.TokenStatus status) {
                criteria.status = status;
                return this;
            }

            public Builder tokenType(String tokenType) {
                criteria.tokenType = tokenType;
                return this;
            }

            public Builder scopes(Set<String> scopes) {
                criteria.scopes = scopes;
                return this;
            }

            public Builder sessionId(String sessionId) {
                criteria.sessionId = sessionId;
                return this;
            }

            public Builder createdAfter(LocalDateTime createdAfter) {
                criteria.createdAfter = createdAfter;
                return this;
            }

            public Builder createdBefore(LocalDateTime createdBefore) {
                criteria.createdBefore = createdBefore;
                return this;
            }

            public Builder expiresAfter(LocalDateTime expiresAfter) {
                criteria.expiresAfter = expiresAfter;
                return this;
            }

            public Builder expiresBefore(LocalDateTime expiresBefore) {
                criteria.expiresBefore = expiresBefore;
                return this;
            }

            public TokenSearchCriteria build() {
                return criteria;
            }
        }

        // Getters
        public String getUsername() { return username; }
        public String getClientId() { return clientId; }
        public String getIpAddress() { return ipAddress; }
        public String getUserAgent() { return userAgent; }
        public TokenInfo.TokenStatus getStatus() { return status; }
        public String getTokenType() { return tokenType; }
        public Set<String> getScopes() { return scopes; }
        public String getSessionId() { return sessionId; }
        public LocalDateTime getCreatedAfter() { return createdAfter; }
        public LocalDateTime getCreatedBefore() { return createdBefore; }
        public LocalDateTime getExpiresAfter() { return expiresAfter; }
        public LocalDateTime getExpiresBefore() { return expiresBefore; }
    }

    /**
     * Token statistics
     */
    class TokenStatistics {
        private final long totalTokens;
        private final long activeTokens;
        private final long expiredTokens;
        private final long revokedTokens;
        private final long accessTokens;
        private final long refreshTokens;
        private final long totalUsageCount;
        private final double averageUsagePerToken;

        public TokenStatistics(long totalTokens, long activeTokens, long expiredTokens,
                              long revokedTokens, long accessTokens, long refreshTokens,
                              long totalUsageCount, double averageUsagePerToken) {
            this.totalTokens = totalTokens;
            this.activeTokens = activeTokens;
            this.expiredTokens = expiredTokens;
            this.revokedTokens = revokedTokens;
            this.accessTokens = accessTokens;
            this.refreshTokens = refreshTokens;
            this.totalUsageCount = totalUsageCount;
            this.averageUsagePerToken = averageUsagePerToken;
        }

        // Getters
        public long getTotalTokens() { return totalTokens; }
        public long getActiveTokens() { return activeTokens; }
        public long getExpiredTokens() { return expiredTokens; }
        public long getRevokedTokens() { return revokedTokens; }
        public long getAccessTokens() { return accessTokens; }
        public long getRefreshTokens() { return refreshTokens; }
        public long getTotalUsageCount() { return totalUsageCount; }
        public double getAverageUsagePerToken() { return averageUsagePerToken; }
    }

    /**
     * Token storage health information
     */
    class TokenStorageHealth {
        private final boolean healthy;
        private final String status;
        private final String message;
        private final long activeConnections;
        private final long memoryUsage;
        private final long keyCount;

        public TokenStorageHealth(boolean healthy, String status, String message,
                                 long activeConnections, long memoryUsage, long keyCount) {
            this.healthy = healthy;
            this.status = status;
            this.message = message;
            this.activeConnections = activeConnections;
            this.memoryUsage = memoryUsage;
            this.keyCount = keyCount;
        }

        // Getters
        public boolean isHealthy() { return healthy; }
        public String getStatus() { return status; }
        public String getMessage() { return message; }
        public long getActiveConnections() { return activeConnections; }
        public long getMemoryUsage() { return memoryUsage; }
        public long getKeyCount() { return keyCount; }
    }

    /**
     * Token storage metrics
     */
    class TokenStorageMetrics {
        private final long totalOperations;
        private final long successfulOperations;
        private final long failedOperations;
        private final double successRate;
        private final long averageResponseTime;
        private final long cacheHits;
        private final long cacheMisses;
        private final double hitRate;

        public TokenStorageMetrics(long totalOperations, long successfulOperations,
                                  long failedOperations, double successRate,
                                  long averageResponseTime, long cacheHits,
                                  long cacheMisses, double hitRate) {
            this.totalOperations = totalOperations;
            this.successfulOperations = successfulOperations;
            this.failedOperations = failedOperations;
            this.successRate = successRate;
            this.averageResponseTime = averageResponseTime;
            this.cacheHits = cacheHits;
            this.cacheMisses = cacheMisses;
            this.hitRate = hitRate;
        }

        // Getters
        public long getTotalOperations() { return totalOperations; }
        public long getSuccessfulOperations() { return successfulOperations; }
        public long getFailedOperations() { return failedOperations; }
        public double getSuccessRate() { return successRate; }
        public long getAverageResponseTime() { return averageResponseTime; }
        public long getCacheHits() { return cacheHits; }
        public long getCacheMisses() { return cacheMisses; }
        public double getHitRate() { return hitRate; }
    }
} 