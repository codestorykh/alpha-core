package com.codestorykh.alpha.identity.service;

import com.codestorykh.alpha.common.service.BaseService;
import com.codestorykh.alpha.identity.domain.Role;
import com.codestorykh.alpha.identity.dto.RoleDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface RoleService extends BaseService<Role, Long> {

    Optional<Role> findByName(String name);

    boolean existsByName(String name);

    Role createRole(RoleDTO roleDTO);

    Role updateRole(Long id, RoleDTO roleDTO);

    void enableRole(Long id);

    void disableRole(Long id);

    void addPermission(Long roleId, Long permissionId);

    void removePermission(Long roleId, Long permissionId);

    List<Role> findByPermissionName(String permissionName);

    List<Role> findByUser(String username);

    List<Role> findSystemRoles();

    Page<Role> searchRoles(String searchTerm, Boolean enabled, Boolean systemRole, Pageable pageable);

    boolean hasPermission(Long roleId, String permissionName);
} 