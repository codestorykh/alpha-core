package com.codestorykh.alpha.identity.specification;

import com.codestorykh.alpha.identity.domain.User;
import com.codestorykh.alpha.identity.domain.UserStatus;
import com.codestorykh.alpha.identity.dto.UserSearchCriteria;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<User> withSearchCriteria(UserSearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Basic search fields
            if (StringUtils.hasText(criteria.getUsername())) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("username")),
                    "%" + criteria.getUsername().toLowerCase() + "%"
                ));
            }

            if (StringUtils.hasText(criteria.getEmail())) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("email")),
                    "%" + criteria.getEmail().toLowerCase() + "%"
                ));
            }

            if (StringUtils.hasText(criteria.getFirstName())) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("firstName")),
                    "%" + criteria.getFirstName().toLowerCase() + "%"
                ));
            }

            if (StringUtils.hasText(criteria.getLastName())) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("lastName")),
                    "%" + criteria.getLastName().toLowerCase() + "%"
                ));
            }

            if (StringUtils.hasText(criteria.getPhoneNumber())) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("phoneNumber")),
                    "%" + criteria.getPhoneNumber().toLowerCase() + "%"
                ));
            }

            // Status filters
            if (criteria.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), criteria.getStatus()));
            }

            if (criteria.getEnabled() != null) {
                predicates.add(criteriaBuilder.equal(root.get("enabled"), criteria.getEnabled()));
            }

            if (criteria.getEmailVerified() != null) {
                predicates.add(criteriaBuilder.equal(root.get("emailVerified"), criteria.getEmailVerified()));
            }

            if (criteria.getPhoneVerified() != null) {
                predicates.add(criteriaBuilder.equal(root.get("phoneVerified"), criteria.getPhoneVerified()));
            }

            if (criteria.getAccountNonLocked() != null) {
                predicates.add(criteriaBuilder.equal(root.get("accountNonLocked"), criteria.getAccountNonLocked()));
            }

            if (criteria.getAccountNonExpired() != null) {
                predicates.add(criteriaBuilder.equal(root.get("accountNonExpired"), criteria.getAccountNonExpired()));
            }

            if (criteria.getCredentialsNonExpired() != null) {
                predicates.add(criteriaBuilder.equal(root.get("credentialsNonExpired"), criteria.getCredentialsNonExpired()));
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

            if (criteria.getLastLoginAfter() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("lastLogin"), criteria.getLastLoginAfter()
                ));
            }

            if (criteria.getLastLoginBefore() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("lastLogin"), criteria.getLastLoginBefore()
                ));
            }

            if (criteria.getPasswordChangedAfter() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("passwordChangedAt"), criteria.getPasswordChangedAfter()
                ));
            }

            if (criteria.getPasswordChangedBefore() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("passwordChangedAt"), criteria.getPasswordChangedBefore()
                ));
            }

            // Security filters
            if (criteria.getMinFailedLoginAttempts() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("failedLoginAttempts"), criteria.getMinFailedLoginAttempts()
                ));
            }

            if (criteria.getMaxFailedLoginAttempts() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("failedLoginAttempts"), criteria.getMaxFailedLoginAttempts()
                ));
            }

            if (criteria.getIsLocked() != null) {
                if (criteria.getIsLocked()) {
                    // User is locked if accountNonLocked is false OR lockedUntil is in the future
                    Predicate notLocked = criteriaBuilder.equal(root.get("accountNonLocked"), false);
                    Predicate lockedUntilFuture = criteriaBuilder.greaterThan(
                        root.get("lockedUntil"), LocalDateTime.now()
                    );
                    predicates.add(criteriaBuilder.or(notLocked, lockedUntilFuture));
                } else {
                    // User is not locked if accountNonLocked is true AND (lockedUntil is null OR in the past)
                    Predicate isLocked = criteriaBuilder.equal(root.get("accountNonLocked"), true);
                    Predicate lockedUntilPastOrNull = criteriaBuilder.or(
                        criteriaBuilder.isNull(root.get("lockedUntil")),
                        criteriaBuilder.lessThanOrEqualTo(root.get("lockedUntil"), LocalDateTime.now())
                    );
                    predicates.add(criteriaBuilder.and(isLocked, lockedUntilPastOrNull));
                }
            }

            if (criteria.getLockedAfter() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("lockedUntil"), criteria.getLockedAfter()
                ));
            }

            if (criteria.getLockedBefore() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("lockedUntil"), criteria.getLockedBefore()
                ));
            }

            // Role filters
            if (criteria.getRoles() != null && !criteria.getRoles().isEmpty()) {
                Join<User, com.codestorykh.alpha.identity.domain.Role> roleJoin = root.join("roles");
                predicates.add(roleJoin.get("name").in(criteria.getRoles()));
            }

            // Group filters
            if (criteria.getGroups() != null && !criteria.getGroups().isEmpty()) {
                Join<User, com.codestorykh.alpha.identity.domain.Group> groupJoin = root.join("groups");
                predicates.add(groupJoin.get("name").in(criteria.getGroups()));
            }

            // Permission filters (through roles and groups)
            if (criteria.getPermissions() != null && !criteria.getPermissions().isEmpty()) {
                Join<User, com.codestorykh.alpha.identity.domain.Role> roleJoin = root.join("roles");
                Join<com.codestorykh.alpha.identity.domain.Role, com.codestorykh.alpha.identity.domain.Permission> rolePermissionJoin = roleJoin.join("permissions");
                
                Join<User, com.codestorykh.alpha.identity.domain.Group> groupJoin = root.join("groups");
                Join<com.codestorykh.alpha.identity.domain.Group, com.codestorykh.alpha.identity.domain.Permission> groupPermissionJoin = groupJoin.join("permissions");
                
                Predicate rolePermissions = rolePermissionJoin.get("name").in(criteria.getPermissions());
                Predicate groupPermissions = groupPermissionJoin.get("name").in(criteria.getPermissions());
                predicates.add(criteriaBuilder.or(rolePermissions, groupPermissions));
            }

            // Apply search mode (AND or OR)
            if (criteria.getSearchMode() == UserSearchCriteria.SearchMode.OR && predicates.size() > 1) {
                return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
            } else {
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };
    }

    public static Specification<User> withUsername(String username) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(username)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("username")),
                "%" + username.toLowerCase() + "%"
            );
        };
    }

    public static Specification<User> withEmail(String email) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(email)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("email")),
                "%" + email.toLowerCase() + "%"
            );
        };
    }

    public static Specification<User> withName(String firstName, String lastName) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (StringUtils.hasText(firstName)) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("firstName")),
                    "%" + firstName.toLowerCase() + "%"
                ));
            }
            
            if (StringUtils.hasText(lastName)) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("lastName")),
                    "%" + lastName.toLowerCase() + "%"
                ));
            }
            
            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            
            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<User> withStatus(UserStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<User> withEnabled(Boolean enabled) {
        return (root, query, criteriaBuilder) -> {
            if (enabled == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("enabled"), enabled);
        };
    }

    public static Specification<User> withRole(String roleName) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(roleName)) {
                return criteriaBuilder.conjunction();
            }
            Join<User, com.codestorykh.alpha.identity.domain.Role> roleJoin = root.join("roles");
            return criteriaBuilder.equal(roleJoin.get("name"), roleName);
        };
    }

    public static Specification<User> withGroup(String groupName) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(groupName)) {
                return criteriaBuilder.conjunction();
            }
            Join<User, com.codestorykh.alpha.identity.domain.Group> groupJoin = root.join("groups");
            return criteriaBuilder.equal(groupJoin.get("name"), groupName);
        };
    }

    public static Specification<User> withPermission(String permissionName) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(permissionName)) {
                return criteriaBuilder.conjunction();
            }
            
            Join<User, com.codestorykh.alpha.identity.domain.Role> roleJoin = root.join("roles");
            Join<com.codestorykh.alpha.identity.domain.Role, com.codestorykh.alpha.identity.domain.Permission> rolePermissionJoin = roleJoin.join("permissions");
            
            Join<User, com.codestorykh.alpha.identity.domain.Group> groupJoin = root.join("groups");
            Join<com.codestorykh.alpha.identity.domain.Group, com.codestorykh.alpha.identity.domain.Permission> groupPermissionJoin = groupJoin.join("permissions");
            
            Predicate rolePermissions = criteriaBuilder.equal(rolePermissionJoin.get("name"), permissionName);
            Predicate groupPermissions = criteriaBuilder.equal(groupPermissionJoin.get("name"), permissionName);
            
            return criteriaBuilder.or(rolePermissions, groupPermissions);
        };
    }

    public static Specification<User> withFailedLoginAttempts(Integer minAttempts, Integer maxAttempts) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (minAttempts != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("failedLoginAttempts"), minAttempts
                ));
            }
            
            if (maxAttempts != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("failedLoginAttempts"), maxAttempts
                ));
            }
            
            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<User> withDateRange(String fieldName, LocalDateTime startDate, LocalDateTime endDate) {
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