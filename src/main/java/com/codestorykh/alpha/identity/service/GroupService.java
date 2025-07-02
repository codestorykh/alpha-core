package com.codestorykh.alpha.identity.service;

import com.codestorykh.alpha.common.service.BaseService;
import com.codestorykh.alpha.identity.domain.Group;
import com.codestorykh.alpha.identity.dto.GroupDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface GroupService extends BaseService<Group, Long> {

    Optional<Group> findByName(String name);

    boolean existsByName(String name);

    Group createGroup(GroupDTO groupDTO);

    Group updateGroup(Long id, GroupDTO groupDTO);

    void enableGroup(Long id);

    void disableGroup(Long id);

    void addPermission(Long groupId, Long permissionId);

    void removePermission(Long groupId, Long permissionId);

    List<Group> findByPermissionName(String permissionName);

    List<Group> findByUser(String username);

    List<Group> findParentGroups();

    List<Group> findChildGroups(Long parentId);

    List<Group> findSystemGroups();

    Page<Group> searchGroups(String searchTerm, Boolean enabled, Boolean systemGroup, Pageable pageable);

    boolean hasPermission(Long groupId, String permissionName);
} 