package com.jippy.driver.dto;

import lombok.Data;
//  Request DTO for COD balance update API

@Data
public class DriverCodRequestDto {

    private Integer driverId;
    private String orderId;
    private String orderStatus;
}
