package com.codestorykh.alpha.oauth2.dto;

import com.codestorykh.alpha.common.dto.BaseDTO;
import com.codestorykh.alpha.oauth2.domain.GrantType;
import com.codestorykh.alpha.oauth2.domain.TokenEndpointAuthMethod;
import com.codestorykh.alpha.validation.annotation.ValidUrl;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2ClientDTO extends BaseDTO {

    @NotBlank(message = "Client ID is required")
    private String clientId;

    @NotBlank(message = "Client secret is required")
    private String clientSecret;

    @NotBlank(message = "Client name is required")
    private String clientName;

    private String description;

    @NotEmpty(message = "At least one redirect URI is required")
    @ValidUrl(allowedProtocols = {"http", "https"}, allowLocalhost = true)
    private Set<String> redirectUris;

    @NotEmpty(message = "At least one grant type is required")
    private Set<GrantType> grantTypes;

    @NotEmpty(message = "At least one scope is required")
    private Set<String> scopes;

    private TokenEndpointAuthMethod tokenEndpointAuthMethod;
    private boolean enabled;
    private boolean requireProofKey;
    private boolean requireAuthorizationConsent;
    private Integer accessTokenValiditySeconds;
    private Integer refreshTokenValiditySeconds;
    private LocalDateTime lastUsed;
    private Long usageCount;
} 