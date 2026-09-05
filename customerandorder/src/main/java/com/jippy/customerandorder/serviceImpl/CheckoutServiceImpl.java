package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.constants.COConstants;
import com.jippy.customerandorder.dto.*;
import com.jippy.customerandorder.entity.CoOrderCheckoutFee;
import com.jippy.customerandorder.entity.CoOrderCheckoutTax;
import com.jippy.customerandorder.exception.CoBadRequestException;
import com.jippy.customerandorder.feignClients.DriverFeignClient;

import com.jippy.customerandorder.feignClients.FMFeignClient;
import com.jippy.customerandorder.iservice.CustomerDeliveryChargeSettingsService;
import com.jippy.customerandorder.iservice.ICartService;
import com.jippy.customerandorder.iservice.ICheckoutService;
import com.jippy.customerandorder.repository.CoCustomerDeliveryAddressRepository;
import com.jippy.customerandorder.repository.CoOrderCheckoutFeeRepository;
import com.jippy.customerandorder.repository.CoOrderCheckoutTaxRepository;
import feign.FeignException;
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
public class CheckoutServiceImpl implements ICheckoutService {

    private final ICartService cartService;

    private final DriverFeignClient driverFeignClient;

    private final FMFeignClient fmFeignClient;

    private final CoOrderCheckoutFeeRepository feeRepository;

    private final CustomerDeliveryChargeSettingsService customerDeliveryChargeSettingsService;

    private final CoOrderCheckoutTaxRepository taxRepository;

    private final CoCustomerDeliveryAddressRepository customerDeliveryAddressRepository;

