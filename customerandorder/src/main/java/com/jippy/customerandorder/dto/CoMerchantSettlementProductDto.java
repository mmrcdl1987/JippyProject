package com.jippy.customerandorder.dto;
import lombok.Data;

import java.math.BigDecimal;

//Product details Dto
@Data
public class CoMerchantSettlementProductDto {

    private Integer productId;

    private String productName;

    private Integer quantity;

    private BigDecimal merchantUnitPrice;

    private BigDecimal merchantTotalPrice;
}

