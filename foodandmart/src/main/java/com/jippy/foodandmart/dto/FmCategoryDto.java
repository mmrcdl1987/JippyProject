package com.jippy.foodandmart.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class FmCategoryDto {
    private Integer categoryId;
    private String categoryName;
    // Outlet Category Toggle
    @Schema(example = "true")
    private Boolean isAvailable;

    private List<FmProductDto> products;
}