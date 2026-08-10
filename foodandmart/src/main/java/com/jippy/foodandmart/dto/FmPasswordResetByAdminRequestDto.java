package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class FmPasswordResetByAdminRequestDto {

    @Schema(
            description = "Username of the account whose password needs to be reset",
            example = "john123",
//            all feilds are required ex: *
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String username;



    @Schema(
            description = "Type of user account",
            example = "\"MERCHANT\" or, \"CUSTOMER\" or, \"DRIVER\" or,\"EMPLOYEE\", \"DIVISION_ADMIN\"}",
            allowableValues = {"MERCHANT", "CUSTOMER", "DRIVER","EMPLOYEE", "DIVISION_ADMIN"},
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String userType;

    @Schema(
            description = "New password to be assigned by the administrator",
            example = "NewPassword@123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Pattern(
            regexp = "^(?!.*[<>])(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$",
            message = "Password must be 8-20 characters, contain at least one letter, " +
                    "one number, one special character, and must not contain '<' or '>'"
    )
    private String newPassword;
}