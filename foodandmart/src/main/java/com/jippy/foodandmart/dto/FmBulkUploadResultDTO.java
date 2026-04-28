package com.jippy.foodandmart.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FmBulkUploadResultDTO {

    private int totalRows;
    private int successCount;
    private int failureCount;
    private List<RowErrorDTO> errors;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RowErrorDTO {
        private int rowNumber;
        private String field;
        private String value;
        private String reason;
    }
}
