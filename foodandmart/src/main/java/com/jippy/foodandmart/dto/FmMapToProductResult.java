package com.jippy.foodandmart.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FmMapToProductResult {

    /**
     * Number of products successfully added to the outlet.
     */
    private int savedCount;

    /**
     * Number of products skipped.
     */
    private int skippedCount;

    /**
     * Names of successfully added products.
     *
     * Kept for backward compatibility.
     */
    private List<String> savedNames;

    /**
     * Names of skipped products.
     *
     * Kept for backward compatibility.
     */
    private List<String> skippedNames;

    /**
     * Detailed information about skipped products.
     *
     * Contains:
     * - productName
     * - reason
     */
    private List<SkippedProductDto> skippedProducts;


    /**
     * Details of a skipped product.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SkippedProductDto {

        private String productName;

        private String reason;
    }
}