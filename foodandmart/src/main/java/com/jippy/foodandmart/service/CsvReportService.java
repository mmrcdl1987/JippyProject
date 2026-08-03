package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.MissingProductReportDto;

import java.io.File;
import java.util.List;

public interface CsvReportService {

    /**
     * Generates CSV report for missing products.
     *
     * @param missingProducts missing product details
     * @return generated CSV file
     */
    File generateMissingProductsReport(
            List<MissingProductReportDto> missingProducts);

}