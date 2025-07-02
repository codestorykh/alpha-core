package com.codestorykh.alpha.identity.service.impl;

import com.codestorykh.alpha.common.service.BaseServiceImpl;
import com.codestorykh.alpha.identity.domain.Permission;
import com.codestorykh.alpha.identity.dto.PermissionDTO;
import com.codestorykh.alpha.identity.repository.PermissionRepository;
import com.codestorykh.alpha.identity.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional
public class PermissionServiceImpl extends BaseServiceImpl<Permission, Long> implements PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionServiceImpl(PermissionRepository permissionRepository) {
        super(permissionRepository);
        this.permissionRepository = permissionRepository;
    }

    @Override
    public Optional<Permission> findByName(String name) {
        return permissionRepository.findByName(name);
    }

    @Override
    public boolean existsByName(String name) {
        return permissionRepository.existsByName(name);
    }

    @Override
    public Permission createPermission(PermissionDTO permissionDTO) {
        if (existsByName(permissionDTO.getName())) {
            throw new IllegalArgumentException("Permission with name '" + permissionDTO.getName() + "' already exists");
        }

        Permission permission = Permission.builder()
                .name(permissionDTO.getName())
                .description(permissionDTO.getDescription())
                .resource(permissionDTO.getResource())
                .action(permissionDTO.getAction())
                .enabled(permissionDTO.isEnabled())
                .systemPermission(permissionDTO.isSystemPermission())
                .build();

        return save(permission);
    }

    @Override
    public Permission updatePermission(Long id, PermissionDTO permissionDTO) {
        Permission existingPermission = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found with id: " + id));

        // Check if name is being changed and if it conflicts with existing permission
        if (!existingPermission.getName().equals(permissionDTO.getName()) && existsByName(permissionDTO.getName())) {
            throw new IllegalArgumentException("Permission with name '" + permissionDTO.getName() + "' already exists");
        }

        existingPermission.setName(permissionDTO.getName());
        existingPermission.setDescription(permissionDTO.getDescription());
        existingPermission.setResource(permissionDTO.getResource());
        existingPermission.setAction(permissionDTO.getAction());
        existingPermission.setEnabled(permissionDTO.isEnabled());
        existingPermission.setSystemPermission(permissionDTO.isSystemPermission());

        return save(existingPermission);
    }

    @Override
    public void enablePermission(Long id) {
        Permission permission = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found with id: " + id));
        permission.setEnabled(true);
        save(permission);
    }

    @Override
    public void disablePermission(Long id) {
        Permission permission = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found with id: " + id));
        permission.setEnabled(false);
        save(permission);
    }

    @Override
    public List<Permission> findByResource(String resource) {
        return permissionRepository.findByResource(resource);
    }

    @Override
    public List<Permission> findByAction(String action) {
        return permissionRepository.findByAction(action);
    }

    @Override
    public List<Permission> findByRole(String roleName) {
        return permissionRepository.findByRole(roleName);
    }

    @Override
    public List<Permission> findByGroup(String groupName) {
        return permissionRepository.findByGroup(groupName);
    }

    @Override
    public List<Permission> findByUser(String username) {
        return permissionRepository.findByUser(username);
    }

    @Override
    public List<Permission> findSystemPermissions() {
        return permissionRepository.findBySystemPermissionTrue();
    }

    @Override
    public Page<Permission> searchPermissions(String searchTerm, Boolean enabled, Boolean systemPermission, String resource, String action, Pageable pageable) {
        return permissionRepository.searchPermissions(searchTerm, enabled, systemPermission, resource, action, pageable);
    }
} 