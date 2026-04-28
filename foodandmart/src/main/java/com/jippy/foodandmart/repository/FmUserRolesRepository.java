package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmUserRolePermissions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

}
