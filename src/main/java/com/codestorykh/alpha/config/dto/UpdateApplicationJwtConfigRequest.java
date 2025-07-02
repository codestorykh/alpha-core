package com.codestorykh.alpha.config.dto;

import com.codestorykh.alpha.config.domain.ApplicationJwtConfig;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateApplicationJwtConfigRequest {

    @Size(min = 32, max = 512, message = "JWT secret must be between 32 and 512 characters")
    private String secret;

    @Positive(message = "Expiration time must be positive")
    private Long expirationMs;

    @Positive(message = "Refresh expiration time must be positive")
    private Long refreshExpirationMs;

    @Size(max = 100, message = "JWT issuer must not exceed 100 characters")
    private String issuer;

    @Size(max = 100, message = "JWT audience must not exceed 100 characters")
    private String audience;

    private Boolean enabled;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    private Boolean isDefault;

    private Boolean system;

    private Boolean readonly;

    private ApplicationJwtConfig.JwtAlgorithm algorithm;

    private String keyId;

    @Size(max = 1000, message = "Additional claims must not exceed 1000 characters")
    private String additionalClaims;
} 