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
@Schema(description = "Master product category details")
public class FmMasterProductCategoryResponseDto {

    @Schema(example = "22")
    private Integer masterProductId;

    @Schema(example = "Premium Cold Coffee")
    private String masterProductName;

    @Schema(example = "1")
    private Integer categoryId;

    @Schema(example = "Beve")
    private String categoryName;
}