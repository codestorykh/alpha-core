package com.codestorykh.alpha.oauth2.service.impl;

import com.codestorykh.alpha.oauth2.domain.GrantType;
import com.codestorykh.alpha.oauth2.domain.OAuth2Client;
import com.codestorykh.alpha.oauth2.domain.TokenEndpointAuthMethod;
import com.codestorykh.alpha.oauth2.dto.OAuth2ClientDTO;
import com.codestorykh.alpha.oauth2.dto.PartnerServiceRegistrationDTO;
import com.codestorykh.alpha.oauth2.dto.PartnerServiceResponseDTO;
import com.codestorykh.alpha.oauth2.repository.OAuth2ClientRepository;
import com.codestorykh.alpha.oauth2.service.JwtTokenService;
import com.codestorykh.alpha.oauth2.service.OAuth2ClientService;
import com.codestorykh.alpha.oauth2.service.PartnerServiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PartnerServiceServiceImpl implements PartnerServiceService {

    private final OAuth2ClientService oauth2ClientService;
    private final OAuth2ClientRepository oauth2ClientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    @Override
    public PartnerServiceResponseDTO registerPartnerService(PartnerServiceRegistrationDTO registrationDTO) {
        log.info("Registering partner service: {}", registrationDTO.getServiceName());

        // Generate client ID and secret
        String clientId = generateClientId(registrationDTO.getServiceName());
        String clientSecret = generateClientSecret();

        // Create OAuth2 client DTO
        OAuth2ClientDTO clientDTO = OAuth2ClientDTO.builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientName(registrationDTO.getServiceName())
                .description(registrationDTO.getDescription())
                .redirectUris(Set.of("http://localhost:8080/callback")) // Default redirect URI
                .grantTypes(Set.of(GrantType.CLIENT_CREDENTIALS))
                .scopes(registrationDTO.getRequestedScopes())
                .tokenEndpointAuthMethod(TokenEndpointAuthMethod.CLIENT_SECRET_BASIC)
                .enabled(true)
                .requireProofKey(registrationDTO.isRequireProofKey())
                .requireAuthorizationConsent(registrationDTO.isRequireAuthorizationConsent())
                .accessTokenValiditySeconds(registrationDTO.getAccessTokenValiditySeconds())
                .refreshTokenValiditySeconds(registrationDTO.getRefreshTokenValiditySeconds())
                .build();

        // Create the OAuth2 client
        OAuth2Client client = oauth2ClientService.createClient(clientDTO);

        // Create response DTO
        PartnerServiceResponseDTO response = PartnerServiceResponseDTO.fromOAuth2Client(client);
        response.setClientSecret(clientSecret); // Include secret only during registration
        response.setOrganizationName(registrationDTO.getOrganizationName());
        response.setContactEmail(registrationDTO.getContactEmail());
        response.setContactPhone(registrationDTO.getContactPhone());
        response.setIntegrationNotes(registrationDTO.getIntegrationNotes());

        log.info("Successfully registered partner service: {} with client ID: {}", 
                registrationDTO.getServiceName(), clientId);

        return response;
    }

    @Override
    public List<PartnerServiceResponseDTO> getAllPartnerServices() {
        log.debug("Retrieving all partner services");
        
        return oauth2ClientRepository.findAll().stream()
                .filter(client -> client.getClientName() != null && 
                        (client.getClientName().contains("Partner") || 
                         client.getClientName().contains("Service")))
                .map(PartnerServiceResponseDTO::fromOAuth2Client)
                .collect(Collectors.toList());
    }

    @Override
    public PartnerServiceResponseDTO getPartnerService(String clientId) {
        log.debug("Retrieving partner service with client ID: {}", clientId);
        
        OAuth2Client client = oauth2ClientService.findByClientId(clientId)
                .orElseThrow(() -> new RuntimeException("Partner service not found with client ID: " + clientId));
        
        return PartnerServiceResponseDTO.fromOAuth2Client(client);
    }

    @Override
    public PartnerServiceResponseDTO regenerateClientSecret(String clientId) {
        log.info("Regenerating client secret for partner service: {}", clientId);
        
        oauth2ClientService.regenerateClientSecret(clientId);
        
        OAuth2Client client = oauth2ClientService.findByClientId(clientId)
                .orElseThrow(() -> new RuntimeException("Partner service not found with client ID: " + clientId));
        
        return PartnerServiceResponseDTO.fromOAuth2Client(client);
    }

    @Override
    public PartnerServiceResponseDTO enablePartnerService(String clientId) {
        log.info("Enabling partner service: {}", clientId);
        
        oauth2ClientService.enableClient(clientId);
        
        OAuth2Client client = oauth2ClientService.findByClientId(clientId)
                .orElseThrow(() -> new RuntimeException("Partner service not found with client ID: " + clientId));
        
        return PartnerServiceResponseDTO.fromOAuth2Client(client);
    }

    @Override
    public PartnerServiceResponseDTO disablePartnerService(String clientId) {
        log.info("Disabling partner service: {}", clientId);
        
        OAuth2Client client = oauth2ClientService.findByClientId(clientId)
                .orElseThrow(() -> new RuntimeException("Partner service not found with client ID: " + clientId));
        
        client.setEnabled(false);
        oauth2ClientService.save(client);
        
        return PartnerServiceResponseDTO.fromOAuth2Client(client);
    }

    @Override
    public void deletePartnerService(String clientId) {
        log.info("Deleting partner service: {}", clientId);
        
        OAuth2Client client = oauth2ClientService.findByClientId(clientId)
                .orElseThrow(() -> new RuntimeException("Partner service not found with client ID: " + clientId));
        
        oauth2ClientService.delete(client);
    }

    @Override
    public Object getAuthorizationServerMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("issuer", "http://localhost:8080");
        metadata.put("authorization_endpoint", "http://localhost:8080/oauth2/authorize");
        metadata.put("token_endpoint", "http://localhost:8080/oauth2/token");
        metadata.put("token_endpoint_auth_methods_supported", 
                List.of("client_secret_basic", "client_secret_post"));
        metadata.put("jwks_uri", "http://localhost:8080/oauth2/jwks");
        metadata.put("response_types_supported", List.of("code", "token"));
        metadata.put("subject_types_supported", List.of("public"));
        metadata.put("id_token_signing_alg_values_supported", List.of("HS256"));
        metadata.put("scopes_supported", List.of("read", "write", "admin", "user", 
                "partner.read", "partner.write", "partner.admin",
                "service.read", "service.write", "service.admin"));
        metadata.put("grant_types_supported", List.of("authorization_code", "client_credentials", 
                "refresh_token", "password"));
        
        return metadata;
    }

    @Override
    public Object validateToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return Map.of("valid", false, "error", "Invalid authorization header format");
        }

        String token = authorizationHeader.substring(7);
        
        try {
            if (jwtTokenService.isTokenValid(token)) {
                String clientId = jwtTokenService.getClientIdFromToken(token);
                List<String> scopes = jwtTokenService.getScopesFromToken(token);
                List<String> roles = jwtTokenService.getRolesFromToken(token);
                
                return Map.of(
                    "valid", true,
                    "subject", clientId,
                    "issuer", "alpha-identity-server",
                    "audience", "alpha-clients",
                    "scopes", scopes,
                    "roles", roles
                );
            } else {
                return Map.of("valid", false, "error", "Invalid token");
            }
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return Map.of("valid", false, "error", e.getMessage());
        }
    }

    private String generateClientId(String serviceName) {
        String baseName = serviceName.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8); // Last 4 digits
        return baseName + "-" + timestamp;
    }

    private String generateClientSecret() {
        return UUID.randomUUID().toString().replace("-", "") + 
               UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
} 