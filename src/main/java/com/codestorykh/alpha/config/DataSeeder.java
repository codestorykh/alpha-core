package com.codestorykh.alpha.config;

import com.codestorykh.alpha.identity.domain.*;
import com.codestorykh.alpha.identity.repository.*;
import com.codestorykh.alpha.oauth2.domain.GrantType;
import com.codestorykh.alpha.oauth2.domain.OAuth2Client;
import com.codestorykh.alpha.oauth2.domain.TokenEndpointAuthMethod;
import com.codestorykh.alpha.oauth2.repository.OAuth2ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile({"dev", "test"})
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final GroupRepository groupRepository;
    private final PasswordEncoder passwordEncoder;
    private final PermissionRepository permissionRepository;
    private final OAuth2ClientRepository oauth2ClientRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("=== DataSeeder starting with profile: {} ===", Arrays.toString(args));
        
        try {
            // Check if data already exists
            long userCount = userRepository.count();
            long roleCount = roleRepository.count();
            long permissionCount = permissionRepository.count();
            long groupCount = groupRepository.count();
            long clientCount = oauth2ClientRepository.count();
            
            log.info("Current database state - Users: {}, Roles: {}, Permissions: {}, Groups: {}, OAuth2Clients: {}", 
                    userCount, roleCount, permissionCount, groupCount, clientCount);
            
            if (userCount > 0) {
                log.info("Data already exists, skipping seeding");
                return;
            }
            
            // Create permissions first
            createPermissions();
            
            // Create roles
            createRoles();
            
            // Create groups
            createGroups();
            
            // Create users
            createUsers();
            
            // Create OAuth2 clients
            createOAuth2Clients();
            
            // Verify data was created
            userCount = userRepository.count();
            roleCount = roleRepository.count();
            permissionCount = permissionRepository.count();
            groupCount = groupRepository.count();
            clientCount = oauth2ClientRepository.count();
            
            log.info("Final database state - Users: {}, Roles: {}, Permissions: {}, Groups: {}, OAuth2Clients: {}", 
                    userCount, roleCount, permissionCount, groupCount, clientCount);
            
            log.info("=== Data seeding completed successfully! ===");
            
        } catch (Exception e) {
            log.error("Error during data seeding", e);
            throw e;
        }
    }

    private void createPermissions() {
        log.info("Creating permissions...");
        
        List<Permission> permissions = Arrays.asList(
            // User Management Permissions
            Permission.builder()
                .name("user:read")
                .description("Read user information")
                .resource("user")
                .action("read")
                .build(),
            Permission.builder()
                .name("user:write")
                .description("Create and update users")
                .resource("user")
                .action("write")
                .build(),
            Permission.builder()
                .name("user:delete")
                .description("Delete users")
                .resource("user")
                .action("delete")
                .build(),
            Permission.builder()
                .name("user:admin")
                .description("Full user administration")
                .resource("user")
                .action("admin")
                .build(),
            
            // Role Management Permissions
            Permission.builder()
                .name("role:read")
                .description("Read role information")
                .resource("role")
                .action("read")
                .build(),
            Permission.builder()
                .name("role:write")
                .description("Create and update roles")
                .resource("role")
                .action("write")
                .build(),
            Permission.builder()
                .name("role:delete")
                .description("Delete roles")
                .resource("role")
                .action("delete")
                .build(),
            Permission.builder()
                .name("role:admin")
                .description("Full role administration")
                .resource("role")
                .action("admin")
                .build(),
            
            // Group Management Permissions
            Permission.builder()
                .name("group:read")
                .description("Read group information")
                .resource("group")
                .action("read")
                .build(),
            Permission.builder()
                .name("group:write")
                .description("Create and update groups")
                .resource("group")
                .action("write")
                .build(),
            Permission.builder()
                .name("group:delete")
                .description("Delete groups")
                .resource("group")
                .action("delete")
                .build(),
            Permission.builder()
                .name("group:admin")
                .description("Full group administration")
                .resource("group")
                .action("admin")
                .build(),
            
            // OAuth2 Client Management Permissions
            Permission.builder()
                .name("oauth2:client:read")
                .description("Read OAuth2 client information")
                .resource("oauth2:client")
                .action("read")
                .build(),
            Permission.builder()
                .name("oauth2:client:write")
                .description("Create and update OAuth2 clients")
                .resource("oauth2:client")
                .action("write")
                .build(),
            Permission.builder()
                .name("oauth2:client:delete")
                .description("Delete OAuth2 clients")
                .resource("oauth2:client")
                .action("delete")
                .build(),
            Permission.builder()
                .name("oauth2:client:admin")
                .description("Full OAuth2 client administration")
                .resource("oauth2:client")
                .action("admin")
                .build(),
            
            // System Administration Permissions
            Permission.builder()
                .name("system:read")
                .description("Read system information")
                .resource("system")
                .action("read")
                .build(),
            Permission.builder()
                .name("system:write")
                .description("Modify system settings")
                .resource("system")
                .action("write")
                .build(),
            Permission.builder()
                .name("system:admin")
                .description("Full system administration")
                .resource("system")
                .action("admin")
                .build(),
            
            // Cache Management Permissions
            Permission.builder()
                .name("cache:read")
                .description("Read cache statistics")
                .resource("cache")
                .action("read")
                .build(),
            Permission.builder()
                .name("cache:write")
                .description("Modify cache entries")
                .resource("cache")
                .action("write")
                .build(),
            Permission.builder()
                .name("cache:admin")
                .description("Full cache administration")
                .resource("cache")
                .action("admin")
                .build()
        );
        
        log.info("Saving {} permissions to database...", permissions.size());
        List<Permission> savedPermissions = permissionRepository.saveAll(permissions);
        log.info("Successfully created {} permissions", savedPermissions.size());
        
        // Log some sample permissions
        savedPermissions.stream()
            .limit(5)
            .forEach(p -> log.debug("Created permission: {} - {}", p.getName(), p.getDescription()));
    }

    private void createRoles() {
        log.info("Creating roles...");
        
        // Get all permissions
        List<Permission> allPermissions = permissionRepository.findAll();
        
        // Create roles with different permission sets
        List<Role> roles = Arrays.asList(
            // Super Admin Role
            Role.builder()
                .name("SUPER_ADMIN")
                .description("Super Administrator with all permissions")
                .permissions(new HashSet<>(allPermissions))
                .build(),
            
            // Admin Role
            Role.builder()
                .name("ADMIN")
                .description("Administrator with most permissions")
                .permissions(getAdminPermissions(allPermissions))
                .build(),
            
            // User Manager Role
            Role.builder()
                .name("USER_MANAGER")
                .description("User management specialist")
                .permissions(getUserManagerPermissions(allPermissions))
                .build(),
            
            // OAuth2 Manager Role
            Role.builder()
                .name("OAUTH2_MANAGER")
                .description("OAuth2 client management specialist")
                .permissions(getOAuth2ManagerPermissions(allPermissions))
                .build(),
            
            // System Monitor Role
            Role.builder()
                .name("SYSTEM_MONITOR")
                .description("System monitoring and read access")
                .permissions(getSystemMonitorPermissions(allPermissions))
                .build(),
            
            // Regular User Role
            Role.builder()
                .name("USER")
                .description("Regular user with basic permissions")
                .permissions(getUserPermissions(allPermissions))
                .build(),
            
            // Guest Role
            Role.builder()
                .name("GUEST")
                .description("Guest user with minimal permissions")
                .permissions(new HashSet<>())
                .build()
        );
        
        roleRepository.saveAll(roles);
        log.info("Created {} roles", roles.size());
    }

    private Set<Permission> getAdminPermissions(List<Permission> allPermissions) {
        return allPermissions.stream()
            .filter(p -> !p.getName().equals("system:admin"))
            .collect(java.util.stream.Collectors.toSet());
    }

    private Set<Permission> getUserManagerPermissions(List<Permission> allPermissions) {
        return allPermissions.stream()
            .filter(p -> p.getResource().equals("user") || p.getResource().equals("role") || p.getResource().equals("group"))
            .collect(java.util.stream.Collectors.toSet());
    }

    private Set<Permission> getOAuth2ManagerPermissions(List<Permission> allPermissions) {
        return allPermissions.stream()
            .filter(p -> p.getResource().equals("oauth2:client"))
            .collect(java.util.stream.Collectors.toSet());
    }

    private Set<Permission> getSystemMonitorPermissions(List<Permission> allPermissions) {
        return allPermissions.stream()
            .filter(p -> p.getAction().equals("read"))
            .collect(java.util.stream.Collectors.toSet());
    }

    private Set<Permission> getUserPermissions(List<Permission> allPermissions) {
        return allPermissions.stream()
            .filter(p -> p.getName().equals("user:read") || p.getName().equals("system:read"))
            .collect(java.util.stream.Collectors.toSet());
    }

    private void createGroups() {
        log.info("Creating groups...");
        
        List<Group> groups = Arrays.asList(
            Group.builder()
                .name("System Administrators")
                .description("Group for system administrators")
                .build(),
            Group.builder()
                .name("User Management Team")
                .description("Group for user management specialists")
                .build(),
            Group.builder()
                .name("OAuth2 Management Team")
                .description("Group for OAuth2 client management")
                .build(),
            Group.builder()
                .name("System Monitoring Team")
                .description("Group for system monitoring")
                .build(),
            Group.builder()
                .name("Regular Users")
                .description("Group for regular users")
                .build(),
            Group.builder()
                .name("Guest Users")
                .description("Group for guest users")
                .build()
        );
        
        groupRepository.saveAll(groups);
        log.info("Created {} groups", groups.size());
    }

    private void createUsers() {
        log.info("Creating users...");
        
        // Get roles and groups
        Role superAdminRole = roleRepository.findByName("SUPER_ADMIN").orElseThrow();
        Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
        Role userManagerRole = roleRepository.findByName("USER_MANAGER").orElseThrow();
        Role oauth2ManagerRole = roleRepository.findByName("OAUTH2_MANAGER").orElseThrow();
        Role systemMonitorRole = roleRepository.findByName("SYSTEM_MONITOR").orElseThrow();
        Role userRole = roleRepository.findByName("USER").orElseThrow();
        Role guestRole = roleRepository.findByName("GUEST").orElseThrow();
        
        Group sysAdminGroup = groupRepository.findByName("System Administrators").orElseThrow();
        Group userMgmtGroup = groupRepository.findByName("User Management Team").orElseThrow();
        Group oauth2Group = groupRepository.findByName("OAuth2 Management Team").orElseThrow();
        Group monitoringGroup = groupRepository.findByName("System Monitoring Team").orElseThrow();
        Group regularUsersGroup = groupRepository.findByName("Regular Users").orElseThrow();
        Group guestUsersGroup = groupRepository.findByName("Guest Users").orElseThrow();
        
        List<User> users = Arrays.asList(
            // Super Admin User
            User.builder()
                .username("superadmin")
                .email("superadmin@codestorykh.com")
                .password(passwordEncoder.encode("SuperAdmin123!"))
                .firstName("Super")
                .lastName("Administrator")
                .phoneNumber("+855123456789")
                .status(UserStatus.ACTIVE)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .emailVerified(true)
                .phoneVerified(true)
                .roles(Set.of(superAdminRole))
                .groups(Set.of(sysAdminGroup))
                .build(),
            
            // Admin User
            User.builder()
                .username("admin")
                .email("admin@codestorykh.com")
                .password(passwordEncoder.encode("Admin123!"))
                .firstName("System")
                .lastName("Admin")
                .phoneNumber("+855123456790")
                .status(UserStatus.ACTIVE)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .emailVerified(true)
                .phoneVerified(true)
                .roles(Set.of(adminRole))
                .groups(Set.of(sysAdminGroup))
                .build(),
            
            // User Manager
            User.builder()
                .username("usermanager")
                .email("usermanager@codestorykh.com")
                .password(passwordEncoder.encode("UserMgr123!"))
                .firstName("User")
                .lastName("Manager")
                .phoneNumber("+855123456791")
                .status(UserStatus.ACTIVE)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .emailVerified(true)
                .phoneVerified(true)
                .roles(Set.of(userManagerRole))
                .groups(Set.of(userMgmtGroup))
                .build(),
            
            // OAuth2 Manager
            User.builder()
                .username("oauth2manager")
                .email("oauth2manager@codestorykh.com")
                .password(passwordEncoder.encode("OAuth2Mgr123!"))
                .firstName("OAuth2")
                .lastName("Manager")
                .phoneNumber("+855123456792")
                .status(UserStatus.ACTIVE)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .emailVerified(true)
                .phoneVerified(true)
                .roles(Set.of(oauth2ManagerRole))
                .groups(Set.of(oauth2Group))
                .build(),
            
            // System Monitor
            User.builder()
                .username("systemmonitor")
                .email("systemmonitor@codestorykh.com")
                .password(passwordEncoder.encode("Monitor123!"))
                .firstName("System")
                .lastName("Monitor")
                .phoneNumber("+855123456793")
                .status(UserStatus.ACTIVE)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .emailVerified(true)
                .phoneVerified(true)
                .roles(Set.of(systemMonitorRole))
                .groups(Set.of(monitoringGroup))
                .build(),
            
            // Regular User
            User.builder()
                .username("user")
                .email("user@codestorykh.com")
                .password(passwordEncoder.encode("User123!"))
                .firstName("Regular")
                .lastName("User")
                .phoneNumber("+855123456794")
                .status(UserStatus.ACTIVE)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .emailVerified(true)
                .phoneVerified(false)
                .roles(Set.of(userRole))
                .groups(Set.of(regularUsersGroup))
                .build(),
            
            // Guest User
            User.builder()
                .username("guest")
                .email("guest@codestorykh.com")
                .password(passwordEncoder.encode("Guest123!"))
                .firstName("Guest")
                .lastName("User")
                .phoneNumber("+855123456795")
                .status(UserStatus.ACTIVE)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .emailVerified(false)
                .phoneVerified(false)
                .roles(Set.of(guestRole))
                .groups(Set.of(guestUsersGroup))
                .build(),
            
            // Test User 1
            User.builder()
                .username("testuser1")
                .email("testuser1@codestorykh.com")
                .password(passwordEncoder.encode("Test123!"))
                .firstName("Test")
                .lastName("User1")
                .phoneNumber("+855123456796")
                .status(UserStatus.ACTIVE)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .emailVerified(true)
                .phoneVerified(true)
                .roles(Set.of(userRole))
                .groups(Set.of(regularUsersGroup))
                .build(),
            
            // Test User 2
            User.builder()
                .username("testuser2")
                .email("testuser2@codestorykh.com")
                .password(passwordEncoder.encode("Test123!"))
                .firstName("Test")
                .lastName("User2")
                .phoneNumber("+855123456797")
                .status(UserStatus.INACTIVE)
                .enabled(false)
                .accountNonExpired(false)
                .accountNonLocked(false)
                .credentialsNonExpired(false)
                .emailVerified(false)
                .phoneVerified(false)
                .roles(Set.of(userRole))
                .groups(Set.of(regularUsersGroup))
                .build(),
            
            // Locked User
            User.builder()
                .username("lockeduser")
                .email("lockeduser@codestorykh.com")
                .password(passwordEncoder.encode("Locked123!"))
                .firstName("Locked")
                .lastName("User")
                .phoneNumber("+855123456798")
                .status(UserStatus.SUSPENDED)
                .enabled(false)
                .accountNonExpired(false)
                .accountNonLocked(false)
                .credentialsNonExpired(false)
                .emailVerified(true)
                .phoneVerified(true)
                .roles(Set.of(userRole))
                .groups(Set.of(regularUsersGroup))
                .build()
        );
        
        userRepository.saveAll(users);
        log.info("Created {} users", users.size());
    }

    private void createOAuth2Clients() {
        log.info("Creating OAuth2 clients...");
        
        List<OAuth2Client> clients = Arrays.asList(
            // Web Application Client
            OAuth2Client.builder()
                .clientId("web-client")
                .clientSecret(passwordEncoder.encode("web-client-secret"))
                .clientName("Web Application Client")
                .description("Client for web applications")
                .redirectUris(Set.of("http://localhost:3000/login/oauth2/code/web-client", "http://localhost:3000/authorized"))
                .scopes(Set.of("read", "write", "client.read", "client.write"))
                .grantTypes(Set.of(GrantType.AUTHORIZATION_CODE, GrantType.REFRESH_TOKEN))
                .tokenEndpointAuthMethod(TokenEndpointAuthMethod.CLIENT_SECRET_BASIC)
                .requireProofKey(true)
                .requireAuthorizationConsent(true)
                .accessTokenValiditySeconds(3600)
                .refreshTokenValiditySeconds(86400)
                .enabled(true)
                .build(),
            
            // Mobile Application Client
            OAuth2Client.builder()
                .clientId("mobile-client")
                .clientSecret(passwordEncoder.encode("mobile-client-secret"))
                .clientName("Mobile Application Client")
                .description("Client for mobile applications")
                .redirectUris(Set.of("com.alpha.mobile://oauth2/callback"))
                .scopes(Set.of("read", "write"))
                .grantTypes(Set.of(GrantType.AUTHORIZATION_CODE, GrantType.REFRESH_TOKEN))
                .tokenEndpointAuthMethod(TokenEndpointAuthMethod.CLIENT_SECRET_POST)
                .requireProofKey(true)
                .requireAuthorizationConsent(false)
                .accessTokenValiditySeconds(7200)
                .refreshTokenValiditySeconds(2592000)
                .enabled(true)
                .build(),
            
            // Service Client
            OAuth2Client.builder()
                .clientId("service-client")
                .clientSecret(passwordEncoder.encode("service-client-secret"))
                .clientName("Service Client")
                .description("Client for service-to-service communication")
                .redirectUris(Set.of())
                .scopes(Set.of("read", "write", "client.read", "client.write"))
                .grantTypes(Set.of(GrantType.CLIENT_CREDENTIALS))
                .tokenEndpointAuthMethod(TokenEndpointAuthMethod.CLIENT_SECRET_BASIC)
                .requireProofKey(false)
                .requireAuthorizationConsent(false)
                .accessTokenValiditySeconds(1800)
                .refreshTokenValiditySeconds(3600)
                .enabled(true)
                .build(),
            
            // Test Client
            OAuth2Client.builder()
                .clientId("test-client")
                .clientSecret(passwordEncoder.encode("test-client-secret"))
                .clientName("Test Client")
                .description("Client for testing purposes")
                .redirectUris(Set.of("http://localhost:8080/test/callback"))
                .scopes(Set.of("read", "write"))
                .grantTypes(Set.of(GrantType.AUTHORIZATION_CODE, GrantType.REFRESH_TOKEN))
                .tokenEndpointAuthMethod(TokenEndpointAuthMethod.CLIENT_SECRET_BASIC)
                .requireProofKey(false)
                .requireAuthorizationConsent(true)
                .accessTokenValiditySeconds(3600)
                .refreshTokenValiditySeconds(86400)
                .enabled(true)
                .build(),
            
            // Disabled Client
            OAuth2Client.builder()
                .clientId("disabled-client")
                .clientSecret(passwordEncoder.encode("disabled-client-secret"))
                .clientName("Disabled Client")
                .description("Disabled client for testing")
                .redirectUris(Set.of("http://localhost:8080/disabled/callback"))
                .scopes(Set.of("read"))
                .grantTypes(Set.of(GrantType.AUTHORIZATION_CODE))
                .tokenEndpointAuthMethod(TokenEndpointAuthMethod.CLIENT_SECRET_BASIC)
                .requireProofKey(false)
                .requireAuthorizationConsent(true)
                .accessTokenValiditySeconds(3600)
                .refreshTokenValiditySeconds(86400)
                .enabled(false)
                .build()
        );
        
        oauth2ClientRepository.saveAll(clients);
        log.info("Created {} OAuth2 clients", clients.size());
    }
} 