package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO used to activate or deactivate a PRODUCT
 * or MASTERPRODUCT.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update product active status")
public class FmProductIsActiveToggleRequestDto {

    /**
     * Product ID.
     *
     * For PRODUCT, this refers to products.product_id.
     * For MASTERPRODUCT, this refers to master_products.master_product_id.
     */
    @NotNull(message = "Product ID is required")
    @Schema(
            description = "Product or master product ID",
            example = "39"
    )
    private Integer productId;

    /**
     * Determines which table should be updated.
     */
    @NotNull(message = "Product type is required")
    @Pattern(
            regexp = "PRODUCT|MASTERPRODUCT",
            message = "Product type must be PRODUCT or MASTERPRODUCT"
    )
    @Schema(
            description = "Product type. Supported values: PRODUCT, MASTERPRODUCT",
            example = "PRODUCT",
            allowableValues = {"PRODUCT", "MASTERPRODUCT"}
    )
    private String productType;

    /**
     * Active status.
     *
     * Y = Active
     * N = Inactive
     */
    @NotNull(message = "Is active is required")
    @Pattern(
            regexp = "Y|N",
            message = "Is active must be Y or N"
    )
    @Schema(
            description = "Active status. Y = Active, N = Inactive",
            example = "Y",
            allowableValues = {"Y", "N"}
    )
    private String isActive;
}