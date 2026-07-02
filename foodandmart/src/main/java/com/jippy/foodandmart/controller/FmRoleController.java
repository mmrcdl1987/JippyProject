package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.FmCreateRoleRequestDto;
import com.jippy.foodandmart.dto.FmRoleResponseDto;
import com.jippy.foodandmart.dto.RolePermissionUpdateDto;
import com.jippy.foodandmart.service.FmRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fm/roles")
@RequiredArgsConstructor
 @Slf4j
public class FmRoleController {

    private final FmRoleService fmRoleService;

    @GetMapping
    public List<FmRoleResponseDto> getAllRoles() {

        log.info("Fetching all roles");

        return fmRoleService.getAllRoles();
    }

    @PutMapping("/{roleId}/permissions")
    public String updateRolePermissions(@PathVariable Integer roleId, @RequestBody RolePermissionUpdateDto dto) {

        log.info("Received permission update request for Role Id : {}", roleId);

        log.info("Permission Ids : {}", dto.getPermissionIds());

        fmRoleService.updateRolePermissions(roleId, dto.getPermissionIds());

        log.info("Permissions updated successfully for Role Id : {}", roleId);

        return "Permissions Updated Successfully";
    }

    @PostMapping
    public String createRole(@RequestBody FmCreateRoleRequestDto dto) {

        log.info("Received Create Role Request : {}", dto.getRoleName());

        fmRoleService.createRole(dto.getRoleName());

        return "Role Created Successfully";
    }
    @PutMapping("/{roleId}")
    public String updateRole(
            @PathVariable Integer roleId,
            @RequestBody FmCreateRoleRequestDto dto
    ) {

        log.info(
                "Received Update Role Request. RoleId : {}",
                roleId
        );

        log.info(
                "New Role Name : {}",
                dto.getRoleName()
        );

        fmRoleService.updateRole(
                roleId,
                dto.getRoleName()
        );

        return "Role Updated Successfully";
    }
}