package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.projections.FmMerchantSettlementBetweenDatesProjection;
import com.jippy.foodandmart.projections.FmMerchantSettlementOutletProjection;
import com.jippy.foodandmart.projections.FmMerchantSettlementProductProjection;
import com.jippy.foodandmart.projections.FmOutletGstProjection;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility mapper used to build the final merchant settlement response.
 * <p>
 * This mapper contains only static methods because it does not
 * maintain any state or depend on Spring-managed beans.
 */
public class FmSettlementMapper {

    // Private constructor prevents object creation.
    private FmSettlementMapper() {
    }

    /**
     * Maps outlet details from FM and settlement calculation
     * details from CO into the final settlement response.
     * <p>
     * GST is calculated based on the outlet's GST applicability.
     * <p>
     * Final settlement:
     * <p>
     * merchantTotalPrice
     * - GST deducted amount
     * - promotion deducted amount
     *
     * @param outlet       outlet details fetched from FM
     * @param coSettlement settlement calculation received from CO
     * @return final merchant settlement response
     */
    public static FmMerchantSettlementResponseDto mapToSettlementResponse(FmMerchantSettlementOutletProjection outlet, CoSettlementCalculationDto coSettlement) {

        FmMerchantSettlementResponseDto response = new FmMerchantSettlementResponseDto();

        // ---------------------------------------------------------
        // Outlet details
        // ---------------------------------------------------------

        if (outlet != null) {

            response.setOutletId(outlet.getOutletId());
            response.setOutletName(outlet.getOutletName());
            response.setOutletPhone(outlet.getOutletPhone());
            response.setIsGstApplied(outlet.getIsGstApplied());
            response.setCreatedAt(outlet.getCreatedAt());
            response.setBuildingNumber(outlet.getBuildingNumber());
        }

        // ---------------------------------------------------------
        // CO settlement values
        // ---------------------------------------------------------

        BigDecimal merchantTotalPrice = BigDecimal.ZERO;
        BigDecimal promotionDeductedAmount = BigDecimal.ZERO;

        if (coSettlement != null) {

            if (coSettlement.getMerchantTotalPrice() != null) {
                merchantTotalPrice = coSettlement.getMerchantTotalPrice();
            }

            if (coSettlement.getPromotionDeductedAmount() != null) {
                promotionDeductedAmount = coSettlement.getPromotionDeductedAmount();
            }
        }

        response.setMerchantTotalPrice(merchantTotalPrice);
        response.setPromotionDeductedAmount(promotionDeductedAmount);

        // ---------------------------------------------------------
        // GST calculation
        // ---------------------------------------------------------

        BigDecimal gstDeductedAmount = BigDecimal.ZERO;

        /*
         * GST is 5% of merchant total price
         * only when GST is applicable for the outlet.
         */
        if (outlet != null && Boolean.TRUE.equals(outlet.getIsGstApplied())) {

            gstDeductedAmount = merchantTotalPrice.multiply(BigDecimal.valueOf(5)).divide(BigDecimal.valueOf(100));
        }

        response.setGstDeductedAmount(gstDeductedAmount);

        // ---------------------------------------------------------
        // Final settlement calculation
        // ---------------------------------------------------------

        BigDecimal netSettlementAmount = merchantTotalPrice.subtract(gstDeductedAmount).subtract(promotionDeductedAmount);

        response.setNetTotalSettlementsAmountAfterDeductions(netSettlementAmount);

        return response;
    }

    public static BigDecimal calculateOutletGstAmount(CoMerchantSettlementSummaryDto coSettlement, List<FmOutletGstProjection> gstProjections) {

        if (coSettlement == null || gstProjections == null) {
            return BigDecimal.ZERO;
        }

        boolean gstApplied = false;

        for (FmOutletGstProjection projection : gstProjections) {

            if (coSettlement.getOutletId().equals(projection.getOutletId())) {

                gstApplied = Boolean.TRUE.equals(projection.getGstApplied());

                break;
            }
        }

        if (!gstApplied) {
            return BigDecimal.ZERO;
        }

        BigDecimal merchantAmount = coSettlement.getMerchantTotalAmount() == null ? BigDecimal.ZERO : coSettlement.getMerchantTotalAmount();

        BigDecimal promotionAmount = coSettlement.getPromotionDeductedAmount() == null ? BigDecimal.ZERO : coSettlement.getPromotionDeductedAmount();

        BigDecimal taxableAmount = merchantAmount.subtract(promotionAmount);

        return taxableAmount.multiply(FmAppConstants.GST_RATE);
    }

