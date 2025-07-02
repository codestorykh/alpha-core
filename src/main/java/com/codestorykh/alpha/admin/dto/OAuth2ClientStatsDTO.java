package com.codestorykh.alpha.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2ClientStatsDTO {
    private LocalDateTime timestamp;
    private long totalClients;
    private long enabledClients;
    private long disabledClients;
    private long clientsWithAuthorizationCode;
    private long clientsWithClientCredentials;
    private long clientsWithPasswordGrant;
    private long clientsWithRefreshToken;
    private long totalTokenRequests;
    private long successfulTokenRequests;
    private long failedTokenRequests;
    private double averageTokenResponseTime;
    private long activeTokens;
    private long expiredTokens;
} 