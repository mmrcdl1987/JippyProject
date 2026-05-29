package com.jippy.customerandorder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Product response from FM microservice
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoFmProductDto {

    private Integer productId;

    private String productName;
}