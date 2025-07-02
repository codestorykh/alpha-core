package com.codestorykh.alpha.oauth2.controller;

import com.codestorykh.alpha.oauth2.service.JwtTokenService;
import com.codestorykh.alpha.oauth2.service.OAuth2ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "OAuth2 Token", description = "OAuth2 token generation and validation")
public class TokenController {

    private final JwtTokenService jwtTokenService;
    private final OAuth2ClientService oauth2ClientService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/token")
    @Operation(summary = "Generate access token", description = "Generate JWT access token using client credentials")
    public ResponseEntity<Map<String, Object>> generateToken(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(value = "grant_type", defaultValue = "client_credentials") String grantType,
            @RequestParam(value = "scope", required = false) String scope) {
        
        try {
            // Extract client credentials from Authorization header
            if (authorizationHeader == null || !authorizationHeader.startsWith("Basic ")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "invalid_client",
                    "error_description", "Client authentication failed"
                ));
            }

            String credentials = authorizationHeader.substring(6);
            String decodedCredentials = new String(java.util.Base64.getDecoder().decode(credentials));
            String[] parts = decodedCredentials.split(":");
            
            if (parts.length != 2) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "invalid_client",
                    "error_description", "Invalid client credentials format"
                ));
            }

            String clientId = parts[0];
            String clientSecret = parts[1];

            // Validate client credentials
            var client = oauth2ClientService.findByClientId(clientId);
            if (client.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "invalid_client",
                    "error_description", "Client not found"
                ));
            }

            if (!client.get().isEnabled()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "invalid_client",
                    "error_description", "Client is disabled"
                ));
            }

            if (!passwordEncoder.matches(clientSecret, client.get().getClientSecret())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "invalid_client",
                    "error_description", "Invalid client secret"
                ));
            }

            // Generate token
            List<String> scopes = scope != null ? List.of(scope.split(" ")) : client.get().getScopes().stream().toList();
            List<String> roles = List.of("ROLE_PARTNER");
            
            String accessToken = jwtTokenService.generateToken(clientId, scopes, roles);

            Map<String, Object> response = new HashMap<>();
            response.put("access_token", accessToken);
            response.put("token_type", "Bearer");
            response.put("expires_in", 3600);
            response.put("scope", String.join(" ", scopes));

            log.info("Token generated for client: {}", clientId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error generating token: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", "invalid_request",
                "error_description", e.getMessage()
            ));
        }
    }

    @GetMapping("/jwks")
    @Operation(summary = "Get JSON Web Key Set", description = "Returns the JSON Web Key Set for token validation")
    public ResponseEntity<Map<String, Object>> getJwks() {
        Map<String, Object> jwks = new HashMap<>();
        jwks.put("keys", List.of());
        return ResponseEntity.ok(jwks);
    }

    @GetMapping("/.well-known/oauth-authorization-server")
    @Operation(summary = "OAuth2 Authorization Server Metadata", description = "Returns OAuth2 authorization server metadata")
    public ResponseEntity<Map<String, Object>> getAuthorizationServerMetadata() {
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
        
        return ResponseEntity.ok(metadata);
    }
} 