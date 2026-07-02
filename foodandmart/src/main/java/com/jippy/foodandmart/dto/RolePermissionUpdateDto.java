package com.jippy.foodandmart.dto;

import lombok.Data;

import java.util.List;

@Data
public class RolePermissionUpdateDto {

    private List<Integer> permissionIds;

}