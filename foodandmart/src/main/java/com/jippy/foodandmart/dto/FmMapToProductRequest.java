package com.jippy.foodandmart.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FmMapToProductRequest {
    private Integer outletCategoryId;
    private Integer outletId;          // required when outletCategoryId is null
    private List<ProductEntry> products;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class ProductEntry {
        private String productName;
        private String description;
        private BigDecimal merchantPrice;
        private Boolean isVeg;
        private Boolean hasProductVariants;
        private List<VariantEntry> variants;
        private Integer masterProductId;   // used to look up category when no outletCategoryId
        private Integer categoryId;        // category from master product
        /** Day-of-week name from CSV daysofaweek column, e.g. "Monday". Used to resolve day_of_week_id. */
        private String csvDayOfWeek;
        /** Raw timing string from CSV, e.g. "9:00-22:00". Parsed into TimingEntry rows. */
        private String csvTiming;
        /** Explicit per-day timing rows (optional; takes precedence over csvTiming). */
        private List<TimingEntry> timings;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class TimingEntry {
        private Integer dayOfWeekId;   // 0 = all days, 1=Mon … 7=Sun
        private String  startTime;     // "HH:mm"
        private String  endTime;       // "HH:mm"
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class VariantEntry {
        private String variantName;
        private BigDecimal merchantPrice;
    }
}
