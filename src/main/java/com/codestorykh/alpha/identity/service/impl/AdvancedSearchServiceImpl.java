package com.codestorykh.alpha.identity.service.impl;

import com.codestorykh.alpha.identity.domain.Group;
import com.codestorykh.alpha.identity.domain.Permission;
import com.codestorykh.alpha.identity.domain.Role;
import com.codestorykh.alpha.identity.domain.User;
import com.codestorykh.alpha.identity.dto.GroupSearchCriteria;
import com.codestorykh.alpha.identity.dto.PermissionSearchCriteria;
import com.codestorykh.alpha.identity.dto.RoleSearchCriteria;
import com.codestorykh.alpha.identity.dto.UserSearchCriteria;
import com.codestorykh.alpha.identity.repository.GroupRepository;
import com.codestorykh.alpha.identity.repository.PermissionRepository;
import com.codestorykh.alpha.identity.repository.RoleRepository;
import com.codestorykh.alpha.identity.repository.UserRepository;
import com.codestorykh.alpha.identity.specification.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Advanced search service that demonstrates the use of JPA specifications
 * for dynamic search functionality across User, Role, Group, and Permission entities.
 */
@Service
@Slf4j
@Transactional(readOnly = true)
public class AdvancedSearchServiceImpl {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final GroupRepository groupRepository;
    private final PermissionRepository permissionRepository;

    public AdvancedSearchServiceImpl(UserRepository userRepository,
                                   RoleRepository roleRepository,
                                   GroupRepository groupRepository,
                                   PermissionRepository permissionRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.groupRepository = groupRepository;
        this.permissionRepository = permissionRepository;
    }

    // ==================== USER SEARCH METHODS ====================

    /**
     * Search users with comprehensive criteria
     */
    public Page<User> searchUsers(UserSearchCriteria criteria, Pageable pageable) {
        log.debug("Searching users with criteria: {}", criteria);
        
        Specification<User> specification = UserSpecification.withSearchCriteria(criteria);
        Pageable sortedPageable = createSortedPageable(pageable, criteria.getSortBy(), criteria.getSortDirection());
        
        return userRepository.findAll(specification, sortedPageable);
    }

    /**
     * Search users with basic criteria using SpecificationBuilder
     */
    public Page<User> searchUsersBasic(String username, String email, String firstName, String lastName, 
                                      Boolean enabled, Pageable pageable) {
        Specification<User> specification = SpecificationBuilder.<User>create()
            .andIfHasText(username, UserSpecification::withUsername)
            .andIfHasText(email, UserSpecification::withEmail)
            .andIfHasText(firstName, name -> UserSpecification.withName(name, null))
            .andIfHasText(lastName, name -> UserSpecification.withName(null, name))
            .andIfNotNull(enabled, UserSpecification::withEnabled)
            .build();

        return userRepository.findAll(specification, pageable);
    }

    /**
     * Find users by role
     */
    public List<User> findUsersByRole(String roleName) {
        Specification<User> specification = UserSpecification.withRole(roleName);
        return userRepository.findAll(specification);
    }

    /**
     * Find users by group
     */
    public List<User> findUsersByGroup(String groupName) {
        Specification<User> specification = UserSpecification.withGroup(groupName);
        return userRepository.findAll(specification);
    }

    /**
     * Find users by permission
     */
    public List<User> findUsersByPermission(String permissionName) {
        Specification<User> specification = UserSpecification.withPermission(permissionName);
        return userRepository.findAll(specification);
    }

    /**
     * Find locked users
     */
    public List<User> findLockedUsers() {
        Specification<User> specification = UserSpecification.withSearchCriteria(
            UserSearchCriteria.builder().isLocked(true).build()
        );
        return userRepository.findAll(specification);
    }

    /**
     * Find users with failed login attempts
     */
    public List<User> findUsersWithFailedLoginAttempts(int minAttempts) {
        Specification<User> specification = UserSpecification.withFailedLoginAttempts(minAttempts, null);
        return userRepository.findAll(specification);
    }

    /**
     * Find users created in date range
     */
    public List<User> findUsersCreatedInRange(LocalDateTime startDate, LocalDateTime endDate) {
        Specification<User> specification = UserSpecification.withDateRange("createdAt", startDate, endDate);
        return userRepository.findAll(specification);
    }

