package com.codestorykh.alpha.config.domain;

import com.codestorykh.alpha.common.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "application_jwt_configs", indexes = {
    @Index(name = "idx_app_jwt_config_app_name", columnList = "application_name"),
    @Index(name = "idx_app_jwt_config_enabled", columnList = "enabled"),
    @Index(name = "idx_app_jwt_config_environment", columnList = "environment")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationJwtConfig extends BaseEntity {

    @NotBlank
    @Size(min = 2, max = 100)
    @Column(name = "application_name", nullable = false)
    private String applicationName;

    @Size(max = 50)
    @Column(name = "environment")
    private String environment = "default";

    @NotBlank
    @Size(min = 32, max = 512)
    @Column(name = "secret", nullable = false)
    private String secret;

    @NotNull
    @Positive
    @Column(name = "expiration_ms", nullable = false)
    private Long expirationMs = 3600000L; // 1 hour default

    @NotNull
    @Positive
    @Column(name = "refresh_expiration_ms", nullable = false)
    private Long refreshExpirationMs = 86400000L; // 24 hours default

    @NotBlank
    @Size(max = 100)
    @Column(name = "issuer", nullable = false)
    private String issuer = "alpha-identity-server";

    @NotBlank
    @Size(max = 100)
    @Column(name = "audience", nullable = false)
    private String audience = "alpha-clients";

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Size(max = 255)
    @Column(name = "description")
    private String description;

    @Column(name = "is_default")
    private boolean isDefault = false;

    @Column(name = "is_system")
    private boolean system = false;

    @Column(name = "is_readonly")
    private boolean readonly = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "algorithm", nullable = false)
    private JwtAlgorithm algorithm = JwtAlgorithm.HS256;

    @Column(name = "key_id")
    private String keyId;

    @Size(max = 1000)
    @Column(name = "additional_claims")
    private String additionalClaims; // JSON string for additional JWT claims

    public enum JwtAlgorithm {
        HS256, HS384, HS512, RS256, RS384, RS512, ES256, ES384, ES512
    }

    // Helper methods
    public boolean isDefaultConfig() {
        return isDefault;
    }

    public boolean isSystemConfig() {
        return system;
    }

    public boolean isReadonlyConfig() {
        return readonly;
    }

    public String getFullApplicationName() {
        return environment.equals("default") ? applicationName : applicationName + "-" + environment;
    }

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        if (environment == null) {
            environment = "default";
        }
    }
} 