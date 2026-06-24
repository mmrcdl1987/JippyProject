package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
            example = "MERCHANT",
            allowableValues = {"MERCHANT", "CUSTOMER", "DRIVER", "DIVISION_ADMIN"},
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String userType;

    @Schema(
            description = "New password to be assigned by the administrator",
            example = "NewPassword@123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String newPassword;
}