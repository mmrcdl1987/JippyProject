package com.jippy.foodandmart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FmAuthResponseDto {

    private String token;
    private String username;
    private String role;
    private List<String> permissions;
}