    public static FmMerchantSettlementBetweenDatesResponseDto findOrCreateMerchantSettlement(List<FmMerchantSettlementBetweenDatesResponseDto> responseList, FmMerchantSettlementBetweenDatesProjection merchantProjection) {

        for (FmMerchantSettlementBetweenDatesResponseDto dto : responseList) {

            if (dto.getMerchantId().equals(merchantProjection.getMerchantId())) {
                return dto;
            }
        }

        FmMerchantSettlementBetweenDatesResponseDto dto = new FmMerchantSettlementBetweenDatesResponseDto();

        dto.setMerchantId(merchantProjection.getMerchantId());
        dto.setMerchantName(merchantProjection.getMerchantName());
        dto.setMerchantPhone(merchantProjection.getMerchantPhone());
        dto.setCityName(merchantProjection.getCityName());
        dto.setGstApplied(merchantProjection.getIsGstApplied());

        dto.setOutletCount(0L);
        dto.setOrderCount(0L);
        dto.setMerchantTotalPrice(BigDecimal.ZERO);
        dto.setPromotionDeductedAmount(BigDecimal.ZERO);
        dto.setGstDeductedAmount(BigDecimal.ZERO);

        responseList.add(dto);

        return dto;
    }

    public static void calculateMerchantSettlementFinalAmount(FmMerchantSettlementBetweenDatesResponseDto dto) {

        BigDecimal merchantTotalPrice = dto.getMerchantTotalPrice() == null ? BigDecimal.ZERO : dto.getMerchantTotalPrice();

        BigDecimal promotionDeductedAmount = dto.getPromotionDeductedAmount() == null ? BigDecimal.ZERO : dto.getPromotionDeductedAmount();

        BigDecimal gstDeductedAmount = dto.getGstDeductedAmount() == null ? BigDecimal.ZERO : dto.getGstDeductedAmount();

        BigDecimal amountAfterPromotion = merchantTotalPrice.subtract(promotionDeductedAmount);

        BigDecimal netSettlementAmount = amountAfterPromotion.subtract(gstDeductedAmount);

        dto.setAmountAfterPromotion(amountAfterPromotion);

        dto.setGstPercentage(gstDeductedAmount.compareTo(BigDecimal.ZERO) > 0 ? BigDecimal.valueOf(5) : BigDecimal.ZERO);

        dto.setGstApplied(gstDeductedAmount.compareTo(BigDecimal.ZERO) > 0);
        BigDecimal gstDeductedAmountFromPromotionAmount = amountAfterPromotion.subtract(gstDeductedAmount);

        dto.setGstDeductedAmountFromPromotionAmount(gstDeductedAmountFromPromotionAmount);

        dto.setNetTotalSettlementsAmountAfterDeductionsBetweenDates(netSettlementAmount);
    }

