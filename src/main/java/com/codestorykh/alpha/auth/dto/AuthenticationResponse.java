package com.codestorykh.alpha.auth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {

    // Token Information
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    
    // Token Metadata
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime issuedAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime accessTokenExpiresAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime refreshTokenExpiresAt;
    private Long accessTokenExpiresIn; // seconds
    private Long refreshTokenExpiresIn; // seconds
    
    // User Information
    private UserInfo user;
    
    // Application Information
    private String applicationName;
    private String version;
    private String environment;
    
    // Security Information
    private List<String> scopes;
    private List<String> roles;
    private List<String> permissions;
    
    // Status Information
    private String status;
    private String message;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String fullName;
        private String phoneNumber;
        private boolean emailVerified;
        private boolean phoneVerified;
        private String status;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        private LocalDateTime lastLogin;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        private LocalDateTime createdAt;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        private LocalDateTime updatedAt;
    }
} 