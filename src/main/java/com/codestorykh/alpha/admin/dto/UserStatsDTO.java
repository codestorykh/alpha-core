package com.codestorykh.alpha.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsDTO {
    private LocalDateTime timestamp;
    private long totalUsers;
    private long activeUsers;
    private long inactiveUsers;
    private long lockedUsers;
    private long usersWithVerifiedEmail;
    private long usersWithVerifiedPhone;
    private long newUsersToday;
    private long newUsersThisWeek;
    private long newUsersThisMonth;
    private long failedLoginAttempts;
    private long successfulLogins;
    private double averageLoginTime;
} 