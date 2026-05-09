package com.jippy.customerandorder.dto;

import lombok.Data;



@Data
public class CoOrderRejectionRequestDto {

    private String orderId;
    private Integer rejectedById;
    private String type;
    private String reason;
}

