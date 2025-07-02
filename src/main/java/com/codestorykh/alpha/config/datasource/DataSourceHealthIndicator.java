package com.codestorykh.alpha.config.datasource;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
@Slf4j
public class DataSourceHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public DataSourceHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) {
                Health.Builder builder = Health.up()
                        .withDetail("database", "Available")
                        .withDetail("connection", "Valid");

                // Add HikariCP specific metrics if available
                if (dataSource instanceof HikariDataSource hikariDataSource) {
                    builder.withDetail("pool.active", hikariDataSource.getHikariPoolMXBean().getActiveConnections())
                            .withDetail("pool.idle", hikariDataSource.getHikariPoolMXBean().getIdleConnections())
                            .withDetail("pool.total", hikariDataSource.getHikariPoolMXBean().getTotalConnections())
                            .withDetail("pool.max", hikariDataSource.getMaximumPoolSize())
                            .withDetail("pool.min", hikariDataSource.getMinimumIdle());
                }

                return builder.build();
            } else {
                return Health.down()
                        .withDetail("database", "Unavailable")
                        .withDetail("connection", "Invalid")
                        .build();
            }
        } catch (SQLException e) {
            log.error("Database health check failed", e);
            return Health.down()
                    .withDetail("database", "Unavailable")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
} 