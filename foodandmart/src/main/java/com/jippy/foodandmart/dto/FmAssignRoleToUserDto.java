package com.jippy.foodandmart.dto;

import lombok.Data;

import java.util.List;

@Data
public class FmAssignRoleToUserDto {

    private Integer userId;

    private List<Integer> roleIds;
}
