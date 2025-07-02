package com.codestorykh.alpha.identity.repository;

import com.codestorykh.alpha.identity.domain.Permission;
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
public interface PermissionRepository extends BaseRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {
    Optional<Permission> findByName(String name);
    
    boolean existsByName(String name);
    
    List<Permission> findBySystemPermissionTrue();
    
    List<Permission> findByResource(String resource);
    
    List<Permission> findByAction(String action);
    
    @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r.name = :roleName")
    List<Permission> findByRole(@Param("roleName") String roleName);
    
    @Query("SELECT p FROM Permission p JOIN p.groups g WHERE g.name = :groupName")
    List<Permission> findByGroup(@Param("groupName") String groupName);
    
    @Query("SELECT DISTINCT p FROM Permission p " +
           "JOIN p.roles r JOIN r.users u WHERE u.username = :username " +
           "UNION " +
           "SELECT DISTINCT p FROM Permission p " +
           "JOIN p.groups g JOIN g.users u WHERE u.username = :username")
    List<Permission> findByUser(@Param("username") String username);
    
    @Query("SELECT p FROM Permission p WHERE " +
           "(:searchTerm IS NULL OR p.name LIKE %:searchTerm% OR p.description LIKE %:searchTerm%) AND " +
           "(:enabled IS NULL OR p.enabled = :enabled) AND " +
           "(:systemPermission IS NULL OR p.systemPermission = :systemPermission) AND " +
           "(:resource IS NULL OR p.resource = :resource) AND " +
           "(:action IS NULL OR p.action = :action)")
    Page<Permission> searchPermissions(@Param("searchTerm") String searchTerm,
                                      @Param("enabled") Boolean enabled,
                                      @Param("systemPermission") Boolean systemPermission,
                                      @Param("resource") String resource,
                                      @Param("action") String action,
                                      Pageable pageable);
} 