package com.codestorykh.alpha.identity.service.impl;

import com.codestorykh.alpha.common.service.BaseServiceImpl;
import com.codestorykh.alpha.identity.domain.Permission;
import com.codestorykh.alpha.identity.domain.Role;
import com.codestorykh.alpha.identity.dto.RoleDTO;
import com.codestorykh.alpha.identity.repository.PermissionRepository;
import com.codestorykh.alpha.identity.repository.RoleRepository;
import com.codestorykh.alpha.identity.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class RoleServiceImpl extends BaseServiceImpl<Role, Long> implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleServiceImpl(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        super(roleRepository);
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public Optional<Role> findByName(String name) {
        return roleRepository.findByName(name);
    }

    @Override
    public boolean existsByName(String name) {
        return roleRepository.existsByName(name);
    }

    @Override
    public Role createRole(RoleDTO roleDTO) {
        if (existsByName(roleDTO.getName())) {
            throw new IllegalArgumentException("Role with name '" + roleDTO.getName() + "' already exists");
        }

        Role role = Role.builder()
                .name(roleDTO.getName())
                .description(roleDTO.getDescription())
                .enabled(roleDTO.isEnabled())
                .systemRole(roleDTO.isSystemRole())
                .build();

        // Add permissions if provided
        if (roleDTO.getPermissionIds() != null && !roleDTO.getPermissionIds().isEmpty()) {
            Set<Permission> permissions = roleDTO.getPermissionIds().stream()
                    .map(permissionRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            role.setPermissions(permissions);
        }

        return save(role);
    }

    @Override
    public Role updateRole(Long id, RoleDTO roleDTO) {
        Role existingRole = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + id));

        // Check if name is being changed and if it conflicts with existing role
        if (!existingRole.getName().equals(roleDTO.getName()) && existsByName(roleDTO.getName())) {
            throw new IllegalArgumentException("Role with name '" + roleDTO.getName() + "' already exists");
        }

        existingRole.setName(roleDTO.getName());
        existingRole.setDescription(roleDTO.getDescription());
        existingRole.setEnabled(roleDTO.isEnabled());
        existingRole.setSystemRole(roleDTO.isSystemRole());

        // Update permissions if provided
        if (roleDTO.getPermissionIds() != null) {
            Set<Permission> permissions = roleDTO.getPermissionIds().stream()
                    .map(permissionRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            existingRole.setPermissions(permissions);
        }

        return save(existingRole);
    }

    @Override
    public void enableRole(Long id) {
        Role role = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + id));
        role.setEnabled(true);
        save(role);
    }

    @Override
    public void disableRole(Long id) {
        Role role = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + id));
        role.setEnabled(false);
        save(role);
    }

    @Override
    public void addPermission(Long roleId, Long permissionId) {
        Role role = findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + roleId));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found with id: " + permissionId));
        
        role.addPermission(permission);
        save(role);
    }

    @Override
    public void removePermission(Long roleId, Long permissionId) {
        Role role = findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + roleId));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found with id: " + permissionId));
        
        role.removePermission(permission);
        save(role);
    }

    @Override
    public List<Role> findByPermissionName(String permissionName) {
        return roleRepository.findByPermissionName(permissionName);
    }

    @Override
    public List<Role> findByUser(String username) {
        return roleRepository.findByUser(username);
    }

    @Override
    public List<Role> findSystemRoles() {
        return roleRepository.findBySystemRoleTrue();
    }

    @Override
    public Page<Role> searchRoles(String searchTerm, Boolean enabled, Boolean systemRole, Pageable pageable) {
        return roleRepository.searchRoles(searchTerm, enabled, systemRole, pageable);
    }

    @Override
    public boolean hasPermission(Long roleId, String permissionName) {
        return findById(roleId)
                .map(role -> role.hasPermission(permissionName))
                .orElse(false);
    }
} 