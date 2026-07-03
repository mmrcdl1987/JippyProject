package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmUserRolePermissions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface FmUserRolesRepository extends JpaRepository<FmUserRolePermissions,Integer> {


    @Query(value = """
    SELECT r.role_name, p.permission_name
    FROM users u
    JOIN user_role_permissions urp ON u.users_id = urp.user_id
    JOIN role_permissions rp ON urp.role_permission_id = rp.role_permission_id
    JOIN roles r ON rp.role_id = r.role_id
    JOIN permissions p ON rp.permission_id = p.permission_id
    WHERE u.users_id = :userId
""", nativeQuery = true)
    List<Object[]> getUserRolesAndPermissions(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("""
        DELETE FROM FmUserRolePermissions urp
        WHERE urp.userId = :userId
    """)
    void deleteByUserId(
            @Param("userId") Integer userId
    );
    @Query("""
SELECT DISTINCT
urp.rolePermission.role.roleId
FROM FmUserRolePermissions urp
WHERE urp.userId = :userId
""")
    List<Integer> findRoleIdsByUserId(
            @Param("userId") Integer userId
    );

    @Modifying
    @Transactional
    @Query("""
DELETE FROM FmUserRolePermissions urp
WHERE urp.rolePermission.rolePermissionId = :rolePermissionId
""")
    void deleteByRolePermissionId(
            @Param("rolePermissionId")
            Long rolePermissionId
    );
    @Query("""
SELECT DISTINCT urp.userId
FROM FmUserRolePermissions urp
WHERE urp.rolePermission.role.roleId = :roleId
""")
    List<Integer> findUserIdsByRoleId(
            @Param("roleId")
            Integer roleId
    );
}
