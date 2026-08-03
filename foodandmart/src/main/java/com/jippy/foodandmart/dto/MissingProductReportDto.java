package com.jippy.foodandmart.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissingProductReportDto {

    private Integer serialNo;

    private Integer productId;

    private String productName;

    private String reason;

}