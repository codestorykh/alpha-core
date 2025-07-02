package com.codestorykh.alpha.config.datasource;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/datasource")
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class DataSourceController {

    private final DataSource dataSource;
    private final DataSourceHealthIndicator healthIndicator;
    private final DataSourceMonitoringConfig.DataSourceMetrics metrics;

    @Autowired
    public DataSourceController(DataSource dataSource, 
                              DataSourceHealthIndicator healthIndicator,
                              DataSourceMonitoringConfig.DataSourceMetrics metrics) {
        this.dataSource = dataSource;
        this.healthIndicator = healthIndicator;
        this.metrics = metrics;
    }

    @GetMapping("/health")
    public ResponseEntity<Health> getHealth() {
        Health health = healthIndicator.health();
        return ResponseEntity.ok(health);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            if (dataSource instanceof HikariDataSource hikariDataSource) {

                status.put("poolName", hikariDataSource.getPoolName());
                status.put("activeConnections", hikariDataSource.getHikariPoolMXBean().getActiveConnections());
                status.put("idleConnections", hikariDataSource.getHikariPoolMXBean().getIdleConnections());
                status.put("totalConnections", hikariDataSource.getHikariPoolMXBean().getTotalConnections());
                status.put("maximumPoolSize", hikariDataSource.getMaximumPoolSize());
                status.put("minimumIdle", hikariDataSource.getMinimumIdle());
                status.put("connectionTimeout", hikariDataSource.getConnectionTimeout());
                status.put("idleTimeout", hikariDataSource.getIdleTimeout());
                status.put("maxLifetime", hikariDataSource.getMaxLifetime());
                status.put("leakDetectionThreshold", hikariDataSource.getLeakDetectionThreshold());
                status.put("validationTimeout", hikariDataSource.getValidationTimeout());
                status.put("autoCommit", hikariDataSource.isAutoCommit());
                status.put("readOnly", hikariDataSource.isReadOnly());
                status.put("initializationFailTimeout", hikariDataSource.getInitializationFailTimeout());
                
                // Calculate usage percentage
                double usagePercentage = (double) hikariDataSource.getHikariPoolMXBean().getActiveConnections() 
                        / hikariDataSource.getMaximumPoolSize() * 100;
                status.put("usagePercentage", String.format("%.2f%%", usagePercentage));
                
                // Connection test
                try (Connection connection = dataSource.getConnection()) {
                    status.put("connectionTest", "SUCCESS");
                    status.put("connectionValid", connection.isValid(5));
                } catch (SQLException e) {
                    status.put("connectionTest", "FAILED");
                    status.put("connectionError", e.getMessage());
                }
                
            } else {
                status.put("error", "DataSource is not HikariCP");
            }
            
            status.put("timestamp", System.currentTimeMillis());
            
        } catch (Exception e) {
            log.error("Error getting datasource status", e);
            status.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(status);
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        Map<String, Object> metricsData = new HashMap<>();
        
        metricsData.put("totalConnections", metrics.getTotalConnections());
        metricsData.put("activeConnections", metrics.getActiveConnections());
        metricsData.put("idleConnections", metrics.getIdleConnections());
        metricsData.put("connectionTimeouts", metrics.getConnectionTimeouts());
        metricsData.put("connectionFailures", metrics.getConnectionFailures());
        metricsData.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(metricsData);
    }

    @PostMapping("/test-connection")
    public ResponseEntity<Map<String, Object>> testConnection() {
        Map<String, Object> result = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            boolean isValid = connection.isValid(5);
            result.put("success", true);
            result.put("connectionValid", isValid);
            result.put("message", isValid ? "Connection test successful" : "Connection test failed");
            
            if (isValid) {
                result.put("databaseProductName", connection.getMetaData().getDatabaseProductName());
                result.put("databaseProductVersion", connection.getMetaData().getDatabaseProductVersion());
                result.put("driverName", connection.getMetaData().getDriverName());
                result.put("driverVersion", connection.getMetaData().getDriverVersion());
            }
            
        } catch (SQLException e) {
            log.error("Connection test failed", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("sqlState", e.getSQLState());
            result.put("errorCode", e.getErrorCode());
        }
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/evict-connections")
    public ResponseEntity<Map<String, Object>> evictConnections() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (dataSource instanceof HikariDataSource hikariDataSource) {
                hikariDataSource.getHikariPoolMXBean().softEvictConnections();
                
                result.put("success", true);
                result.put("message", "Connection eviction initiated");
                result.put("timestamp", System.currentTimeMillis());
                
                log.info("Connection eviction initiated by admin");
            } else {
                result.put("success", false);
                result.put("error", "DataSource is not HikariCP");
            }
        } catch (Exception e) {
            log.error("Error evicting connections", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/configuration")
    public ResponseEntity<Map<String, Object>> getConfiguration() {
        Map<String, Object> config = new HashMap<>();
        
        try {
            if (dataSource instanceof HikariDataSource hikariDataSource) {

                config.put("poolName", hikariDataSource.getPoolName());
                config.put("maximumPoolSize", hikariDataSource.getMaximumPoolSize());
                config.put("minimumIdle", hikariDataSource.getMinimumIdle());
                config.put("connectionTimeout", hikariDataSource.getConnectionTimeout());
                config.put("idleTimeout", hikariDataSource.getIdleTimeout());
                config.put("maxLifetime", hikariDataSource.getMaxLifetime());
                config.put("leakDetectionThreshold", hikariDataSource.getLeakDetectionThreshold());
                config.put("validationTimeout", hikariDataSource.getValidationTimeout());
                config.put("autoCommit", hikariDataSource.isAutoCommit());
                config.put("readOnly", hikariDataSource.isReadOnly());
                config.put("initializationFailTimeout", hikariDataSource.getInitializationFailTimeout());
                config.put("connectionTestQuery", hikariDataSource.getConnectionTestQuery());
                config.put("connectionInitSql", hikariDataSource.getConnectionInitSql());
                config.put("transactionIsolation", hikariDataSource.getTransactionIsolation());
                config.put("registerMbeans", hikariDataSource.isRegisterMbeans());
                config.put("allowPoolSuspension", hikariDataSource.isAllowPoolSuspension());
                
            } else {
                config.put("error", "DataSource is not HikariCP");
            }
            
        } catch (Exception e) {
            log.error("Error getting datasource configuration", e);
            config.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(config);
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            if (dataSource instanceof HikariDataSource hikariDataSource) {

                // Pool statistics
                stats.put("activeConnections", hikariDataSource.getHikariPoolMXBean().getActiveConnections());
                stats.put("idleConnections", hikariDataSource.getHikariPoolMXBean().getIdleConnections());
                stats.put("totalConnections", hikariDataSource.getHikariPoolMXBean().getTotalConnections());
                stats.put("threadsAwaitingConnection", hikariDataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
                
                // Usage statistics
                int maxPoolSize = hikariDataSource.getMaximumPoolSize();
                int activeConnections = hikariDataSource.getHikariPoolMXBean().getActiveConnections();
                double usagePercentage = (double) activeConnections / maxPoolSize * 100;
                
                stats.put("usagePercentage", String.format("%.2f%%", usagePercentage));
                stats.put("availableConnections", maxPoolSize - activeConnections);
                stats.put("connectionUtilization", String.format("%.2f%%", usagePercentage));
                
                // Performance indicators
                if (usagePercentage > 80) {
                    stats.put("status", "HIGH_USAGE");
                    stats.put("warning", "High connection pool usage detected");
                } else if (usagePercentage > 60) {
                    stats.put("status", "MEDIUM_USAGE");
                    stats.put("warning", "Moderate connection pool usage");
                } else {
                    stats.put("status", "NORMAL_USAGE");
                    stats.put("warning", "Normal connection pool usage");
                }
                
                // Metrics
                stats.put("connectionTimeouts", metrics.getConnectionTimeouts());
                stats.put("connectionFailures", metrics.getConnectionFailures());
                
            } else {
                stats.put("error", "DataSource is not HikariCP");
            }
            
            stats.put("timestamp", System.currentTimeMillis());
            
        } catch (Exception e) {
            log.error("Error getting datasource statistics", e);
            stats.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(stats);
    }
} 