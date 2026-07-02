package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmPermissionResponseDto;

import java.util.List;

public interface FmPermissionService {

    List<FmPermissionResponseDto>
    getPermissionsByRoleId(
            Integer roleId);

    List<FmPermissionResponseDto>
    getAllPermissions();

    FmPermissionResponseDto createPermission(
            String permissionName
    );

    FmPermissionResponseDto updatePermission(
            Integer permissionId,
            String permissionName
    );

    void deletePermission(
            Integer permissionId
    );
}