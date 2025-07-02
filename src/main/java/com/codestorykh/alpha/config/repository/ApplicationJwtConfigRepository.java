package com.codestorykh.alpha.config.repository;

import com.codestorykh.alpha.config.domain.ApplicationJwtConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationJwtConfigRepository extends JpaRepository<ApplicationJwtConfig, Long> {

    Optional<ApplicationJwtConfig> findByApplicationNameAndEnvironmentAndActiveTrue(String applicationName, String environment);

    Optional<ApplicationJwtConfig> findByApplicationNameAndActiveTrue(String applicationName);

    @Query("SELECT c FROM ApplicationJwtConfig c WHERE c.applicationName = :applicationName AND c.environment = :environment AND c.enabled = true AND c.active = true")
    Optional<ApplicationJwtConfig> findEnabledByApplicationNameAndEnvironment(@Param("applicationName") String applicationName, @Param("environment") String environment);

    @Query("SELECT c FROM ApplicationJwtConfig c WHERE c.applicationName = :applicationName AND c.enabled = true AND c.active = true")
    Optional<ApplicationJwtConfig> findEnabledByApplicationName(@Param("applicationName") String applicationName);

    @Query("SELECT c FROM ApplicationJwtConfig c WHERE c.isDefault = true AND c.enabled = true AND c.active = true")
    Optional<ApplicationJwtConfig> findDefaultConfiguration();

    @Query("SELECT c FROM ApplicationJwtConfig c WHERE c.environment = :environment AND c.enabled = true AND c.active = true")
    List<ApplicationJwtConfig> findEnabledByEnvironment(@Param("environment") String environment);

    @Query("SELECT c FROM ApplicationJwtConfig c WHERE c.enabled = true AND c.active = true")
    List<ApplicationJwtConfig> findAllEnabled();

    @Query("SELECT c FROM ApplicationJwtConfig c WHERE c.system = true AND c.active = true")
    List<ApplicationJwtConfig> findAllSystemConfigurations();

    boolean existsByApplicationNameAndEnvironment(String applicationName, String environment);

    boolean existsByApplicationName(String applicationName);

    @Query("SELECT DISTINCT c.applicationName FROM ApplicationJwtConfig c WHERE c.active = true")
    List<String> findAllApplicationNames();

    @Query("SELECT DISTINCT c.environment FROM ApplicationJwtConfig c WHERE c.active = true")
    List<String> findAllEnvironments();

    @Query("SELECT c FROM ApplicationJwtConfig c WHERE " +
           "(:applicationName IS NULL OR c.applicationName = :applicationName) AND " +
           "(:environment IS NULL OR c.environment = :environment) AND " +
           "(:enabled IS NULL OR c.enabled = :enabled) AND " +
           "(:system IS NULL OR c.system = :system) AND " +
           "c.active = true")
    List<ApplicationJwtConfig> searchConfigurations(@Param("applicationName") String applicationName,
                                                   @Param("environment") String environment,
                                                   @Param("enabled") Boolean enabled,
                                                   @Param("system") Boolean system);
} 