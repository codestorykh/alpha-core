package com.codestorykh.alpha.identity.controller;

import com.codestorykh.alpha.identity.domain.Permission;
import com.codestorykh.alpha.identity.dto.PermissionDTO;
import com.codestorykh.alpha.identity.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER_MANAGER')")
    public ResponseEntity<Page<Permission>> getAllPermissions(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) Boolean systemPermission,
            @RequestParam(required = false) String resource,
            @RequestParam(required = false) String action,
            Pageable pageable) {
        return ResponseEntity.ok(permissionService.searchPermissions(search, enabled, systemPermission, resource, action, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER_MANAGER')")
    public ResponseEntity<Permission> getPermissionById(@PathVariable Long id) {
        return permissionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Permission> createPermission(@Valid @RequestBody PermissionDTO permissionDTO) {
        Permission createdPermission = permissionService.createPermission(permissionDTO);
        return ResponseEntity.ok(createdPermission);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Permission> updatePermission(@PathVariable Long id, @Valid @RequestBody PermissionDTO permissionDTO) {
        Permission updatedPermission = permissionService.updatePermission(id, permissionDTO);
        return ResponseEntity.ok(updatedPermission);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePermission(@PathVariable Long id) {
        permissionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> enablePermission(@PathVariable Long id) {
        permissionService.enablePermission(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> disablePermission(@PathVariable Long id) {
        permissionService.disablePermission(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER_MANAGER')")
    public ResponseEntity<Page<Permission>> searchPermissions(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) Boolean systemPermission,
            @RequestParam(required = false) String resource,
            @RequestParam(required = false) String action,
            Pageable pageable) {
        return ResponseEntity.ok(permissionService.searchPermissions(searchTerm, enabled, systemPermission, resource, action, pageable));
    }

    @GetMapping("/by-resource/{resource}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Permission>> getPermissionsByResource(@PathVariable String resource) {
        return ResponseEntity.ok(permissionService.findByResource(resource));
    }

    @GetMapping("/by-action/{action}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Permission>> getPermissionsByAction(@PathVariable String action) {
        return ResponseEntity.ok(permissionService.findByAction(action));
    }

    @GetMapping("/by-role/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Permission>> getPermissionsByRole(@PathVariable String roleName) {
        return ResponseEntity.ok(permissionService.findByRole(roleName));
    }

    @GetMapping("/by-group/{groupName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Permission>> getPermissionsByGroup(@PathVariable String groupName) {
        return ResponseEntity.ok(permissionService.findByGroup(groupName));
    }

    @GetMapping("/by-user/{username}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER_MANAGER') or #username == authentication.principal.username")
    public ResponseEntity<List<Permission>> getPermissionsByUser(@PathVariable String username) {
        return ResponseEntity.ok(permissionService.findByUser(username));
    }

    @GetMapping("/system")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Permission>> getSystemPermissions() {
        return ResponseEntity.ok(permissionService.findSystemPermissions());
    }
} 