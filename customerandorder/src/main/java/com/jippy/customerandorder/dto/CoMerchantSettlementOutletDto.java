package com.jippy.customerandorder.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/*
 Outlet level settlement response
 */
@Data
public class CoMerchantSettlementOutletDto {

//     Outlet details
    private Integer outletId;

    private String outletName;

    private String outletPhone;

    private String areaName;


//     Total settlement amount for all outlet orders
    private BigDecimal settlementAmount;

//     Orders of outlet for settlement
    private List<CoMerchantSettlementResponseDto>
            orders;
}