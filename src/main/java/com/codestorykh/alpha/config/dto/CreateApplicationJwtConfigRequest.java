package com.codestorykh.alpha.config.dto;

import com.codestorykh.alpha.config.domain.ApplicationJwtConfig;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateApplicationJwtConfigRequest {

    @NotBlank(message = "Application name is required")
    @Size(min = 2, max = 100, message = "Application name must be between 2 and 100 characters")
    private String applicationName;

    @Size(max = 50, message = "Environment name must not exceed 50 characters")
    private String environment = "default";

    @NotBlank(message = "JWT secret is required")
    @Size(min = 32, max = 512, message = "JWT secret must be between 32 and 512 characters")
    private String secret;

    @NotNull(message = "Expiration time is required")
    @Positive(message = "Expiration time must be positive")
    private Long expirationMs = 3600000L;

    @NotNull(message = "Refresh expiration time is required")
    @Positive(message = "Refresh expiration time must be positive")
    private Long refreshExpirationMs = 86400000L;

    @NotBlank(message = "JWT issuer is required")
    @Size(max = 100, message = "JWT issuer must not exceed 100 characters")
    private String issuer = "alpha-identity-server";

    @NotBlank(message = "JWT audience is required")
    @Size(max = 100, message = "JWT audience must not exceed 100 characters")
    private String audience = "alpha-clients";

    private boolean enabled = true;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    private boolean isDefault = false;

    private boolean system = false;

    private boolean readonly = false;

    private ApplicationJwtConfig.JwtAlgorithm algorithm = ApplicationJwtConfig.JwtAlgorithm.HS256;

    private String keyId;

    @Size(max = 1000, message = "Additional claims must not exceed 1000 characters")
    private String additionalClaims;
} 