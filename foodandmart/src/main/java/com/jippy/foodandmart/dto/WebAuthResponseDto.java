package com.jippy.foodandmart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebAuthResponseDto {

    private String jwt;
    private String userType;
    private Integer userId;
    private List<String> roles;
    private List<String> permissions;
}
