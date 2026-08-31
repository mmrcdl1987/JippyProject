package com.jippy.foodandmart.dto;

import lombok.Data;

import java.util.List;

@Data
public class FmPublicCategoryDto {

    private Integer categoryId;
    private String categoryName;
    private Boolean isAvailable;
    private List<FmPublicProductDto> products;
}
