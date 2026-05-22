package com.jippy.customerandorder.dto;

import lombok.Data;

@Data
public class CoUserDto {
    private String username;
    private String password;
    private Integer userId;
    private String userType;
}