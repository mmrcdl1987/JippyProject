package com.jippy.foodandmart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FmCategoryFilterResponseDto {

    private long totalCount;

    private List<FmCategoryTypeCountDto> categoryTypeCounts;

    private String selectedCategoryType;

    private long filteredCount;

    private List<FmCreateCategoryResponseDto> categories;
}