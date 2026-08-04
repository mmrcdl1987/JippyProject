package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FmCreateEmployeeDto {

    @Schema(
            description = "Employee full name",
            example = "Rohan Vadluri",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Employee Name is required")
    @Size(max = 100, message = "Employee Name cannot exceed 100 characters")
    @Pattern(
            regexp = "^[A-Za-z ]+$",
            message = "Employee Name should contain only alphabets and spaces"
    )
    private String employeeName;

    @Schema(
            description = "Employee email address",
            example = "rohan@gmail.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @Schema(
            description = "Employee mobile number",
            example = "9876543210",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Mobile Number is required")
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Mobile Number must be a valid 10-digit Indian mobile number"
    )
    private String mobileNumber;

    @Schema(
            description = "Employee username",
            example = "rohan.vadluri",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Username is required")
    @Size(min = 4, max = 30,
            message = "Username must be between 4 and 30 characters")
    @Pattern(
            regexp = "^[A-Za-z0-9._]+$",
            message = "Username can contain only letters, numbers, '.' and '_'"
    )
    private String username;

    @Schema(
            description = "Employee login password",
            example = "Rohan@123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = "^(?!.*[<>])(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$",
            message = "Password must be 8-20 characters, contain at least one letter, " +
                    "one number, one special character, and must not contain '<' or '>'"
    )
    private String password;
}