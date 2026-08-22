package com.jippy.driver.serviceImpl;

import com.jippy.driver.constants.DConstants;
import com.jippy.driver.dto.*;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.jippy.driver.dto.FMAreaDto;
import com.jippy.driver.dto.FMCityDto;
import com.jippy.driver.dto.FMStateDto;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

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

//            BigDecimal pickupCharge = calculateCharge(pickupDistanceKm, pickupSlab.getUnitPricePerPickKm());
//
//            BigDecimal deliveryCharge = calculateCharge(deliveryDistanceKm, deliverySlab.getUnitPricePerDeliverKm());

            BigDecimal pickupCharge = calculateCharge(pickupDistanceKm, pickupSlab.getUnitPricePerKm());

            BigDecimal deliveryCharge = calculateCharge(deliveryDistanceKm, deliverySlab.getUnitPricePerKm());

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


    // =========================================================
    // CHECKOUT DELIVERY CHARGE
    // =========================================================

//    @Override
//    public DeliveryChargeCalculationResponseDto calculateDeliveryCharge(DeliveryChargeCalculationRequestDto requestDto) {
//
//        log.info("SERVICE_START | CALCULATE_DELIVERY_CHARGE | outletId={} | customerAddressId={}", requestDto.getOutletId(), requestDto.getCustomerAddressId());
//
//        validateDeliveryChargeRequest(requestDto);
//
//        try {
//            OutletLocationResponseDto outletLocation = getOutletLocation(requestDto.getOutletId());
//
//            DriveCustomerLocationDto customerLocation = getCustomerLocation(requestDto.getCustomerAddressId());
//
//            BigDecimal deliveryDistanceKm = calculateDistance(outletLocation.getLatitude().doubleValue(), outletLocation.getLongitude().doubleValue(), customerLocation.getLatitude(), customerLocation.getLongitude());
//
//            log.info("DELIVERY_DISTANCE_KM = {}", deliveryDistanceKm);
//
//            DriverDeliveryChargeSettings deliverySlab = getDeliverySlab(deliveryDistanceKm);
//
//            // BigDecimal deliveryCharge = calculateCharge(deliveryDistanceKm, deliverySlab.getUnitPricePerDeliverKm());
//
//            // BigDecimal taxAmount = calculateTax(deliveryCharge);
//
//            // BigDecimal totalDeliveryCharge = deliveryCharge.add(taxAmount).setScale(2, RoundingMode.HALF_UP);
//
//            //DeliveryChargeCalculationResponseDto response = driverChargeMapper.mapToDeliveryChargeResponse(deliveryDistanceKm, deliveryCharge, taxAmount, totalDeliveryCharge, isCodAvailable(requestDto.getOrderAmount()));
//
//            // log.info("SERVICE_END | CALCULATE_DELIVERY_CHARGE_SUCCESS | totalDeliveryCharge={}", totalDeliveryCharge);
//
//            // return response;
//
//        } catch (DriverBadRequestException ex) {
//
//            log.error("BUSINESS_EXCEPTION | CALCULATE_DELIVERY_CHARGE_FAILED | error={}", ex.getMessage(), ex);
//
//            throw ex;
//
//        } catch (Exception ex) {
//            log.error("EXCEPTION | CALCULATE_DELIVERY_CHARGE_FAILED | error={}", ex.getMessage(), ex);
//
//            throw new DriverBadRequestException(DConstants.MSG_DISTANCE_CALCULATION_FAILED);
//        }
//    }

    @Override
    public DeliveryChargeCalculationResponseDto calculateDeliveryCharge(
            DeliveryChargeCalculationRequestDto requestDto) {

        log.info(
                "SERVICE_START | CALCULATE_DELIVERY_CHARGE | outletId={} | customerAddressId={}",
                requestDto != null ? requestDto.getOutletId() : null,
                requestDto != null ? requestDto.getCustomerAddressId() : null
        );

        validateDeliveryChargeRequest(requestDto);

        try {

            // =========================================================
            // STEP 1: GET OUTLET LOCATION
            // =========================================================

            OutletLocationResponseDto outletLocation =
                    getOutletLocation(requestDto.getOutletId());

            // =========================================================
            // STEP 2: GET CUSTOMER LOCATION
            // =========================================================

            DriveCustomerLocationDto customerLocation =
                    getCustomerLocation(requestDto.getCustomerAddressId());

            // =========================================================
            // STEP 3: CALCULATE DELIVERY DISTANCE
            // =========================================================

            BigDecimal deliveryDistanceKm = calculateDistance(
                    outletLocation.getLatitude().doubleValue(),
                    outletLocation.getLongitude().doubleValue(),
                    customerLocation.getLatitude(),
                    customerLocation.getLongitude()
            );

            log.info(
                    "DELIVERY_DISTANCE_KM = {} | outletId={} | customerAddressId={}",
                    deliveryDistanceKm,
                    requestDto.getOutletId(),
                    requestDto.getCustomerAddressId()
            );

            // =========================================================
            // STEP 4: FIND DELIVERY SLAB
            // =========================================================

            DriverDeliveryChargeSettings deliverySlab =
                    getDeliverySlab(deliveryDistanceKm);

            // =========================================================
            // STEP 5: CALCULATE DELIVERY CHARGE
            // =========================================================

            BigDecimal deliveryCharge = calculateCharge(
                    deliveryDistanceKm,
                    deliverySlab.getUnitPricePerKm()
            );

            // =========================================================
            // STEP 6: CALCULATE TAX
            // =========================================================

            BigDecimal taxAmount = calculateTax(deliveryCharge);

            // =========================================================
            // STEP 7: TOTAL DELIVERY CHARGE
            // =========================================================

            BigDecimal totalDeliveryCharge = deliveryCharge
                    .add(taxAmount)
                    .setScale(2, RoundingMode.HALF_UP);

            // =========================================================
            // STEP 8: BUILD RESPONSE
            // =========================================================

            DeliveryChargeCalculationResponseDto response =
                    driverChargeMapper.mapToDeliveryChargeResponse(
                            deliveryDistanceKm,
                            deliveryCharge,
                            taxAmount,
                            totalDeliveryCharge,
                            isCodAvailable(requestDto.getOrderAmount())
                    );

            log.info(
                    "SERVICE_END | CALCULATE_DELIVERY_CHARGE_SUCCESS | " +
                            "distanceKm={} | deliveryCharge={} | taxAmount={} | totalDeliveryCharge={}",
                    deliveryDistanceKm,
                    deliveryCharge,
                    taxAmount,
                    totalDeliveryCharge
            );

            return response;

        } catch (DriverBadRequestException ex) {

            log.error(
                    "BUSINESS_EXCEPTION | CALCULATE_DELIVERY_CHARGE_FAILED | error={}",
                    ex.getMessage(),
                    ex
            );

            throw ex;

        } catch (Exception ex) {

            log.error(
                    "EXCEPTION | CALCULATE_DELIVERY_CHARGE_FAILED | error={}",
                    ex.getMessage(),
                    ex
            );

            throw new DriverBadRequestException(
                    DConstants.MSG_DISTANCE_CALCULATION_FAILED
            );
        }
    }

    // VALIDATIONS

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

    // HELPER METHODS

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

        log.info("Finding delivery slab for distance: {} KM", deliveryDistanceKm);

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


