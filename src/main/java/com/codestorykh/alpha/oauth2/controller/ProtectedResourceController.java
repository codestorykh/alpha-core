package com.codestorykh.alpha.oauth2.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/protected")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Protected Resources", description = "Protected APIs that require OAuth2 authentication")
public class ProtectedResourceController {

    @GetMapping("/resource")
    @PreAuthorize("hasAuthority('SCOPE_read')")
    @Operation(summary = "Get protected resource", description = "Access a protected resource that requires read scope")
    public ResponseEntity<Map<String, Object>> getProtectedResource() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Access granted to protected resource");
        response.put("timestamp", LocalDateTime.now());
        response.put("clientId", jwt.getSubject());
        response.put("scopes", jwt.getClaim("scope"));
        response.put("issuer", jwt.getIssuer());
        response.put("data", Map.of(
            "id", 1,
            "name", "Sample Resource",
            "description", "This is a protected resource that requires OAuth2 authentication"
        ));
        
        log.info("Protected resource accessed by client: {}", jwt.getSubject());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resource")
    @PreAuthorize("hasAuthority('SCOPE_write')")
    @Operation(summary = "Create protected resource", description = "Create a protected resource that requires write scope")
    public ResponseEntity<Map<String, Object>> createProtectedResource(
            @RequestBody Map<String, Object> requestBody) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Resource created successfully");
        response.put("timestamp", LocalDateTime.now());
        response.put("clientId", jwt.getSubject());
        response.put("scopes", jwt.getClaim("scope"));
        response.put("createdData", requestBody);
        response.put("resourceId", System.currentTimeMillis());
        
        log.info("Protected resource created by client: {}", jwt.getSubject());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/partner/data")
    @PreAuthorize("hasAuthority('SCOPE_partner.read')")
    @Operation(summary = "Get partner data", description = "Access partner-specific data that requires partner.read scope")
    public ResponseEntity<Map<String, Object>> getPartnerData() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Partner data accessed successfully");
        response.put("timestamp", LocalDateTime.now());
        response.put("clientId", jwt.getSubject());
        response.put("partnerData", Map.of(
            "partnerId", "PARTNER_" + jwt.getSubject().hashCode(),
            "organization", "Partner Organization",
            "integrationDate", LocalDateTime.now().minusDays(30),
            "status", "active",
            "permissions", List.of("read", "write", "partner.read")
        ));
        
        log.info("Partner data accessed by client: {}", jwt.getSubject());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/partner/data")
    @PreAuthorize("hasAuthority('SCOPE_partner.write')")
    @Operation(summary = "Update partner data", description = "Update partner-specific data that requires partner.write scope")
    public ResponseEntity<Map<String, Object>> updatePartnerData(
            @RequestBody Map<String, Object> requestBody) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Partner data updated successfully");
        response.put("timestamp", LocalDateTime.now());
        response.put("clientId", jwt.getSubject());
        response.put("updatedData", requestBody);
        response.put("updateId", System.currentTimeMillis());
        
        log.info("Partner data updated by client: {}", jwt.getSubject());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/stats")
    @PreAuthorize("hasAuthority('SCOPE_admin')")
    @Operation(summary = "Get admin statistics", description = "Access admin statistics that requires admin scope")
    public ResponseEntity<Map<String, Object>> getAdminStats() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Admin statistics accessed successfully");
        response.put("timestamp", LocalDateTime.now());
        response.put("clientId", jwt.getSubject());
        response.put("adminStats", Map.of(
            "totalClients", 150,
            "activeTokens", 89,
            "totalRequests", 15420,
            "systemHealth", "excellent",
            "lastBackup", LocalDateTime.now().minusHours(6)
        ));
        
        log.info("Admin stats accessed by client: {}", jwt.getSubject());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/profile")
    @PreAuthorize("hasAuthority('SCOPE_user')")
    @Operation(summary = "Get user profile", description = "Access user profile data that requires user scope")
    public ResponseEntity<Map<String, Object>> getUserProfile(
            @Parameter(description = "User ID") @RequestParam String userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User profile accessed successfully");
        response.put("timestamp", LocalDateTime.now());
        response.put("clientId", jwt.getSubject());
        response.put("requestedUserId", userId);
        response.put("userProfile", Map.of(
            "id", userId,
            "name", "John Doe",
            "email", "john.doe@example.com",
            "role", "USER",
            "createdAt", LocalDateTime.now().minusDays(365),
            "lastLogin", LocalDateTime.now().minusHours(2)
        ));
        
        log.info("User profile accessed by client: {} for user: {}", jwt.getSubject(), userId);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/service/metrics")
    @PreAuthorize("hasAuthority('SCOPE_service.read')")
    @Operation(summary = "Get service metrics", description = "Access service metrics that requires service.read scope")
    public ResponseEntity<Map<String, Object>> getServiceMetrics() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Service metrics accessed successfully");
        response.put("timestamp", LocalDateTime.now());
        response.put("clientId", jwt.getSubject());
        response.put("serviceMetrics", Map.of(
            "cpuUsage", "45%",
            "memoryUsage", "67%",
            "diskUsage", "23%",
            "activeConnections", 234,
            "responseTime", "120ms",
            "errorRate", "0.1%"
        ));
        
        log.info("Service metrics accessed by client: {}", jwt.getSubject());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/token-info")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get token information", description = "Get information about the current access token")
    public ResponseEntity<Map<String, Object>> getTokenInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        
        Map<String, Object> response = new HashMap<>();
        response.put("subject", jwt.getSubject());
        response.put("issuer", jwt.getIssuer());
        response.put("audience", jwt.getAudience());
        response.put("issuedAt", jwt.getIssuedAt());
        response.put("expiresAt", jwt.getExpiresAt());
        response.put("scopes", jwt.getClaim("scope"));
        response.put("roles", jwt.getClaim("roles"));
        response.put("clientId", jwt.getClaim("client_id"));
        response.put("tokenType", "Bearer");
        
        return ResponseEntity.ok(response);
    }
} 