package com.jippy.foodandmart.dto;

import lombok.Data;

@Data
public class FmCreateMasterProductResponseDto {

    private Integer masterProductId;

    private Integer categoryId;

    private String categoryName;

    private String masterProductName;

    private String photo;

    private String thumbnail;

    private Integer veg;

    private Integer nonVeg;

    private Integer publish;
}