    //    ================================================================================
    public static void addOutletSettlementToMerchant(FmMerchantSettlementBetweenDatesResponseDto dto, CoMerchantSettlementSummaryDto coSettlement, List<FmOutletGstProjection> gstProjections) {

        Long currentOutletCount = dto.getOutletCount() == null ? 0L : dto.getOutletCount();

        dto.setOutletCount(currentOutletCount + 1);

        Long currentOrderCount = dto.getOrderCount() == null ? 0L : dto.getOrderCount();

        Long outletOrderCount = coSettlement.getOrderCount() == null ? 0L : coSettlement.getOrderCount();

        dto.setOrderCount(currentOrderCount + outletOrderCount);

        BigDecimal currentMerchantAmount = dto.getMerchantTotalPrice() == null ? BigDecimal.ZERO : dto.getMerchantTotalPrice();

        BigDecimal outletMerchantAmount = coSettlement.getMerchantTotalAmount() == null ? BigDecimal.ZERO : coSettlement.getMerchantTotalAmount();

        dto.setMerchantTotalPrice(currentMerchantAmount.add(outletMerchantAmount));

        BigDecimal currentPromotionAmount = dto.getPromotionDeductedAmount() == null ? BigDecimal.ZERO : dto.getPromotionDeductedAmount();

        BigDecimal outletPromotionAmount = coSettlement.getPromotionDeductedAmount() == null ? BigDecimal.ZERO : coSettlement.getPromotionDeductedAmount();

        BigDecimal promotionGstAmount = outletPromotionAmount.multiply(FmAppConstants.GST_RATE);

        BigDecimal currentPromotionGstAmount = dto.getGstDeductedAmountFromPromotionAmount() == null ? BigDecimal.ZERO : dto.getGstDeductedAmountFromPromotionAmount();

        dto.setGstDeductedAmountFromPromotionAmount(currentPromotionGstAmount.add(promotionGstAmount));
        dto.setPromotionDeductedAmount(currentPromotionAmount.add(outletPromotionAmount));

        BigDecimal outletGstAmount = calculateOutletGstAmount(coSettlement, gstProjections);

        BigDecimal currentGstAmount = dto.getGstDeductedAmount() == null ? BigDecimal.ZERO : dto.getGstDeductedAmount();

        dto.setGstDeductedAmount(currentGstAmount.add(outletGstAmount));
    }
//========================================================================

    /**
     * Converts the CO settlement response into
     * the FM settlement response.
     * <p>
     * CO provides:
     * - outletId
     * - orderCount
     * - merchantTotalAmount
     * - promotionDeductedAmount
     * <p>
     * FM will calculate:
     * - amountAfterPromotion
     * - GST
     * - final settlement amount
     *
     * @param coResponse settlement response received from CO
     * @return FM settlement response
     */
    public static FmMerchantSettlementForOutletResponseDto toSettlementResponse(CoMerchantSettlementSummaryDto coResponse) {

        FmMerchantSettlementForOutletResponseDto response = new FmMerchantSettlementForOutletResponseDto();

        /*
         * Copy outlet ID.
         */
        response.setOutletId(coResponse.getOutletId());

        /*
         * Copy delivered order count.
         */
        response.setOrderCount(coResponse.getOrderCount() == null ? 0L : coResponse.getOrderCount());

        /*
         * Copy merchant total amount.
         */
        response.setMerchantTotalPrice(coResponse.getMerchantTotalAmount() == null ? BigDecimal.ZERO : coResponse.getMerchantTotalAmount());

        /*
         * Copy merchant promotion deduction.
         */
        response.setPromotionDeductedAmount(coResponse.getPromotionDeductedAmount() == null ? BigDecimal.ZERO : coResponse.getPromotionDeductedAmount());

        /*
         * Calculate amount after promotion.
         *
         * merchantTotalPrice
         *          -
         * promotionDeductedAmount
         */
        BigDecimal amountAfterPromotion = response.getMerchantTotalPrice().subtract(response.getPromotionDeductedAmount());

        /*
         * Prevent negative settlement amount.
         */
        if (amountAfterPromotion.compareTo(BigDecimal.ZERO) < 0) {
            amountAfterPromotion = BigDecimal.ZERO;
        }

        response.setAmountAfterPromotion(amountAfterPromotion);

        return response;
    }

    public static void calculateOutletSettlementFinalAmount(FmMerchantSettlementForOutletResponseDto response, Boolean gstApplied) {

        BigDecimal amountAfterPromotion = response.getAmountAfterPromotion() == null ? BigDecimal.ZERO : response.getAmountAfterPromotion();

        BigDecimal gstDeductedAmount = BigDecimal.ZERO;

        if (Boolean.TRUE.equals(gstApplied)) {

            gstDeductedAmount = amountAfterPromotion.multiply(FmAppConstants.GST_RATE);
        }

        response.setGstApplied(Boolean.TRUE.equals(gstApplied));

        response.setGstPercentage(Boolean.TRUE.equals(gstApplied) ? BigDecimal.valueOf(5) : BigDecimal.ZERO);

        response.setGstDeductedAmount(gstDeductedAmount);

        BigDecimal gstDeductedAmountFromPromotionAmount = amountAfterPromotion.subtract(gstDeductedAmount);

        response.setGstDeductedAmountFromPromotionAmount(gstDeductedAmountFromPromotionAmount);

        response.setNetTotalSettlementsAmountAfterDeductionsBetweenDates(gstDeductedAmountFromPromotionAmount);
    }

