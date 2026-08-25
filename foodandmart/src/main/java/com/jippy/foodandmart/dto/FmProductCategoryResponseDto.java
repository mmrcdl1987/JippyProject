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
@Schema(description = "Product category and outlet details")
public class FmProductCategoryResponseDto {

    @Schema(example = "160")
    private Integer productId;

    @Schema(example = "Lemon Soda")
    private String productName;

    @Schema(example = "220")
    private Integer outletId;

    @Schema(example = "Mehfil Restaurantt")
    private String outletName;

    @Schema(example = "80")
    private Integer outletCategoryId;

    @Schema(example = "5")
    private Integer categoryId;

    @Schema(example = "Beverages")
    private String categoryName;


}