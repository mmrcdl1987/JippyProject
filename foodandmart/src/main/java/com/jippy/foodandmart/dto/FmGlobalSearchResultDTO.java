package com.jippy.foodandmart.dto;

import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FmGlobalSearchResultDTO {

    private String keyword;
    private int totalResults;

    private List<SearchItem> merchants;
    private List<SearchItem> outlets;
    private List<SearchItem> masterProducts;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SearchItem {
        private Integer id;
        private String title;       // primary display text
        private String subtitle;    // secondary display text
        private String badge;       // e.g. "ACTIVE", "North Indian"
        private String section;     // "merchant" | "outlet" | "master-product"
    }
}
