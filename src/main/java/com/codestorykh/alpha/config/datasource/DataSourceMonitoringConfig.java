package com.codestorykh.alpha.config.datasource;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.sql.DataSource;
import java.util.concurrent.atomic.AtomicLong;

@Configuration
@EnableScheduling
@Slf4j
public class DataSourceMonitoringConfig {

    private final DataSource dataSource;
    private final AtomicLong totalConnections = new AtomicLong(0);
    private final AtomicLong activeConnections = new AtomicLong(0);
    private final AtomicLong idleConnections = new AtomicLong(0);

    @Autowired
    public DataSourceMonitoringConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean
    public DataSourceMetrics dataSourceMetrics() {
        return new DataSourceMetrics();
    }

    @Scheduled(fixedRate = 30000) // Every 30 seconds
    public void monitorDataSource() {
        if (dataSource instanceof HikariDataSource hikariDataSource) {

            int active = hikariDataSource.getHikariPoolMXBean().getActiveConnections();
            int idle = hikariDataSource.getHikariPoolMXBean().getIdleConnections();
            int total = hikariDataSource.getHikariPoolMXBean().getTotalConnections();
            int max = hikariDataSource.getMaximumPoolSize();
            int min = hikariDataSource.getMinimumIdle();
            
            // Update metrics
            activeConnections.set(active);
            idleConnections.set(idle);
            totalConnections.set(total);
            
            // Log pool status
            log.debug("HikariCP Pool Status - Active: {}, Idle: {}, Total: {}, Max: {}, Min: {}", 
                    active, idle, total, max, min);
            
            // Log warnings for high connection usage
            double usagePercentage = (double) active / max * 100;
            if (usagePercentage > 80) {
                log.warn("High connection pool usage: {}% ({} active connections out of {})", 
                        String.format("%.1f", usagePercentage), active, max);
            }
            
            // Log warnings for connection leaks
            if (active > 0 && idle == 0 && total == max) {
                log.warn("Potential connection leak detected - all connections are active");
            }
        }
    }

    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void logDataSourceStats() {
        if (dataSource instanceof HikariDataSource hikariDataSource) {

            log.info("=== DataSource Statistics ===");
            log.info("Pool Name: {}", hikariDataSource.getPoolName());
            log.info("Active Connections: {}", hikariDataSource.getHikariPoolMXBean().getActiveConnections());
            log.info("Idle Connections: {}", hikariDataSource.getHikariPoolMXBean().getIdleConnections());
            log.info("Total Connections: {}", hikariDataSource.getHikariPoolMXBean().getTotalConnections());
            log.info("Maximum Pool Size: {}", hikariDataSource.getMaximumPoolSize());
            log.info("Minimum Idle: {}", hikariDataSource.getMinimumIdle());
            log.info("Connection Timeout: {}ms", hikariDataSource.getConnectionTimeout());
            log.info("Idle Timeout: {}ms", hikariDataSource.getIdleTimeout());
            log.info("Max Lifetime: {}ms", hikariDataSource.getMaxLifetime());
            log.info("Leak Detection Threshold: {}ms", hikariDataSource.getLeakDetectionThreshold());
            log.info("=============================");
        }
    }

    public static class DataSourceMetrics {
        private final AtomicLong totalConnections = new AtomicLong(0);
        private final AtomicLong activeConnections = new AtomicLong(0);
        private final AtomicLong idleConnections = new AtomicLong(0);
        private final AtomicLong connectionTimeouts = new AtomicLong(0);
        private final AtomicLong connectionFailures = new AtomicLong(0);

        public long getTotalConnections() {
            return totalConnections.get();
        }

        public long getActiveConnections() {
            return activeConnections.get();
        }

        public long getIdleConnections() {
            return idleConnections.get();
        }

        public long getConnectionTimeouts() {
            return connectionTimeouts.get();
        }

        public long getConnectionFailures() {
            return connectionFailures.get();
        }

        public void incrementConnectionTimeouts() {
            connectionTimeouts.incrementAndGet();
        }

        public void incrementConnectionFailures() {
            connectionFailures.incrementAndGet();
        }

        public void updatePoolStats(int active, int idle, int total) {
            activeConnections.set(active);
            idleConnections.set(idle);
            totalConnections.set(total);
        }
    }
} 