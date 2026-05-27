package com.jippy.driver.serviceImpl;

import com.jippy.driver.constants.DConstants;
import com.jippy.driver.dto.DeliveryChargeCalculationRequestDto;
import com.jippy.driver.dto.DeliveryChargeCalculationResponseDto;
import com.jippy.driver.dto.DriverChargeCalculationRequestDto;
import com.jippy.driver.dto.DriverChargeCalculationResponseDto;
import com.jippy.driver.dto.DriveCustomerLocationDto;
import com.jippy.driver.dto.OutletLocationResponseDto;
import com.jippy.driver.entity.DriverDeliveryChargeSettings;
import com.jippy.driver.exception.DriverBadRequestException;
import com.jippy.driver.feignClients.COFeignClient;
import com.jippy.driver.feignClients.FMFeignClient;

import com.jippy.driver.mapper.DriverDeliveryChargeSettingsMapper;
import com.jippy.driver.repositary.DriverDeliveryChargeSettingsRepository;
import com.jippy.driver.service.DriverChargeService;
import com.jippy.driver.utils.DistanceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DriverChargeServiceImpl implements DriverChargeService {

    private final FMFeignClient fmFeignClient;

    private final COFeignClient coFeignClient;

    private final DriverDeliveryChargeSettingsRepository chargeSettingsRepository;

    private final DriverDeliveryChargeSettingsMapper driverChargeMapper;

    // DRIVER PAYOUT CALCULATION
    @Override
    public DriverChargeCalculationResponseDto calculateDriverCharge(DriverChargeCalculationRequestDto requestDto) {

        log.info("SERVICE_START | CALCULATE_DRIVER_CHARGE | outletId={} | customerAddressId={}", requestDto.getOutletId(), requestDto.getCustomerAddressId());

        validateDriverChargeRequest(requestDto);

        try {

            OutletLocationResponseDto outletLocation = getOutletLocation(requestDto.getOutletId());

            DriveCustomerLocationDto customerLocation = getCustomerLocation(requestDto.getCustomerAddressId());

            BigDecimal pickupDistanceKm = calculateDistance(requestDto.getDriverLatitude().doubleValue(), requestDto.getDriverLongitude().doubleValue(), outletLocation.getLatitude().doubleValue(), outletLocation.getLongitude().doubleValue());

            BigDecimal deliveryDistanceKm = calculateDistance(outletLocation.getLatitude().doubleValue(), outletLocation.getLongitude().doubleValue(), customerLocation.getLatitude(), customerLocation.getLongitude());

            DriverDeliveryChargeSettings pickupSlab = getPickupSlab(pickupDistanceKm);

            DriverDeliveryChargeSettings deliverySlab = getDeliverySlab(deliveryDistanceKm);

            BigDecimal pickupCharge = calculateCharge(pickupDistanceKm, pickupSlab.getUnitPricePerPickKm());

            BigDecimal deliveryCharge = calculateCharge(deliveryDistanceKm, deliverySlab.getUnitPricePerDeliverKm());

            BigDecimal subtotal = pickupCharge.add(deliveryCharge);

            BigDecimal taxAmount = calculateTax(subtotal);

            BigDecimal totalDriverCharge = subtotal.add(taxAmount);

            DriverChargeCalculationResponseDto response = driverChargeMapper.mapToDriverChargeResponse(pickupDistanceKm, deliveryDistanceKm, pickupSlab, deliverySlab, pickupCharge, deliveryCharge, taxAmount, totalDriverCharge, isCodAvailable(requestDto.getOrderAmount()));

            log.info("SERVICE_END | CALCULATE_DRIVER_CHARGE_SUCCESS | totalDriverCharge={}", totalDriverCharge);

            return response;

        } catch (DriverBadRequestException ex) {

            log.error("BUSINESS_EXCEPTION | CALCULATE_DRIVER_CHARGE_FAILED | error={}", ex.getMessage(), ex);

            throw ex;

        } catch (Exception ex) {

            log.error("EXCEPTION | CALCULATE_DRIVER_CHARGE_FAILED | error={}", ex.getMessage(), ex);

            throw new DriverBadRequestException(DConstants.MSG_DISTANCE_CALCULATION_FAILED);
        }
    }

    // CHECKOUT DELIVERY CHARGE
    @Override
    public DeliveryChargeCalculationResponseDto calculateDeliveryCharge(DeliveryChargeCalculationRequestDto requestDto) {

        log.info("SERVICE_START | CALCULATE_DELIVERY_CHARGE | outletId={} | customerAddressId={}", requestDto.getOutletId(), requestDto.getCustomerAddressId());

        validateDeliveryChargeRequest(requestDto);

        try {

            OutletLocationResponseDto outletLocation = getOutletLocation(requestDto.getOutletId());

            DriveCustomerLocationDto customerLocation = getCustomerLocation(requestDto.getCustomerAddressId());

            BigDecimal deliveryDistanceKm = calculateDistance(outletLocation.getLatitude().doubleValue(), outletLocation.getLongitude().doubleValue(), customerLocation.getLatitude(), customerLocation.getLongitude());

            DriverDeliveryChargeSettings deliverySlab = getDeliverySlab(deliveryDistanceKm);

            BigDecimal deliveryCharge = calculateCharge(deliveryDistanceKm, deliverySlab.getUnitPricePerDeliverKm());

            BigDecimal taxAmount = calculateTax(deliveryCharge);

            BigDecimal totalDeliveryCharge = deliveryCharge.add(taxAmount).setScale(2, RoundingMode.HALF_UP);

            DeliveryChargeCalculationResponseDto response = driverChargeMapper.mapToDeliveryChargeResponse(deliveryDistanceKm, deliveryCharge, taxAmount, totalDeliveryCharge, isCodAvailable(requestDto.getOrderAmount()));

            log.info("SERVICE_END | CALCULATE_DELIVERY_CHARGE_SUCCESS | totalDeliveryCharge={}", totalDeliveryCharge);

            return response;

        } catch (DriverBadRequestException ex) {

            log.error("BUSINESS_EXCEPTION | CALCULATE_DELIVERY_CHARGE_FAILED | error={}", ex.getMessage(), ex);

            throw ex;

        } catch (Exception ex) {

            log.error("EXCEPTION | CALCULATE_DELIVERY_CHARGE_FAILED | error={}", ex.getMessage(), ex);

            throw new DriverBadRequestException(DConstants.MSG_DISTANCE_CALCULATION_FAILED);
        }
    }

    // ================= VALIDATIONS =================

    private void validateDriverChargeRequest(DriverChargeCalculationRequestDto requestDto) {

        if (requestDto == null) {

            log.error("VALIDATION_FAILED | REQUEST_BODY_NULL");

            throw new DriverBadRequestException("Request body cannot be null");
        }

        validateCommonRequest(requestDto.getOutletId(), requestDto.getCustomerAddressId(), requestDto.getOrderAmount());

        if (requestDto.getDriverLatitude() == null || requestDto.getDriverLongitude() == null) {

            log.error("VALIDATION_FAILED | DRIVER_COORDINATES_REQUIRED");

            throw new DriverBadRequestException("Driver coordinates are required");
        }
    }

    private void validateDeliveryChargeRequest(DeliveryChargeCalculationRequestDto requestDto) {

        if (requestDto == null) {

            log.error("VALIDATION_FAILED | REQUEST_BODY_NULL");

            throw new DriverBadRequestException("Request body cannot be null");
        }

        validateCommonRequest(requestDto.getOutletId(), requestDto.getCustomerAddressId(), requestDto.getOrderAmount());
    }

    private void validateCommonRequest(Integer outletId, Integer customerAddressId, BigDecimal orderAmount) {

        if (outletId == null) {

            log.error("VALIDATION_FAILED | OUTLET_ID_REQUIRED");

            throw new DriverBadRequestException("Outlet id is required");
        }

        if (customerAddressId == null) {

            log.error("VALIDATION_FAILED | CUSTOMER_ADDRESS_REQUIRED");

            throw new DriverBadRequestException("Customer address id is required");
        }

        if (orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {

            log.error("VALIDATION_FAILED | INVALID_ORDER_AMOUNT");

            throw new DriverBadRequestException("Order amount must be greater than zero");
        }
    }

    // ================= HELPER METHODS =================

    private OutletLocationResponseDto getOutletLocation(Integer outletId) {

        log.info("FETCH_OUTLET_LOCATION | outletId={}", outletId);

        OutletLocationResponseDto outletLocation = fmFeignClient.getOutletLocation(outletId);

        if (outletLocation == null) {

            log.error("OUTLET_LOCATION_NOT_FOUND | outletId={}", outletId);

            throw new DriverBadRequestException(DConstants.MSG_OUTLET_LOCATION_NOT_FOUND);
        }

        return outletLocation;
    }

    private DriveCustomerLocationDto getCustomerLocation(Integer customerAddressId) {

        log.info("FETCH_CUSTOMER_LOCATION | customerAddressId={}", customerAddressId);

        DriveCustomerLocationDto customerLocation = coFeignClient.getCustomerLocation(customerAddressId);

        if (customerLocation == null) {

            log.error("CUSTOMER_LOCATION_NOT_FOUND | customerAddressId={}", customerAddressId);

            throw new DriverBadRequestException(DConstants.MSG_CUSTOMER_LOCATION_NOT_FOUND);
        }

        return customerLocation;
    }

    private BigDecimal calculateDistance(double sourceLatitude, double sourceLongitude, double destinationLatitude, double destinationLongitude) {

        double distance = DistanceUtils.calculateDistance(sourceLatitude, sourceLongitude, destinationLatitude, destinationLongitude);

        return BigDecimal.valueOf(distance).setScale(2, RoundingMode.HALF_UP);
    }

    private DriverDeliveryChargeSettings getPickupSlab(BigDecimal pickupDistanceKm) {

        return chargeSettingsRepository.findPickupSlab(pickupDistanceKm).orElseThrow(() -> {

            log.error("PICKUP_SLAB_NOT_FOUND | pickupDistanceKm={}", pickupDistanceKm);

            return new DriverBadRequestException(DConstants.MSG_PICKUP_SLAB_NOT_FOUND);
        });
    }

    private DriverDeliveryChargeSettings getDeliverySlab(BigDecimal deliveryDistanceKm) {

        return chargeSettingsRepository.findDeliverySlab(deliveryDistanceKm).orElseThrow(() -> {

            log.error("DELIVERY_SLAB_NOT_FOUND | deliveryDistanceKm={}", deliveryDistanceKm);

            return new DriverBadRequestException(DConstants.MSG_DELIVERY_SLAB_NOT_FOUND);
        });
    }

    private BigDecimal calculateCharge(BigDecimal distance, BigDecimal unitPrice) {

        return distance.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTax(BigDecimal amount) {

        return amount.multiply(new BigDecimal(DConstants.TAX_PERCENTAGE)).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    private boolean isCodAvailable(BigDecimal orderAmount) {

        return orderAmount.compareTo(new BigDecimal(DConstants.COD_LIMIT)) <= 0;
    }
}