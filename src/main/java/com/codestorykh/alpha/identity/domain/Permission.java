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
@Table(name = "permissions", indexes = {
    @Index(name = "idx_permission_name", columnList = "name"),
    @Index(name = "idx_permission_resource", columnList = "resource"),
    @Index(name = "idx_permission_action", columnList = "action")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"roles", "groups"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission extends BaseEntity {

    @NotBlank
    @Size(min = 2, max = 100)
    @Column(unique = true, nullable = false)
    private String name;

    @Size(max = 255)
    private String description;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false)
    private String resource;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "is_system_permission")
    private boolean systemPermission = false;

    @ManyToMany(mappedBy = "permissions")
    @ToString.Exclude
    @JsonIgnore
    private Set<Role> roles = new HashSet<>();

    @ManyToMany(mappedBy = "permissions")
    @ToString.Exclude
    @JsonIgnore
    private Set<Group> groups = new HashSet<>();

    public String getFullPermission() {
        return resource + ":" + action;
    }
} 