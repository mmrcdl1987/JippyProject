package com.jippy.customerandorder.iservice;

import com.jippy.customerandorder.dto.CoOrderSettingsRequestDto;
import com.jippy.customerandorder.dto.CoOrderSettingsResponseDto;
import com.jippy.customerandorder.dto.CoPaymentModeResponse;
import com.jippy.customerandorder.dto.CoPaymentRequest;
import com.jippy.customerandorder.entity.CoPaymentModes;

import java.util.List;

public interface IOrderSettingsService {
    CoOrderSettingsResponseDto saveOrUpdate(CoOrderSettingsRequestDto requestDto);

    CoPaymentModeResponse getPaymentModeById(Integer paymentModeId);

    List<CoPaymentModeResponse> getActivePaymentModes();

    void softDelete(Integer paymentModeId, Integer userId);

    CoPaymentModeResponse update(Integer paymentModeId, CoPaymentRequest request, Integer userId);

    CoPaymentModeResponse create(CoPaymentRequest request, Integer userId);

    List<CoPaymentModeResponse> getAllPaymentModes();
}
