package com.jippy.foodandmart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FmMasterProductResponseDto {

    private Integer masterProductId;
    private String masterProductName;
    private Integer categoryId;
    private String categoryName;
    private String photo;
    private String thumbnail;
    private Integer veg;
    private Integer nonVeg;
    private Integer publish;
}