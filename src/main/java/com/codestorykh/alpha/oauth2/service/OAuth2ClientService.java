package com.codestorykh.alpha.oauth2.service;

import com.codestorykh.alpha.common.service.BaseService;
import com.codestorykh.alpha.oauth2.domain.OAuth2Client;
import com.codestorykh.alpha.oauth2.dto.OAuth2ClientDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface OAuth2ClientService extends BaseService<OAuth2Client, Long> {

    Optional<OAuth2Client> findByClientId(String clientId);

    OAuth2Client createClient(OAuth2ClientDTO clientDTO);

    OAuth2Client updateClient(String clientId, OAuth2ClientDTO clientDTO);

    Page<OAuth2Client> searchClients(String searchTerm, Pageable pageable);

    List<OAuth2Client> getEnabledClients();

    List<OAuth2Client> getRecentlyUsedClients();

    List<OAuth2Client> getMostUsedClients();

    void deleteClient(String clientId);

    void enableClient(String clientId);

    void disableClient(String clientId);

    void regenerateClientSecret(String clientId);

    void incrementUsage(String clientId);

    boolean validateClient(String clientId, String clientSecret);

    boolean validateRedirectUri(String clientId, String redirectUri);

    boolean validateScope(String clientId, String scope);
} 