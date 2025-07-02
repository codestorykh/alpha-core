package com.codestorykh.alpha.identity.service.impl;

import com.codestorykh.alpha.common.service.BaseServiceImpl;
import com.codestorykh.alpha.identity.domain.Group;
import com.codestorykh.alpha.identity.domain.Permission;
import com.codestorykh.alpha.identity.dto.GroupDTO;
import com.codestorykh.alpha.identity.repository.GroupRepository;
import com.codestorykh.alpha.identity.repository.PermissionRepository;
import com.codestorykh.alpha.identity.service.GroupService;
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
public class GroupServiceImpl extends BaseServiceImpl<Group, Long> implements GroupService {

    private final GroupRepository groupRepository;
    private final PermissionRepository permissionRepository;

    public GroupServiceImpl(GroupRepository groupRepository, PermissionRepository permissionRepository) {
        super(groupRepository);
        this.groupRepository = groupRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public Optional<Group> findByName(String name) {
        return groupRepository.findByName(name);
    }

    @Override
    public boolean existsByName(String name) {
        return groupRepository.existsByName(name);
    }

    @Override
    public Group createGroup(GroupDTO groupDTO) {
        if (existsByName(groupDTO.getName())) {
            throw new IllegalArgumentException("Group with name '" + groupDTO.getName() + "' already exists");
        }

        Group group = Group.builder()
                .name(groupDTO.getName())
                .description(groupDTO.getDescription())
                .enabled(groupDTO.isEnabled())
                .systemGroup(groupDTO.isSystemGroup())
                .build();

        // Set parent group if provided
        if (groupDTO.getParentGroupId() != null) {
            Group parentGroup = findById(groupDTO.getParentGroupId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent group not found with id: " + groupDTO.getParentGroupId()));
            group.setParentGroup(parentGroup);
        }

        // Add permissions if provided
        if (groupDTO.getPermissionIds() != null && !groupDTO.getPermissionIds().isEmpty()) {
            Set<Permission> permissions = groupDTO.getPermissionIds().stream()
                    .map(permissionRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            group.setPermissions(permissions);
        }

        return save(group);
    }

    @Override
    public Group updateGroup(Long id, GroupDTO groupDTO) {
        Group existingGroup = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + id));

        // Check if name is being changed and if it conflicts with existing group
        if (!existingGroup.getName().equals(groupDTO.getName()) && existsByName(groupDTO.getName())) {
            throw new IllegalArgumentException("Group with name '" + groupDTO.getName() + "' already exists");
        }

        existingGroup.setName(groupDTO.getName());
        existingGroup.setDescription(groupDTO.getDescription());
        existingGroup.setEnabled(groupDTO.isEnabled());
        existingGroup.setSystemGroup(groupDTO.isSystemGroup());

        // Update parent group if provided
        if (groupDTO.getParentGroupId() != null) {
            Group parentGroup = findById(groupDTO.getParentGroupId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent group not found with id: " + groupDTO.getParentGroupId()));
            existingGroup.setParentGroup(parentGroup);
        } else {
            existingGroup.setParentGroup(null);
        }

        // Update permissions if provided
        if (groupDTO.getPermissionIds() != null) {
            Set<Permission> permissions = groupDTO.getPermissionIds().stream()
                    .map(permissionRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            existingGroup.setPermissions(permissions);
        }

        return save(existingGroup);
    }

    @Override
    public void enableGroup(Long id) {
        Group group = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + id));
        group.setEnabled(true);
        save(group);
    }

    @Override
    public void disableGroup(Long id) {
        Group group = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + id));
        group.setEnabled(false);
        save(group);
    }

    @Override
    public void addPermission(Long groupId, Long permissionId) {
        Group group = findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + groupId));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found with id: " + permissionId));
        
        group.addPermission(permission);
        save(group);
    }

    @Override
    public void removePermission(Long groupId, Long permissionId) {
        Group group = findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + groupId));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found with id: " + permissionId));
        
        group.removePermission(permission);
        save(group);
    }

    @Override
    public List<Group> findByPermissionName(String permissionName) {
        return groupRepository.findByPermissionName(permissionName);
    }

    @Override
    public List<Group> findByUser(String username) {
        return groupRepository.findByUser(username);
    }

    @Override
    public List<Group> findParentGroups() {
        return groupRepository.findByParentGroupIsNull();
    }

    @Override
    public List<Group> findChildGroups(Long parentId) {
        return groupRepository.findByParentGroupId(parentId);
    }

    @Override
    public List<Group> findSystemGroups() {
        return groupRepository.findBySystemGroupTrue();
    }

    @Override
    public Page<Group> searchGroups(String searchTerm, Boolean enabled, Boolean systemGroup, Pageable pageable) {
        return groupRepository.searchGroups(searchTerm, enabled, systemGroup, pageable);
    }

    @Override
    public boolean hasPermission(Long groupId, String permissionName) {
        return findById(groupId)
                .map(group -> group.getPermissions().stream()
                        .anyMatch(permission -> permission.getName().equals(permissionName)))
                .orElse(false);
    }
} 