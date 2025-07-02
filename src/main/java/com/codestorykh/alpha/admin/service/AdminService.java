package com.codestorykh.alpha.admin.service;

import com.codestorykh.alpha.admin.dto.SystemStatsDTO;
import com.codestorykh.alpha.admin.dto.UserStatsDTO;
import com.codestorykh.alpha.admin.dto.OAuth2ClientStatsDTO;
import com.codestorykh.alpha.admin.dto.AuditLogDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminService {

    SystemStatsDTO getSystemStats();

    UserStatsDTO getUserStats();

    OAuth2ClientStatsDTO getOAuth2ClientStats();

    List<AuditLogDTO> getAuditLogs(LocalDateTime from, LocalDateTime to, String username, String action);

    void clearAuditLogs(LocalDateTime before);

    void backupDatabase();

    void restoreDatabase(String backupFile);

    void generateSystemReport();

    void sendSystemAlert(String message, String level);

    void updateSystemConfiguration(String key, String value);

    String getSystemConfiguration(String key);

    void restartSystem();

    void shutdownSystem();
} 