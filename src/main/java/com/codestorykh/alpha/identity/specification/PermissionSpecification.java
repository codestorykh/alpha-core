package com.codestorykh.alpha.identity.specification;

import com.codestorykh.alpha.identity.domain.Permission;
import com.codestorykh.alpha.identity.dto.PermissionSearchCriteria;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PermissionSpecification {

    public static Specification<Permission> withSearchCriteria(PermissionSearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Basic search fields
            if (StringUtils.hasText(criteria.getName())) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + criteria.getName().toLowerCase() + "%"
                ));
            }

            if (StringUtils.hasText(criteria.getDescription())) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")),
                    "%" + criteria.getDescription().toLowerCase() + "%"
                ));
            }

            if (StringUtils.hasText(criteria.getResource())) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("resource")),
                    "%" + criteria.getResource().toLowerCase() + "%"
                ));
            }

            if (StringUtils.hasText(criteria.getAction())) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("action")),
                    "%" + criteria.getAction().toLowerCase() + "%"
                ));
            }

            // Status filters
            if (criteria.getEnabled() != null) {
                predicates.add(criteriaBuilder.equal(root.get("enabled"), criteria.getEnabled()));
            }

            if (criteria.getSystemPermission() != null) {
                predicates.add(criteriaBuilder.equal(root.get("systemPermission"), criteria.getSystemPermission()));
            }

            // Date range filters
            if (criteria.getCreatedAfter() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("createdAt"), criteria.getCreatedAfter()
                ));
            }

            if (criteria.getCreatedBefore() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("createdAt"), criteria.getCreatedBefore()
                ));
            }

            if (criteria.getUpdatedAfter() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("updatedAt"), criteria.getUpdatedAfter()
                ));
            }

            if (criteria.getUpdatedBefore() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("updatedAt"), criteria.getUpdatedBefore()
                ));
            }

            // Role filters
            if (criteria.getRoles() != null && !criteria.getRoles().isEmpty()) {
                Join<Permission, com.codestorykh.alpha.identity.domain.Role> roleJoin = root.join("roles");
                predicates.add(roleJoin.get("name").in(criteria.getRoles()));
            }

            if (criteria.getHasRoles() != null) {
                if (criteria.getHasRoles()) {
                    // Permission has roles
                    Join<Permission, com.codestorykh.alpha.identity.domain.Role> roleJoin = root.join("roles");
                    predicates.add(criteriaBuilder.isNotNull(roleJoin.get("id")));
                } else {
                    // Permission has no roles
                    predicates.add(criteriaBuilder.isEmpty(root.get("roles")));
                }
            }

            if (criteria.getMinRoleCount() != null || criteria.getMaxRoleCount() != null) {
                Subquery<Long> roleCountSubquery = query.subquery(Long.class);
                Root<Permission> permissionSubquery = roleCountSubquery.from(Permission.class);
                Join<Permission, com.codestorykh.alpha.identity.domain.Role> roleJoin = permissionSubquery.join("roles");
                
                roleCountSubquery.select(criteriaBuilder.count(roleJoin))
                    .where(criteriaBuilder.equal(permissionSubquery.get("id"), root.get("id")));
                
                if (criteria.getMinRoleCount() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(roleCountSubquery, criteria.getMinRoleCount().longValue()));
                }
                
                if (criteria.getMaxRoleCount() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(roleCountSubquery, criteria.getMaxRoleCount().longValue()));
                }
            }

            // Group filters
            if (criteria.getGroups() != null && !criteria.getGroups().isEmpty()) {
                Join<Permission, com.codestorykh.alpha.identity.domain.Group> groupJoin = root.join("groups");
                predicates.add(groupJoin.get("name").in(criteria.getGroups()));
            }

            if (criteria.getHasGroups() != null) {
                if (criteria.getHasGroups()) {
                    // Permission has groups
                    Join<Permission, com.codestorykh.alpha.identity.domain.Group> groupJoin = root.join("groups");
                    predicates.add(criteriaBuilder.isNotNull(groupJoin.get("id")));
                } else {
                    // Permission has no groups
                    predicates.add(criteriaBuilder.isEmpty(root.get("groups")));
                }
            }

            if (criteria.getMinGroupCount() != null || criteria.getMaxGroupCount() != null) {
                Subquery<Long> groupCountSubquery = query.subquery(Long.class);
                Root<Permission> permissionSubquery = groupCountSubquery.from(Permission.class);
                Join<Permission, com.codestorykh.alpha.identity.domain.Group> groupJoin = permissionSubquery.join("groups");
                
                groupCountSubquery.select(criteriaBuilder.count(groupJoin))
                    .where(criteriaBuilder.equal(permissionSubquery.get("id"), root.get("id")));
                
                if (criteria.getMinGroupCount() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(groupCountSubquery, criteria.getMinGroupCount().longValue()));
                }
                
                if (criteria.getMaxGroupCount() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(groupCountSubquery, criteria.getMaxGroupCount().longValue()));
                }
            }

            // User filters (through roles and groups)
            if (criteria.getUsers() != null && !criteria.getUsers().isEmpty()) {
                Join<Permission, com.codestorykh.alpha.identity.domain.Role> roleJoin = root.join("roles");
                Join<com.codestorykh.alpha.identity.domain.Role, com.codestorykh.alpha.identity.domain.User> roleUserJoin = roleJoin.join("users");
                
                Join<Permission, com.codestorykh.alpha.identity.domain.Group> groupJoin = root.join("groups");
                Join<com.codestorykh.alpha.identity.domain.Group, com.codestorykh.alpha.identity.domain.User> groupUserJoin = groupJoin.join("users");
                
                Predicate roleUsers = roleUserJoin.get("username").in(criteria.getUsers());
                Predicate groupUsers = groupUserJoin.get("username").in(criteria.getUsers());
                predicates.add(criteriaBuilder.or(roleUsers, groupUsers));
            }

            if (criteria.getHasUsers() != null) {
                if (criteria.getHasUsers()) {
                    // Permission has users through roles or groups
                    Join<Permission, com.codestorykh.alpha.identity.domain.Role> roleJoin = root.join("roles");
                    Join<com.codestorykh.alpha.identity.domain.Role, com.codestorykh.alpha.identity.domain.User> roleUserJoin = roleJoin.join("users");
                    
                    Join<Permission, com.codestorykh.alpha.identity.domain.Group> groupJoin = root.join("groups");
                    Join<com.codestorykh.alpha.identity.domain.Group, com.codestorykh.alpha.identity.domain.User> groupUserJoin = groupJoin.join("users");
                    
                    Predicate hasRoleUsers = criteriaBuilder.isNotNull(roleUserJoin.get("id"));
                    Predicate hasGroupUsers = criteriaBuilder.isNotNull(groupUserJoin.get("id"));
                    predicates.add(criteriaBuilder.or(hasRoleUsers, hasGroupUsers));
                } else {
                    // Permission has no users
                    predicates.add(criteriaBuilder.and(
                        criteriaBuilder.isEmpty(root.get("roles")),
                        criteriaBuilder.isEmpty(root.get("groups"))
                    ));
                }
            }

            if (criteria.getMinUserCount() != null || criteria.getMaxUserCount() != null) {
                Subquery<Long> userCountSubquery = query.subquery(Long.class);
                Root<Permission> permissionSubquery = userCountSubquery.from(Permission.class);
                Join<Permission, com.codestorykh.alpha.identity.domain.Role> roleJoin = permissionSubquery.join("roles");
                Join<com.codestorykh.alpha.identity.domain.Role, com.codestorykh.alpha.identity.domain.User> roleUserJoin = roleJoin.join("users");
                
                Join<Permission, com.codestorykh.alpha.identity.domain.Group> groupJoin = permissionSubquery.join("groups");
                Join<com.codestorykh.alpha.identity.domain.Group, com.codestorykh.alpha.identity.domain.User> groupUserJoin = groupJoin.join("users");
                
                userCountSubquery.select(criteriaBuilder.countDistinct(criteriaBuilder.coalesce(roleUserJoin.get("id"), groupUserJoin.get("id"))))
                    .where(criteriaBuilder.equal(permissionSubquery.get("id"), root.get("id")));
                
                if (criteria.getMinUserCount() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(userCountSubquery, criteria.getMinUserCount().longValue()));
                }
                
                if (criteria.getMaxUserCount() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(userCountSubquery, criteria.getMaxUserCount().longValue()));
                }
            }

            // Apply search mode (AND or OR)
            if (criteria.getSearchMode() == PermissionSearchCriteria.SearchMode.OR && predicates.size() > 1) {
                return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
            } else {
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };
    }

    public static Specification<Permission> withName(String name) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(name)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("name")),
                "%" + name.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Permission> withDescription(String description) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(description)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("description")),
                "%" + description.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Permission> withResource(String resource) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(resource)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("resource")),
                "%" + resource.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Permission> withAction(String action) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(action)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("action")),
                "%" + action.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Permission> withEnabled(Boolean enabled) {
        return (root, query, criteriaBuilder) -> {
            if (enabled == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("enabled"), enabled);
        };
    }

    public static Specification<Permission> withSystemPermission(Boolean systemPermission) {
        return (root, query, criteriaBuilder) -> {
            if (systemPermission == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("systemPermission"), systemPermission);
        };
    }

    public static Specification<Permission> withRole(String roleName) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(roleName)) {
                return criteriaBuilder.conjunction();
            }
            Join<Permission, com.codestorykh.alpha.identity.domain.Role> roleJoin = root.join("roles");
            return criteriaBuilder.equal(roleJoin.get("name"), roleName);
        };
    }

    public static Specification<Permission> withGroup(String groupName) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(groupName)) {
                return criteriaBuilder.conjunction();
            }
            Join<Permission, com.codestorykh.alpha.identity.domain.Group> groupJoin = root.join("groups");
            return criteriaBuilder.equal(groupJoin.get("name"), groupName);
        };
    }

    public static Specification<Permission> withUser(String username) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(username)) {
                return criteriaBuilder.conjunction();
            }
            
            Join<Permission, com.codestorykh.alpha.identity.domain.Role> roleJoin = root.join("roles");
            Join<com.codestorykh.alpha.identity.domain.Role, com.codestorykh.alpha.identity.domain.User> roleUserJoin = roleJoin.join("users");
            
            Join<Permission, com.codestorykh.alpha.identity.domain.Group> groupJoin = root.join("groups");
            Join<com.codestorykh.alpha.identity.domain.Group, com.codestorykh.alpha.identity.domain.User> groupUserJoin = groupJoin.join("users");
            
            Predicate roleUsers = criteriaBuilder.equal(roleUserJoin.get("username"), username);
            Predicate groupUsers = criteriaBuilder.equal(groupUserJoin.get("username"), username);
            
            return criteriaBuilder.or(roleUsers, groupUsers);
        };
    }

    public static Specification<Permission> withDateRange(String fieldName, LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get(fieldName), startDate
                ));
            }
            
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get(fieldName), endDate
                ));
            }
            
            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
} 