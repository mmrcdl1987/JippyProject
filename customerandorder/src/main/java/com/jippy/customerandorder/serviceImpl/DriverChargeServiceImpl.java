package com.jippy.customerandorder.serviceImpl;
import com.jippy.customerandorder.constants.COConstants;
import com.jippy.customerandorder.projection.CustomerLocationProjection;
import com.jippy.customerandorder.dto.DriverChargeCalculationRequestDto;
import com.jippy.customerandorder.dto.DriverChargeCalculationResponseDto;
import com.jippy.customerandorder.dto.OutletLocationResponseDto;
import com.jippy.customerandorder.entity.DriverDeliveryChargeSettings;
import com.jippy.customerandorder.exception.CoBadRequestException;
import com.jippy.customerandorder.feignClients.FMFeignClient;
import com.jippy.customerandorder.iservice.IDriverChargeService;
import com.jippy.customerandorder.repository.CustomerDeliveryAddressRepository;
import com.jippy.customerandorder.repository.DriverDeliveryChargeSettingsRepository;
import com.jippy.customerandorder.utils.DistanceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverChargeServiceImpl implements IDriverChargeService {

    private final FMFeignClient foodMartFeignClient;

    private final CustomerDeliveryAddressRepository customerDeliveryAddressRepository;

    private final DriverDeliveryChargeSettingsRepository chargeSettingsRepository;

    @Override
    public DriverChargeCalculationResponseDto calculateDriverCharge(DriverChargeCalculationRequestDto requestDto) {

        log.info("SERVICE START: Calculate driver charge | outletId={}, customerAddressId={}", requestDto.getOutletId(), requestDto.getCustomerAddressId());

        OutletLocationResponseDto outletLocation = foodMartFeignClient.getOutletLocation(requestDto.getOutletId());

        if (outletLocation == null) {

            log.error("Outlet location not found | outletId={}", requestDto.getOutletId());

            throw new CoBadRequestException(COConstants.MSG_OUTLET_LOCATION_NOT_FOUND);
        }

        log.info("Outlet location fetched successfully | outletId={}", requestDto.getOutletId());

        CustomerLocationProjection customerLocation = customerDeliveryAddressRepository.getCustomerLocation(requestDto.getCustomerAddressId());

        if (customerLocation == null) {

            log.error("Customer location not found | customerAddressId={}", requestDto.getCustomerAddressId());

            throw new CoBadRequestException(COConstants.MSG_CUSTOMER_LOCATION_NOT_FOUND);
        }

        log.info("Customer location fetched successfully | customerAddressId={}", requestDto.getCustomerAddressId());

        double pickupDistance = DistanceUtils.calculateDistance(requestDto.getDriverLatitude(), requestDto.getDriverLongitude(), outletLocation.getLatitude(), outletLocation.getLongitude());

        double deliveryDistance = DistanceUtils.calculateDistance(outletLocation.getLatitude(), outletLocation.getLongitude(), customerLocation.getLatitude(), customerLocation.getLongitude());

        log.info("Distance calculated successfully | pickupDistance={}, deliveryDistance={}", pickupDistance, deliveryDistance);

        BigDecimal pickupDistanceKm = BigDecimal.valueOf(pickupDistance).setScale(2, RoundingMode.HALF_UP);

        BigDecimal deliveryDistanceKm = BigDecimal.valueOf(deliveryDistance).setScale(2, RoundingMode.HALF_UP);

        DriverDeliveryChargeSettings pickupSlab = chargeSettingsRepository.findPickupSlab(pickupDistanceKm).orElseThrow(() -> {

            log.error("Pickup slab not found | pickupDistance={}", pickupDistanceKm);

            return new CoBadRequestException(COConstants.MSG_PICKUP_SLAB_NOT_FOUND);
        });

        DriverDeliveryChargeSettings deliverySlab = chargeSettingsRepository.findDeliverySlab(deliveryDistanceKm).orElseThrow(() -> {

            log.error("Delivery slab not found | deliveryDistance={}", deliveryDistanceKm);

            return new CoBadRequestException(COConstants.MSG_DELIVERY_SLAB_NOT_FOUND);
        });

        BigDecimal pickupCharge = pickupDistanceKm.multiply(pickupSlab.getUnitPricePerPickKm()).setScale(2, RoundingMode.HALF_UP);

        BigDecimal deliveryCharge = deliveryDistanceKm.multiply(deliverySlab.getUnitPricePerDeliverKm()).setScale(2, RoundingMode.HALF_UP);

        BigDecimal subtotal = pickupCharge.add(deliveryCharge);

        BigDecimal taxAmount = subtotal.multiply(new BigDecimal(COConstants.TAX_PERCENTAGE)).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        BigDecimal totalDriverCharge = subtotal.add(taxAmount);

        DriverChargeCalculationResponseDto response = new DriverChargeCalculationResponseDto();

        response.setPickupDistanceKm(pickupDistanceKm.doubleValue());

        response.setDeliveryDistanceKm(deliveryDistanceKm.doubleValue());

        response.setPickupUnitPrice(pickupSlab.getUnitPricePerPickKm());

        response.setDeliveryUnitPrice(deliverySlab.getUnitPricePerDeliverKm());

        response.setPickupCharge(pickupCharge);

        response.setDeliveryCharge(deliveryCharge);

        response.setTaxAmount(taxAmount);

        response.setTotalDriverCharge(totalDriverCharge);

        response.setCodAvailable(requestDto.getOrderAmount().compareTo(new BigDecimal(COConstants.COD_LIMIT)) <= 0);

        log.info("SERVICE END: Driver charge calculated successfully | totalDriverCharge={}", totalDriverCharge);

        return response;
    }
}