    // ==================== ROLE SEARCH METHODS ====================

    /**
     * Search roles with comprehensive criteria
     */
    public Page<Role> searchRoles(RoleSearchCriteria criteria, Pageable pageable) {
        log.debug("Searching roles with criteria: {}", criteria);
        
        Specification<Role> specification = RoleSpecification.withSearchCriteria(criteria);
        Pageable sortedPageable = createSortedPageable(pageable, criteria.getSortBy(), criteria.getSortDirection());
        
        return roleRepository.findAll(specification, sortedPageable);
    }

    /**
     * Find roles by permission
     */
    public List<Role> findRolesByPermission(String permissionName) {
        Specification<Role> specification = RoleSpecification.withPermission(permissionName);
        return roleRepository.findAll(specification);
    }

    /**
     * Find roles by resource
     */
    public List<Role> findRolesByResource(String resource) {
        Specification<Role> specification = RoleSpecification.withResource(resource);
        return roleRepository.findAll(specification);
    }

    /**
     * Find roles by action
     */
    public List<Role> findRolesByAction(String action) {
        Specification<Role> specification = RoleSpecification.withAction(action);
        return roleRepository.findAll(specification);
    }

    /**
     * Find roles assigned to user
     */
    public List<Role> findRolesByUser(String username) {
        Specification<Role> specification = RoleSpecification.withUser(username);
        return roleRepository.findAll(specification);
    }

    /**
     * Find system roles
     */
    public List<Role> findSystemRoles() {
        Specification<Role> specification = RoleSpecification.withSystemRole(true);
        return roleRepository.findAll(specification);
    }

    /**
     * Find roles created in date range
     */
    public List<Role> findRolesCreatedInRange(LocalDateTime startDate, LocalDateTime endDate) {
        Specification<Role> specification = RoleSpecification.withDateRange("createdAt", startDate, endDate);
        return roleRepository.findAll(specification);
    }

    // ==================== GROUP SEARCH METHODS ====================

    /**
     * Search groups with comprehensive criteria
     */
    public Page<Group> searchGroups(GroupSearchCriteria criteria, Pageable pageable) {
        log.debug("Searching groups with criteria: {}", criteria);
        
        Specification<Group> specification = GroupSpecification.withSearchCriteria(criteria);
        Pageable sortedPageable = createSortedPageable(pageable, criteria.getSortBy(), criteria.getSortDirection());
        
        return groupRepository.findAll(specification, sortedPageable);
    }

    /**
     * Find groups by permission
     */
    public List<Group> findGroupsByPermission(String permissionName) {
        Specification<Group> specification = GroupSpecification.withPermission(permissionName);
        return groupRepository.findAll(specification);
    }

    /**
     * Find groups by resource
     */
    public List<Group> findGroupsByResource(String resource) {
        Specification<Group> specification = GroupSpecification.withResource(resource);
        return groupRepository.findAll(specification);
    }

    /**
     * Find groups by action
     */
    public List<Group> findGroupsByAction(String action) {
        Specification<Group> specification = GroupSpecification.withAction(action);
        return groupRepository.findAll(specification);
    }

    /**
     * Find groups assigned to user
     */
    public List<Group> findGroupsByUser(String username) {
        Specification<Group> specification = GroupSpecification.withUser(username);
        return groupRepository.findAll(specification);
    }

    /**
     * Find parent groups
     */
    public List<Group> findParentGroups() {
        Specification<Group> specification = GroupSpecification.hasNoParent();
        return groupRepository.findAll(specification);
    }

    /**
     * Find child groups of a parent
     */
    public List<Group> findChildGroups(Long parentGroupId) {
        Specification<Group> specification = GroupSpecification.withParentGroup(parentGroupId);
        return groupRepository.findAll(specification);
    }

    /**
     * Find groups with children
     */
    public List<Group> findGroupsWithChildren() {
        Specification<Group> specification = GroupSpecification.hasChildren();
        return groupRepository.findAll(specification);
    }

    /**
     * Find system groups
     */
    public List<Group> findSystemGroups() {
        Specification<Group> specification = GroupSpecification.withSystemGroup(true);
        return groupRepository.findAll(specification);
    }

