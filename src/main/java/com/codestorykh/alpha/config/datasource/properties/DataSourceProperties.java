package com.codestorykh.alpha.config.datasource.properties;

import com.codestorykh.alpha.config.datasource.DataSourceHealthIndicator;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

@Configuration
@Slf4j
public class DataSourceProperties {

    @Value("${spring.datasource.url:jdbc:postgresql://localhost:5432/alpha_identity}")
    private String url;

    @Value("${spring.datasource.username:postgres}")
    private String username;

    @Value("${spring.datasource.password:password}")
    private String password;

    @Value("${spring.datasource.driver-class-name:org.postgresql.Driver}")
    private String driverClassName;

    @Value("${spring.datasource.hikari.pool-name:AlphaHikariCP}")
    private String poolName;

    @Value("${spring.datasource.hikari.maximum-pool-size:20}")
    private int maximumPoolSize;

    @Value("${spring.datasource.hikari.minimum-idle:5}")
    private int minimumIdle;

    @Value("${spring.datasource.hikari.idle-timeout:300000}")
    private long idleTimeout;

    @Value("${spring.datasource.hikari.connection-timeout:20000}")
    private long connectionTimeout;

    @Value("${spring.datasource.hikari.max-lifetime:1200000}")
    private long maxLifetime;

    @Value("${spring.datasource.hikari.leak-detection-threshold:60000}")
    private long leakDetectionThreshold;

    @Value("${spring.datasource.hikari.connection-test-query:SELECT 1}")
    private String connectionTestQuery;

    @Value("${spring.datasource.hikari.validation-timeout:5000}")
    private long validationTimeout;

    @Value("${spring.datasource.hikari.auto-commit:true}")
    private boolean autoCommit;

    @Value("${spring.datasource.hikari.register-mbeans:false}")
    private boolean registerMbeans;

    @Value("${spring.datasource.hikari.allow-pool-suspension:false}")
    private boolean allowPoolSuspension;

    @Value("${spring.datasource.hikari.read-only:false}")
    private boolean readOnly;

    @Value("${spring.datasource.hikari.transaction-isolation:TRANSACTION_READ_COMMITTED}")
    private String transactionIsolation;

    @Value("${spring.datasource.hikari.initialization-fail-timeout:1}")
    private long initializationFailTimeout;

    @Value("${spring.datasource.hikari.connection-init-sql:}")
    private String connectionInitSql;

    @Value("${spring.datasource.hikari.data-source-properties:}")
    private String dataSourceProperties;

    private final Environment environment;

    public DataSourceProperties(Environment environment) {
        this.environment = environment;
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "spring.datasource.type", havingValue = "com.zaxxer.hikari.HikariDataSource", matchIfMissing = true)
    public DataSource dataSource() {
        log.info("Configuring HikariCP datasource for URL: {}", url);
        
        HikariConfig config = new HikariConfig();
        
        // Basic connection properties
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);
        
        // Pool configuration
        config.setPoolName(poolName);
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(minimumIdle);
        config.setIdleTimeout(idleTimeout);
        config.setConnectionTimeout(connectionTimeout);
        config.setMaxLifetime(maxLifetime);
        config.setLeakDetectionThreshold(leakDetectionThreshold);
        config.setValidationTimeout(validationTimeout);
        config.setAutoCommit(autoCommit);
        config.setRegisterMbeans(registerMbeans);
        config.setAllowPoolSuspension(allowPoolSuspension);
        config.setReadOnly(readOnly);
        config.setInitializationFailTimeout(initializationFailTimeout);
        
        // Connection test query
        if (connectionTestQuery != null && !connectionTestQuery.trim().isEmpty()) {
            config.setConnectionTestQuery(connectionTestQuery);
        }
        
        // Connection initialization SQL
        if (connectionInitSql != null && !connectionInitSql.trim().isEmpty()) {
            config.setConnectionInitSql(connectionInitSql);
        }
        
        // Transaction isolation
        if (transactionIsolation != null && !transactionIsolation.trim().isEmpty()) {
            config.setTransactionIsolation(transactionIsolation);
        }
        
        // Data source properties
        if (dataSourceProperties != null && !dataSourceProperties.trim().isEmpty()) {
            String[] properties = dataSourceProperties.split(",");
            for (String property : properties) {
                String[] keyValue = property.split("=");
                if (keyValue.length == 2) {
                    config.addDataSourceProperty(keyValue[0].trim(), keyValue[1].trim());
                }
            }
        }
        
        // PostgreSQL specific optimizations
        if (driverClassName.contains("postgresql")) {
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");
        }
        
        // MySQL specific optimizations
        if (driverClassName.contains("mysql")) {
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");
        }
        
        // H2 specific optimizations
        if (driverClassName.contains("h2")) {
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        }
        
        HikariDataSource dataSource = new HikariDataSource(config);
        
        log.info("HikariCP datasource configured successfully with pool name: {}", poolName);
        log.info("Pool configuration - Max: {}, Min: {}, Idle Timeout: {}ms, Connection Timeout: {}ms", 
                maximumPoolSize, minimumIdle, idleTimeout, connectionTimeout);
        
        return dataSource;
    }

    @Bean
    public DataSourceHealthIndicator dataSourceHealthIndicator(DataSource dataSource) {
        return new DataSourceHealthIndicator(dataSource);
    }
} 