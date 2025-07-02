package com.codestorykh.alpha.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {
    private Long id;
    private LocalDateTime timestamp;
    private String username;
    private String action;
    private String resource;
    private String details;
    private String ipAddress;
    private String userAgent;
    private String status;
    private Long duration;
} 