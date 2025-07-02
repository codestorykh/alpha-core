package com.codestorykh.alpha.admin.controller;

import com.codestorykh.alpha.admin.dto.SystemStatsDTO;
import com.codestorykh.alpha.admin.dto.UserStatsDTO;
import com.codestorykh.alpha.admin.dto.OAuth2ClientStatsDTO;
import com.codestorykh.alpha.admin.dto.AuditLogDTO;
import com.codestorykh.alpha.admin.service.AdminService;
import com.codestorykh.alpha.admin.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final AuditService auditService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "Identity Server"));
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        return ResponseEntity.ok(Map.of("message", "Admin test endpoint working"));
    }

    @GetMapping("/stats/system")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SystemStatsDTO> getSystemStats(Authentication authentication, HttpServletRequest request) {
        auditService.logAction(authentication.getName(), "VIEW", "SYSTEM_STATS", "Retrieved system statistics", request);
        return ResponseEntity.ok(adminService.getSystemStats());
    }

    @GetMapping("/stats/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserStatsDTO> getUserStats(Authentication authentication, HttpServletRequest request) {
        auditService.logAction(authentication.getName(), "VIEW", "USER_STATS", "Retrieved user statistics", request);
        return ResponseEntity.ok(adminService.getUserStats());
    }

    @GetMapping("/stats/oauth2")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OAuth2ClientStatsDTO> getOAuth2ClientStats(Authentication authentication, HttpServletRequest request) {
        auditService.logAction(authentication.getName(), "VIEW", "OAUTH2_STATS", "Retrieved OAuth2 client statistics", request);
        return ResponseEntity.ok(adminService.getOAuth2ClientStats());
    }

    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogDTO>> getAuditLogs(
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action,
            Authentication authentication,
            HttpServletRequest request) {
        auditService.logAction(authentication.getName(), "VIEW", "AUDIT_LOGS", 
                String.format("Retrieved audit logs from %s to %s for user %s with action %s", from, to, username, action), request);
        return ResponseEntity.ok(adminService.getAuditLogs(from, to, username, action));
    }

    @DeleteMapping("/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> clearAuditLogs(@RequestParam LocalDateTime before, 
                                             Authentication authentication, 
                                             HttpServletRequest request) {
        auditService.logAction(authentication.getName(), "DELETE", "AUDIT_LOGS", 
                String.format("Cleared audit logs before %s", before), request);
        adminService.clearAuditLogs(before);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/backup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> backupDatabase(Authentication authentication, HttpServletRequest request) {
        auditService.logAction(authentication.getName(), "BACKUP", "DATABASE", "Started database backup", request);
        adminService.backupDatabase();
        return ResponseEntity.ok(Map.of("message", "Database backup started"));
    }

    @PostMapping("/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> restoreDatabase(@RequestParam String backupFile, 
                                                              Authentication authentication, 
                                                              HttpServletRequest request) {
        auditService.logAction(authentication.getName(), "RESTORE", "DATABASE", 
                String.format("Started database restore from %s", backupFile), request);
        adminService.restoreDatabase(backupFile);
        return ResponseEntity.ok(Map.of("message", "Database restore started"));
    }

    @PostMapping("/report")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> generateSystemReport(Authentication authentication, HttpServletRequest request) {
        auditService.logAction(authentication.getName(), "GENERATE", "SYSTEM_REPORT", "Started system report generation", request);
        adminService.generateSystemReport();
        return ResponseEntity.ok(Map.of("message", "System report generation started"));
    }

    @PostMapping("/alert")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> sendSystemAlert(
            @RequestParam String message,
            @RequestParam String level,
            Authentication authentication,
            HttpServletRequest request) {
        auditService.logAction(authentication.getName(), "ALERT", "SYSTEM", 
                String.format("Sent system alert - Level: %s, Message: %s", level, message), request);
        adminService.sendSystemAlert(message, level);
        return ResponseEntity.ok(Map.of("message", "System alert sent"));
    }

    @PutMapping("/config/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> updateSystemConfiguration(
            @PathVariable String key,
            @RequestParam String value,
            Authentication authentication,
            HttpServletRequest request) {
        auditService.logAction(authentication.getName(), "UPDATE", "SYSTEM_CONFIG", 
                String.format("Updated configuration - Key: %s, Value: %s", key, value), request);
        adminService.updateSystemConfiguration(key, value);
        return ResponseEntity.ok(Map.of("message", "Configuration updated"));
    }

    @GetMapping("/config/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> getSystemConfiguration(@PathVariable String key, 
                                                                     Authentication authentication, 
                                                                     HttpServletRequest request) {
        auditService.logAction(authentication.getName(), "VIEW", "SYSTEM_CONFIG", 
                String.format("Retrieved configuration for key: %s", key), request);
        String value = adminService.getSystemConfiguration(key);
        return ResponseEntity.ok(Map.of("key", key, "value", value));
    }

    @PostMapping("/restart")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> restartSystem(Authentication authentication, HttpServletRequest request) {
        auditService.logSecurityEvent(authentication.getName(), "SYSTEM_RESTART", "System restart initiated", request);
        adminService.restartSystem();
        return ResponseEntity.ok(Map.of("message", "System restart initiated"));
    }

    @PostMapping("/shutdown")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> shutdownSystem(Authentication authentication, HttpServletRequest request) {
        auditService.logSecurityEvent(authentication.getName(), "SYSTEM_SHUTDOWN", "System shutdown initiated", request);
        adminService.shutdownSystem();
        return ResponseEntity.ok(Map.of("message", "System shutdown initiated"));
    }
} 