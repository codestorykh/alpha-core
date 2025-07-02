package com.codestorykh.alpha.identity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionSearchCriteria {

    // Basic search fields
    private String name;
    private String description;
    private String resource;
    private String action;
    
    // Status filters
    private Boolean enabled;
    private Boolean systemPermission;
    
    // Date range filters
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdAfter;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdBefore;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime updatedAfter;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime updatedBefore;
    
    // Role filters
    private List<String> roles;
    private Boolean hasRoles;
    private Integer minRoleCount;
    private Integer maxRoleCount;
    
    // Group filters
    private List<String> groups;
    private Boolean hasGroups;
    private Integer minGroupCount;
    private Integer maxGroupCount;
    
    // User filters (through roles and groups)
    private List<String> users;
    private Boolean hasUsers;
    private Integer minUserCount;
    private Integer maxUserCount;
    
    // Search mode
    private SearchMode searchMode = SearchMode.AND;
    
    // Sort options
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
    
    public enum SearchMode {
        AND,    // All criteria must match
        OR      // Any criteria can match
    }
} 