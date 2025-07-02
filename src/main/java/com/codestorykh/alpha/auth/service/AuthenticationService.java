package com.codestorykh.alpha.auth.service;

import com.codestorykh.alpha.auth.dto.AuthenticationRequest;
import com.codestorykh.alpha.auth.dto.AuthenticationResponse;
import com.codestorykh.alpha.auth.dto.RegisterRequest;
import com.codestorykh.alpha.config.service.ConfigurationService;
import com.codestorykh.alpha.identity.domain.User;
import com.codestorykh.alpha.identity.service.UserService;
import com.codestorykh.alpha.security.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ConfigurationService configurationService;

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = userService.loadUserByUsername(request.getUsername());
        String jwtToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Update last login
        userService.updateLastLogin(request.getUsername());

        // Get user entity for additional info
        User user = userService.findByUsername(request.getUsername()).orElse(null);

        // Calculate token expiration times
        LocalDateTime now = LocalDateTime.now();
        long jwtExpiration = configurationService.getJwtExpiration();
        long refreshExpiration = configurationService.getJwtRefreshExpiration();
        LocalDateTime accessTokenExpiresAt = now.plusSeconds(jwtExpiration / 1000);
        LocalDateTime refreshTokenExpiresAt = now.plusSeconds(refreshExpiration / 1000);

        return buildAuthenticationResponse(jwtToken, refreshToken, userDetails, user, now, accessTokenExpiresAt, refreshTokenExpiresAt);
    }

    public AuthenticationResponse register(RegisterRequest request) {
        // Create user DTO from register request
        var userDTO = com.codestorykh.alpha.identity.dto.UserDTO.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .enabled(true)
                .build();

        User user = userService.createUser(userDTO);
        UserDetails userDetails = userService.loadUserByUsername(user.getUsername());
        
        String jwtToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Calculate token expiration times
        LocalDateTime now = LocalDateTime.now();
        long jwtExpiration = configurationService.getJwtExpiration();
        long refreshExpiration = configurationService.getJwtRefreshExpiration();
        LocalDateTime accessTokenExpiresAt = now.plusSeconds(jwtExpiration / 1000);
        LocalDateTime refreshTokenExpiresAt = now.plusSeconds(refreshExpiration / 1000);

        return buildAuthenticationResponse(jwtToken, refreshToken, userDetails, user, now, accessTokenExpiresAt, refreshTokenExpiresAt);
    }

    public AuthenticationResponse refreshToken(String refreshToken) {
        final String username = jwtService.extractUsername(refreshToken);
        if (username != null) {
            UserDetails userDetails = userService.loadUserByUsername(username);
            if (jwtService.isTokenValid(refreshToken, userDetails)) {
                String newAccessToken = jwtService.generateToken(userDetails);
                
                // Get user entity for additional info
                User user = userService.findByUsername(username).orElse(null);
                
                // Calculate token expiration times
                LocalDateTime now = LocalDateTime.now();
                long jwtExpiration = configurationService.getJwtExpiration();
                long refreshExpiration = configurationService.getJwtRefreshExpiration();
                LocalDateTime accessTokenExpiresAt = now.plusSeconds(jwtExpiration / 1000);
                LocalDateTime refreshTokenExpiresAt = now.plusSeconds(refreshExpiration / 1000);

                return buildAuthenticationResponse(newAccessToken, refreshToken, userDetails, user, now, accessTokenExpiresAt, refreshTokenExpiresAt);
            }
        }
        throw new RuntimeException("Invalid refresh token");
    }

    private AuthenticationResponse buildAuthenticationResponse(
            String accessToken, 
            String refreshToken, 
            UserDetails userDetails, 
            User user, 
            LocalDateTime issuedAt,
            LocalDateTime accessTokenExpiresAt,
            LocalDateTime refreshTokenExpiresAt) {
        
        // Extract authorities
        List<String> authorities = userDetails.getAuthorities().stream()
                .map(Object::toString)
                .toList();

        // Separate roles and permissions
        List<String> roles = authorities.stream()
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.substring(5)) // Remove "ROLE_" prefix
                .collect(Collectors.toList());

        List<String> permissions = authorities.stream()
                .filter(auth -> !auth.startsWith("ROLE_"))
                .collect(Collectors.toList());

        // Build user info
        AuthenticationResponse.UserInfo userInfo = null;
        if (user != null) {
            userInfo = AuthenticationResponse.UserInfo.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .fullName(user.getFullName())
                    .phoneNumber(user.getPhoneNumber())
                    .emailVerified(user.isEmailVerified())
                    .phoneVerified(user.isPhoneVerified())
                    .status(user.getStatus().toString())
                    .lastLogin(user.getLastLogin())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .build();
        }

        return AuthenticationResponse.builder()
                // Token Information
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                
                // Token Metadata
                .issuedAt(issuedAt)
                .accessTokenExpiresAt(accessTokenExpiresAt)
                .refreshTokenExpiresAt(refreshTokenExpiresAt)
                .accessTokenExpiresIn(configurationService.getJwtExpiration() / 1000)
                .refreshTokenExpiresIn(configurationService.getJwtRefreshExpiration() / 1000)
                
                // User Information
                .user(userInfo)
                
                // Application Information
                .applicationName(configurationService.getValue("app.name", "alpha-oauth2").orElse("alpha-oauth2"))
                .version(configurationService.getValue("app.version", "1.0.0").orElse("1.0.0"))
                .environment(configurationService.getValue("app.environment", "dev").orElse("dev"))
                
                // Security Information
                .scopes(configurationService.getOAuth2Scopes()) // Configurable OAuth2 scopes
                .roles(roles)
                .permissions(permissions)
                
                // Status Information
                .status("success")
                .message("Authentication successful")
                .build();
    }
} 