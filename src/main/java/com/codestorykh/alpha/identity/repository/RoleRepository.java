package com.codestorykh.alpha.identity.repository;

import com.codestorykh.alpha.identity.domain.Role;
import com.codestorykh.alpha.common.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends BaseRepository<Role, Long>, JpaSpecificationExecutor<Role> {
    Optional<Role> findByName(String name);
    
    boolean existsByName(String name);
    
    List<Role> findBySystemRoleTrue();
    
    @Query("SELECT r FROM Role r JOIN r.permissions p WHERE p.name = :permissionName")
    List<Role> findByPermissionName(@Param("permissionName") String permissionName);
    
    @Query("SELECT r FROM Role r JOIN r.users u WHERE u.username = :username")
    List<Role> findByUser(@Param("username") String username);
    
    @Query("SELECT r FROM Role r WHERE " +
           "(:searchTerm IS NULL OR r.name LIKE %:searchTerm% OR r.description LIKE %:searchTerm%) AND " +
           "(:enabled IS NULL OR r.enabled = :enabled) AND " +
           "(:systemRole IS NULL OR r.systemRole = :systemRole)")
    Page<Role> searchRoles(@Param("searchTerm") String searchTerm, 
                          @Param("enabled") Boolean enabled, 
                          @Param("systemRole") Boolean systemRole, 
                          Pageable pageable);
} 