package com.jippy.foodandmart.dto;

import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FmMenuCopyResultDTO {
    private int totalItems;
    private int totalOutlets;
    private int successCount;
    private int failureCount;
    private List<CopyError> errors;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CopyError {
        private Integer destOutletId;
        private String  destOutletName;
        private Integer itemId;
        private String  itemName;
        private String  reason;
    }
}
