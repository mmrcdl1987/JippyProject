package com.jippy.foodandmart.dto;

import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FmOutletCategoryDTO {
    private Integer outletCategoryId;
    private Integer categoryId;
    private String  categoryName;
    private List<FmProductDto> products;
}
