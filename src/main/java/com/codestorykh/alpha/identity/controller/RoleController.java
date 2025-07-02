package com.codestorykh.alpha.identity.controller;

import com.codestorykh.alpha.identity.domain.Role;
import com.codestorykh.alpha.identity.dto.RoleDTO;
import com.codestorykh.alpha.identity.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER_MANAGER')")
    public ResponseEntity<Page<Role>> getAllRoles(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) Boolean systemRole,
            Pageable pageable) {
        return ResponseEntity.ok(roleService.searchRoles(search, enabled, systemRole, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER_MANAGER')")
    public ResponseEntity<Role> getRoleById(@PathVariable Long id) {
        return roleService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Role> createRole(@Valid @RequestBody RoleDTO roleDTO) {
        Role createdRole = roleService.createRole(roleDTO);
        return ResponseEntity.ok(createdRole);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Role> updateRole(@PathVariable Long id, @Valid @RequestBody RoleDTO roleDTO) {
        Role updatedRole = roleService.updateRole(id, roleDTO);
        return ResponseEntity.ok(updatedRole);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> enableRole(@PathVariable Long id) {
        roleService.enableRole(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> disableRole(@PathVariable Long id) {
        roleService.disableRole(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER_MANAGER')")
    public ResponseEntity<Page<Role>> searchRoles(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) Boolean systemRole,
            Pageable pageable) {
        return ResponseEntity.ok(roleService.searchRoles(searchTerm, enabled, systemRole, pageable));
    }

    @GetMapping("/by-permission/{permissionName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Role>> getRolesByPermission(@PathVariable String permissionName) {
        return ResponseEntity.ok(roleService.findByPermissionName(permissionName));
    }

    @GetMapping("/by-user/{username}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER_MANAGER') or #username == authentication.principal.username")
    public ResponseEntity<List<Role>> getRolesByUser(@PathVariable String username) {
        return ResponseEntity.ok(roleService.findByUser(username));
    }

    @PostMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> addPermissionToRole(@PathVariable Long roleId, @PathVariable Long permissionId) {
        roleService.addPermission(roleId, permissionId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removePermissionFromRole(@PathVariable Long roleId, @PathVariable Long permissionId) {
        roleService.removePermission(roleId, permissionId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/system")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Role>> getSystemRoles() {
        return ResponseEntity.ok(roleService.findSystemRoles());
    }
} 