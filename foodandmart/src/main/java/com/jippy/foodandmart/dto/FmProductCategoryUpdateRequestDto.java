package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(
        description = "Request to update category for Product or Master Product"
)
public class FmProductCategoryUpdateRequestDto {

    @NotBlank(message = "Product name is required.")
    @Schema(
            description = "Product or Master Product name",
            example = "Lemon Soda",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String productName;


    @NotBlank(message = "Product type is required.")
    @Pattern(
            regexp = "PRODUCT|MASTERPRODUCT",
            message = "Invalid product type. Allowed values are PRODUCT or MASTERPRODUCT."
    )
    @Schema(
            description = "Product type. Allowed values: PRODUCT or MASTERPRODUCT",
            allowableValues = {
                    "PRODUCT",
                    "MASTERPRODUCT"
            },
            example = "PRODUCT",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String productType;


    @NotNull(message = "Updated category ID is required.")
    @Positive(message = "Updated category ID must be greater than 0.")
    @Schema(
            description = "New category ID to be assigned",
            example = "33",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer updatedCategoryId;
}