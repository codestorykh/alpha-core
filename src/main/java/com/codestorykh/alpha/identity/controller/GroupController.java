package com.codestorykh.alpha.identity.controller;

import com.codestorykh.alpha.identity.domain.Group;
import com.codestorykh.alpha.identity.dto.GroupDTO;
import com.codestorykh.alpha.identity.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER_MANAGER')")
    public ResponseEntity<Page<Group>> getAllGroups(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) Boolean systemGroup,
            Pageable pageable) {
        return ResponseEntity.ok(groupService.searchGroups(search, enabled, systemGroup, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER_MANAGER')")
    public ResponseEntity<Group> getGroupById(@PathVariable Long id) {
        return groupService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Group> createGroup(@Valid @RequestBody GroupDTO groupDTO) {
        Group createdGroup = groupService.createGroup(groupDTO);
        return ResponseEntity.ok(createdGroup);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Group> updateGroup(@PathVariable Long id, @Valid @RequestBody GroupDTO groupDTO) {
        Group updatedGroup = groupService.updateGroup(id, groupDTO);
        return ResponseEntity.ok(updatedGroup);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        groupService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> enableGroup(@PathVariable Long id) {
        groupService.enableGroup(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> disableGroup(@PathVariable Long id) {
        groupService.disableGroup(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER_MANAGER')")
    public ResponseEntity<Page<Group>> searchGroups(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) Boolean systemGroup,
            Pageable pageable) {
        return ResponseEntity.ok(groupService.searchGroups(searchTerm, enabled, systemGroup, pageable));
    }

    @GetMapping("/by-permission/{permissionName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Group>> getGroupsByPermission(@PathVariable String permissionName) {
        return ResponseEntity.ok(groupService.findByPermissionName(permissionName));
    }

    @GetMapping("/by-user/{username}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER_MANAGER') or #username == authentication.principal.username")
    public ResponseEntity<List<Group>> getGroupsByUser(@PathVariable String username) {
        return ResponseEntity.ok(groupService.findByUser(username));
    }

    @GetMapping("/parents")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER_MANAGER')")
    public ResponseEntity<List<Group>> getParentGroups() {
        return ResponseEntity.ok(groupService.findParentGroups());
    }

    @GetMapping("/{parentId}/children")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER_MANAGER')")
    public ResponseEntity<List<Group>> getChildGroups(@PathVariable Long parentId) {
        return ResponseEntity.ok(groupService.findChildGroups(parentId));
    }

    @PostMapping("/{groupId}/permissions/{permissionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> addPermissionToGroup(@PathVariable Long groupId, @PathVariable Long permissionId) {
        groupService.addPermission(groupId, permissionId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{groupId}/permissions/{permissionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removePermissionFromGroup(@PathVariable Long groupId, @PathVariable Long permissionId) {
        groupService.removePermission(groupId, permissionId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/system")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Group>> getSystemGroups() {
        return ResponseEntity.ok(groupService.findSystemGroups());
    }
} 