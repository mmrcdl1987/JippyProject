package com.jippy.division.service;

import com.jippy.division.dto.DivRefundDetailsDto;

public interface DivOrderRefundService {

    String orderRefund(String orderId, String reason);

    DivRefundDetailsDto getRefundDetails(String orderId);
}
