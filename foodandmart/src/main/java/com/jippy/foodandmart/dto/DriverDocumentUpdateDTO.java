package com.jippy.foodandmart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverDocumentUpdateDTO {

    private Integer driverId;
    private String aadharDocUrl;
    private String panDocUrl;
    private String rcCopyDocUrl;
    private String drivingLicenseDocUrl;
}
