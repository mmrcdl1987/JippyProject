package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.constants.COConstants;
import com.jippy.customerandorder.dto.*;
import com.jippy.customerandorder.entity.DriverDeliveryChargeSettings;
import com.jippy.customerandorder.entity.OrderSettings;
import com.jippy.customerandorder.exception.CoBadRequestException;
import com.jippy.customerandorder.feignClients.FMFeignClient;
import com.jippy.customerandorder.iservice.ICartService;
import com.jippy.customerandorder.iservice.ICheckoutService;
import com.jippy.customerandorder.projection.CustomerLocationProjection;
import com.jippy.customerandorder.repository.CustomerDeliveryAddressRepository;
import com.jippy.customerandorder.repository.DriverDeliveryChargeSettingsRepository;
import com.jippy.customerandorder.repository.OrderSettingsRepository;
import com.jippy.customerandorder.utils.DistanceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutServiceImpl implements ICheckoutService {

    private final ICartService cartService;

    private final FMFeignClient foodMartFeignClient;

    private final CustomerDeliveryAddressRepository customerDeliveryAddressRepository;

    private final DriverDeliveryChargeSettingsRepository chargeSettingsRepository;

    private final OrderSettingsRepository orderSettingsRepository;

    @Override
    public CoCheckoutResponseDto checkout(CoCheckoutRequestDto requestDto) {

        log.info("CHECKOUT SERVICE START");

        validateRequest(requestDto);

        CoCartResponseDto cartResponse = cartService.getCart(requestDto.getCustomerId());

        if (cartResponse == null || cartResponse.getItems() == null || cartResponse.getItems().isEmpty()) {

            log.error("CART EMPTY | customerId={}", requestDto.getCustomerId());

            throw new CoBadRequestException(COConstants.MSG_CART_EMPTY);
        }

        log.info("CART FETCHED SUCCESSFULLY");

        OrderSettings orderSettings = orderSettingsRepository.findAll().stream().findFirst().orElseThrow(() -> {

            log.error("ORDER SETTINGS NOT FOUND");

            return new CoBadRequestException(COConstants.MSG_ORDER_SETTINGS_NOT_FOUND);
        });

        log.info("ORDER SETTINGS FETCHED SUCCESSFULLY");

        OutletLocationResponseDto outletLocation = foodMartFeignClient.getOutletLocation(requestDto.getOutletId());

        if (outletLocation == null) {

            log.error("OUTLET LOCATION NOT FOUND");

            throw new CoBadRequestException(COConstants.MSG_OUTLET_LOCATION_NOT_FOUND);
        }

        log.info("OUTLET LOCATION FETCHED SUCCESSFULLY");

        CustomerLocationProjection customerLocation = customerDeliveryAddressRepository.getCustomerLocation(requestDto.getCustomerAddressId());

        if (customerLocation == null) {

            log.error("CUSTOMER LOCATION NOT FOUND");

            throw new CoBadRequestException(COConstants.MSG_CUSTOMER_LOCATION_NOT_FOUND);
        }

        log.info("CUSTOMER LOCATION FETCHED SUCCESSFULLY");

        double distance = DistanceUtils.calculateDistance(outletLocation.getLatitude(), outletLocation.getLongitude(), customerLocation.getLatitude(), customerLocation.getLongitude());

        log.info("DISTANCE CALCULATED | distance={}", distance);

        BigDecimal deliveryDistance = BigDecimal.valueOf(distance).setScale(2, RoundingMode.HALF_UP);

        DriverDeliveryChargeSettings deliverySlab = chargeSettingsRepository.findDeliverySlab(deliveryDistance).orElseThrow(() -> {

            log.error("DELIVERY SLAB NOT FOUND");

            return new CoBadRequestException(COConstants.MSG_DELIVERY_SLAB_NOT_FOUND);
        });

        BigDecimal baseDeliveryCharge = deliveryDistance.multiply(deliverySlab.getUnitPricePerDeliverKm()).setScale(2, RoundingMode.HALF_UP);

        log.info("BASE DELIVERY CHARGE CALCULATED | charge={}", baseDeliveryCharge);

        BigDecimal deliveryTax = calculatePercentage(baseDeliveryCharge, orderSettings.getDeliveryFeeTax());

        log.info("DELIVERY TAX CALCULATED | deliveryTax={}", deliveryTax);

        BigDecimal finalDeliveryCharge = baseDeliveryCharge.add(deliveryTax);

        log.info("FINAL DELIVERY CHARGE CALCULATED | finalDeliveryCharge={}", finalDeliveryCharge);

        BigDecimal itemTotal = BigDecimal.ZERO;

        for (CoCartItemResponseDto item : cartResponse.getItems()) {

            itemTotal = itemTotal.add(item.getTotalPrice());
        }

        log.info("ITEM TOTAL CALCULATED | itemTotal={}", itemTotal);

        BigDecimal foodTax = calculatePercentage(itemTotal, orderSettings.getFoodTotalAmountTax());

        log.info("FOOD TAX CALCULATED | foodTax={}", foodTax);

        BigDecimal taxesAndCharges = foodTax.add(deliveryTax);

        log.info("TAXES AND CHARGES CALCULATED | taxesAndCharges={}", taxesAndCharges);

        BigDecimal platformFee = defaultValue(orderSettings.getPlatformFee());

        BigDecimal surgeFee = defaultValue(orderSettings.getSurgeFee());

        BigDecimal packagingFee = defaultValue(orderSettings.getPackagingFee());

        BigDecimal couponDiscount = defaultValue(requestDto.getCouponDiscount());

        BigDecimal deliveryTip = defaultValue(requestDto.getDeliveryTip());

        BigDecimal toPay = itemTotal.add(finalDeliveryCharge).add(platformFee).add(surgeFee).add(packagingFee).add(foodTax).add(deliveryTip).subtract(couponDiscount);

        log.info("FINAL PAYABLE AMOUNT CALCULATED | toPay={}", toPay);

        CoCheckoutResponseDto response = new CoCheckoutResponseDto();

        response.setItems(cartResponse.getItems());

        response.setItemTotal(itemTotal);

        response.setDeliveryCharge(finalDeliveryCharge);

        response.setPlatformFee(platformFee);

        response.setSurgeFee(surgeFee);

        response.setPackagingFee(packagingFee);

        response.setFoodTax(foodTax);

        response.setDeliveryTax(deliveryTax);

        response.setTaxesAndCharges(taxesAndCharges);

        response.setCouponDiscount(couponDiscount);

        response.setDeliveryTip(deliveryTip);

        response.setToPay(toPay);

        response.setCodAvailable(true);

//        response.setMessage(COConstants.MSG_CHECKOUT_SUCCESS);

        log.info("CHECKOUT SERVICE SUCCESS");

        return response;
    }

    private void validateRequest(CoCheckoutRequestDto requestDto) {

        if (requestDto == null) {

            log.error("REQUEST DTO IS NULL");

            throw new CoBadRequestException("Request body cannot be null");
        }

        if (requestDto.getCustomerId() == null) {

            log.error("CUSTOMER ID IS NULL");

            throw new CoBadRequestException("Customer id is required");
        }

        if (requestDto.getCustomerAddressId() == null) {

            log.error("CUSTOMER ADDRESS ID IS NULL");

            throw new CoBadRequestException("Customer address id is required");
        }

        if (requestDto.getOutletId() == null) {

            log.error("OUTLET ID IS NULL");

            throw new CoBadRequestException("Outlet id is required");
        }
    }

    private BigDecimal calculatePercentage(BigDecimal amount, BigDecimal percentage) {

        return amount.multiply(percentage).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal defaultValue(BigDecimal value) {

        return value == null ? BigDecimal.ZERO : value;
    }
}