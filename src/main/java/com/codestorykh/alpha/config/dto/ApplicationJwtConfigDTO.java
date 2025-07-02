package com.codestorykh.alpha.config.dto;

import com.codestorykh.alpha.common.dto.BaseDTO;
import com.codestorykh.alpha.config.domain.ApplicationJwtConfig;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationJwtConfigDTO extends BaseDTO {

    @NotBlank
    @Size(min = 2, max = 100)
    private String applicationName;

    @Size(max = 50)
    private String environment = "default";

    @NotBlank
    @Size(min = 32, max = 512)
    private String secret;

    @NotNull
    @Positive
    private Long expirationMs = 3600000L;

    @NotNull
    @Positive
    private Long refreshExpirationMs = 86400000L;

    @NotBlank
    @Size(max = 100)
    private String issuer = "alpha-identity-server";

    @NotBlank
    @Size(max = 100)
    private String audience = "alpha-clients";

    private boolean enabled = true;

    @Size(max = 255)
    private String description;

    private boolean isDefault = false;

    private boolean system = false;

    private boolean readonly = false;

    private ApplicationJwtConfig.JwtAlgorithm algorithm = ApplicationJwtConfig.JwtAlgorithm.HS256;

    private String keyId;

    @Size(max = 1000)
    private String additionalClaims;

    // Helper methods
    public String getFullApplicationName() {
        return environment.equals("default") ? applicationName : applicationName + "-" + environment;
    }

    public boolean isDefaultConfig() {
        return isDefault;
    }

    public boolean isSystemConfig() {
        return system;
    }

    public boolean isReadonlyConfig() {
        return readonly;
    }
} 