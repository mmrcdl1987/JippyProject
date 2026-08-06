package com.jippy.foodandmart.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


@Data
public class FmCreateRoleRequestDto {

    @NotBlank(message = "Role name cannot be blank")
    @Size(max = 50, message = "Role name must not exceed 50 characters")
    @Pattern(regexp = "^[A-Za-z-]+$", message = "Role name should contain only letters (A-Z, a-z) and hyphens (-).")
    private String roleName;

}