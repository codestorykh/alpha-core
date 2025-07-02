package com.codestorykh.alpha.admin.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Service
@Slf4j
public class AuditService {

    public void logAction(String username, String action, String resource, String details, 
                         HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        
        log.info("AUDIT: User={}, Action={}, Resource={}, Details={}, IP={}, UserAgent={}, Timestamp={}", 
                username, action, resource, details, ipAddress, userAgent, LocalDateTime.now());
    }

    public void logSecurityEvent(String username, String event, String details, 
                                HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        
        log.warn("SECURITY: User={}, Event={}, Details={}, IP={}, UserAgent={}, Timestamp={}", 
                username, event, details, ipAddress, userAgent, LocalDateTime.now());
    }

    public void logSystemEvent(String event, String details) {
        log.info("SYSTEM: Event={}, Details={}, Timestamp={}", 
                event, details, LocalDateTime.now());
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
} 