    @Override
    public CoCheckoutResponseDto checkout(CoCheckoutRequestDto requestDto) {

        log.info("SERVICE_START | CHECKOUT | customerId={} | outletId={}", requestDto != null ? requestDto.getCustomerId() : null, requestDto != null ? requestDto.getOutletId() : null);

        validateRequest(requestDto);

        try {

            // ============================================================
            // CART
            // ============================================================

            CoCartResponseDto cartResponse = cartService.getCart(requestDto.getCustomerId());

            validateCart(cartResponse, requestDto.getCustomerId(), requestDto.getOutletId());

            // ============================================================
            // ITEM TOTAL
            // ============================================================

            BigDecimal itemTotal = calculateItemTotal(cartResponse);

            log.info("ITEM_TOTAL_CALCULATED | customerId={} | itemTotal={}", requestDto.getCustomerId(), itemTotal);

            // ============================================================
            // AREA / FEE CONFIGURATION
            // ============================================================

            Integer areaId = getAreaId(requestDto.getOutletId());

            log.info("AREA_ID_RESOLVED | outletId={} | areaId={}", requestDto.getOutletId(), areaId);

            CoOrderCheckoutFee feeConfig = getFeeConfiguration(areaId);

            // ============================================================
            // GST CONFIGURATION
            // ============================================================

            CoOrderCheckoutTax taxConfig = getTaxConfiguration();

            // ============================================================
            // FOOD GST
            // ============================================================

            BigDecimal foodTax = calculatePercentage(itemTotal, taxConfig.getFoodAmountTax());

            // ============================================================
            // COUPON
            // ============================================================

            BigDecimal couponDiscount = defaultValue(requestDto.getCouponDiscount());

            BigDecimal orderAmountDiscounted = itemTotal.subtract(couponDiscount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

            log.info("ORDER_AMOUNT_DISCOUNTED | itemTotal={} | couponDiscount={} | orderAmountDiscounted={}", itemTotal, couponDiscount, orderAmountDiscounted);

            // ============================================================
            // CUSTOMER CITY
            // ============================================================

            Integer customerCityId = getCustomerCityId(requestDto.getCustomerId(), requestDto.getCustomerAddressId());

            // ============================================================
            // DRIVER DELIVERY CHARGE
            // KEEP EXISTING DRIVER CALCULATION
            // ============================================================

            DeliveryChargeCalculationResponseDto deliveryResponse = getDeliveryCharge(requestDto, itemTotal);

            BigDecimal driverDeliveryCharge = defaultValue(deliveryResponse.getDeliveryCharge());

            BigDecimal deliveryDistanceKm = defaultValue(deliveryResponse.getDeliveryDistanceKm());

            log.info("DRIVER_DELIVERY_CHARGE | distance={} | driverDeliveryCharge={}", deliveryDistanceKm, driverDeliveryCharge);

            // ============================================================
            // CUSTOMER DELIVERY CHARGE
            // ============================================================

            CustomerDeliveryChargeCalculationResponseDto customerDeliveryResponse = customerDeliveryChargeSettingsService.calculateCustomerDeliveryCharge(customerCityId, orderAmountDiscounted, deliveryDistanceKm);

            BigDecimal customerGrossDeliveryCharge = defaultValue(customerDeliveryResponse.getGrossDeliveryCharge());

            BigDecimal customerFreeDistanceBenefit = defaultValue(customerDeliveryResponse.getFreeDistanceBenefit());

            BigDecimal customerDeliveryCharge = defaultValue(customerDeliveryResponse.getDeliveryCharge());

            log.info("CUSTOMER_DELIVERY_CHARGE | cityId={} | discountedAmount={} | distance={} | gross={} | freeBenefit={} | payable={}", customerCityId, orderAmountDiscounted, deliveryDistanceKm, customerGrossDeliveryCharge, customerFreeDistanceBenefit, customerDeliveryCharge);

            // ============================================================
            // CUSTOMER DELIVERY GST
            //
            // GST is calculated on GROSS delivery charge.
            // NOT on discounted/free-distance delivery charge.
            //
            // DO NOT USE:
            // deliveryResponse.getTaxAmount()
            // ============================================================

            BigDecimal customerDeliveryTax = calculatePercentage(customerGrossDeliveryCharge, taxConfig.getDeliveryFeeTax());

            log.info("CUSTOMER_DELIVERY_GST | grossDeliveryCharge={} | taxPercentage={} | customerDeliveryTax={}", customerGrossDeliveryCharge, taxConfig.getDeliveryFeeTax(), customerDeliveryTax);

            // ============================================================
            // PLATFORM FEE
            // ============================================================

            BigDecimal platformFee = defaultValue(feeConfig.getPlatformFee());

            BigDecimal platformFeeTax = calculatePercentage(platformFee, taxConfig.getPlatformFeeTax());

            // ============================================================
            // SURGE FEE
            // ============================================================

            BigDecimal surgeFee = defaultValue(feeConfig.getSurgeFee());

            BigDecimal surgeFeeTax = calculatePercentage(surgeFee, taxConfig.getSurgeFeeTax());

            // ============================================================
            // PACKAGING FEE
            // ============================================================

            BigDecimal packagingFee = defaultValue(feeConfig.getPackagingFee());

            BigDecimal packagingFeeTax = calculatePercentage(packagingFee, taxConfig.getPackagingFeeTax());

            // ============================================================
            // TOGGLES
            // ============================================================

            Boolean platformFeeToggle = Boolean.TRUE.equals(feeConfig.getPlatformFeeToggle());

            Boolean surgeFeeToggle = Boolean.TRUE.equals(feeConfig.getSurgeFeeToggle());

            Boolean packagingFeeToggle = Boolean.TRUE.equals(feeConfig.getPackagingFeeToggle());

            // ============================================================
            // TAXES AND CHARGES
            //
            // IMPORTANT:
            // customerDeliveryTax is used.
            // Driver taxAmount is NOT used.
            // ============================================================

            BigDecimal taxesAndCharges = foodTax.add(customerDeliveryTax).add(platformFeeTax).add(surgeFeeTax).add(packagingFeeTax).setScale(2, RoundingMode.HALF_UP);

            // ============================================================
            // TIP
            // ============================================================

            BigDecimal deliveryTip = defaultValue(requestDto.getDeliveryTip());

            // ============================================================
            // WALLET
            // ============================================================

            BigDecimal walletAmount = defaultValue(requestDto.getWalletAmount());

            // ============================================================
            // FINAL TO PAY
            //
            // CUSTOMER DELIVERY CHARGE is used here.
            // DRIVER DELIVERY CHARGE is NOT added to customer payable.
            // ============================================================

            BigDecimal toPay = calculateFinalAmount(itemTotal,

                    customerDeliveryCharge,

                    platformFee, platformFeeTax, platformFeeToggle,

                    surgeFee, surgeFeeTax, surgeFeeToggle,

                    packagingFee, packagingFeeTax, packagingFeeToggle,

                    foodTax, customerDeliveryTax,

                    deliveryTip, couponDiscount, walletAmount);

            log.info("FINAL_AMOUNT_CALCULATED | itemTotal={} | customerDeliveryCharge={} | customerDeliveryTax={} | toPay={}", itemTotal, customerDeliveryCharge, customerDeliveryTax, toPay);

            // ============================================================
            // RESPONSE
            // ============================================================

            CoCheckoutResponseDto response = buildCheckoutResponse(cartResponse,

                    itemTotal,

                    orderAmountDiscounted,

                    // DRIVER
                    driverDeliveryCharge,deliveryDistanceKm,

                    // CUSTOMER
                    customerGrossDeliveryCharge, customerFreeDistanceBenefit, customerDeliveryCharge,

                    platformFee, platformFeeTax, platformFeeToggle,

                    surgeFee, surgeFeeTax, surgeFeeToggle,

                    packagingFee, packagingFeeTax, packagingFeeToggle,

                    foodTax, customerDeliveryTax,

                    taxesAndCharges,

                    couponDiscount, deliveryTip,

                    toPay,

                    deliveryResponse.getCodAvailable());

            log.info("SERVICE_END | CHECKOUT_SUCCESS | customerId={} | areaId={} | driverDeliveryCharge={} | customerDeliveryCharge={} | toPay={}", requestDto.getCustomerId(), areaId, driverDeliveryCharge, customerDeliveryCharge, toPay);

            return response;

        } catch (CoBadRequestException ex) {

            log.error("BUSINESS_EXCEPTION | CHECKOUT_FAILED | error={}", ex.getMessage(), ex);

            throw ex;

        } catch (Exception ex) {

            log.error("EXCEPTION | CHECKOUT_FAILED | error={}", ex.getMessage(), ex);

            throw new CoBadRequestException("Unable to process checkout");
        }
    }

    // VALIDATION

    private void validateRequest(CoCheckoutRequestDto requestDto) {

        if (requestDto == null) {

            log.error("VALIDATION_FAILED | REQUEST_BODY_NULL");

            throw new CoBadRequestException("Request body cannot be null");
        }

        if (requestDto.getCustomerId() == null) {

            log.error("VALIDATION_FAILED | CUSTOMER_ID_REQUIRED");

            throw new CoBadRequestException("Customer id is required");
        }

        if (requestDto.getCustomerAddressId() == null) {

            log.error("VALIDATION_FAILED | CUSTOMER_ADDRESS_REQUIRED");

            throw new CoBadRequestException("Customer address id is required");
        }

        if (requestDto.getOutletId() == null) {

            log.error("VALIDATION_FAILED | OUTLET_ID_REQUIRED");

            throw new CoBadRequestException("Outlet id is required");
        }
    }

    // CART VALIDATION

    private void validateCart(CoCartResponseDto cartResponse, Integer customerId, Integer requestedOutletId) {

        if (cartResponse == null || cartResponse.getItems() == null || cartResponse.getItems().isEmpty()) {

            log.error("CART_EMPTY | customerId={}", customerId);

            throw new CoBadRequestException(COConstants.MSG_CART_EMPTY);
        }

        if (cartResponse.getOutletId() == null) {

            log.error("CART_OUTLET_ID_MISSING | customerId={}", customerId);

            throw new CoBadRequestException("Cart outlet information not found");
        }

        if (!cartResponse.getOutletId().equals(requestedOutletId)) {

            log.error("CART_OUTLET_MISMATCH | customerId={} | cartOutletId={} | requestedOutletId={}", customerId, cartResponse.getOutletId(), requestedOutletId);

            throw new CoBadRequestException("Selected outlet does not match the cart outlet");
        }
    }

    // GET AREA ID FROM FOOD MART

    private Integer getAreaId(Integer outletId) {

        log.info("GET_AREA_ID | outletId={}", outletId);

        try {

            Integer areaId = fmFeignClient.getAreaIdByOutletId(outletId);

            if (areaId == null) {

                log.error("AREA_ID_NULL | outletId={}", outletId);

                throw new CoBadRequestException("Area not found for outlet id : " + outletId);
            }

            return areaId;

        } catch (FeignException ex) {

            log.error("FM_SERVICE_CALL_FAILED | GET_AREA_ID | outletId={} | error={}", outletId, ex.getMessage(), ex);

            throw new CoBadRequestException("Unable to fetch outlet area");
        }
    }

    // GET FEE BY AREA
    private CoOrderCheckoutFee getFeeConfiguration(Integer areaId) {

        log.info("GET_CHECKOUT_FEE | areaId={}", areaId);

        return feeRepository.findByAreaId(areaId).orElseThrow(() -> {

            log.error("CHECKOUT_FEE_NOT_FOUND | areaId={}", areaId);

            return new CoBadRequestException("Checkout fee configuration not found for area id : " + areaId);
        });
    }
    // GET GST CONFIGURATION

    private CoOrderCheckoutTax getTaxConfiguration() {

        log.info("GET_CHECKOUT_TAX_CONFIGURATION");

        return taxRepository.findAll().stream().findFirst().orElseThrow(() -> {

            log.error("CHECKOUT_TAX_CONFIGURATION_NOT_FOUND");

            return new CoBadRequestException("Checkout tax configuration not found");
        });
    }

    // DELIVERY CHARGE
    private DeliveryChargeCalculationResponseDto getDeliveryCharge(CoCheckoutRequestDto requestDto, BigDecimal itemTotal) {

        DeliveryChargeCalculationRequestDto deliveryRequest = new DeliveryChargeCalculationRequestDto();

        deliveryRequest.setOutletId(requestDto.getOutletId());

        deliveryRequest.setCustomerAddressId(requestDto.getCustomerAddressId());

        deliveryRequest.setOrderAmount(itemTotal);

        try {

            DeliveryChargeCalculationResponseDto response = driverFeignClient.calculateDeliveryCharge(deliveryRequest);

            if (response == null) {

                log.error("DELIVERY_CHARGE_RESPONSE_NULL");

                throw new CoBadRequestException("Unable to calculate delivery charge");
            }

            return response;

        } catch (FeignException ex) {

            log.error("DRIVER_SERVICE_CALL_FAILED | error={}", ex.getMessage(), ex);

            throw new CoBadRequestException("Driver service unavailable");
        }
    }

    // ITEM TOTAL
    private BigDecimal calculateItemTotal(CoCartResponseDto cartResponse) {

        BigDecimal itemTotal = BigDecimal.ZERO;

        for (CoCartItemResponseDto item : cartResponse.getItems()) {

            itemTotal = itemTotal.add(defaultValue(item.getTotalPrice()));
        }

        return itemTotal.setScale(2, RoundingMode.HALF_UP);
    }
    // FINAL AMOUNT

    private BigDecimal calculateFinalAmount(BigDecimal itemTotal, BigDecimal deliveryCharge,

                                            BigDecimal platformFee, BigDecimal platformFeeTax, Boolean platformFeeToggle,

                                            BigDecimal surgeFee, BigDecimal surgeFeeTax, Boolean surgeFeeToggle,

                                            BigDecimal packagingFee, BigDecimal packagingFeeTax, Boolean packagingFeeToggle,

                                            BigDecimal foodTax, BigDecimal deliveryTax,

                                            BigDecimal deliveryTip, BigDecimal couponDiscount,

                                            BigDecimal walletAmount) {


        // BASE AMOUNT
        BigDecimal totalAmount = itemTotal.add(deliveryCharge).add(foodTax).add(deliveryTax).add(platformFeeTax).add(surgeFeeTax).add(packagingFeeTax);


        // PLATFORM FEE
        // Only fee amount depends on toggle

        if (Boolean.TRUE.equals(platformFeeToggle)) {

            totalAmount = totalAmount.add(platformFee);
        }
        // SURGE FEE
        // Only fee amount depends on toggle

        if (Boolean.TRUE.equals(surgeFeeToggle)) {

            totalAmount = totalAmount.add(surgeFee);
        }

        // PACKAGING FEE
        // Only fee amount depends on toggle


        if (Boolean.TRUE.equals(packagingFeeToggle)) {

            totalAmount = totalAmount.add(packagingFee);
        }

        // TIP / COUPON


        totalAmount = totalAmount.add(deliveryTip).subtract(couponDiscount).subtract(walletAmount).setScale(2, RoundingMode.HALF_UP);

        return totalAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : totalAmount;
    }


    // GST PERCENTAGE

    private BigDecimal calculatePercentage(BigDecimal amount, BigDecimal percentage) {

        return amount.multiply(defaultValue(percentage)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }


    // RESPONSE

    private CoCheckoutResponseDto buildCheckoutResponse(
            CoCartResponseDto cartResponse,

            BigDecimal itemTotal,

            BigDecimal orderAmountDiscounted,

            // DRIVER DELIVERY
            BigDecimal driverDeliveryCharge,
            BigDecimal deliveryDistanceKm,


            // CUSTOMER DELIVERY
            BigDecimal customerGrossDeliveryCharge,
            BigDecimal customerFreeDistanceBenefit,
            BigDecimal customerDeliveryCharge,

            BigDecimal platformFee,
            BigDecimal platformFeeTax,
            Boolean platformFeeToggle,

            BigDecimal surgeFee,
            BigDecimal surgeFeeTax,
            Boolean surgeFeeToggle,

            BigDecimal packagingFee,
            BigDecimal packagingFeeTax,
            Boolean packagingFeeToggle,

            BigDecimal foodTax,
            BigDecimal customerDeliveryTax,

            BigDecimal taxesAndCharges,

            BigDecimal couponDiscount,
            BigDecimal deliveryTip,

            BigDecimal toPay,

            Boolean codAvailable
    ) {

        CoCheckoutResponseDto response =
                new CoCheckoutResponseDto();

        response.setOutletId(
                cartResponse.getOutletId()
        );

        response.setItems(
                cartResponse.getItems()
        );

        response.setItemTotal(
                itemTotal
        );

        response.setOrderAmountDiscounted(
                orderAmountDiscounted
        );

        // ============================================================
        // DRIVER DELIVERY
        // ============================================================

        response.setDriverDeliveryCharge(
                driverDeliveryCharge
        );

        response.setDeliveryDistanceKm(deliveryDistanceKm);
        // ============================================================
        // CUSTOMER DELIVERY
        // ============================================================

        response.setCustomerGrossDeliveryCharge(
                customerGrossDeliveryCharge
        );

        response.setCustomerFreeDistanceBenefit(
                customerFreeDistanceBenefit
        );

        response.setCustomerDeliveryCharge(
                customerDeliveryCharge
        );

        // ============================================================
        // PLATFORM
        // ============================================================

        response.setPlatformFee(
                platformFee
        );

        response.setPlatformFeeTax(
                platformFeeTax
        );

        response.setPlatformFeeToggle(
                platformFeeToggle
        );

        // ============================================================
        // SURGE
        // ============================================================

        response.setSurgeFee(
                surgeFee
        );

        response.setSurgeFeeTax(
                surgeFeeTax
        );

        response.setSurgeFeeToggle(
                surgeFeeToggle
        );

        // ============================================================
        // PACKAGING
        // ============================================================

        response.setPackagingFee(
                packagingFee
        );

        response.setPackagingFeeTax(
                packagingFeeTax
        );

        response.setPackagingFeeToggle(
                packagingFeeToggle
        );

        // ============================================================
        // GST
        // ============================================================

        response.setFoodTax(
                foodTax
        );

        response.setCustomerDeliveryTax(
                customerDeliveryTax
        );

        response.setTaxesAndCharges(
                taxesAndCharges
        );

        // ============================================================
        // OTHER
        // ============================================================

        response.setCouponDiscount(
                couponDiscount
        );

        response.setDeliveryTip(
                deliveryTip
        );

        response.setToPay(
                toPay
        );

        response.setCodAvailable(
                codAvailable
        );

        return response;
    }
    private Integer getCustomerCityId(Integer customerId, Integer customerAddressId) {

        log.info("GET_CUSTOMER_CITY | customerId={} | customerAddressId={}", customerId, customerAddressId);

        Integer cityId = customerDeliveryAddressRepository.findCityByCustomerAddressId(customerAddressId, customerId);

        if (cityId == null) {

            log.error("CUSTOMER_CITY_NOT_FOUND | customerId={} | customerAddressId={}", customerId, customerAddressId);

            throw new CoBadRequestException("City not found for customer address");
        }

        log.info("CUSTOMER_CITY_RESOLVED | customerId={} | customerAddressId={} | cityId={}", customerId, customerAddressId, cityId);

        return cityId;
    }

    // COMMON

    private BigDecimal defaultValue(BigDecimal value) {

        return value == null ? BigDecimal.ZERO : value;
    }
}