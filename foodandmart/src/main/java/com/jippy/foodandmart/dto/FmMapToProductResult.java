package com.jippy.foodandmart.dto;

import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FmMapToProductResult {
    private int savedCount;
    private int skippedCount;
    private List<String> savedNames;
    private List<String> skippedNames;
}
