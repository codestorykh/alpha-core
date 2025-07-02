package com.codestorykh.alpha.oauth2.repository;

import com.codestorykh.alpha.common.repository.BaseRepository;
import com.codestorykh.alpha.oauth2.domain.OAuth2Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OAuth2ClientRepository extends BaseRepository<OAuth2Client, Long> {

    Optional<OAuth2Client> findByClientId(String clientId);

    boolean existsByClientId(String clientId);

    @Query("SELECT c FROM OAuth2Client c WHERE c.enabled = :enabled")
    List<OAuth2Client> findByEnabled(@Param("enabled") boolean enabled);

    @Query("SELECT c FROM OAuth2Client c WHERE c.clientName LIKE %:searchTerm% OR c.description LIKE %:searchTerm%")
    Page<OAuth2Client> searchClients(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT c FROM OAuth2Client c WHERE c.lastUsed IS NOT NULL ORDER BY c.lastUsed DESC")
    List<OAuth2Client> findRecentlyUsedClients();

    @Query("SELECT c FROM OAuth2Client c WHERE c.usageCount > 0 ORDER BY c.usageCount DESC")
    List<OAuth2Client> findMostUsedClients();
} 