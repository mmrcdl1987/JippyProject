package com.jippy.foodandmart.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class ProductNameNormalizer {

    /**
     * Normalizes a product name for matching.
     *
     * Rules:
     * 1. Convert to lowercase.
     * 2. Trim leading and trailing spaces.
     * 3. Remove all non-alphanumeric characters.
     *
     * Example:
     * "Premium Cold Coffee!!"
     * becomes
     * "premiumcoldcoffee"
     *
     * @param productName Original product name.
     * @return Normalized product name.
     */
    public String normalize(String productName) {

        if (productName == null || productName.isBlank()) {

            log.warn("Product name normalization skipped. Input is null or blank.");

            return "";
        }

        String normalizedName = productName
                .trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "");

        log.debug("Product normalized successfully. Original='{}', Normalized='{}'",
                productName,
                normalizedName);

        return normalizedName;
    }
}