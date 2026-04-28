package com.jippy.foodandmart.dto;

import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FmUpdateMenuResultDTO {
    private Integer outletId;
    private String  outletName;
    private int     totalRows;
    private int     categoriesCreated;
    private int     productsCreated;
    private int     productsUpdated;
    private int     variantsCreated;
    private int     failureCount;
    private List<RowError> errors;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RowError {
        private int    rowNumber;
        private String productName;
        private String reason;
    }
}