    /**
     * Find groups created in date range
     */
    public List<Group> findGroupsCreatedInRange(LocalDateTime startDate, LocalDateTime endDate) {
        Specification<Group> specification = GroupSpecification.withDateRange("createdAt", startDate, endDate);
        return groupRepository.findAll(specification);
    }

    // ==================== PERMISSION SEARCH METHODS ====================

    /**
     * Search permissions with comprehensive criteria
     */
    public Page<Permission> searchPermissions(PermissionSearchCriteria criteria, Pageable pageable) {
        log.debug("Searching permissions with criteria: {}", criteria);
        
        Specification<Permission> specification = PermissionSpecification.withSearchCriteria(criteria);
        Pageable sortedPageable = createSortedPageable(pageable, criteria.getSortBy(), criteria.getSortDirection());
        
        return permissionRepository.findAll(specification, sortedPageable);
    }

    /**
     * Find permissions by resource
     */
    public List<Permission> findPermissionsByResource(String resource) {
        Specification<Permission> specification = PermissionSpecification.withResource(resource);
        return permissionRepository.findAll(specification);
    }

    /**
     * Find permissions by action
     */
    public List<Permission> findPermissionsByAction(String action) {
        Specification<Permission> specification = PermissionSpecification.withAction(action);
        return permissionRepository.findAll(specification);
    }

    /**
     * Find permissions assigned to role
     */
    public List<Permission> findPermissionsByRole(String roleName) {
        Specification<Permission> specification = PermissionSpecification.withRole(roleName);
        return permissionRepository.findAll(specification);
    }

    /**
     * Find permissions assigned to group
     */
    public List<Permission> findPermissionsByGroup(String groupName) {
        Specification<Permission> specification = PermissionSpecification.withGroup(groupName);
        return permissionRepository.findAll(specification);
    }

    /**
     * Find permissions assigned to user (through roles and groups)
     */
    public List<Permission> findPermissionsByUser(String username) {
        Specification<Permission> specification = PermissionSpecification.withUser(username);
        return permissionRepository.findAll(specification);
    }

    /**
     * Find system permissions
     */
    public List<Permission> findSystemPermissions() {
        Specification<Permission> specification = PermissionSpecification.withSystemPermission(true);
        return permissionRepository.findAll(specification);
    }

    /**
     * Find permissions created in date range
     */
    public List<Permission> findPermissionsCreatedInRange(LocalDateTime startDate, LocalDateTime endDate) {
        Specification<Permission> specification = PermissionSpecification.withDateRange("createdAt", startDate, endDate);
        return permissionRepository.findAll(specification);
    }

    // ==================== COMPLEX SEARCH METHODS ====================

    /**
     * Find users with specific roles and permissions
     */
    public List<User> findUsersWithRolesAndPermissions(List<String> roles, List<String> permissions) {
        Specification<User> specification = SpecificationBuilder.<User>create()
            .andIfNotEmpty(roles, roleList -> {
                // Create OR specification for roles
                return roleList.stream()
                    .map(UserSpecification::withRole)
                    .reduce(Specification.where(null), Specification::or);
            })
            .andIfNotEmpty(permissions, permissionList -> {
                // Create OR specification for permissions
                return permissionList.stream()
                    .map(UserSpecification::withPermission)
                    .reduce(Specification.where(null), Specification::or);
            })
            .build();

        return userRepository.findAll(specification);
    }

    /**
     * Find roles with specific permissions and user count
     */
    public List<Role> findRolesWithPermissionsAndUserCount(List<String> permissions, Integer minUserCount) {
        RoleSearchCriteria criteria = RoleSearchCriteria.builder()
            .permissions(permissions)
            .minUserCount(minUserCount)
            .build();
        
        Specification<Role> specification = RoleSpecification.withSearchCriteria(criteria);
        return roleRepository.findAll(specification);
    }

