package com.codestorykh.alpha.oauth2.controller;

import com.codestorykh.alpha.oauth2.domain.OAuth2Client;
import com.codestorykh.alpha.oauth2.dto.OAuth2ClientDTO;
import com.codestorykh.alpha.oauth2.service.OAuth2ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/oauth2/clients")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class OAuth2ClientController {

    private final OAuth2ClientService oAuth2ClientService;

    @GetMapping
    public ResponseEntity<Page<OAuth2Client>> getAllClients(Pageable pageable) {
        return ResponseEntity.ok(oAuth2ClientService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OAuth2Client> getClientById(@PathVariable Long id) {
        return oAuth2ClientService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/client-id/{clientId}")
    public ResponseEntity<OAuth2Client> getClientByClientId(@PathVariable String clientId) {
        return oAuth2ClientService.findByClientId(clientId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<OAuth2Client> createClient(@Valid @RequestBody OAuth2ClientDTO clientDTO) {
        OAuth2Client createdClient = oAuth2ClientService.createClient(clientDTO);
        return ResponseEntity.ok(createdClient);
    }

    @PutMapping("/{clientId}")
    public ResponseEntity<OAuth2Client> updateClient(@PathVariable String clientId, @Valid @RequestBody OAuth2ClientDTO clientDTO) {
        OAuth2Client updatedClient = oAuth2ClientService.updateClient(clientId, clientDTO);
        return ResponseEntity.ok(updatedClient);
    }

    @DeleteMapping("/{clientId}")
    public ResponseEntity<Void> deleteClient(@PathVariable String clientId) {
        oAuth2ClientService.deleteClient(clientId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{clientId}/enable")
    public ResponseEntity<Void> enableClient(@PathVariable String clientId) {
        oAuth2ClientService.enableClient(clientId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{clientId}/disable")
    public ResponseEntity<Void> disableClient(@PathVariable String clientId) {
        oAuth2ClientService.disableClient(clientId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{clientId}/regenerate-secret")
    public ResponseEntity<Void> regenerateClientSecret(@PathVariable String clientId) {
        oAuth2ClientService.regenerateClientSecret(clientId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<Page<OAuth2Client>> searchClients(
            @RequestParam String searchTerm,
            Pageable pageable) {
        return ResponseEntity.ok(oAuth2ClientService.searchClients(searchTerm, pageable));
    }

    @GetMapping("/enabled")
    public ResponseEntity<List<OAuth2Client>> getEnabledClients() {
        return ResponseEntity.ok(oAuth2ClientService.getEnabledClients());
    }

    @GetMapping("/recently-used")
    public ResponseEntity<List<OAuth2Client>> getRecentlyUsedClients() {
        return ResponseEntity.ok(oAuth2ClientService.getRecentlyUsedClients());
    }

    @GetMapping("/most-used")
    public ResponseEntity<List<OAuth2Client>> getMostUsedClients() {
        return ResponseEntity.ok(oAuth2ClientService.getMostUsedClients());
    }
} 