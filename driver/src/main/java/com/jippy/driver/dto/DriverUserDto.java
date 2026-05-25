package com.jippy.driver.dto;

import lombok.Data;

@Data
public class DriverUserDto {

    private String username;
    private String password;
    private Integer userId;
    private String userType;
}