package com.codestorykh.alpha.oauth2.dto;

import com.codestorykh.alpha.oauth2.domain.OAuth2Client;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerServiceResponseDTO {

    private String clientId;
    private String clientSecret; // Only included during registration
    private String serviceName;
    private String description;
    private String organizationName;
    private String contactEmail;
    private String contactPhone;
    private Set<String> scopes;
    private Set<String> grantTypes;
    private String tokenEndpointAuthMethod;
    private boolean enabled;
    private boolean requireProofKey;
    private boolean requireAuthorizationConsent;
    private Integer accessTokenValiditySeconds;
    private Integer refreshTokenValiditySeconds;
    private LocalDateTime lastUsed;
    private Long usageCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String integrationNotes;
    private String tokenEndpoint;
    private String wellKnownEndpoint;

    public static PartnerServiceResponseDTO fromOAuth2Client(OAuth2Client client) {
        return PartnerServiceResponseDTO.builder()
                .clientId(client.getClientId())
                .serviceName(client.getClientName())
                .description(client.getDescription())
                .scopes(client.getScopes())
                .grantTypes(client.getGrantTypes().stream()
                        .map(Enum::name)
                        .collect(java.util.stream.Collectors.toSet()))
                .tokenEndpointAuthMethod(client.getTokenEndpointAuthMethod().name())
                .enabled(client.isEnabled())
                .requireProofKey(client.isRequireProofKey())
                .requireAuthorizationConsent(client.isRequireAuthorizationConsent())
                .accessTokenValiditySeconds(client.getAccessTokenValiditySeconds())
                .refreshTokenValiditySeconds(client.getRefreshTokenValiditySeconds())
                .lastUsed(client.getLastUsed())
                .usageCount(client.getUsageCount())
                .createdAt(client.getCreatedAt())
                .updatedAt(client.getUpdatedAt())
                .tokenEndpoint("http://localhost:8080/oauth2/token")
                .wellKnownEndpoint("http://localhost:8080/.well-known/oauth-authorization-server")
                .build();
    }
} 