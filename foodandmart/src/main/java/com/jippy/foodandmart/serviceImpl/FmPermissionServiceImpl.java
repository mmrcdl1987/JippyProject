package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.dto.FmPermissionResponseDto;
import com.jippy.foodandmart.entity.FmPermission;
import com.jippy.foodandmart.entity.FmRoles;
import com.jippy.foodandmart.repository.FmPermissionRepository;
import com.jippy.foodandmart.repository.FmRoleRepository;
import com.jippy.foodandmart.service.FmPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FmPermissionServiceImpl
        implements FmPermissionService {

    private final FmRoleRepository
            roleRepository;
    private final FmPermissionRepository
            permissionRepository;

    @Override
    public List<FmPermissionResponseDto>
    getPermissionsByRoleId(
            Integer roleId) {

        FmRoles role =
                roleRepository.findById(roleId)
                        .orElseThrow();

        return role.getPermissions()
                .stream()
                .map(permission -> {

                    FmPermissionResponseDto dto =
                            new FmPermissionResponseDto();

                    dto.setPermissionId(
                            permission.getPermissionId());

                    dto.setPermissionName(
                            permission.getPermissionName());

                    return dto;

                })
                .collect(Collectors.toList());
    }
    @Override
    public List<FmPermissionResponseDto>
    getAllPermissions() {

        return permissionRepository
                .findAll()
                .stream()
                .map(permission -> {

                    FmPermissionResponseDto dto =
                            new FmPermissionResponseDto();

                    dto.setPermissionId(
                            permission.getPermissionId());

                    dto.setPermissionName(
                            permission.getPermissionName());

                    return dto;
                })
                .toList();
    }
    @Override
    public FmPermissionResponseDto createPermission(
            String permissionName
    ) {

        FmPermission permission =
                new FmPermission();

        permission.setPermissionName(
                permissionName
        );

        permission.setCreatedAt(
                LocalDateTime.now()
        );

        permissionRepository.save(
                permission
        );

        FmPermissionResponseDto dto =
                new FmPermissionResponseDto();

        dto.setPermissionId(
                permission.getPermissionId()
        );

        dto.setPermissionName(
                permission.getPermissionName()
        );

        return dto;
    }
    @Override
    public FmPermissionResponseDto updatePermission(
            Integer permissionId,
            String permissionName
    ) {

        FmPermission permission =
                permissionRepository.findById(
                        permissionId
                ).orElseThrow();

        permission.setPermissionName(
                permissionName
        );

        permission.setUpdatedAt(
                LocalDateTime.now()
        );

        permissionRepository.save(
                permission
        );

        FmPermissionResponseDto dto =
                new FmPermissionResponseDto();

        dto.setPermissionId(
                permission.getPermissionId()
        );

        dto.setPermissionName(
                permission.getPermissionName()
        );

        return dto;
    }
    @Override
    public void deletePermission(
            Integer permissionId
    ) {

        permissionRepository.deleteById(
                permissionId
        );

    }
}