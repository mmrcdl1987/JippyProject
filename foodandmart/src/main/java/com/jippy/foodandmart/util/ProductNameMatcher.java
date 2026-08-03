package com.jippy.foodandmart.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.Map;

@Slf4j
@UtilityClass
public class ProductNameMatcher {

    private static final double MATCH_THRESHOLD = 0.70;

    private static final LevenshteinDistance DISTANCE =
            LevenshteinDistance.getDefaultInstance();

    /**
     * Finds the best matching normalized product name
     * from the Excel lookup map.
     *
     * @param dbProductName normalized product name from database
     * @param excelProducts normalized product lookup map
     * @return best matching product name or null
     */
    public String findBestMatch(
            String dbProductName,
            Map<String, ?> excelProducts) {

        if (dbProductName == null || dbProductName.isBlank()) {

            log.warn("Cannot perform product matching. Database product name is null or blank.");

            return null;
        }

        if (excelProducts == null || excelProducts.isEmpty()) {

            log.warn("Cannot perform product matching. Excel product lookup map is empty.");

            return null;
        }

        String bestMatch = null;
        double highestSimilarity = 0.0;

        for (String excelProductName : excelProducts.keySet()) {

            if (excelProductName == null || excelProductName.isBlank()) {
                continue;
            }

            double similarity =
                    calculateSimilarity(dbProductName, excelProductName);

            if (similarity > highestSimilarity) {

                highestSimilarity = similarity;
                bestMatch = excelProductName;
            }
        }

        if (bestMatch != null) {

            log.debug(
                    "Product matched successfully. DatabaseProduct='{}', ExcelProduct='{}', Similarity={}",
                    dbProductName,
                    bestMatch,
                    String.format("%.2f", highestSimilarity));
        } else {

            log.debug(
                    "No matching product found. DatabaseProduct='{}'",
                    dbProductName);
        }

        return highestSimilarity >= MATCH_THRESHOLD
                ? bestMatch
                : null;
    }

    /**
     * Calculates similarity between two normalized strings.
     *
     * @return similarity value between 0.0 and 1.0
     */
    private double calculateSimilarity(
            String first,
            String second) {

        int distance = DISTANCE.apply(first, second);

        int maxLength = Math.max(first.length(), second.length());

        if (maxLength == 0) {
            return 1.0;
        }

        return 1.0 - ((double) distance / maxLength);
    }
}