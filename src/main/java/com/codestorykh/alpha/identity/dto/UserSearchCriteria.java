package com.codestorykh.alpha.identity.dto;

import com.codestorykh.alpha.identity.domain.UserStatus;
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
public class UserSearchCriteria {

    // Basic search fields
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    
    // Status filters
    private UserStatus status;
    private Boolean enabled;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    private Boolean accountNonLocked;
    private Boolean accountNonExpired;
    private Boolean credentialsNonExpired;
    
    // Date range filters
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdAfter;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdBefore;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime lastLoginAfter;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime lastLoginBefore;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime passwordChangedAfter;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime passwordChangedBefore;
    
    // Security filters
    private Integer minFailedLoginAttempts;
    private Integer maxFailedLoginAttempts;
    private Boolean isLocked;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime lockedAfter;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime lockedBefore;
    
    // Role and group filters
    private List<String> roles;
    private List<String> groups;
    private List<String> permissions;
    
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