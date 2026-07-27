package com.jippy.customerandorder.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class WelcomeCouponNotificationEvent{
    private Integer customerId;

    private String customerName;

    private Integer couponId;

    private String couponCode;

    private BigDecimal discountValue;

    private BigDecimal minOrderValue;

}