package com.codestorykh.alpha.oauth2.domain;

import com.codestorykh.alpha.common.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "oauth2_clients", indexes = {
    @Index(name = "idx_client_id", columnList = "client_id"),
    @Index(name = "idx_client_enabled", columnList = "enabled")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"scopes", "redirectUris", "grantTypes"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuth2Client extends BaseEntity {

    @NotBlank
    @Size(min = 3, max = 100)
    @Column(name = "client_id", unique = true, nullable = false)
    private String clientId;

    @NotBlank
    @Size(min = 8)
    @Column(name = "client_secret", nullable = false)
    private String clientSecret;

    @Size(max = 255)
    @Column(name = "client_name")
    private String clientName;

    @Size(max = 500)
    private String description;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "require_proof_key")
    private boolean requireProofKey = false;

    @Column(name = "require_authorization_consent")
    private boolean requireAuthorizationConsent = true;

    @Column(name = "access_token_validity_seconds")
    private Integer accessTokenValiditySeconds = 3600;

    @Column(name = "refresh_token_validity_seconds")
    private Integer refreshTokenValiditySeconds = 86400;

    @Enumerated(EnumType.STRING)
    @Column(name = "token_endpoint_auth_method", nullable = false)
    private TokenEndpointAuthMethod tokenEndpointAuthMethod = TokenEndpointAuthMethod.CLIENT_SECRET_BASIC;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "oauth2_client_scopes",
        joinColumns = @JoinColumn(name = "client_id")
    )
    @Column(name = "scope")
    private Set<String> scopes = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "oauth2_client_redirect_uris",
        joinColumns = @JoinColumn(name = "client_id")
    )
    @Column(name = "redirect_uri")
    private Set<String> redirectUris = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "oauth2_client_grant_types",
        joinColumns = @JoinColumn(name = "client_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "grant_type")
    private Set<GrantType> grantTypes = new HashSet<>();

    @Column(name = "last_used")
    private LocalDateTime lastUsed;

    @Column(name = "usage_count")
    private Long usageCount = 0L;

    public void incrementUsage() {
        this.usageCount++;
        this.lastUsed = LocalDateTime.now();
    }

    public boolean supportsGrantType(GrantType grantType) {
        return grantTypes.contains(grantType);
    }

    public boolean hasScope(String scope) {
        return scopes.contains(scope);
    }

    public boolean isValidRedirectUri(String redirectUri) {
        return redirectUris.contains(redirectUri);
    }
} 