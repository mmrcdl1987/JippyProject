package com.jippy.foodandmart.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FmForgotPasswordRequestDto {

    @NotBlank(message = "Username is required")
    private String username;

}