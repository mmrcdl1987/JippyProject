package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmRolePermissions;
import com.jippy.foodandmart.entity.FmRoles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FmRolePermissionsRepository extends JpaRepository<FmRolePermissions, Integer> {
    List<FmRolePermissions> findByRole(FmRoles role);
}