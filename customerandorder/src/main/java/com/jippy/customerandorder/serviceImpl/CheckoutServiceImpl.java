package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.constants.COConstants;
import com.jippy.customerandorder.dto.*;
import com.jippy.customerandorder.entity.CoOrderSettings;
import com.jippy.customerandorder.exception.CoBadRequestException;
import com.jippy.customerandorder.feignClients.DriverFeignClient;
import com.jippy.customerandorder.iservice.ICartService;
import com.jippy.customerandorder.iservice.ICheckoutService;
import com.jippy.customerandorder.repository.CoOrderSettingsRepository;
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

    private final CoOrderSettingsRepository coOrderSettingsRepository;

    @Override
    public CoCheckoutResponseDto checkout(CoCheckoutRequestDto requestDto) {

        log.info("SERVICE_START | CHECKOUT | customerId={} | outletId={}", requestDto.getCustomerId(), requestDto.getOutletId());

        validateRequest(requestDto);

        try {

            CoCartResponseDto cartResponse = cartService.getCart(requestDto.getCustomerId());

            validateCart(cartResponse, requestDto.getCustomerId(), requestDto.getOutletId());

            CoOrderSettings coOrderSettings = getOrderSettings();

            BigDecimal itemTotal = calculateItemTotal(cartResponse);

            log.info("ITEM_TOTAL_CALCULATED | customerId={} | itemTotal={}", requestDto.getCustomerId(), itemTotal);

            DeliveryChargeCalculationResponseDto deliveryResponse = getDeliveryCharge(requestDto, itemTotal);

            BigDecimal finalDeliveryCharge = defaultValue(deliveryResponse.getTotalDeliveryCharge());

            BigDecimal deliveryTax = defaultValue(deliveryResponse.getTaxAmount());

            BigDecimal foodTax = calculatePercentage(itemTotal, defaultValue(coOrderSettings.getFoodTotalAmountTax()));

            BigDecimal taxesAndCharges = foodTax.add(deliveryTax);

            BigDecimal platformFee = defaultValue(coOrderSettings.getPlatformFee());

            BigDecimal surgeFee = defaultValue(coOrderSettings.getSurgeFee());

            BigDecimal packagingFee = defaultValue(coOrderSettings.getPackagingFee());

            BigDecimal couponDiscount = defaultValue(requestDto.getCouponDiscount());

            BigDecimal deliveryTip = defaultValue(requestDto.getDeliveryTip());

            BigDecimal toPay = calculateFinalAmount(itemTotal, finalDeliveryCharge, platformFee, surgeFee, packagingFee, foodTax, deliveryTip, couponDiscount);

            CoCheckoutResponseDto response = buildCheckoutResponse(cartResponse, itemTotal, finalDeliveryCharge, platformFee, surgeFee, packagingFee, foodTax, deliveryTax, taxesAndCharges, couponDiscount, deliveryTip, toPay, deliveryResponse.getCodAvailable());

            log.info("SERVICE_END | CHECKOUT_SUCCESS | customerId={} | toPay={}", requestDto.getCustomerId(), toPay);

            return response;

        } catch (CoBadRequestException ex) {

            log.error("BUSINESS_EXCEPTION | CHECKOUT_FAILED | error={}", ex.getMessage(), ex);

            throw ex;

        } catch (Exception ex) {

            log.error("EXCEPTION | CHECKOUT_FAILED | error={}", ex.getMessage(), ex);

            throw new CoBadRequestException("Unable to process checkout");
        }
    }

    // ================= VALIDATIONS =================

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

    // ================= ORDER SETTINGS =================

    private CoOrderSettings getOrderSettings() {

        return coOrderSettingsRepository.findAll().stream().findFirst().orElseThrow(() -> {

            log.error("ORDER_SETTINGS_NOT_FOUND");

            return new CoBadRequestException(COConstants.MSG_ORDER_SETTINGS_NOT_FOUND);
        });
    }

    // ================= DELIVERY CHARGE =================

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

    // ================= CALCULATIONS =================

    private BigDecimal calculateItemTotal(CoCartResponseDto cartResponse) {

        BigDecimal itemTotal = BigDecimal.ZERO;

        for (CoCartItemResponseDto item : cartResponse.getItems()) {

            itemTotal = itemTotal.add(defaultValue(item.getTotalPrice()));
        }

        return itemTotal.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateFinalAmount(BigDecimal itemTotal, BigDecimal deliveryCharge, BigDecimal platformFee, BigDecimal surgeFee, BigDecimal packagingFee, BigDecimal foodTax, BigDecimal deliveryTip, BigDecimal couponDiscount) {

        BigDecimal totalAmount = itemTotal.add(deliveryCharge).add(platformFee).add(surgeFee).add(packagingFee).add(foodTax).add(deliveryTip).subtract(couponDiscount).setScale(2, RoundingMode.HALF_UP);

        return totalAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : totalAmount;
    }

    private BigDecimal calculatePercentage(BigDecimal amount, BigDecimal percentage) {

        return amount.multiply(percentage).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    //================ RESPONSE =================

    private CoCheckoutResponseDto buildCheckoutResponse(CoCartResponseDto cartResponse, BigDecimal itemTotal, BigDecimal deliveryCharge, BigDecimal platformFee, BigDecimal surgeFee, BigDecimal packagingFee, BigDecimal foodTax, BigDecimal deliveryTax, BigDecimal taxesAndCharges, BigDecimal couponDiscount, BigDecimal deliveryTip, BigDecimal toPay, Boolean codAvailable) {

        CoCheckoutResponseDto response = new CoCheckoutResponseDto();

        response.setOutletId(cartResponse.getOutletId());

        response.setItems(cartResponse.getItems());

        response.setItemTotal(itemTotal);

        response.setDeliveryCharge(deliveryCharge);

        response.setPlatformFee(platformFee);

        response.setSurgeFee(surgeFee);

        response.setPackagingFee(packagingFee);

        response.setFoodTax(foodTax);

        response.setDeliveryTax(deliveryTax);

        response.setTaxesAndCharges(taxesAndCharges);

        response.setCouponDiscount(couponDiscount);

        response.setDeliveryTip(deliveryTip);

        response.setToPay(toPay);

        response.setCodAvailable(codAvailable);

        return response;
    }

    // ================= COMMON METHODS =================

    private BigDecimal defaultValue(BigDecimal value) {

        return value == null ? BigDecimal.ZERO : value;
    }
}