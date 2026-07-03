package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.FmPermissionRequestDto;
import com.jippy.foodandmart.dto.FmPermissionResponseDto;
import com.jippy.foodandmart.service.FmPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fm")
@RequiredArgsConstructor
 @Slf4j
public class FmPermissionController {

    private final FmPermissionService permissionService;

    @GetMapping("/roles/{roleId}/permissions")
    public List<FmPermissionResponseDto> getPermissionsByRole(
            @PathVariable Integer roleId) {

        log.info("Fetching permissions for roleId : {}", roleId);

        List<FmPermissionResponseDto> response =
                permissionService.getPermissionsByRoleId(roleId);

        log.info("Permissions found : {}", response.size());

        return response;
    }
    @GetMapping("/permissions")
    public List<FmPermissionResponseDto>
    getAllPermissions() {

        log.info(
                "Fetching all permissions"
        );

        return permissionService
                .getAllPermissions();
    }
    @PostMapping("/permissions")
    public FmPermissionResponseDto
    createPermission(
            @RequestBody
            FmPermissionRequestDto dto
    ) {

        return permissionService
                .createPermission(
                        dto.getPermissionName()
                );
    }
    @PutMapping("/permissions/{id}")
    public FmPermissionResponseDto
    updatePermission(
            @PathVariable Integer id,
            @RequestBody
            FmPermissionRequestDto dto
    ) {

        return permissionService
                .updatePermission(
                        id,
                        dto.getPermissionName()
                );
    }
    @DeleteMapping("/permissions/{id}")
    public void deletePermission(
            @PathVariable Integer id
    ) {

        permissionService
                .deletePermission(id);
    }
}

