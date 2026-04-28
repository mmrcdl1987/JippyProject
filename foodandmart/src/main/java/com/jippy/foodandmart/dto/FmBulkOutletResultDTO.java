package com.jippy.foodandmart.dto;

import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FmBulkOutletResultDTO {

    private int totalRows;
    private int successCount;
    private int failureCount;
    private List<OutletCredential> credentials;
    private List<OutletError>      errors;

    /** Credential entry for each successfully created outlet */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OutletCredential {
        private Integer outletId;
        private String  outletName;
        private String  outletLoginId;
        private String  outletPassword;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OutletError {
        private int    rowNumber;
        private String outletName;
        private String reason;
    }
}
