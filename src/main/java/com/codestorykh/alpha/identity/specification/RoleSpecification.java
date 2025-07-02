package com.codestorykh.alpha.identity.specification;

import com.codestorykh.alpha.identity.domain.Role;
import com.codestorykh.alpha.identity.dto.RoleSearchCriteria;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RoleSpecification {

    public static Specification<Role> withSearchCriteria(RoleSearchCriteria criteria) {
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

            // Status filters
            if (criteria.getEnabled() != null) {
                predicates.add(criteriaBuilder.equal(root.get("enabled"), criteria.getEnabled()));
            }

            if (criteria.getSystemRole() != null) {
                predicates.add(criteriaBuilder.equal(root.get("systemRole"), criteria.getSystemRole()));
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

            // Permission filters
            if (criteria.getPermissions() != null && !criteria.getPermissions().isEmpty()) {
                Join<Role, com.codestorykh.alpha.identity.domain.Permission> permissionJoin = root.join("permissions");
                predicates.add(permissionJoin.get("name").in(criteria.getPermissions()));
            }

            if (criteria.getResources() != null && !criteria.getResources().isEmpty()) {
                Join<Role, com.codestorykh.alpha.identity.domain.Permission> permissionJoin = root.join("permissions");
                predicates.add(permissionJoin.get("resource").in(criteria.getResources()));
            }

            if (criteria.getActions() != null && !criteria.getActions().isEmpty()) {
                Join<Role, com.codestorykh.alpha.identity.domain.Permission> permissionJoin = root.join("permissions");
                predicates.add(permissionJoin.get("action").in(criteria.getActions()));
            }

            // User filters
            if (criteria.getUsers() != null && !criteria.getUsers().isEmpty()) {
                Join<Role, com.codestorykh.alpha.identity.domain.User> userJoin = root.join("users");
                predicates.add(userJoin.get("username").in(criteria.getUsers()));
            }

            if (criteria.getHasUsers() != null) {
                if (criteria.getHasUsers()) {
                    // Role has users
                    Join<Role, com.codestorykh.alpha.identity.domain.User> userJoin = root.join("users");
                    predicates.add(criteriaBuilder.isNotNull(userJoin.get("id")));
                } else {
                    // Role has no users
                    predicates.add(criteriaBuilder.isEmpty(root.get("users")));
                }
            }

            if (criteria.getMinUserCount() != null || criteria.getMaxUserCount() != null) {
                Subquery<Long> userCountSubquery = query.subquery(Long.class);
                Root<Role> roleSubquery = userCountSubquery.from(Role.class);
                Join<Role, com.codestorykh.alpha.identity.domain.User> userJoin = roleSubquery.join("users");
                
                userCountSubquery.select(criteriaBuilder.count(userJoin))
                    .where(criteriaBuilder.equal(roleSubquery.get("id"), root.get("id")));
                
                if (criteria.getMinUserCount() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(userCountSubquery, criteria.getMinUserCount().longValue()));
                }
                
                if (criteria.getMaxUserCount() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(userCountSubquery, criteria.getMaxUserCount().longValue()));
                }
            }

            // Apply search mode (AND or OR)
            if (criteria.getSearchMode() == RoleSearchCriteria.SearchMode.OR && predicates.size() > 1) {
                return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
            } else {
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };
    }

    public static Specification<Role> withName(String name) {
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

    public static Specification<Role> withDescription(String description) {
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

    public static Specification<Role> withEnabled(Boolean enabled) {
        return (root, query, criteriaBuilder) -> {
            if (enabled == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("enabled"), enabled);
        };
    }

    public static Specification<Role> withSystemRole(Boolean systemRole) {
        return (root, query, criteriaBuilder) -> {
            if (systemRole == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("systemRole"), systemRole);
        };
    }

    public static Specification<Role> withPermission(String permissionName) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(permissionName)) {
                return criteriaBuilder.conjunction();
            }
            Join<Role, com.codestorykh.alpha.identity.domain.Permission> permissionJoin = root.join("permissions");
            return criteriaBuilder.equal(permissionJoin.get("name"), permissionName);
        };
    }

    public static Specification<Role> withResource(String resource) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(resource)) {
                return criteriaBuilder.conjunction();
            }
            Join<Role, com.codestorykh.alpha.identity.domain.Permission> permissionJoin = root.join("permissions");
            return criteriaBuilder.equal(permissionJoin.get("resource"), resource);
        };
    }

    public static Specification<Role> withAction(String action) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(action)) {
                return criteriaBuilder.conjunction();
            }
            Join<Role, com.codestorykh.alpha.identity.domain.Permission> permissionJoin = root.join("permissions");
            return criteriaBuilder.equal(permissionJoin.get("action"), action);
        };
    }

    public static Specification<Role> withUser(String username) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(username)) {
                return criteriaBuilder.conjunction();
            }
            Join<Role, com.codestorykh.alpha.identity.domain.User> userJoin = root.join("users");
            return criteriaBuilder.equal(userJoin.get("username"), username);
        };
    }

    public static Specification<Role> withDateRange(String fieldName, LocalDateTime startDate, LocalDateTime endDate) {
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