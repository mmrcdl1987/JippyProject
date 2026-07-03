package com.jippy.foodandmart.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FmResetPasswordRequestDto {

    @NotBlank
    private String username;

    @NotBlank
    private String newPassword;

    @NotBlank
    private String confirmPassword;

}