    /**
     * Maps CO order-item settlement data and FM product details
     * into the final FM order-item settlement response.
     * <p>
     * One CO order-item produces one response object.
     * <p>
     * Calculation:
     * <p>
     * merchantTotalPrice
     * -
     * promotionDeductedAmount
     * =
     * amountAfterPromotion
     * <p>
     * amountAfterPromotion
     * × 5%
     * =
     * gstDeductedAmount
     * <p>
     * amountAfterPromotion
     * -
     * gstDeductedAmount
     * =
     * finalSettlementAmount
     */
    public static FmMerchantSettlementOrderDetailsResponseDto toOrderDetailsResponse(
            FmMerchantSettlementOrderDetailsDto coOrder,
            List<FmMerchantSettlementProductDetailsDto> products,
            Boolean gstApplied,
            BigDecimal merchantTotalPrice,
            BigDecimal promotionDeductedAmount) {

        FmMerchantSettlementOrderDetailsResponseDto dto =
                new FmMerchantSettlementOrderDetailsResponseDto();

        dto.setOrderId(coOrder.getOrderId());
        dto.setCreatedAt(coOrder.getCreatedAt());

        dto.setProducts(products);

        dto.setGstApplied(Boolean.TRUE.equals(gstApplied));

        // ---------------------------------------------------------
        // Merchant total price
        // ---------------------------------------------------------

        dto.setMerchantTotalPrice(merchantTotalPrice);

        // ---------------------------------------------------------
        // Promotion
        // ---------------------------------------------------------

        dto.setPromotionDeductedAmount(promotionDeductedAmount);

        // ---------------------------------------------------------
        // Amount after promotion
        // ---------------------------------------------------------

        BigDecimal amountAfterPromotion =
                merchantTotalPrice.subtract(promotionDeductedAmount);

        dto.setAmountAfterPromotion(amountAfterPromotion);

        // ---------------------------------------------------------
        // GST calculation
        // ---------------------------------------------------------

        BigDecimal gstDeductedAmount = BigDecimal.ZERO;

        if (Boolean.TRUE.equals(gstApplied)) {

            gstDeductedAmount =
                    amountAfterPromotion.multiply(
                            FmAppConstants.GST_RATE
                    );

            BigDecimal gstPercentage =
                    FmAppConstants.GST_RATE.multiply(
                            BigDecimal.valueOf(100)
                    );

            dto.setGstPercentage(gstPercentage);

        } else {

            dto.setGstPercentage(BigDecimal.ZERO);
        }

        dto.setGstDeductedAmount(gstDeductedAmount);

        // ---------------------------------------------------------
        // Final settlement amount
        // ---------------------------------------------------------

        BigDecimal finalSettlementAmount =
                amountAfterPromotion.subtract(gstDeductedAmount);

        dto.setGstDeductedAmountFromPromotionAmount(
                finalSettlementAmount
        );

        dto.setNetTotalSettlementsAmountAfterDeductionsBetweenDates(
                finalSettlementAmount
        );

        return dto;
    }

//    ==============================================================================
public static void addProductVariant(
        List<FmMerchantSettlementProductDetailsDto> products,
        FmMerchantSettlementProductProjection fmDetails,
        Integer quantity) {

    FmMerchantSettlementProductDetailsDto productDto = null;

    for (FmMerchantSettlementProductDetailsDto product : products) {

        if (product.getProductName()
                .equals(fmDetails.getProductName())) {

            productDto = product;
            break;
        }
    }

    if (productDto == null) {

        productDto =
                new FmMerchantSettlementProductDetailsDto();

        productDto.setProductName(
                fmDetails.getProductName()
        );

        productDto.setVariants(
                new ArrayList<>()
        );

        products.add(productDto);
    }

    FmMerchantSettlementVariantDetailsDto variantDto =
            new FmMerchantSettlementVariantDetailsDto();

    variantDto.setVariantName(
            fmDetails.getVariantName()
    );

    variantDto.setQuantity(quantity);

    productDto.getVariants().add(variantDto);
}
}