package com.codestorykh.alpha.config.repository;

import com.codestorykh.alpha.config.domain.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfigurationRepository extends JpaRepository<Configuration, Long> {

    Optional<Configuration> findByKey(String key);

    List<Configuration> findByCategory(String category);

    List<Configuration> findByCategoryAndActiveTrue(String category);

    @Query("SELECT c FROM Configuration c WHERE c.key = :key AND c.active = true")
    Optional<Configuration> findActiveByKey(@Param("key") String key);

    @Query("SELECT c FROM Configuration c WHERE c.category = :category AND c.active = true")
    List<Configuration> findActiveByCategory(@Param("category") String category);

    @Query("SELECT c FROM Configuration c WHERE c.system = true AND c.active = true")
    List<Configuration> findAllActiveSystemConfigurations();

    boolean existsByKey(String key);

    @Query("SELECT c.value FROM Configuration c WHERE c.key = :key AND c.active = true")
    Optional<String> findValueByKey(@Param("key") String key);
} 