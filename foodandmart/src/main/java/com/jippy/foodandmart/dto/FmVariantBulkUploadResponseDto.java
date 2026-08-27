package com.jippy.foodandmart.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FmVariantBulkUploadResponseDto {

    private Boolean success;

    private String message;

    private Integer outletId;

    private Integer totalRows;

    private Integer createdCount;

    private Integer updatedCount;

    private Integer skippedCount;

    private List<FmVariantBulkUploadResultDto> results =
            new ArrayList<>();
}