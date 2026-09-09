package com.jippy.driver.dto;

import lombok.Data;

@Data
public class DriverDocumentUpdateDTO {

    private Integer driverId;
    private String aadharDocUrl;
    private String panDocUrl;
    private String rcCopyDocUrl;
    private String drivingLicenseDocUrl;
}
