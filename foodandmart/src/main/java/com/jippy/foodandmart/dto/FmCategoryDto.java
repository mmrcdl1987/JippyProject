package com.jippy.foodandmart.dto;


import lombok.Data;

import java.util.List;

@Data
public class FmCategoryDto {
    private Integer categoryId;
    private String categoryName;
    private List<FmProductDto> products;
}