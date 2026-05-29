package com.jippy.customerandorder.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


// Response Dto for merchant settlement
@Data
public class CoMerchantSettlementResponseDto {

//    from orders table
    private String orderId;
    private Integer outletId;
    private String orderStatus;
    private LocalDateTime createdAt;


//    calculating from order_items table
    private BigDecimal ProductsTotalPrice;

//    List of products for the order, calculated from order_items table
    private List<CoMerchantSettlementProductDto> products;
}