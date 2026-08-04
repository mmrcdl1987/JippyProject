package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDto {

    @Schema(description = "Username used to log in ",
            example = "devadmin")
    @NotBlank(message = "Username is required")
    private String username;

    @Schema(description = "Password used to log in ",
            example = "*****")
    @NotBlank(message = "Password is required")
    private String password;
}
