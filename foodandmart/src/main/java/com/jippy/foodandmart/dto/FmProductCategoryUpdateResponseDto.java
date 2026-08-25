package com.jippy.foodandmart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Category update response")
public class FmProductCategoryUpdateResponseDto {

    @Schema(example = "PRODUCT")
    private String productType;

    @Schema(example = "Lemon Soda")
    private String productName;

    @Schema(example = "33")
    private Integer updatedCategoryId;

    @Schema(
            description = "Number of records updated",
            example = "1"
    )
    private Integer updatedRecords;

    @Schema(example = "Category updated successfully.")
    private String message;
}