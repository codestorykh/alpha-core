package com.codestorykh.alpha.oauth2.service;

import com.codestorykh.alpha.oauth2.dto.PartnerServiceRegistrationDTO;
import com.codestorykh.alpha.oauth2.dto.PartnerServiceResponseDTO;

import java.util.List;

public interface PartnerServiceService {

    /**
     * Register a new partner service and create OAuth2 client credentials
     */
    PartnerServiceResponseDTO registerPartnerService(PartnerServiceRegistrationDTO registrationDTO);

    /**
     * Get all registered partner services
     */
    List<PartnerServiceResponseDTO> getAllPartnerServices();

    /**
     * Get a specific partner service by client ID
     */
    PartnerServiceResponseDTO getPartnerService(String clientId);

    /**
     * Regenerate client secret for a partner service
     */
    PartnerServiceResponseDTO regenerateClientSecret(String clientId);

    /**
     * Enable a partner service
     */
    PartnerServiceResponseDTO enablePartnerService(String clientId);

    /**
     * Disable a partner service
     */
    PartnerServiceResponseDTO disablePartnerService(String clientId);

    /**
     * Delete a partner service
     */
    void deletePartnerService(String clientId);

    /**
     * Get OAuth2 authorization server metadata
     */
    Object getAuthorizationServerMetadata();

    /**
     * Validate an OAuth2 access token
     */
    Object validateToken(String authorizationHeader);
} 