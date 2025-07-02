package com.codestorykh.alpha.identity.service.impl;

import com.codestorykh.alpha.cache.annotation.DynamicCache;
import com.codestorykh.alpha.common.service.BaseServiceImpl;
import com.codestorykh.alpha.exception.InvalidPasswordException;
import com.codestorykh.alpha.exception.UserAlreadyExistsException;
import com.codestorykh.alpha.exception.UserNotFoundException;
import com.codestorykh.alpha.identity.domain.User;
import com.codestorykh.alpha.identity.domain.UserStatus;
import com.codestorykh.alpha.identity.dto.UserDTO;
import com.codestorykh.alpha.identity.repository.UserRepository;
import com.codestorykh.alpha.identity.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;

@Service
@Slf4j
@Transactional
public class UserServiceImpl extends BaseServiceImpl<User, Long> implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        super(userRepository);
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @DynamicCache(cacheName = "users", keyPrefix = "user", ttl = 60, timeUnit = TimeUnit.MINUTES)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsernameWithRolesAndGroups(username);
    }

    @Override
    @DynamicCache(cacheName = "users", keyPrefix = "user", ttl = 60, timeUnit = TimeUnit.MINUTES)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @DynamicCache(cacheName = "users", keyPrefix = "exists", ttl = 30, timeUnit = TimeUnit.MINUTES)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @DynamicCache(cacheName = "users", keyPrefix = "exists", ttl = 30, timeUnit = TimeUnit.MINUTES)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User createUser(UserDTO userDTO) {
        if (existsByUsername(userDTO.getUsername())) {
            throw new UserAlreadyExistsException("username", userDTO.getUsername());
        }
        if (existsByEmail(userDTO.getEmail())) {
            throw new UserAlreadyExistsException("email", userDTO.getEmail());
        }

        User user = User.builder()
                .username(userDTO.getUsername())
                .email(userDTO.getEmail())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .firstName(userDTO.getFirstName())
                .lastName(userDTO.getLastName())
                .phoneNumber(userDTO.getPhoneNumber())
                .status(userDTO.getStatus() != null ? userDTO.getStatus() : UserStatus.ACTIVE)
                .enabled(userDTO.isEnabled())
                .emailVerified(userDTO.isEmailVerified())
                .phoneVerified(userDTO.isPhoneVerified())
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .failedLoginAttempts(0)
                .lockedUntil(null)
                .build();

        return save(user);
    }

    @Override
    public User updateUser(Long id, UserDTO userDTO) {
        User user = findById(id)
                .orElseThrow(() -> new UserNotFoundException("id", id.toString()));

        if (userDTO.getFirstName() != null) {
            user.setFirstName(userDTO.getFirstName());
        }
        if (userDTO.getLastName() != null) {
            user.setLastName(userDTO.getLastName());
        }
        if (userDTO.getEmail() != null && !userDTO.getEmail().equals(user.getEmail())) {
            if (existsByEmail(userDTO.getEmail())) {
                throw new UserAlreadyExistsException("email", userDTO.getEmail());
            }
            user.setEmail(userDTO.getEmail());
        }
        if (userDTO.getPhoneNumber() != null) {
            user.setPhoneNumber(userDTO.getPhoneNumber());
        }
        if (userDTO.getStatus() != null) {
            user.setStatus(userDTO.getStatus());
        }

        return save(user);
    }

    @Override
    public void changePassword(Long id, String oldPassword, String newPassword) {
        User user = findById(id)
                .orElseThrow(() -> new UserNotFoundException("id", id.toString()));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new InvalidPasswordException("Old password does not match");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        save(user);
    }

    @Override
    public void resetPassword(Long id, String newPassword) {
        User user = findById(id)
                .orElseThrow(() -> new UserNotFoundException("id", id.toString()));

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.resetFailedLoginAttempts();
        save(user);
    }

    @Override
    public void enableUser(Long id) {
        User user = findById(id)
                .orElseThrow(() -> new UserNotFoundException("id", id.toString()));
        user.setEnabled(true);
        save(user);
    }

    @Override
    public void disableUser(Long id) {
        User user = findById(id)
                .orElseThrow(() -> new UserNotFoundException("id", id.toString()));
        user.setEnabled(false);
        save(user);
    }

    @Override
    public void verifyEmail(Long id) {
        User user = findById(id)
                .orElseThrow(() -> new UserNotFoundException("id", id.toString()));
        user.setEmailVerified(true);
        save(user);
    }

    @Override
    public void addRole(Long userId, Long roleId) {
        // Implementation would require RoleService
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void removeRole(Long userId, Long roleId) {
        // Implementation would require RoleService
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void addGroup(Long userId, Long groupId) {
        // Implementation would require GroupService
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void removeGroup(Long userId, Long groupId) {
        // Implementation would require GroupService
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<User> findByRoleName(String roleName) {
        return userRepository.findByRoleName(roleName);
    }

    @Override
    public List<User> findByGroupName(String groupName) {
        return userRepository.findByGroupName(groupName);
    }

    @Override
    public Page<User> searchUsers(String username, String email, String firstName, String lastName,
                                Boolean enabled, Boolean emailVerified, Pageable pageable) {
        // For now, use a simple search with the first non-null parameter
        String searchTerm = username != null ? username : 
                           email != null ? email : 
                           firstName != null ? firstName : 
                           lastName != null ? lastName : "";
        
        return userRepository.searchUsers(searchTerm, pageable);
    }

    @Override
    public boolean hasPermission(Long userId, String resource, String action) {
        // Implementation would require permission checking logic
        return false;
    }

    @Override
    public boolean hasRole(Long userId, String roleName) {
        // Implementation would require role checking logic
        return false;
    }

    @Override
    public boolean hasAnyRole(Long userId, List<String> roleNames) {
        // Implementation would require role checking logic
        return false;
    }

    @Override
    public boolean hasAllRoles(Long userId, List<String> roleNames) {
        // Implementation would require role checking logic
        return false;
    }

    @Override
    public void updateLastLogin(String username) {
        findByUsername(username).ifPresent(user -> {
            user.updateLastLogin();
            save(user);
        });
    }

    @Override
    public void incrementFailedLoginAttempts(String username) {
        findByUsername(username).ifPresent(user -> {
            user.incrementFailedLoginAttempts();
            save(user);
        });
    }

    @Override
    public void resetFailedLoginAttempts(String username) {
        findByUsername(username).ifPresent(user -> {
            user.resetFailedLoginAttempts();
            save(user);
        });
    }

    @Override
    public boolean isUserLocked(String username) {
        return findByUsername(username)
                .map(User::isAccountNonLocked)
                .map(locked -> !locked)
                .orElse(false);
    }

    @Override
    public Map<String, Object> getUserAccountStatus(String username) {
        return findByUsername(username)
                .map(user -> {
                    Map<String, Object> status = new HashMap<>();
                    status.put("username", user.getUsername());
                    status.put("enabled", user.isEnabled());
                    status.put("status", user.getStatus());
                    status.put("accountNonExpired", user.isAccountNonExpired());
                    status.put("accountNonLocked", user.isAccountNonLocked());
                    status.put("credentialsNonExpired", user.isCredentialsNonExpired());
                    status.put("emailVerified", user.isEmailVerified());
                    status.put("failedLoginAttempts", user.getFailedLoginAttempts());
                    status.put("lockedUntil", user.getLockedUntil());
                    status.put("lastLogin", user.getLastLogin());
                    status.put("passwordChangedAt", user.getPasswordChangedAt());
                    
                    // Overall account status
                    boolean isActive = user.isEnabled() && 
                                     user.getStatus() == UserStatus.ACTIVE &&
                                     user.isAccountNonExpired() &&
                                     user.isAccountNonLocked() &&
                                     user.isCredentialsNonExpired();
                    status.put("isActive", isActive);
                    
                    return status;
                })
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);
        try {
            User user = findByUsernameForAuthentication(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
            
            log.debug("User loaded successfully: {} with {} roles and {} groups", 
                     username, user.getRoles().size(), user.getGroups().size());
            
            return user;
        } catch (Exception e) {
            log.error("Error loading user by username: {}", username, e);
            throw new UsernameNotFoundException("Error loading user with username: " + username, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsernameForAuthentication(String username) {
        return userRepository.findByUsernameWithRolesAndGroups(username);
    }
} 