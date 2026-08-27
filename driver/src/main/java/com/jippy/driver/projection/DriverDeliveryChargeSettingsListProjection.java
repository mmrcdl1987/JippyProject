package com.jippy.driver.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface DriverDeliveryChargeSettingsListProjection {

    Integer getDeliveryChargeSettingId();

    Integer getAreaId();

    Integer getStateId();

    String getStateName();

    Integer getCityId();

    String getCityName();

    String getAreaName();

    BigDecimal getPickUpKmsRangeFrom();

    BigDecimal getPickUpKmsRangeTo();

    BigDecimal getUnitPricePerPickKm();

    BigDecimal getDeliveryKmsRangeFrom();

    BigDecimal getDeliveryKmsRangeTo();

    BigDecimal getUnitPricePerDeliverKm();

    LocalDateTime getCreatedAt();

    Integer getCreatedBy();

    LocalDateTime getUpdatedAt();

    Integer getUpdatedBy();
}