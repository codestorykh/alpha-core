package com.codestorykh.alpha.identity.repository;

import com.codestorykh.alpha.identity.domain.Group;
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
public interface GroupRepository extends BaseRepository<Group, Long>, JpaSpecificationExecutor<Group> {
    Optional<Group> findByName(String name);
    
    boolean existsByName(String name);
    
    List<Group> findBySystemGroupTrue();
    
    List<Group> findByParentGroupIsNull();
    
    List<Group> findByParentGroupId(Long parentId);
    
    @Query("SELECT g FROM Group g JOIN g.permissions p WHERE p.name = :permissionName")
    List<Group> findByPermissionName(@Param("permissionName") String permissionName);
    
    @Query("SELECT g FROM Group g JOIN g.users u WHERE u.username = :username")
    List<Group> findByUser(@Param("username") String username);
    
    @Query("SELECT g FROM Group g WHERE " +
           "(:searchTerm IS NULL OR g.name LIKE %:searchTerm% OR g.description LIKE %:searchTerm%) AND " +
           "(:enabled IS NULL OR g.enabled = :enabled) AND " +
           "(:systemGroup IS NULL OR g.systemGroup = :systemGroup)")
    Page<Group> searchGroups(@Param("searchTerm") String searchTerm, 
                            @Param("enabled") Boolean enabled, 
                            @Param("systemGroup") Boolean systemGroup, 
                            Pageable pageable);
} 