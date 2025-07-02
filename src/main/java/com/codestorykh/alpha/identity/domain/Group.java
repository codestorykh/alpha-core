package com.codestorykh.alpha.identity.domain;

import com.codestorykh.alpha.common.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "groups", indexes = {
    @Index(name = "idx_group_name", columnList = "name"),
    @Index(name = "idx_group_enabled", columnList = "enabled")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"permissions", "users", "parentGroup", "childGroups"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Group extends BaseEntity {

    @NotBlank
    @Size(min = 2, max = 50)
    @Column(unique = true, nullable = false)
    private String name;

    @Size(max = 255)
    private String description;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "is_system_group")
    private boolean systemGroup = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_group_id")
    @JsonIgnore
    private Group parentGroup;

    @OneToMany(mappedBy = "parentGroup", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Group> childGroups = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "group_permissions",
        joinColumns = @JoinColumn(name = "group_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @JsonIgnore
    private Set<Permission> permissions = new HashSet<>();

    @ManyToMany(mappedBy = "groups")
    @ToString.Exclude
    @JsonIgnore
    private Set<User> users = new HashSet<>();

    public void addPermission(Permission permission) {
        this.permissions.add(permission);
    }

    public void removePermission(Permission permission) {
        this.permissions.remove(permission);
    }

    public boolean hasPermission(String permissionName) {
        return permissions.stream()
            .anyMatch(permission -> permission.getName().equals(permissionName));
    }
} 