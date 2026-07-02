package com.jippy.foodandmart.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FmCreateEmployeeDto {

    private String employeeName;
    private String email;
    private String mobileNumber;

    private String username;
    private String password;
}