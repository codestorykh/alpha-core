package com.codestorykh.alpha.oauth2.service.impl;

import com.codestorykh.alpha.common.service.BaseServiceImpl;
import com.codestorykh.alpha.oauth2.domain.OAuth2Client;
import com.codestorykh.alpha.oauth2.dto.OAuth2ClientDTO;
import com.codestorykh.alpha.oauth2.repository.OAuth2ClientRepository;
import com.codestorykh.alpha.oauth2.service.OAuth2ClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional
public class OAuth2ClientServiceImpl extends BaseServiceImpl<OAuth2Client, Long> implements OAuth2ClientService {

    private final OAuth2ClientRepository oauth2ClientRepository;
    private final PasswordEncoder passwordEncoder;

    public OAuth2ClientServiceImpl(OAuth2ClientRepository oauth2ClientRepository, PasswordEncoder passwordEncoder) {
        super(oauth2ClientRepository);
        this.oauth2ClientRepository = oauth2ClientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<OAuth2Client> findByClientId(String clientId) {
        return oauth2ClientRepository.findByClientId(clientId);
    }

    @Override
    public OAuth2Client createClient(OAuth2ClientDTO clientDTO) {
        if (oauth2ClientRepository.existsByClientId(clientDTO.getClientId())) {
            throw new RuntimeException("Client ID already exists");
        }

        OAuth2Client client = OAuth2Client.builder()
                .clientId(clientDTO.getClientId())
                .clientSecret(passwordEncoder.encode(clientDTO.getClientSecret()))
                .clientName(clientDTO.getClientName())
                .description(clientDTO.getDescription())
                .redirectUris(clientDTO.getRedirectUris())
                .grantTypes(clientDTO.getGrantTypes())
                .scopes(clientDTO.getScopes())
                .tokenEndpointAuthMethod(clientDTO.getTokenEndpointAuthMethod())
                .enabled(clientDTO.isEnabled())
                .build();

        return save(client);
    }

    @Override
    public OAuth2Client updateClient(String clientId, OAuth2ClientDTO clientDTO) {
        OAuth2Client client = findByClientId(clientId)
                .orElseThrow(() -> new RuntimeException("OAuth2 Client not found with clientId: " + clientId));

        if (clientDTO.getClientName() != null) {
            client.setClientName(clientDTO.getClientName());
        }
        if (clientDTO.getDescription() != null) {
            client.setDescription(clientDTO.getDescription());
        }
        if (clientDTO.getRedirectUris() != null) {
            client.setRedirectUris(clientDTO.getRedirectUris());
        }
        if (clientDTO.getGrantTypes() != null) {
            client.setGrantTypes(clientDTO.getGrantTypes());
        }
        if (clientDTO.getScopes() != null) {
            client.setScopes(clientDTO.getScopes());
        }
        if (clientDTO.getTokenEndpointAuthMethod() != null) {
            client.setTokenEndpointAuthMethod(clientDTO.getTokenEndpointAuthMethod());
        }

        return save(client);
    }

    @Override
    public void regenerateClientSecret(String clientId) {
        OAuth2Client client = findByClientId(clientId)
                .orElseThrow(() -> new RuntimeException("OAuth2 Client not found with clientId: " + clientId));

        // Generate a new client secret (you might want to use a more secure method)
        String newSecret = java.util.UUID.randomUUID().toString();
        client.setClientSecret(passwordEncoder.encode(newSecret));
        save(client);
    }

    @Override
    public void enableClient(String clientId) {
        OAuth2Client client = findByClientId(clientId)
                .orElseThrow(() -> new RuntimeException("OAuth2 Client not found with clientId: " + clientId));
        client.setEnabled(true);
        save(client);
    }

    @Override
    public void disableClient(String clientId) {
        OAuth2Client client = findByClientId(clientId)
                .orElseThrow(() -> new RuntimeException("OAuth2 Client not found with clientId: " + clientId));
        client.setEnabled(false);
        save(client);
    }

    @Override
    public void deleteClient(String clientId) {
        OAuth2Client client = findByClientId(clientId)
                .orElseThrow(() -> new RuntimeException("OAuth2 Client not found with clientId: " + clientId));
        delete(client);
    }

    @Override
    public Page<OAuth2Client> searchClients(String searchTerm, Pageable pageable) {
        return oauth2ClientRepository.searchClients(searchTerm, pageable);
    }

    @Override
    public List<OAuth2Client> getEnabledClients() {
        return oauth2ClientRepository.findByEnabled(true);
    }

    @Override
    public List<OAuth2Client> getRecentlyUsedClients() {
        return oauth2ClientRepository.findRecentlyUsedClients();
    }

    @Override
    public List<OAuth2Client> getMostUsedClients() {
        return oauth2ClientRepository.findMostUsedClients();
    }

    @Override
    public void incrementUsage(String clientId) {
        findByClientId(clientId).ifPresent(client -> {
            client.incrementUsage();
            save(client);
        });
    }

    @Override
    public boolean validateClient(String clientId, String clientSecret) {
        return findByClientId(clientId)
                .map(client -> passwordEncoder.matches(clientSecret, client.getClientSecret()) && client.isEnabled())
                .orElse(false);
    }

    @Override
    public boolean validateRedirectUri(String clientId, String redirectUri) {
        return findByClientId(clientId)
                .map(client -> client.getRedirectUris().contains(redirectUri))
                .orElse(false);
    }

    @Override
    public boolean validateScope(String clientId, String scope) {
        return findByClientId(clientId)
                .map(client -> client.getScopes().contains(scope))
                .orElse(false);
    }
} 