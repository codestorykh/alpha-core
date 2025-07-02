package com.codestorykh.alpha.identity.service;

import com.codestorykh.alpha.common.service.BaseService;
import com.codestorykh.alpha.identity.domain.Permission;
import com.codestorykh.alpha.identity.dto.PermissionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PermissionService extends BaseService<Permission, Long> {

    Optional<Permission> findByName(String name);

    boolean existsByName(String name);

    Permission createPermission(PermissionDTO permissionDTO);

    Permission updatePermission(Long id, PermissionDTO permissionDTO);

    void enablePermission(Long id);

    void disablePermission(Long id);

    List<Permission> findByResource(String resource);

    List<Permission> findByAction(String action);

    List<Permission> findByRole(String roleName);

    List<Permission> findByGroup(String groupName);

    List<Permission> findByUser(String username);

    List<Permission> findSystemPermissions();

    Page<Permission> searchPermissions(String searchTerm, Boolean enabled, Boolean systemPermission, String resource, String action, Pageable pageable);
} 