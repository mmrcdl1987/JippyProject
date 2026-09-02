package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.constants.COConstants;
import com.jippy.customerandorder.dto.*;
import com.jippy.customerandorder.entity.CoOrderCheckoutFee;
import com.jippy.customerandorder.entity.CoOrderCheckoutTax;
import com.jippy.customerandorder.exception.CoBadRequestException;
import com.jippy.customerandorder.feignClients.DriverFeignClient;

import com.jippy.customerandorder.feignClients.FMFeignClient;
import com.jippy.customerandorder.iservice.ICartService;
import com.jippy.customerandorder.iservice.ICheckoutService;
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

    private final CoOrderCheckoutTaxRepository taxRepository;

    @Override
    public CoCheckoutResponseDto checkout(CoCheckoutRequestDto requestDto) {

        log.info("SERVICE_START | CHECKOUT | customerId={} | outletId={}", requestDto != null ? requestDto.getCustomerId() : null, requestDto != null ? requestDto.getOutletId() : null);

        validateRequest(requestDto);

        try {
            // CART
            CoCartResponseDto cartResponse = cartService.getCart(requestDto.getCustomerId());

            validateCart(cartResponse, requestDto.getCustomerId(), requestDto.getOutletId());
            // ITEM TOTAL
            BigDecimal itemTotal = calculateItemTotal(cartResponse);

            log.info("ITEM_TOTAL_CALCULATED | customerId={} | itemTotal={}", requestDto.getCustomerId(), itemTotal);

            // GET AREA ID FROM OUTLET ID

            Integer areaId = getAreaId(requestDto.getOutletId());

            log.info("AREA_ID_RESOLVED | outletId={} | areaId={}", requestDto.getOutletId(), areaId);

            // GET FEE CONFIGURATION BY AREA

            CoOrderCheckoutFee feeConfig = getFeeConfiguration(areaId);
            // GET GST CONFIGURATION
            CoOrderCheckoutTax taxConfig = getTaxConfiguration();
            // DELIVERY CHARGE

            DeliveryChargeCalculationResponseDto deliveryResponse = getDeliveryCharge(requestDto, itemTotal);

            BigDecimal deliveryCharge = defaultValue(deliveryResponse.getTotalDeliveryCharge());


            // GST CALCULATION
            //
            // GST IS ALWAYS CALCULATED
            // REGARDLESS OF FEE TOGGLE

            BigDecimal foodTax = calculatePercentage(itemTotal, taxConfig.getFoodAmountTax());

            BigDecimal deliveryTax = calculatePercentage(deliveryCharge, taxConfig.getDeliveryFeeTax());

            BigDecimal platformFee = defaultValue(feeConfig.getPlatformFee());

            BigDecimal platformFeeTax = calculatePercentage(platformFee, taxConfig.getPlatformFeeTax());

            BigDecimal surgeFee = defaultValue(feeConfig.getSurgeFee());

            BigDecimal surgeFeeTax = calculatePercentage(surgeFee, taxConfig.getSurgeFeeTax());

            BigDecimal packagingFee = defaultValue(feeConfig.getPackagingFee());

            BigDecimal packagingFeeTax = calculatePercentage(packagingFee, taxConfig.getPackagingFeeTax());
            // TOGGLE
            Boolean platformFeeToggle = Boolean.TRUE.equals(feeConfig.getPlatformFeeToggle());

            Boolean surgeFeeToggle = Boolean.TRUE.equals(feeConfig.getSurgeFeeToggle());

            Boolean packagingFeeToggle = Boolean.TRUE.equals(feeConfig.getPackagingFeeToggle());
            // TAXES AND CHARGES
            // GST IS ALWAYS INCLUDED

            BigDecimal taxesAndCharges = foodTax.add(deliveryTax).add(platformFeeTax).add(surgeFeeTax).add(packagingFeeTax).setScale(2, RoundingMode.HALF_UP);
            // COUPON / TIP
            BigDecimal couponDiscount = defaultValue(requestDto.getCouponDiscount());

            BigDecimal deliveryTip = defaultValue(requestDto.getDeliveryTip());

            //deduct wallet amount
            BigDecimal walletAmount = defaultValue(requestDto.getWalletAmount());

            // FINAL TO PAY
            BigDecimal toPay = calculateFinalAmount(itemTotal, deliveryCharge,

                    platformFee, platformFeeTax, platformFeeToggle,

                    surgeFee, surgeFeeTax, surgeFeeToggle,

                    packagingFee, packagingFeeTax, packagingFeeToggle,

                    foodTax, deliveryTax,

                    deliveryTip, couponDiscount,walletAmount);

            // RESPONSE

            CoCheckoutResponseDto response = buildCheckoutResponse(cartResponse,

                    itemTotal,

                    deliveryCharge,

                    platformFee, platformFeeTax, platformFeeToggle,

                    surgeFee, surgeFeeTax, surgeFeeToggle,

                    packagingFee, packagingFeeTax, packagingFeeToggle,

                    foodTax, deliveryTax,

                    taxesAndCharges,

                    couponDiscount, deliveryTip,

                    toPay,

                    deliveryResponse.getCodAvailable());

            log.info("SERVICE_END | CHECKOUT_SUCCESS | customerId={} | areaId={} | toPay={}", requestDto.getCustomerId(), areaId, toPay);

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

                                            BigDecimal walletAmount
    ) {


        // BASE AMOUNT
        BigDecimal totalAmount = itemTotal.add(deliveryCharge)
                .add(foodTax)
                .add(deliveryTax)
                .add(platformFeeTax)
                .add(surgeFeeTax)
                .add(packagingFeeTax);


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


        totalAmount = totalAmount.add(deliveryTip).subtract(couponDiscount).subtract(walletAmount)
                .setScale(2, RoundingMode.HALF_UP);

        return totalAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : totalAmount;
    }


    // GST PERCENTAGE

    private BigDecimal calculatePercentage(BigDecimal amount, BigDecimal percentage) {

        return amount.multiply(defaultValue(percentage)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }


    // RESPONSE

    private CoCheckoutResponseDto buildCheckoutResponse(CoCartResponseDto cartResponse,

                                                        BigDecimal itemTotal,

                                                        BigDecimal deliveryCharge,

                                                        BigDecimal platformFee, BigDecimal platformFeeTax, Boolean platformFeeToggle,

                                                        BigDecimal surgeFee, BigDecimal surgeFeeTax, Boolean surgeFeeToggle,

                                                        BigDecimal packagingFee, BigDecimal packagingFeeTax, Boolean packagingFeeToggle,

                                                        BigDecimal foodTax, BigDecimal deliveryTax,

                                                        BigDecimal taxesAndCharges,

                                                        BigDecimal couponDiscount, BigDecimal deliveryTip,

                                                        BigDecimal toPay,

                                                        Boolean codAvailable) {

        CoCheckoutResponseDto response = new CoCheckoutResponseDto();

        response.setOutletId(cartResponse.getOutletId());

        response.setItems(cartResponse.getItems());

        response.setItemTotal(itemTotal);

        response.setDeliveryCharge(deliveryCharge);
        // PLATFORM
        response.setPlatformFee(platformFee);

        response.setPlatformFeeTax(platformFeeTax);

        response.setPlatformFeeToggle(platformFeeToggle);
        // SURGE
        response.setSurgeFee(surgeFee);

        response.setSurgeFeeTax(surgeFeeTax);

        response.setSurgeFeeToggle(surgeFeeToggle);
        // PACKAGING
        response.setPackagingFee(packagingFee);

        response.setPackagingFeeTax(packagingFeeTax);

        response.setPackagingFeeToggle(packagingFeeToggle);
        // GST
        response.setFoodTax(foodTax);

        response.setDeliveryTax(deliveryTax);

        response.setTaxesAndCharges(taxesAndCharges);
        // OTHER
        response.setCouponDiscount(couponDiscount);

        response.setDeliveryTip(deliveryTip);

        response.setToPay(toPay);

        response.setCodAvailable(codAvailable);

        return response;
    }

    // COMMON

    private BigDecimal defaultValue(BigDecimal value) {

        return value == null ? BigDecimal.ZERO : value;
    }
}