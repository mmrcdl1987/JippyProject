package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.dto.FmRoleResponseDto;
import com.jippy.foodandmart.entity.FmPermission;
import com.jippy.foodandmart.entity.FmRolePermissions;
import com.jippy.foodandmart.entity.FmRoles;
import com.jippy.foodandmart.entity.FmUser;
import com.jippy.foodandmart.repository.*;
import com.jippy.foodandmart.service.FmRoleService;
import com.jippy.foodandmart.service.IFmUsersService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FmRoleServiceImpl implements FmRoleService {

    private final FmRoleRepository fmRoleRepository;
    private final FmPermissionRepository permissionRepository;
    private final FmRolePermissionsRepository rolePermissionsRepository;
    private final FmUserRolesRepository userRolesRepository;
    private final IFmUsersService fmUsersService;
    private final FmUserRepository userRepository;

    @Override
    public List<FmRoleResponseDto> getAllRoles() {

        log.info("Fetching all roles");

        List<FmRoleResponseDto> roles = fmRoleRepository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());

        log.info("Total roles found : {}", roles.size());

        return roles;
    }

    private FmRoleResponseDto convertToDto(FmRoles role) {

        FmRoleResponseDto dto = new FmRoleResponseDto();

        dto.setRoleId(role.getRoleId());
        dto.setRoleName(role.getRoleName());

        return dto;
    }

    @Override
    @Transactional
    public void updateRolePermissions(Integer roleId, List<Integer> permissionIds) {

        FmRoles role = fmRoleRepository.findById(roleId).orElseThrow(() -> new RuntimeException("Role Not Found"));

        // GET USERS BEFORE ANY DELETE
        List<Integer> affectedUsers = userRolesRepository.findUserIdsByRoleId(roleId);

        List<FmRolePermissions> existingPermissions = rolePermissionsRepository.findByRole(role);

        // REMOVE UNCHECKED PERMISSIONS
        for (FmRolePermissions existing : existingPermissions) {

            Integer existingPermissionId = existing.getPermission().getPermissionId();

            if (!permissionIds.contains(existingPermissionId)) {

                log.info("Removing Permission : {}", existingPermissionId);

                userRolesRepository.deleteByRolePermissionId(existing.getRolePermissionId().longValue());

                rolePermissionsRepository.delete(existing);
            }
        }

        // RELOAD AFTER DELETE
        List<Integer> latestPermissionIds = rolePermissionsRepository.findByRole(role).stream().map(rp -> rp.getPermission().getPermissionId()).toList();

        // ADD NEWLY CHECKED PERMISSIONS
        for (Integer permissionId : permissionIds) {

            if (!latestPermissionIds.contains(permissionId)) {

                FmPermission permission = permissionRepository.findById(permissionId).orElseThrow(() -> new RuntimeException("Permission Not Found"));

                String username = SecurityContextHolder.getContext().getAuthentication().getName();

                FmUser loggedInUser = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User Not Found : " + username));

                FmRolePermissions rolePermission = new FmRolePermissions();

                rolePermission.setRole(role);

                rolePermission.setPermission(permission);

                rolePermission.setCreatedAt(LocalDateTime.now());

                rolePermission.setCreatedBy(loggedInUser.getUsersId());

                rolePermissionsRepository.save(rolePermission);

                log.info("Added Permission : {} By User : {} UserId : {}", permission.getPermissionName(), loggedInUser.getUsername(), loggedInUser.getUsersId());
            }
        }

        // REFRESH ALL USERS OF THIS ROLE
        for (Integer userId : affectedUsers) {

            try {

                List<Integer> roleIds = userRolesRepository.findRoleIdsByUserId(userId);

                fmUsersService.assignRolesToUser(userId, roleIds);

            } catch (Exception e) {

                log.error("Failed to refresh permissions for user {}", userId, e);
            }
        }

        log.info("Permissions Updated Successfully");
    }

    @Override
    public void createRole(String roleName) {

        try {

            log.info("Creating Role : {}", roleName);

            FmRoles role = new FmRoles();

            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            FmUser loggedInUser = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User Not Found : " + username));

            role.setRoleName(roleName);

            role.setCreatedBy(loggedInUser.getUsersId());

            role.setCreatedAt(LocalDateTime.now());

            log.info("Role Created By User : {} UserId : {}", loggedInUser.getUsername(), loggedInUser.getUsersId());

            log.info("Before Save");

            role = fmRoleRepository.save(role);

            log.info("Role Saved Successfully. Id : {} Name : {}", role.getRoleId(), role.getRoleName());

        } catch (Exception e) {

            log.error("Error while creating role", e);

            throw e;
        }
    }

    @Override
    @Transactional
    public void updateRole(Integer roleId, String roleName) {

        log.info("Updating Role. RoleId : {}", roleId);

        FmRoles role = fmRoleRepository.findById(roleId).orElseThrow(() -> {

            log.error("Role Not Found : {}", roleId);

            return new RuntimeException("Role Not Found");
        });

        log.info("Existing Role Name : {}", role.getRoleName());

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        FmUser loggedInUser = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User Not Found : " + username));

        role.setRoleName(roleName);

        role.setUpdatedBy(loggedInUser.getUsersId());

        role.setUpdatedAt(LocalDateTime.now());

        fmRoleRepository.save(role);

        log.info("Role Updated Successfully. RoleId : {} New Role Name : {} Updated By : {}", roleId, roleName, loggedInUser.getUsersId());
    }
}