    /**
     * Find groups in hierarchy with specific permissions
     */
    public List<Group> findGroupsInHierarchyWithPermissions(String parentGroupName, List<String> permissions) {
        Specification<Group> specification = SpecificationBuilder.<Group>create()
            .andIfHasText(parentGroupName, GroupSpecification::withParentGroupName)
            .andIfNotEmpty(permissions, permissionList -> {
                // Create OR specification for permissions
                return permissionList.stream()
                    .map(GroupSpecification::withPermission)
                    .reduce(Specification.where(null), Specification::or);
            })
            .build();

        return groupRepository.findAll(specification);
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Create sorted pageable
     */
    private Pageable createSortedPageable(Pageable pageable, String sortBy, String sortDirection) {
        if (sortBy != null && sortDirection != null) {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection.toUpperCase()), sortBy);
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        }
        return pageable;
    }

    /**
     * Get search statistics
     */
    public SearchStatistics getSearchStatistics() {
        long totalUsers = userRepository.count();
        long totalRoles = roleRepository.count();
        long totalGroups = groupRepository.count();
        long totalPermissions = permissionRepository.count();

        // Count active users
        Specification<User> activeUsersSpec = UserSpecification.withEnabled(true);
        long activeUsers = userRepository.count(activeUsersSpec);

        // Count system roles
        Specification<Role> systemRolesSpec = RoleSpecification.withSystemRole(true);
        long systemRoles = roleRepository.count(systemRolesSpec);

        // Count system groups
        Specification<Group> systemGroupsSpec = GroupSpecification.withSystemGroup(true);
        long systemGroups = groupRepository.count(systemGroupsSpec);

        // Count system permissions
        Specification<Permission> systemPermissionsSpec = PermissionSpecification.withSystemPermission(true);
        long systemPermissions = permissionRepository.count(systemPermissionsSpec);

        return SearchStatistics.builder()
            .totalUsers(totalUsers)
            .activeUsers(activeUsers)
            .totalRoles(totalRoles)
            .systemRoles(systemRoles)
            .totalGroups(totalGroups)
            .systemGroups(systemGroups)
            .totalPermissions(totalPermissions)
            .systemPermissions(systemPermissions)
            .build();
    }

    /**
     * Search statistics DTO
     */
    public static class SearchStatistics {
        private final long totalUsers;
        private final long activeUsers;
        private final long totalRoles;
        private final long systemRoles;
        private final long totalGroups;
        private final long systemGroups;
        private final long totalPermissions;
        private final long systemPermissions;

        public SearchStatistics(long totalUsers, long activeUsers, long totalRoles, long systemRoles,
                              long totalGroups, long systemGroups, long totalPermissions, long systemPermissions) {
            this.totalUsers = totalUsers;
            this.activeUsers = activeUsers;
            this.totalRoles = totalRoles;
            this.systemRoles = systemRoles;
            this.totalGroups = totalGroups;
            this.systemGroups = systemGroups;
            this.totalPermissions = totalPermissions;
            this.systemPermissions = systemPermissions;
        }

        // Getters
        public long getTotalUsers() { return totalUsers; }
        public long getActiveUsers() { return activeUsers; }
        public long getTotalRoles() { return totalRoles; }
        public long getSystemRoles() { return systemRoles; }
        public long getTotalGroups() { return totalGroups; }
        public long getSystemGroups() { return systemGroups; }
        public long getTotalPermissions() { return totalPermissions; }
        public long getSystemPermissions() { return systemPermissions; }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private long totalUsers;
            private long activeUsers;
            private long totalRoles;
            private long systemRoles;
            private long totalGroups;
            private long systemGroups;
            private long totalPermissions;
            private long systemPermissions;

            public Builder totalUsers(long totalUsers) {
                this.totalUsers = totalUsers;
                return this;
            }

            public Builder activeUsers(long activeUsers) {
                this.activeUsers = activeUsers;
                return this;
            }

            public Builder totalRoles(long totalRoles) {
                this.totalRoles = totalRoles;
                return this;
            }

            public Builder systemRoles(long systemRoles) {
                this.systemRoles = systemRoles;
                return this;
            }

            public Builder totalGroups(long totalGroups) {
                this.totalGroups = totalGroups;
                return this;
            }

            public Builder systemGroups(long systemGroups) {
                this.systemGroups = systemGroups;
                return this;
            }

            public Builder totalPermissions(long totalPermissions) {
                this.totalPermissions = totalPermissions;
                return this;
            }

            public Builder systemPermissions(long systemPermissions) {
                this.systemPermissions = systemPermissions;
                return this;
            }

            public SearchStatistics build() {
                return new SearchStatistics(totalUsers, activeUsers, totalRoles, systemRoles,
                    totalGroups, systemGroups, totalPermissions, systemPermissions);
            }
        }
    }
} 