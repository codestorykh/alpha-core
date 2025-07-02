package com.codestorykh.alpha.oauth2.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerServiceRegistrationDTO {

    @NotBlank(message = "Service name is required")
    @Size(min = 3, max = 100, message = "Service name must be between 3 and 100 characters")
    private String serviceName;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotBlank(message = "Partner organization name is required")
    @Size(min = 2, max = 200, message = "Organization name must be between 2 and 200 characters")
    private String organizationName;

    @NotBlank(message = "Contact email is required")
    @jakarta.validation.constraints.Email(message = "Contact email must be valid")
    private String contactEmail;

    @Size(max = 20, message = "Contact phone cannot exceed 20 characters")
    private String contactPhone;

    @NotEmpty(message = "At least one scope is required")
    private Set<String> requestedScopes;

    @Size(max = 1000, message = "Integration notes cannot exceed 1000 characters")
    private String integrationNotes;

    @Builder.Default
    private boolean requireProofKey = false;

    @Builder.Default
    private boolean requireAuthorizationConsent = false;

    @Builder.Default
    private Integer accessTokenValiditySeconds = 3600; // 1 hour

    @Builder.Default
    private Integer refreshTokenValiditySeconds = 86400; // 24 hours
} 