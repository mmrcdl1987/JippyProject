package com.jippy.foodandmart.dto;

import lombok.Data;

@Data
public class FmUserResponseDto {

    private Integer usersId;
    private Integer userId;
    private String username;
    private String userType;

    private String roleName;
}
