package com.jippy.customerandorder.dto;

import com.jippy.customerandorder.enums.PromotionSourceType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CoCheckoutRequestDto {

    private Integer customerId;

    private Integer customerAddressId;

    private Integer outletId;

    private Integer discountId;

    private PromotionSourceType promotionSourceType;

    private BigDecimal walletAmount;

    private BigDecimal discount;

    private BigDecimal deliveryTip;
}