package com.codestorykh.alpha.identity.specification;

import com.codestorykh.alpha.identity.domain.Group;
import com.codestorykh.alpha.identity.dto.GroupSearchCriteria;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GroupSpecification {

    public static Specification<Group> withSearchCriteria(GroupSearchCriteria criteria) {
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

            if (criteria.getSystemGroup() != null) {
                predicates.add(criteriaBuilder.equal(root.get("systemGroup"), criteria.getSystemGroup()));
            }

            // Hierarchical filters
            if (criteria.getParentGroupId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("parentGroup").get("id"), criteria.getParentGroupId()));
            }

            if (StringUtils.hasText(criteria.getParentGroupName())) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("parentGroup").get("name")),
                    "%" + criteria.getParentGroupName().toLowerCase() + "%"
                ));
            }

            if (criteria.getHasParent() != null) {
                if (criteria.getHasParent()) {
                    predicates.add(criteriaBuilder.isNotNull(root.get("parentGroup")));
                } else {
                    predicates.add(criteriaBuilder.isNull(root.get("parentGroup")));
                }
            }

            if (criteria.getHasChildren() != null) {
                if (criteria.getHasChildren()) {
                    predicates.add(criteriaBuilder.isNotEmpty(root.get("childGroups")));
                } else {
                    predicates.add(criteriaBuilder.isEmpty(root.get("childGroups")));
                }
            }

            if (criteria.getMinChildCount() != null || criteria.getMaxChildCount() != null) {
                Subquery<Long> childCountSubquery = query.subquery(Long.class);
                Root<Group> groupSubquery = childCountSubquery.from(Group.class);
                Join<Group, Group> childJoin = groupSubquery.join("childGroups");
                
                childCountSubquery.select(criteriaBuilder.count(childJoin))
                    .where(criteriaBuilder.equal(groupSubquery.get("id"), root.get("id")));
                
                if (criteria.getMinChildCount() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(childCountSubquery, criteria.getMinChildCount().longValue()));
                }
                
                if (criteria.getMaxChildCount() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(childCountSubquery, criteria.getMaxChildCount().longValue()));
                }
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
                Join<Group, com.codestorykh.alpha.identity.domain.Permission> permissionJoin = root.join("permissions");
                predicates.add(permissionJoin.get("name").in(criteria.getPermissions()));
            }

            if (criteria.getResources() != null && !criteria.getResources().isEmpty()) {
                Join<Group, com.codestorykh.alpha.identity.domain.Permission> permissionJoin = root.join("permissions");
                predicates.add(permissionJoin.get("resource").in(criteria.getResources()));
            }

            if (criteria.getActions() != null && !criteria.getActions().isEmpty()) {
                Join<Group, com.codestorykh.alpha.identity.domain.Permission> permissionJoin = root.join("permissions");
                predicates.add(permissionJoin.get("action").in(criteria.getActions()));
            }

            // User filters
            if (criteria.getUsers() != null && !criteria.getUsers().isEmpty()) {
                Join<Group, com.codestorykh.alpha.identity.domain.User> userJoin = root.join("users");
                predicates.add(userJoin.get("username").in(criteria.getUsers()));
            }

            if (criteria.getHasUsers() != null) {
                if (criteria.getHasUsers()) {
                    // Group has users
                    Join<Group, com.codestorykh.alpha.identity.domain.User> userJoin = root.join("users");
                    predicates.add(criteriaBuilder.isNotNull(userJoin.get("id")));
                } else {
                    // Group has no users
                    predicates.add(criteriaBuilder.isEmpty(root.get("users")));
                }
            }

            if (criteria.getMinUserCount() != null || criteria.getMaxUserCount() != null) {
                Subquery<Long> userCountSubquery = query.subquery(Long.class);
                Root<Group> groupSubquery = userCountSubquery.from(Group.class);
                Join<Group, com.codestorykh.alpha.identity.domain.User> userJoin = groupSubquery.join("users");
                
                userCountSubquery.select(criteriaBuilder.count(userJoin))
                    .where(criteriaBuilder.equal(groupSubquery.get("id"), root.get("id")));
                
                if (criteria.getMinUserCount() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(userCountSubquery, criteria.getMinUserCount().longValue()));
                }
                
                if (criteria.getMaxUserCount() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(userCountSubquery, criteria.getMaxUserCount().longValue()));
                }
            }

            // Apply search mode (AND or OR)
            if (criteria.getSearchMode() == GroupSearchCriteria.SearchMode.OR && predicates.size() > 1) {
                return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
            } else {
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };
    }

    public static Specification<Group> withName(String name) {
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

    public static Specification<Group> withDescription(String description) {
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

    public static Specification<Group> withEnabled(Boolean enabled) {
        return (root, query, criteriaBuilder) -> {
            if (enabled == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("enabled"), enabled);
        };
    }

    public static Specification<Group> withSystemGroup(Boolean systemGroup) {
        return (root, query, criteriaBuilder) -> {
            if (systemGroup == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("systemGroup"), systemGroup);
        };
    }

    public static Specification<Group> withParentGroup(Long parentGroupId) {
        return (root, query, criteriaBuilder) -> {
            if (parentGroupId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("parentGroup").get("id"), parentGroupId);
        };
    }

    public static Specification<Group> withParentGroupName(String parentGroupName) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(parentGroupName)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("parentGroup").get("name")),
                "%" + parentGroupName.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Group> hasParent() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isNotNull(root.get("parentGroup"));
    }

    public static Specification<Group> hasNoParent() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("parentGroup"));
    }

    public static Specification<Group> hasChildren() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isNotEmpty(root.get("childGroups"));
    }

    public static Specification<Group> hasNoChildren() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isEmpty(root.get("childGroups"));
    }

    public static Specification<Group> withPermission(String permissionName) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(permissionName)) {
                return criteriaBuilder.conjunction();
            }
            Join<Group, com.codestorykh.alpha.identity.domain.Permission> permissionJoin = root.join("permissions");
            return criteriaBuilder.equal(permissionJoin.get("name"), permissionName);
        };
    }

    public static Specification<Group> withResource(String resource) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(resource)) {
                return criteriaBuilder.conjunction();
            }
            Join<Group, com.codestorykh.alpha.identity.domain.Permission> permissionJoin = root.join("permissions");
            return criteriaBuilder.equal(permissionJoin.get("resource"), resource);
        };
    }

    public static Specification<Group> withAction(String action) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(action)) {
                return criteriaBuilder.conjunction();
            }
            Join<Group, com.codestorykh.alpha.identity.domain.Permission> permissionJoin = root.join("permissions");
            return criteriaBuilder.equal(permissionJoin.get("action"), action);
        };
    }

    public static Specification<Group> withUser(String username) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(username)) {
                return criteriaBuilder.conjunction();
            }
            Join<Group, com.codestorykh.alpha.identity.domain.User> userJoin = root.join("users");
            return criteriaBuilder.equal(userJoin.get("username"), username);
        };
    }

    public static Specification<Group> withDateRange(String fieldName, LocalDateTime startDate, LocalDateTime endDate) {
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