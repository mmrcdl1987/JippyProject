package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmRolePermissions;
import com.jippy.foodandmart.entity.FmRoles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface FmRolePermissionsRepository extends JpaRepository<FmRolePermissions, Integer> {
    List<FmRolePermissions> findByRole(FmRoles role);
    @Modifying
    @Transactional
    @Query("""
        delete from FmRolePermissions rp
        where rp.role.roleId = :roleId
    """)
    void deleteByRoleId(Integer roleId);

    @Query("""
SELECT rp
FROM FmRolePermissions rp
WHERE rp.role.roleId = :roleId
""")
    List<FmRolePermissions> findByRoleId(
            @Param("roleId")
            Integer roleId
    );
}