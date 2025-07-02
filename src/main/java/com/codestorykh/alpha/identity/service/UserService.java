package com.codestorykh.alpha.identity.service;

import com.codestorykh.alpha.common.service.BaseService;
import com.codestorykh.alpha.identity.domain.User;
import com.codestorykh.alpha.identity.domain.UserStatus;
import com.codestorykh.alpha.identity.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserService extends BaseService<User, Long>, UserDetailsService {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    User createUser(UserDTO userDTO);

    User updateUser(Long id, UserDTO userDTO);

    void changePassword(Long id, String oldPassword, String newPassword);

    void resetPassword(Long id, String newPassword);

    void enableUser(Long id);

    void disableUser(Long id);

    void verifyEmail(Long id);

    void addRole(Long userId, Long roleId);

    void removeRole(Long userId, Long roleId);

    void addGroup(Long userId, Long groupId);

    void removeGroup(Long userId, Long groupId);

    List<User> findByRoleName(String roleName);

    List<User> findByGroupName(String groupName);

    Page<User> searchUsers(
            String username,
            String email,
            String firstName,
            String lastName,
            Boolean enabled,
            Boolean emailVerified,
            Pageable pageable);

    boolean hasPermission(Long userId, String resource, String action);

    boolean hasRole(Long userId, String roleName);

    boolean hasAnyRole(Long userId, List<String> roleNames);

    boolean hasAllRoles(Long userId, List<String> roleNames);

    void updateLastLogin(String username);

    void incrementFailedLoginAttempts(String username);

    void resetFailedLoginAttempts(String username);

    boolean isUserLocked(String username);

    /**
     * Get detailed account status information for a user
     * @param username the username to check
     * @return Map containing detailed account status information, or null if user not found
     */
    Map<String, Object> getUserAccountStatus(String username);

    /**
     * Load user for authentication purposes (no caching to avoid lazy loading issues)
     */
    Optional<User> findByUsernameForAuthentication(String username);
} 