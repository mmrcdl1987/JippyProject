package com.jippy.foodandmart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchedulerSummaryDto {

    /**
     * Total products fetched from database.
     */
    private int totalProducts;

    /**
     * Successfully updated products.
     */
    private int updatedProducts;

    /**
     * Products missing in Excel or invalid content.
     */
    private int missingProducts;

    /**
     * Products failed during processing.
     */
    private int failedProducts;

    /**
     * Total pages processed.
     */
    private int totalPages;

    /**
     * Total scheduler execution time in milliseconds.
     */
    private long executionTimeInMillis;

}