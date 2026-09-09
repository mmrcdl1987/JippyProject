package com.jippy.customerandorder.mapper;

import com.jippy.customerandorder.dto.*;
import com.jippy.customerandorder.projection.*;

import java.util.ArrayList;
import java.util.List;

public class CoOrderCompleteDetailsMapper {

    private CoOrderCompleteDetailsMapper() {
        // Utility class
    }

    /**
     * Maps the main order projection.
     */
    public static CoOrderCompleteDetailsResponseDto mapMainDetails(CoOrderCompleteDetailsProjection projection) {

        CoOrderCompleteDetailsResponseDto response = new CoOrderCompleteDetailsResponseDto();

        response.setOrderId(projection.getOrderId());

        response.setCreatedAt(projection.getCreatedAt());

        response.setOrderType(projection.getOrderType());

        response.setOrderStatus(projection.getOrderStatus());

        response.setPaymentMode(projection.getPaymentMode());

        CoCustomerDetailsDto customer = new CoCustomerDetailsDto();

        customer.setCustomerId(projection.getCustomerId());

        customer.setCustomerName(projection.getCustomerName());

        customer.setEmail(projection.getEmail());

        customer.setPhoneNumber(projection.getPhoneNumber());

        customer.setBuildingName(projection.getBuildingName());

        response.setCustomer(customer);

        return response;
    }

    /**
     * Maps order item projections.
     */
    public static List<CoOrderItemDetailsDto> mapOrderItems(List<CoOrderItemProjection> projections) {

        List<CoOrderItemDetailsDto> items = new ArrayList<>();

        for (CoOrderItemProjection projection : projections) {

            CoOrderItemDetailsDto item = new CoOrderItemDetailsDto();

            item.setProductId(projection.getProductId());

            item.setVariantOptionId(projection.getVariantOptionId());

            item.setQuantity(projection.getQuantity());

            item.setOnlineUnitPrice(projection.getOnlineUnitPrice());

            item.setOnlinePriceTotal(projection.getOnlinePriceTotal());

            items.add(item);
        }

        return items;
    }

    /**
     * Maps price breakup projection.
     */
    /**
     * Maps price breakup projection to price breakup DTO.
     */
    public static CoOrderPriceBreakupDto mapPriceBreakup(
            CoOrderPriceBreakupProjection projection) {

        if (projection == null) {
            return null;
        }

        CoOrderPriceBreakupDto dto = new CoOrderPriceBreakupDto();

        // ================= ORDER =================

        dto.setOrderId(projection.getOrderId());

        dto.setOrderAmount(projection.getOrderAmount());

        dto.setOrderAmountDiscounted(
                projection.getOrderAmountDiscounted()
        );

        // ================= DELIVERY =================

        dto.setPickUpDistanceKms(
                projection.getPickUpDistanceInKms()
        );

        dto.setDeliveryDistanceKms(
                projection.getDeliveryDistanceInKms()
        );

        dto.setPickUpCharges(
                projection.getPickUpCharges()
        );

        dto.setDriverDeliveryFee(
                projection.getDriverDeliveryFee()
        );

        dto.setCustomerDeliveryFee(
                projection.getCustomerDeliveryFee()
        );

        dto.setTotalDeliveryFee(
                projection.getTotalDeliveryFee()
        );

        dto.setCustomerDeliveryFeeTax(
                projection.getCustomerDeliveryFeeTax()
        );

        // ================= PLATFORM =================

        dto.setPlatformFee(
                projection.getPlatformFee()
        );

        dto.setPlatformFeeTax(
                projection.getPlatformFeeTax()
        );

        // ================= SURGE =================

        dto.setSurgeFee(
                projection.getSurgeFee()
        );

        dto.setSurgeFeeTax(
                projection.getSurgeFeeTax()
        );

        // ================= PACKAGING =================

        dto.setPackagingFee(
                projection.getPackagingFee()
        );

        dto.setPackagingFeeTax(
                projection.getPackagingFeeTax()
        );

        // ================= TAX =================

        dto.setFoodTax(
                projection.getFoodTax()
        );

        dto.setTotalTax(
                projection.getTotalTax()
        );

        // ================= PAYMENT / DISCOUNT =================

        dto.setTip(
                projection.getTip()
        );

        dto.setCouponDiscount(
                projection.getCouponDiscount()
        );

        dto.setWalletAmount(
                projection.getWalletAmount()
        );

        // ================= FINAL =================

        dto.setOrderTotalAmount(
                projection.getOrderTotalAmount()
        );

        return dto;
    }
}