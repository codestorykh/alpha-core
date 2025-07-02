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
public class SystemStatsDTO {
    private LocalDateTime timestamp;
    private String version;
    private String status;
    private long uptime;
    private double cpuUsage;
    private double memoryUsage;
    private double diskUsage;
    private int activeConnections;
    private int totalRequests;
    private double averageResponseTime;
    private int errorRate;
    private String environment;
} 