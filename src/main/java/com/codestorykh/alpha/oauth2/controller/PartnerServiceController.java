package com.codestorykh.alpha.oauth2.controller;

import com.codestorykh.alpha.oauth2.dto.PartnerServiceRegistrationDTO;
import com.codestorykh.alpha.oauth2.dto.PartnerServiceResponseDTO;
import com.codestorykh.alpha.oauth2.service.OAuth2ClientService;
import com.codestorykh.alpha.oauth2.service.PartnerServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/oauth2/partner")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Partner Service Management", description = "APIs for managing partner service OAuth2 clients")
public class PartnerServiceController {

    private final PartnerServiceService partnerServiceService;
    private final OAuth2ClientService oauth2ClientService;

    @PostMapping("/register")
    @Operation(summary = "Register a new partner service", description = "Creates OAuth2 client credentials for a partner service")
    public ResponseEntity<PartnerServiceResponseDTO> registerPartnerService(
            @Valid @RequestBody PartnerServiceRegistrationDTO registrationDTO) {
        log.info("Registering partner service: {}", registrationDTO.getServiceName());
        
        PartnerServiceResponseDTO response = partnerServiceService.registerPartnerService(registrationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/services")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all partner services", description = "Retrieves all registered partner services")
    public ResponseEntity<List<PartnerServiceResponseDTO>> getAllPartnerServices() {
        List<PartnerServiceResponseDTO> services = partnerServiceService.getAllPartnerServices();
        return ResponseEntity.ok(services);
    }

    @GetMapping("/services/{clientId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get partner service by client ID", description = "Retrieves a specific partner service")
    public ResponseEntity<PartnerServiceResponseDTO> getPartnerService(
            @Parameter(description = "OAuth2 client ID") @PathVariable String clientId) {
        PartnerServiceResponseDTO service = partnerServiceService.getPartnerService(clientId);
        return ResponseEntity.ok(service);
    }

    @PutMapping("/services/{clientId}/regenerate-secret")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Regenerate client secret", description = "Generates a new client secret for the partner service")
    public ResponseEntity<PartnerServiceResponseDTO> regenerateClientSecret(
            @Parameter(description = "OAuth2 client ID") @PathVariable String clientId) {
        PartnerServiceResponseDTO response = partnerServiceService.regenerateClientSecret(clientId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/services/{clientId}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Enable partner service", description = "Enables a disabled partner service")
    public ResponseEntity<PartnerServiceResponseDTO> enablePartnerService(
            @Parameter(description = "OAuth2 client ID") @PathVariable String clientId) {
        PartnerServiceResponseDTO response = partnerServiceService.enablePartnerService(clientId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/services/{clientId}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Disable partner service", description = "Disables a partner service")
    public ResponseEntity<PartnerServiceResponseDTO> disablePartnerService(
            @Parameter(description = "OAuth2 client ID") @PathVariable String clientId) {
        PartnerServiceResponseDTO response = partnerServiceService.disablePartnerService(clientId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/services/{clientId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete partner service", description = "Permanently deletes a partner service")
    public ResponseEntity<Void> deletePartnerService(
            @Parameter(description = "OAuth2 client ID") @PathVariable String clientId) {
        partnerServiceService.deletePartnerService(clientId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/token-endpoint")
    @Operation(summary = "Get token endpoint", description = "Returns the OAuth2 token endpoint URL")
    public ResponseEntity<String> getTokenEndpoint() {
        String tokenEndpoint = "http://localhost:8080/oauth2/token";
        return ResponseEntity.ok(tokenEndpoint);
    }

    @GetMapping("/well-known/oauth-authorization-server")
    @Operation(summary = "OAuth2 Authorization Server Metadata", description = "Returns OAuth2 authorization server metadata")
    public ResponseEntity<Object> getAuthorizationServerMetadata() {
        return ResponseEntity.ok(partnerServiceService.getAuthorizationServerMetadata());
    }

    @GetMapping("/scopes")
    @Operation(summary = "Get available scopes", description = "Returns available OAuth2 scopes for partner services")
    public ResponseEntity<List<String>> getAvailableScopes() {
        List<String> scopes = List.of(
            "read", "write", "admin", "user", 
            "partner.read", "partner.write", "partner.admin",
            "service.read", "service.write", "service.admin"
        );
        return ResponseEntity.ok(scopes);
    }

    @PostMapping("/validate-token")
    @Operation(summary = "Validate access token", description = "Validates an OAuth2 access token")
    public ResponseEntity<Object> validateToken(
            @RequestHeader("Authorization") String authorizationHeader) {
        Object validationResult = partnerServiceService.validateToken(authorizationHeader);
        return ResponseEntity.ok(validationResult);
    }

    @GetMapping("/test")
    @Operation(summary = "Test endpoint", description = "Simple test endpoint to check if the service is working")
    public ResponseEntity<String> test() {
        try {
            // Try to access the repository
            long count = oauth2ClientService.count();
            return ResponseEntity.ok("Service is working. OAuth2 clients count: " + count);
        } catch (Exception e) {
            log.error("Test failed: ", e);
            return ResponseEntity.status(500).body("Test failed: " + e.getMessage());
        }
    }
} 