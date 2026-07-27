package com.jippy.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WelcomeCouponNotificationEvent {

    private Integer customerId;

    private String customerName;

    private Integer couponId;

    private String couponCode;

    private BigDecimal discountValue;

    private BigDecimal minOrderValue;
}