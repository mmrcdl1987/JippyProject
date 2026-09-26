package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to deactivate a user account for a specific user type")
public class FmInActiveAccountRequestDTO {

    @NotNull
    @Schema(
            description = "User ID of the employee/customer/outlet/merchant/driver",
            example = "2",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer userId;

    @NotBlank
    @Schema(
            description = "User type. Allowed values: EMPLOYEE, CUSTOMER, OUTLET, MERCHANT, DRIVER",
            example = "CUSTOMER",
            allowableValues = {
                    "EMPLOYEE",
                    "CUSTOMER",
                    "OUTLET",
                    "MERCHANT",
                    "DRIVER"
            },
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String userType;
}