// GET DELIVERY CHARGE SETTINGS
// STATE + CITY + AREA + PAGINATION


//    @Override
//    public DriverDeliveryChargeSettingsPageResponseDto getDeliveryChargeSettings(Pageable pageable) {
//
//        log.info("SERVICE_START | GET_DELIVERY_CHARGE_SETTINGS | page={} | size={}", pageable.getPageNumber(), pageable.getPageSize());
//
//        // FETCH PAGINATED DATA FROM DRIVER DATABASE
//
//        Page<DriverDeliveryChargeSettings> settingsPage = chargeSettingsRepository.findAll(pageable);
//
//        // LOCATION MAPS
//
//        // stateId -> stateName
//        Map<Integer, String> stateNameMap = new HashMap<>();
//
//        // cityId -> cityName
//        Map<Integer, String> cityNameMap = new HashMap<>();
//
//        // cityId -> stateId
//        Map<Integer, Integer> cityStateMap = new HashMap<>();
//
//        // areaId -> areaName
//        Map<Integer, String> areaNameMap = new HashMap<>();
//
//        // areaId -> cityId
//        Map<Integer, Integer> areaCityMap = new HashMap<>();
//
//        // STEP 1: FETCH STATES FROM FM
//
//        try {
//
//            ResponseEntity<List<FMStateDto>> stateResponse = fmFeignClient.fetchStates();
//
//            if (stateResponse.getBody() != null) {
//
//                for (FMStateDto state : stateResponse.getBody()) {
//
//                    if (state.getStateId() == null) {
//                        continue;
//                    }
//
//                    stateNameMap.put(state.getStateId(), state.getStateName());
//                }
//            }
//
//            log.info("FM_LOCATION_SUCCESS | States fetched | count={}", stateNameMap.size());
//
//        } catch (Exception e) {
//
//            log.error("FM_LOCATION_ERROR | Failed to fetch states", e);
//        }
//
//        // STEP 2: FETCH CITIES FOR EACH STATE
//        //
//        // API:
//        // /fetchCityInState?stateId=1
//        //
//        // Response:
//        // cityId + cityName
//        //
//        // We manually maintain:
//        // cityId -> stateId
//
//        for (Integer stateId : stateNameMap.keySet()) {
//
//            try {
//
//                ResponseEntity<List<FMCityDto>> cityResponse = fmFeignClient.fetchCitiesByState(stateId);
//
//                if (cityResponse.getBody() == null) {
//                    continue;
//                }
//
//                for (FMCityDto city : cityResponse.getBody()) {
//
//                    if (city.getCityId() == null) {
//                        continue;
//                    }
//
//                    // City name
//                    cityNameMap.put(city.getCityId(), city.getCityName());
//
//                    // City belongs to this state
//                    cityStateMap.put(city.getCityId(), stateId);
//                }
//
//            } catch (Exception e) {
//
//                log.error("FM_LOCATION_ERROR | Failed to fetch cities | stateId={}", stateId, e);
//            }
//        }
//
//        log.info("FM_LOCATION_SUCCESS | Cities fetched | count={}", cityNameMap.size());
//
//
//        // STEP 3: FETCH AREAS FOR EACH CITY
//        //
//        // API:
//        // /fetchAreaInCity?cityId=1
//        //
//        // Response:
//        // areaId + areaName
//        //
//        // We manually maintain:
//        // areaId -> cityId
//
//        for (Integer cityId : cityNameMap.keySet()) {
//
//            try {
//
//                ResponseEntity<List<FMAreaDto>> areaResponse = fmFeignClient.fetchAreasByCity(cityId);
//
//                if (areaResponse.getBody() == null) {
//                    continue;
//                }
//
//                for (FMAreaDto area : areaResponse.getBody()) {
//
//                    if (area.getAreaId() == null) {
//                        continue;
//                    }
//
//                    // Area name
//                    areaNameMap.put(area.getAreaId(), area.getAreaName());
//
//                    // Area belongs to this city
//                    areaCityMap.put(area.getAreaId(), cityId);
//                }
//
//            } catch (Exception e) {
//
//                log.error("FM_LOCATION_ERROR | Failed to fetch areas | cityId={}", cityId, e);
//            }
//        }
//
//        log.info("FM_LOCATION_SUCCESS | Areas fetched | count={}", areaNameMap.size());
//
//        // STEP 4: MAP DRIVER ENTITY -> RESPONSE DTO
//
//        List<DriverDeliveryChargeSettingsListResponseDto> content = settingsPage.getContent().stream().map(setting -> {
//
//            DriverDeliveryChargeSettingsListResponseDto dto = driverChargeMapper.mapToListResponseDto(setting);
//
//            // AREA
//
//            Integer areaId = setting.getAreaId();
//
//            dto.setAreaId(areaId);
//
//            String areaName = areaNameMap.get(areaId);
//
//            dto.setAreaName(areaName);
//
//            // CITY
//
//           // Integer cityId = areaCityMap.get(areaId);
//
//            dto.setCityId(cityId);
//
//            if (cityId != null) {
//
//                dto.setCityName(cityNameMap.get(cityId));
//            }
//
//            // STATE
//
//            Integer stateId = cityStateMap.get(cityId);
//
//            dto.setStateId(stateId);
//
//            if (stateId != null) {
//
//                dto.setStateName(stateNameMap.get(stateId));
//            }
//
//
//            return dto;
//
//        }).toList();
//
//        // STEP 5: BUILD PAGINATION RESPONSE
//
//        DriverDeliveryChargeSettingsPageResponseDto response = DriverDeliveryChargeSettingsPageResponseDto.builder().content(content).page(settingsPage.getNumber()).size(settingsPage.getSize()).totalElements(settingsPage.getTotalElements()).totalPages(settingsPage.getTotalPages()).first(settingsPage.isFirst()).last(settingsPage.isLast()).build();
//
//        // SUCCESS LOG
//
//        log.info("SERVICE_SUCCESS | GET_DELIVERY_CHARGE_SETTINGS | page={} | size={} | totalElements={} | totalPages={}", settingsPage.getNumber(), settingsPage.getSize(), settingsPage.getTotalElements(), settingsPage.getTotalPages());
//
//        return response;
//    }
}