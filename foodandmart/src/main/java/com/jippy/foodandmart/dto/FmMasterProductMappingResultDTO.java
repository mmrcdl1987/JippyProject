package com.jippy.foodandmart.dto;

import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FmMasterProductMappingResultDTO {

    private Integer outletCategoryId;
    private Integer categoryId;
    private String  categoryName;
    private int     totalMasterProducts;   // how many found in master_products
    private int     savedCount;            // newly inserted into products
    private int     skippedCount;          // already existed in products (duplicates)
    private List<String> savedProductNames;
    private List<String> skippedProductNames;
}
