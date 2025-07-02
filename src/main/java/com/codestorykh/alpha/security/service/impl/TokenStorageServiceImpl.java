package com.codestorykh.alpha.security.service.impl;

import com.codestorykh.alpha.cache.service.CacheService;
import com.codestorykh.alpha.config.service.ConfigurationService;
import com.codestorykh.alpha.security.dto.TokenInfo;
import com.codestorykh.alpha.security.service.TokenStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenStorageServiceImpl implements TokenStorageService {

    private final CacheService cacheService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ConfigurationService configurationService;

    // Cache names
    private static final String ACCESS_TOKEN_CACHE = "access-tokens";
    private static final String REFRESH_TOKEN_CACHE = "refresh-tokens";
    private static final String TOKEN_BLACKLIST_CACHE = "token-blacklist";
    private static final String TOKEN_STATS_CACHE = "token-stats";
    private static final String USER_TOKENS_CACHE = "user-tokens";

    // Key prefixes
    private static final String ACCESS_TOKEN_PREFIX = "access:";
    private static final String REFRESH_TOKEN_PREFIX = "refresh:";
    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final String USER_TOKENS_PREFIX = "user:";
    private static final String CLIENT_TOKENS_PREFIX = "client:";
    private static final String SESSION_TOKENS_PREFIX = "session:";

    // Metrics tracking
    private final Map<String, AtomicLong> operationCounters = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> successCounters = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> failureCounters = new ConcurrentHashMap<>();

    // ==================== ACCESS TOKEN OPERATIONS ====================

    @Override
    public void storeAccessToken(String token, TokenInfo tokenInfo) {
        Duration ttl = Duration.ofMillis(configurationService.getJwtExpiration());
        storeAccessToken(token, tokenInfo, ttl);
    }

    @Override
    public void storeAccessToken(String token, TokenInfo tokenInfo, Duration ttl) {
        try {
            String tokenHash = generateTokenHash(token);
            String key = ACCESS_TOKEN_PREFIX + tokenHash;
            
            // Set token info
            cacheService.set(ACCESS_TOKEN_CACHE, key, tokenInfo, ttl);
            
            // Store user token mapping
            if (tokenInfo.getUsername() != null) {
                String userKey = USER_TOKENS_PREFIX + tokenInfo.getUsername() + ":access:" + tokenHash;
                cacheService.set(USER_TOKENS_CACHE, userKey, tokenInfo, ttl);
            }
            
            // Store client token mapping
            if (tokenInfo.getClientId() != null) {
                String clientKey = CLIENT_TOKENS_PREFIX + tokenInfo.getClientId() + ":access";
                cacheService.set(USER_TOKENS_CACHE, clientKey + ":" + tokenHash, tokenInfo, ttl);
            }
            
            // Store session token mapping
            if (tokenInfo.getSessionId() != null) {
                String sessionKey = SESSION_TOKENS_PREFIX + tokenInfo.getSessionId() + ":access";
                cacheService.set(USER_TOKENS_CACHE, sessionKey + ":" + tokenHash, tokenInfo, ttl);
            }
            
            incrementOperationCounter("storeAccessToken", true);
            log.debug("Stored access token for user: {}", tokenInfo.getUsername());
            
        } catch (Exception e) {
            incrementOperationCounter("storeAccessToken", false);
            log.error("Failed to store access token for user: {}", tokenInfo.getUsername(), e);
            throw new RuntimeException("Failed to store access token", e);
        }
    }

    @Override
    public Optional<TokenInfo> getAccessTokenInfo(String token) {
        try {
            String tokenHash = generateTokenHash(token);
            String key = ACCESS_TOKEN_PREFIX + tokenHash;
            
            Optional<TokenInfo> tokenInfo = cacheService.get(ACCESS_TOKEN_CACHE, key, TokenInfo.class);
            incrementOperationCounter("getAccessTokenInfo", tokenInfo.isPresent());
            
            return tokenInfo;
        } catch (Exception e) {
            incrementOperationCounter("getAccessTokenInfo", false);
            log.error("Failed to get access token info", e);
            return Optional.empty();
        }
    }

    @Override
    public boolean isAccessTokenValid(String token) {
        try {
            Optional<TokenInfo> tokenInfo = getAccessTokenInfo(token);
            if (tokenInfo.isEmpty()) {
                return false;
            }
            
            TokenInfo info = tokenInfo.get();
            boolean isValid = info.isActive() && !isTokenBlacklisted(token);
            
            incrementOperationCounter("isAccessTokenValid", isValid);
            return isValid;
            
        } catch (Exception e) {
            incrementOperationCounter("isAccessTokenValid", false);
            log.error("Failed to validate access token", e);
            return false;
        }
    }

    @Override
    public void invalidateAccessToken(String token, String reason) {
        try {
            Optional<TokenInfo> tokenInfo = getAccessTokenInfo(token);
            if (tokenInfo.isPresent()) {
                TokenInfo info = tokenInfo.get();
                info.setStatus(TokenInfo.TokenStatus.REVOKED);
                info.setStatusReason(reason);
                info.setUpdatedAt(LocalDateTime.now());
                
                // Update the stored token info
                Duration ttl = Duration.ofMillis(configurationService.getJwtExpiration());
                cacheService.set(ACCESS_TOKEN_CACHE, ACCESS_TOKEN_PREFIX + generateTokenHash(token), info, ttl);
                
                // Add to blacklist
                blacklistToken(token, reason, ttl);
            }
            
            incrementOperationCounter("invalidateAccessToken", true);
            log.debug("Invalidated access token for user: {}", tokenInfo.map(TokenInfo::getUsername).orElse("unknown"));
            
        } catch (Exception e) {
            incrementOperationCounter("invalidateAccessToken", false);
            log.error("Failed to invalidate access token", e);
            throw new RuntimeException("Failed to invalidate access token", e);
        }
    }

    @Override
    public void deleteAccessToken(String token) {
        try {
            String tokenHash = generateTokenHash(token);
            String key = ACCESS_TOKEN_PREFIX + tokenHash;
            
            cacheService.delete(ACCESS_TOKEN_CACHE, key);
            
            // Clean up user mapping
            Optional<TokenInfo> tokenInfo = getAccessTokenInfo(token);
            if (tokenInfo.isPresent()) {
                TokenInfo info = tokenInfo.get();
                if (info.getUsername() != null) {
                    String userKey = USER_TOKENS_PREFIX + info.getUsername() + ":access:" + tokenHash;
                    cacheService.delete(USER_TOKENS_CACHE, userKey);
                }
            }
            
            incrementOperationCounter("deleteAccessToken", true);
            log.debug("Deleted access token");
            
        } catch (Exception e) {
            incrementOperationCounter("deleteAccessToken", false);
            log.error("Failed to delete access token", e);
            throw new RuntimeException("Failed to delete access token", e);
        }
    }

    @Override
    public List<TokenInfo> getUserAccessTokens(String username) {
        try {
            String pattern = USER_TOKENS_PREFIX + username + ":access:*";
            Set<String> keys = cacheService.getKeys(pattern);
            
            List<TokenInfo> tokens = new ArrayList<>();
            for (String key : keys) {
                Optional<TokenInfo> tokenInfo = cacheService.get(USER_TOKENS_CACHE, key, TokenInfo.class);
                tokenInfo.ifPresent(tokens::add);
            }
            
            incrementOperationCounter("getUserAccessTokens", true);
            return tokens;
            
        } catch (Exception e) {
            incrementOperationCounter("getUserAccessTokens", false);
            log.error("Failed to get user access tokens for: {}", username, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<TokenInfo> getUserActiveAccessTokens(String username) {
        return getUserAccessTokens(username).stream()
            .filter(TokenInfo::isActive)
            .collect(Collectors.toList());
    }

    // ==================== REFRESH TOKEN OPERATIONS ====================

    @Override
    public void storeRefreshToken(String token, TokenInfo tokenInfo) {
        Duration ttl = Duration.ofMillis(configurationService.getJwtRefreshExpiration());
        storeRefreshToken(token, tokenInfo, ttl);
    }

    @Override
    public void storeRefreshToken(String token, TokenInfo tokenInfo, Duration ttl) {
        try {
            String tokenHash = generateTokenHash(token);
            String key = REFRESH_TOKEN_PREFIX + tokenHash;
            
            // Set token info
            cacheService.set(REFRESH_TOKEN_CACHE, key, tokenInfo, ttl);
            
            // Store user token mapping
            if (tokenInfo.getUsername() != null) {
                String userKey = USER_TOKENS_PREFIX + tokenInfo.getUsername() + ":refresh:" + tokenHash;
                cacheService.set(USER_TOKENS_CACHE, userKey, tokenInfo, ttl);
            }
            
            incrementOperationCounter("storeRefreshToken", true);
            log.debug("Stored refresh token for user: {}", tokenInfo.getUsername());
            
        } catch (Exception e) {
            incrementOperationCounter("storeRefreshToken", false);
            log.error("Failed to store refresh token for user: {}", tokenInfo.getUsername(), e);
            throw new RuntimeException("Failed to store refresh token", e);
        }
    }

    @Override
    public Optional<TokenInfo> getRefreshTokenInfo(String token) {
        try {
            String tokenHash = generateTokenHash(token);
            String key = REFRESH_TOKEN_PREFIX + tokenHash;
            
            Optional<TokenInfo> tokenInfo = cacheService.get(REFRESH_TOKEN_CACHE, key, TokenInfo.class);
            incrementOperationCounter("getRefreshTokenInfo", tokenInfo.isPresent());
            
            return tokenInfo;
        } catch (Exception e) {
            incrementOperationCounter("getRefreshTokenInfo", false);
            log.error("Failed to get refresh token info", e);
            return Optional.empty();
        }
    }

    @Override
    public boolean isRefreshTokenValid(String token) {
        try {
            Optional<TokenInfo> tokenInfo = getRefreshTokenInfo(token);
            if (tokenInfo.isEmpty()) {
                return false;
            }
            
            TokenInfo info = tokenInfo.get();
            boolean isValid = info.isActive() && !isTokenBlacklisted(token);
            
            incrementOperationCounter("isRefreshTokenValid", isValid);
            return isValid;
            
        } catch (Exception e) {
            incrementOperationCounter("isRefreshTokenValid", false);
            log.error("Failed to validate refresh token", e);
            return false;
        }
    }

    @Override
    public void invalidateRefreshToken(String token, String reason) {
        try {
            Optional<TokenInfo> tokenInfo = getRefreshTokenInfo(token);
            if (tokenInfo.isPresent()) {
                TokenInfo info = tokenInfo.get();
                info.setStatus(TokenInfo.TokenStatus.REVOKED);
                info.setStatusReason(reason);
                info.setUpdatedAt(LocalDateTime.now());
                
                // Update the stored token info
                Duration ttl = Duration.ofMillis(configurationService.getJwtRefreshExpiration());
                cacheService.set(REFRESH_TOKEN_CACHE, REFRESH_TOKEN_PREFIX + generateTokenHash(token), info, ttl);
                
                // Add to blacklist
                blacklistToken(token, reason, ttl);
            }
            
            incrementOperationCounter("invalidateRefreshToken", true);
            log.debug("Invalidated refresh token for user: {}", tokenInfo.map(TokenInfo::getUsername).orElse("unknown"));
            
        } catch (Exception e) {
            incrementOperationCounter("invalidateRefreshToken", false);
            log.error("Failed to invalidate refresh token", e);
            throw new RuntimeException("Failed to invalidate refresh token", e);
        }
    }

    @Override
    public void deleteRefreshToken(String token) {
        try {
            String tokenHash = generateTokenHash(token);
            String key = REFRESH_TOKEN_PREFIX + tokenHash;
            
            cacheService.delete(REFRESH_TOKEN_CACHE, key);
            
            // Clean up user mapping
            Optional<TokenInfo> tokenInfo = getRefreshTokenInfo(token);
            if (tokenInfo.isPresent()) {
                TokenInfo info = tokenInfo.get();
                if (info.getUsername() != null) {
                    String userKey = USER_TOKENS_PREFIX + info.getUsername() + ":refresh:" + tokenHash;
                    cacheService.delete(USER_TOKENS_CACHE, userKey);
                }
            }
            
            incrementOperationCounter("deleteRefreshToken", true);
            log.debug("Deleted refresh token");
            
        } catch (Exception e) {
            incrementOperationCounter("deleteRefreshToken", false);
            log.error("Failed to delete refresh token", e);
            throw new RuntimeException("Failed to delete refresh token", e);
        }
    }

    @Override
    public List<TokenInfo> getUserRefreshTokens(String username) {
        try {
            String pattern = USER_TOKENS_PREFIX + username + ":refresh:*";
            Set<String> keys = cacheService.getKeys(pattern);
            
            List<TokenInfo> tokens = new ArrayList<>();
            for (String key : keys) {
                Optional<TokenInfo> tokenInfo = cacheService.get(USER_TOKENS_CACHE, key, TokenInfo.class);
                tokenInfo.ifPresent(tokens::add);
            }
            
            incrementOperationCounter("getUserRefreshTokens", true);
            return tokens;
            
        } catch (Exception e) {
            incrementOperationCounter("getUserRefreshTokens", false);
            log.error("Failed to get user refresh tokens for: {}", username, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<TokenInfo> getUserActiveRefreshTokens(String username) {
        return getUserRefreshTokens(username).stream()
            .filter(TokenInfo::isActive)
            .collect(Collectors.toList());
    }

    // ==================== TOKEN PAIR OPERATIONS ====================

    @Override
    public void storeTokenPair(String accessToken, TokenInfo accessTokenInfo, 
                              String refreshToken, TokenInfo refreshTokenInfo) {
        Duration accessTokenTtl = Duration.ofMillis(configurationService.getJwtExpiration());
        Duration refreshTokenTtl = Duration.ofMillis(configurationService.getJwtRefreshExpiration());
        storeTokenPair(accessToken, accessTokenInfo, accessTokenTtl, refreshToken, refreshTokenInfo, refreshTokenTtl);
    }

    @Override
    public void storeTokenPair(String accessToken, TokenInfo accessTokenInfo, Duration accessTokenTtl,
                              String refreshToken, TokenInfo refreshTokenInfo, Duration refreshTokenTtl) {
        try {
            // Store access token
            storeAccessToken(accessToken, accessTokenInfo, accessTokenTtl);
            
            // Store refresh token
            storeRefreshToken(refreshToken, refreshTokenInfo, refreshTokenTtl);
            
            // Link tokens
            String accessTokenHash = generateTokenHash(accessToken);
            String refreshTokenHash = generateTokenHash(refreshToken);
            
            accessTokenInfo.setRefreshTokenId(refreshTokenHash);
            accessTokenInfo.setRefreshTokenHash(refreshTokenHash);
            refreshTokenInfo.setParentTokenId(accessTokenHash);
            refreshTokenInfo.setParentTokenHash(accessTokenHash);
            
            // Update stored tokens with links
            cacheService.set(ACCESS_TOKEN_CACHE, ACCESS_TOKEN_PREFIX + accessTokenHash, accessTokenInfo, accessTokenTtl);
            cacheService.set(REFRESH_TOKEN_CACHE, REFRESH_TOKEN_PREFIX + refreshTokenHash, refreshTokenInfo, refreshTokenTtl);
            
            incrementOperationCounter("storeTokenPair", true);
            log.debug("Stored token pair for user: {}", accessTokenInfo.getUsername());
            
        } catch (Exception e) {
            incrementOperationCounter("storeTokenPair", false);
            log.error("Failed to store token pair for user: {}", accessTokenInfo.getUsername(), e);
            throw new RuntimeException("Failed to store token pair", e);
        }
    }

    @Override
    public Optional<TokenPairInfo> getTokenPairInfo(String accessToken, String refreshToken) {
        try {
            Optional<TokenInfo> accessTokenInfo = getAccessTokenInfo(accessToken);
            Optional<TokenInfo> refreshTokenInfo = getRefreshTokenInfo(refreshToken);
            
            if (accessTokenInfo.isPresent() && refreshTokenInfo.isPresent()) {
                TokenPairInfo pairInfo = new TokenPairInfo(accessTokenInfo.get(), refreshTokenInfo.get());
                incrementOperationCounter("getTokenPairInfo", true);
                return Optional.of(pairInfo);
            }
            
            incrementOperationCounter("getTokenPairInfo", false);
            return Optional.empty();
            
        } catch (Exception e) {
            incrementOperationCounter("getTokenPairInfo", false);
            log.error("Failed to get token pair info", e);
            return Optional.empty();
        }
    }

    @Override
    public void invalidateTokenPair(String accessToken, String refreshToken, String reason) {
        try {
            invalidateAccessToken(accessToken, reason);
            invalidateRefreshToken(refreshToken, reason);
            
            incrementOperationCounter("invalidateTokenPair", true);
            log.debug("Invalidated token pair");
            
        } catch (Exception e) {
            incrementOperationCounter("invalidateTokenPair", false);
            log.error("Failed to invalidate token pair", e);
            throw new RuntimeException("Failed to invalidate token pair", e);
        }
    }

    @Override
    public void deleteTokenPair(String accessToken, String refreshToken) {
        try {
            deleteAccessToken(accessToken);
            deleteRefreshToken(refreshToken);
            
            incrementOperationCounter("deleteTokenPair", true);
            log.debug("Deleted token pair");
            
        } catch (Exception e) {
            incrementOperationCounter("deleteTokenPair", false);
            log.error("Failed to delete token pair", e);
            throw new RuntimeException("Failed to delete token pair", e);
        }
    }

    // ==================== TOKEN VALIDATION OPERATIONS ====================

    @Override
    public Optional<TokenInfo> validateToken(String token, String tokenType) {
        try {
            Optional<TokenInfo> tokenInfo;
            if ("ACCESS".equals(tokenType)) {
                tokenInfo = getAccessTokenInfo(token);
            } else if ("REFRESH".equals(tokenType)) {
                tokenInfo = getRefreshTokenInfo(token);
            } else {
                log.warn("Invalid token type: {}", tokenType);
                return Optional.empty();
            }
            
            if (tokenInfo.isPresent() && tokenInfo.get().isActive() && !isTokenBlacklisted(token)) {
                incrementOperationCounter("validateToken", true);
                return tokenInfo;
            }
            
            incrementOperationCounter("validateToken", false);
            return Optional.empty();
            
        } catch (Exception e) {
            incrementOperationCounter("validateToken", false);
            log.error("Failed to validate token", e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<TokenInfo> validateTokenWithScope(String token, String tokenType, Set<String> requiredScopes) {
        Optional<TokenInfo> tokenInfo = validateToken(token, tokenType);
        if (tokenInfo.isPresent() && tokenInfo.get().hasAllScopes(requiredScopes)) {
            return tokenInfo;
        }
        return Optional.empty();
    }

    @Override
    public Optional<TokenInfo> validateTokenWithRole(String token, String tokenType, String requiredRole) {
        Optional<TokenInfo> tokenInfo = validateToken(token, tokenType);
        if (tokenInfo.isPresent() && tokenInfo.get().hasRole(requiredRole)) {
            return tokenInfo;
        }
        return Optional.empty();
    }

    @Override
    public Optional<TokenInfo> validateTokenWithPermission(String token, String tokenType, String requiredPermission) {
        Optional<TokenInfo> tokenInfo = validateToken(token, tokenType);
        if (tokenInfo.isPresent() && tokenInfo.get().hasPermission(requiredPermission)) {
            return tokenInfo;
        }
        return Optional.empty();
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        try {
            String tokenHash = generateTokenHash(token);
            String key = BLACKLIST_PREFIX + tokenHash;
            
            boolean isBlacklisted = cacheService.exists(TOKEN_BLACKLIST_CACHE, key);
            incrementOperationCounter("isTokenBlacklisted", true);
            return isBlacklisted;
            
        } catch (Exception e) {
            incrementOperationCounter("isTokenBlacklisted", false);
            log.error("Failed to check token blacklist", e);
            return false;
        }
    }

    @Override
    public void blacklistToken(String token, String reason, Duration ttl) {
        try {
            String tokenHash = generateTokenHash(token);
            String key = BLACKLIST_PREFIX + tokenHash;
            
            Map<String, Object> blacklistEntry = new HashMap<>();
            blacklistEntry.put("tokenHash", tokenHash);
            blacklistEntry.put("reason", reason);
            blacklistEntry.put("blacklistedAt", LocalDateTime.now());
            
            cacheService.set(TOKEN_BLACKLIST_CACHE, key, blacklistEntry, ttl);
            
            incrementOperationCounter("blacklistToken", true);
            log.debug("Blacklisted token: {}", tokenHash);
            
        } catch (Exception e) {
            incrementOperationCounter("blacklistToken", false);
            log.error("Failed to blacklist token", e);
            throw new RuntimeException("Failed to blacklist token", e);
        }
    }

    // ==================== TOKEN USAGE TRACKING ====================

    @Override
    public void incrementTokenUsage(String token, String tokenType) {
        try {
            Optional<TokenInfo> tokenInfo;
            if ("ACCESS".equals(tokenType)) {
                tokenInfo = getAccessTokenInfo(token);
            } else if ("REFRESH".equals(tokenType)) {
                tokenInfo = getRefreshTokenInfo(token);
            } else {
                log.warn("Invalid token type for usage tracking: {}", tokenType);
                return;
            }
            
            if (tokenInfo.isPresent()) {
                TokenInfo info = tokenInfo.get();
                info.incrementUsage();
                
                // Update stored token
                Duration ttl = "ACCESS".equals(tokenType) ? 
                    Duration.ofMillis(configurationService.getJwtExpiration()) :
                    Duration.ofMillis(configurationService.getJwtRefreshExpiration());
                
                String cacheName = "ACCESS".equals(tokenType) ? ACCESS_TOKEN_CACHE : REFRESH_TOKEN_CACHE;
                String prefix = "ACCESS".equals(tokenType) ? ACCESS_TOKEN_PREFIX : REFRESH_TOKEN_PREFIX;
                
                cacheService.set(cacheName, prefix + generateTokenHash(token), info, ttl);
                
                incrementOperationCounter("incrementTokenUsage", true);
                log.debug("Incremented usage for token: {}", info.getTokenId());
            }
            
        } catch (Exception e) {
            incrementOperationCounter("incrementTokenUsage", false);
            log.error("Failed to increment token usage", e);
        }
    }

    @Override
    public TokenUsageStats getTokenUsageStats(String username) {
        try {
            List<TokenInfo> accessTokens = getUserAccessTokens(username);
            List<TokenInfo> refreshTokens = getUserRefreshTokens(username);
            
            long totalAccessTokens = accessTokens.size();
            long activeAccessTokens = accessTokens.stream().filter(TokenInfo::isActive).count();
            long totalRefreshTokens = refreshTokens.size();
            long activeRefreshTokens = refreshTokens.stream().filter(TokenInfo::isActive).count();
            
            long totalUsageCount = accessTokens.stream()
                .mapToLong(token -> token.getUsageCount() != null ? token.getUsageCount() : 0)
                .sum();
            
            long averageUsagePerToken = totalAccessTokens > 0 ? totalUsageCount / totalAccessTokens : 0;
            
            incrementOperationCounter("getTokenUsageStats", true);
            return new TokenUsageStats(username, totalAccessTokens, activeAccessTokens,
                totalRefreshTokens, activeRefreshTokens, totalUsageCount, averageUsagePerToken);
            
        } catch (Exception e) {
            incrementOperationCounter("getTokenUsageStats", false);
            log.error("Failed to get token usage stats for user: {}", username, e);
            return new TokenUsageStats(username, 0, 0, 0, 0, 0, 0);
        }
    }

    @Override
    public Map<String, TokenUsageStats> getAllTokenUsageStats() {
        // This would require scanning all user tokens, which could be expensive
        // For now, return empty map - implement based on your specific needs
        log.warn("getAllTokenUsageStats not implemented - would be expensive operation");
        return new HashMap<>();
    }

    // ==================== TOKEN CLEANUP OPERATIONS ====================

    @Override
    public void cleanupExpiredTokens() {
        try {
            // This would require scanning all tokens and checking expiration
            // For now, rely on Redis TTL for automatic cleanup
            log.info("Token cleanup relies on Redis TTL for automatic expiration");
            incrementOperationCounter("cleanupExpiredTokens", true);
            
        } catch (Exception e) {
            incrementOperationCounter("cleanupExpiredTokens", false);
            log.error("Failed to cleanup expired tokens", e);
        }
    }

    @Override
    public void cleanupExpiredTokensForUser(String username) {
        try {
            List<TokenInfo> accessTokens = getUserAccessTokens(username);
            List<TokenInfo> refreshTokens = getUserRefreshTokens(username);
            
            // Remove expired tokens
            accessTokens.stream()
                .filter(token -> token.isExpired())
                .forEach(token -> deleteAccessToken(token.getToken()));
            
            refreshTokens.stream()
                .filter(token -> token.isExpired())
                .forEach(token -> deleteRefreshToken(token.getToken()));
            
            incrementOperationCounter("cleanupExpiredTokensForUser", true);
            log.debug("Cleaned up expired tokens for user: {}", username);
            
        } catch (Exception e) {
            incrementOperationCounter("cleanupExpiredTokensForUser", false);
            log.error("Failed to cleanup expired tokens for user: {}", username, e);
        }
    }

    @Override
    public void cleanupAllTokensForUser(String username) {
        try {
            List<TokenInfo> accessTokens = getUserAccessTokens(username);
            List<TokenInfo> refreshTokens = getUserRefreshTokens(username);
            
            // Remove all tokens
            accessTokens.forEach(token -> deleteAccessToken(token.getToken()));
            refreshTokens.forEach(token -> deleteRefreshToken(token.getToken()));
            
            incrementOperationCounter("cleanupAllTokensForUser", true);
            log.debug("Cleaned up all tokens for user: {}", username);
            
        } catch (Exception e) {
            incrementOperationCounter("cleanupAllTokensForUser", false);
            log.error("Failed to cleanup all tokens for user: {}", username, e);
        }
    }

    @Override
    public void cleanupTokensForSession(String sessionId) {
        try {
            String pattern = SESSION_TOKENS_PREFIX + sessionId + ":*";
            Set<String> keys = cacheService.getKeys(pattern);
            
            for (String key : keys) {
                cacheService.delete(USER_TOKENS_CACHE, key);
            }
            
            incrementOperationCounter("cleanupTokensForSession", true);
            log.debug("Cleaned up tokens for session: {}", sessionId);
            
        } catch (Exception e) {
            incrementOperationCounter("cleanupTokensForSession", false);
            log.error("Failed to cleanup tokens for session: {}", sessionId, e);
        }
    }

    // ==================== TOKEN SEARCH OPERATIONS ====================

    @Override
    public List<TokenInfo> searchTokens(TokenSearchCriteria criteria) {
        try {
            List<TokenInfo> allTokens = new ArrayList<>();
            
            // Search access tokens
            Set<String> accessTokenKeys = cacheService.getKeys(ACCESS_TOKEN_PREFIX + "*");
            for (String key : accessTokenKeys) {
                Optional<TokenInfo> tokenInfo = cacheService.get(ACCESS_TOKEN_CACHE, key, TokenInfo.class);
                tokenInfo.ifPresent(allTokens::add);
            }
            
            // Search refresh tokens
            Set<String> refreshTokenKeys = cacheService.getKeys(REFRESH_TOKEN_PREFIX + "*");
            for (String key : refreshTokenKeys) {
                Optional<TokenInfo> tokenInfo = cacheService.get(REFRESH_TOKEN_CACHE, key, TokenInfo.class);
                tokenInfo.ifPresent(allTokens::add);
            }
            
            // Apply filters
            return allTokens.stream()
                .filter(token -> criteria.getUsername() == null || criteria.getUsername().equals(token.getUsername()))
                .filter(token -> criteria.getClientId() == null || criteria.getClientId().equals(token.getClientId()))
                .filter(token -> criteria.getStatus() == null || criteria.getStatus().equals(token.getStatus()))
                .filter(token -> criteria.getTokenType() == null || criteria.getTokenType().equals(token.getTokenType()))
                .filter(token -> criteria.getScopes() == null || token.hasAllScopes(criteria.getScopes()))
                .filter(token -> criteria.getSessionId() == null || criteria.getSessionId().equals(token.getSessionId()))
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("Failed to search tokens", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<TokenInfo> getTokensByClientId(String clientId) {
        return searchTokens(TokenSearchCriteria.builder().clientId(clientId).build());
    }

    @Override
    public List<TokenInfo> getTokensByIpAddress(String ipAddress) {
        return searchTokens(TokenSearchCriteria.builder().ipAddress(ipAddress).build());
    }

    @Override
    public List<TokenInfo> getTokensByUserAgent(String userAgent) {
        return searchTokens(TokenSearchCriteria.builder().userAgent(userAgent).build());
    }

    // ==================== TOKEN STATISTICS ====================

    @Override
    public TokenStatistics getTokenStatistics() {
        try {
            List<TokenInfo> allTokens = searchTokens(new TokenSearchCriteria());
            
            long totalTokens = allTokens.size();
            long activeTokens = allTokens.stream().filter(TokenInfo::isActive).count();
            long expiredTokens = allTokens.stream().filter(TokenInfo::isExpired).count();
            long revokedTokens = allTokens.stream()
                .filter(token -> TokenInfo.TokenStatus.REVOKED.equals(token.getStatus())).count();
            
            long accessTokens = allTokens.stream()
                .filter(token -> "ACCESS".equals(token.getTokenType())).count();
            long refreshTokens = allTokens.stream()
                .filter(token -> "REFRESH".equals(token.getTokenType())).count();
            
            long totalUsageCount = allTokens.stream()
                .mapToLong(token -> token.getUsageCount() != null ? token.getUsageCount() : 0)
                .sum();
            
            double averageUsagePerToken = totalTokens > 0 ? (double) totalUsageCount / totalTokens : 0.0;
            
            return new TokenStatistics(totalTokens, activeTokens, expiredTokens, revokedTokens,
                accessTokens, refreshTokens, totalUsageCount, averageUsagePerToken);
                
        } catch (Exception e) {
            log.error("Failed to get token statistics", e);
            return new TokenStatistics(0, 0, 0, 0, 0, 0, 0, 0.0);
        }
    }

    @Override
    public TokenStatistics getTokenStatisticsForUser(String username) {
        try {
            List<TokenInfo> userTokens = new ArrayList<>();
            userTokens.addAll(getUserAccessTokens(username));
            userTokens.addAll(getUserRefreshTokens(username));
            
            long totalTokens = userTokens.size();
            long activeTokens = userTokens.stream().filter(TokenInfo::isActive).count();
            long expiredTokens = userTokens.stream().filter(TokenInfo::isExpired).count();
            long revokedTokens = userTokens.stream()
                .filter(token -> TokenInfo.TokenStatus.REVOKED.equals(token.getStatus())).count();
            
            long accessTokens = userTokens.stream()
                .filter(token -> "ACCESS".equals(token.getTokenType())).count();
            long refreshTokens = userTokens.stream()
                .filter(token -> "REFRESH".equals(token.getTokenType())).count();
            
            long totalUsageCount = userTokens.stream()
                .mapToLong(token -> token.getUsageCount() != null ? token.getUsageCount() : 0)
                .sum();
            
            double averageUsagePerToken = totalTokens > 0 ? (double) totalUsageCount / totalTokens : 0.0;
            
            return new TokenStatistics(totalTokens, activeTokens, expiredTokens, revokedTokens,
                accessTokens, refreshTokens, totalUsageCount, averageUsagePerToken);
                
        } catch (Exception e) {
            log.error("Failed to get token statistics for user: {}", username, e);
            return new TokenStatistics(0, 0, 0, 0, 0, 0, 0, 0.0);
        }
    }

    @Override
    public TokenStatistics getTokenStatisticsForClient(String clientId) {
        try {
            List<TokenInfo> clientTokens = getTokensByClientId(clientId);
            
            long totalTokens = clientTokens.size();
            long activeTokens = clientTokens.stream().filter(TokenInfo::isActive).count();
            long expiredTokens = clientTokens.stream().filter(TokenInfo::isExpired).count();
            long revokedTokens = clientTokens.stream()
                .filter(token -> TokenInfo.TokenStatus.REVOKED.equals(token.getStatus())).count();
            
            long accessTokens = clientTokens.stream()
                .filter(token -> "ACCESS".equals(token.getTokenType())).count();
            long refreshTokens = clientTokens.stream()
                .filter(token -> "REFRESH".equals(token.getTokenType())).count();
            
            long totalUsageCount = clientTokens.stream()
                .mapToLong(token -> token.getUsageCount() != null ? token.getUsageCount() : 0)
                .sum();
            
            double averageUsagePerToken = totalTokens > 0 ? (double) totalUsageCount / totalTokens : 0.0;
            
            return new TokenStatistics(totalTokens, activeTokens, expiredTokens, revokedTokens,
                accessTokens, refreshTokens, totalUsageCount, averageUsagePerToken);
                
        } catch (Exception e) {
            log.error("Failed to get token statistics for client: {}", clientId, e);
            return new TokenStatistics(0, 0, 0, 0, 0, 0, 0, 0.0);
        }
    }

    // ==================== TOKEN MONITORING ====================

    @Override
    public long getActiveTokenCount() {
        try {
            List<TokenInfo> allTokens = searchTokens(new TokenSearchCriteria());
            return allTokens.stream().filter(TokenInfo::isActive).count();
        } catch (Exception e) {
            log.error("Failed to get active token count", e);
            return 0;
        }
    }

    @Override
    public long getActiveTokenCountForUser(String username) {
        try {
            List<TokenInfo> userTokens = new ArrayList<>();
            userTokens.addAll(getUserAccessTokens(username));
            userTokens.addAll(getUserRefreshTokens(username));
            return userTokens.stream().filter(TokenInfo::isActive).count();
        } catch (Exception e) {
            log.error("Failed to get active token count for user: {}", username, e);
            return 0;
        }
    }

    @Override
    public Map<TokenInfo.TokenStatus, Long> getTokenCountByStatus() {
        try {
            List<TokenInfo> allTokens = searchTokens(new TokenSearchCriteria());
            return allTokens.stream()
                .collect(Collectors.groupingBy(TokenInfo::getStatus, Collectors.counting()));
        } catch (Exception e) {
            log.error("Failed to get token count by status", e);
            return new HashMap<>();
        }
    }

    @Override
    public Map<String, Long> getTokenCountByType() {
        try {
            List<TokenInfo> allTokens = searchTokens(new TokenSearchCriteria());
            return allTokens.stream()
                .collect(Collectors.groupingBy(TokenInfo::getTokenType, Collectors.counting()));
        } catch (Exception e) {
            log.error("Failed to get token count by type", e);
            return new HashMap<>();
        }
    }

    // ==================== TOKEN HEALTH CHECK ====================

    @Override
    public TokenStorageHealth checkTokenStorageHealth() {
        try {
            // Simple health check by trying to store and retrieve a test value
            String testKey = "health-check-" + System.currentTimeMillis();
            String testValue = "ok";
            
            cacheService.set(ACCESS_TOKEN_CACHE, testKey, testValue, Duration.ofSeconds(10));
            Optional<String> result = cacheService.get(ACCESS_TOKEN_CACHE, testKey, String.class);
            
            boolean healthy = result.isPresent() && testValue.equals(result.get());
            String status = healthy ? "HEALTHY" : "UNHEALTHY";
            String message = healthy ? "Token storage is operational" : "Token storage is not responding";
            
            return new TokenStorageHealth(healthy, status, message, 1, 0, 0);
            
        } catch (Exception e) {
            log.error("Token storage health check failed", e);
            return new TokenStorageHealth(false, "ERROR", e.getMessage(), 0, 0, 0);
        }
    }

    @Override
    public TokenStorageMetrics getTokenStorageMetrics() {
        try {
            long totalOperations = operationCounters.values().stream()
                .mapToLong(AtomicLong::get).sum();
            long successfulOperations = successCounters.values().stream()
                .mapToLong(AtomicLong::get).sum();
            long failedOperations = failureCounters.values().stream()
                .mapToLong(AtomicLong::get).sum();
            
            double successRate = totalOperations > 0 ? (double) successfulOperations / totalOperations : 0.0;
            long averageResponseTime = 0; // Would need to track response times
            long cacheHits = successfulOperations;
            long cacheMisses = failedOperations;
            double hitRate = totalOperations > 0 ? (double) cacheHits / totalOperations : 0.0;
            
            return new TokenStorageMetrics(totalOperations, successfulOperations, failedOperations,
                successRate, averageResponseTime, cacheHits, cacheMisses, hitRate);
                
        } catch (Exception e) {
            log.error("Failed to get token storage metrics", e);
            return new TokenStorageMetrics(0, 0, 0, 0.0, 0, 0, 0, 0.0);
        }
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Generate a hash for the token
     */
    private String generateTokenHash(String token) {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Increment operation counters for metrics
     */
    private void incrementOperationCounter(String operation, boolean success) {
        operationCounters.computeIfAbsent(operation, k -> new AtomicLong()).incrementAndGet();
        if (success) {
            successCounters.computeIfAbsent(operation, k -> new AtomicLong()).incrementAndGet();
        } else {
            failureCounters.computeIfAbsent(operation, k -> new AtomicLong()).incrementAndGet();
        }
    }
} 