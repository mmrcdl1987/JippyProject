package com.jippy.foodandmart.dto;

import lombok.Data;

@Data
public class FmUserDto {
    private String username;
    private String password;
    private Integer userId;
    private String userType;
    private Integer usersId;
    private String isActive;
}

