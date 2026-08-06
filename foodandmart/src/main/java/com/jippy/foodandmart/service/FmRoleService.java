package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmRoleResponseDto;

import java.util.List;

public interface FmRoleService {

    List<FmRoleResponseDto> getAllRoles();

    void updateRolePermissions(Integer roleId, List<Integer> permissionIds);

    void createRole(String roleName);

    // ADD THIS METHOD
    void updateRole(
            Integer roleId,
            String roleName
    );

}