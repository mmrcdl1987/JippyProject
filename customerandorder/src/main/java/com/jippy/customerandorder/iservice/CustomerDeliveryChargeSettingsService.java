package com.jippy.customerandorder.iservice;

import com.jippy.customerandorder.dto.CustomerDeliveryChargeCalculationResponseDto;
import com.jippy.customerandorder.dto.CustomerDeliveryChargeSettingsDTO;

import java.math.BigDecimal;
import java.util.List;

public interface CustomerDeliveryChargeSettingsService {

    CustomerDeliveryChargeSettingsDTO create(CustomerDeliveryChargeSettingsDTO dto, Integer userId);

    List<CustomerDeliveryChargeSettingsDTO> getAll();

    CustomerDeliveryChargeSettingsDTO getById(Integer id);

    List<CustomerDeliveryChargeSettingsDTO> getByCityId(Integer cityId);

    CustomerDeliveryChargeSettingsDTO update(Integer id, CustomerDeliveryChargeSettingsDTO dto, Integer userId);

    void delete(Integer id);

    CustomerDeliveryChargeSettingsDTO getApplicablePlan(Integer cityId, BigDecimal orderValue);
    CustomerDeliveryChargeCalculationResponseDto calculateCustomerDeliveryCharge(
            Integer cityId,
            BigDecimal orderAmountDiscounted,
            BigDecimal deliveryDistanceKm
    );
}