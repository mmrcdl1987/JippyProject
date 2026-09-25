package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.dto.CoFmOutletDetailsDto;
import com.jippy.customerandorder.dto.CoOrderProfitLossDto;
import com.jippy.customerandorder.dto.CoOutletDetailsRequestDto;
import com.jippy.customerandorder.enums.DateRangeFilter;
import com.jippy.customerandorder.feignClients.FMFeignClient;
import com.jippy.customerandorder.iservice.CoOrderProfitLossService;
import com.jippy.customerandorder.projection.CoOrderProfitLossProjection;
import com.jippy.customerandorder.repository.CoOrderProfitLossRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoOrderProfitLossServiceImpl implements CoOrderProfitLossService {

    private final CoOrderProfitLossRepository orderRepository;
    private final FMFeignClient fmFeignClient;

    /**
     * Merchant GST rate = 5%
     */
    private static final BigDecimal MERCHANT_GST_RATE =
            new BigDecimal("0.05");


    @Override
    public CoOrderProfitLossDto getOrderProfitLoss(String orderId) {

        log.info(
                "SERVICE_START | GET_ORDER_PROFIT_LOSS | orderId={}",
                orderId
        );

        CoOrderProfitLossProjection profitLossData =
                orderRepository.getOrderProfitLossData(orderId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Order not found with id: " + orderId
                                )
                        );

        // Fetch outlet details from FM service
        Map<Integer, OutletDetails> outletDetailsMap = fetchOutletDetails(
                List.of(profitLossData.getOutletId())
        );

        OutletDetails outletDetails = outletDetailsMap.get(profitLossData.getOutletId());

        CoOrderProfitLossDto dto =
                calculateProfitLoss(profitLossData, outletDetails);

        log.info(
                "SERVICE_END | GET_ORDER_PROFIT_LOSS_SUCCESS | " +
                        "orderId={} | revenue={} | merchantPayout={} | " +
                        "profitOrLoss={} | status={}",
                orderId,
                dto.getRevenue(),
                dto.getMerchantPayout(),
                dto.getProfitOrLoss(),
                dto.getProfitLossStatus()
        );

        return dto;
    }


    @Override
    public List<CoOrderProfitLossDto> getAllOrdersProfitLoss(
            LocalDate fromDate,
            LocalDate toDate,
            DateRangeFilter dateRangeFilter
    ) {

        log.info(
                "SERVICE_START | GET_ALL_ORDERS_PROFIT_LOSS | " +
                        "fromDate={} | toDate={} | dateRangeFilter={}",
                fromDate,
                toDate,
                dateRangeFilter
        );

        // Calculate date range if filter is provided
        if (dateRangeFilter != null) {
            LocalDate[] dateRange = calculateDateRange(dateRangeFilter);
            fromDate = dateRange[0];
            toDate = dateRange[1];
        }

        List<CoOrderProfitLossProjection> profitLossDataList =
                orderRepository.getAllOrdersProfitLossData(
                        fromDate,
                        toDate
                );

        // Collect unique outlet IDs
        List<Integer> outletIds = profitLossDataList.stream()
                .map(CoOrderProfitLossProjection::getOutletId)
                .filter(outletId -> outletId != null)
                .distinct()
                .toList();

        // Fetch outlet details from FM service in batch
        Map<Integer, OutletDetails> outletDetailsMap = fetchOutletDetails(outletIds);

        List<CoOrderProfitLossDto> dtoList = new ArrayList<>();

        for (CoOrderProfitLossProjection profitLossData : profitLossDataList) {

            OutletDetails outletDetails = profitLossData.getOutletId() != null
                    ? outletDetailsMap.get(profitLossData.getOutletId())
                    : null;

            CoOrderProfitLossDto dto = calculateProfitLoss(profitLossData, outletDetails);

            dtoList.add(dto);
        }

        log.info(
                "SERVICE_END | GET_ALL_ORDERS_PROFIT_LOSS_SUCCESS | " +
                        "totalOrders={}",
                dtoList.size()
        );

        return dtoList;
    }


    /**
     * Centralized P&L calculation.
     *
     * Business flow:
     *
     * Customer Revenue
     *      = order_total_amount
     *
     * Revenue Per Order
     *      = order_total_amount - delivery_charge
     *
     * Merchant GST
     *      = merchantTotalPrice * 5% (only if is_gst_applied is true)
     *
     * Merchant Promotion
     *      = deducted from merchant payout
     *
     * Other Discounts
     *      = borne by Jippy
     *
     * Merchant Payout
     *      = merchantTotalPrice
     *        - merchantGST
     *        - merchantPromotion
     *
     * Profit/Loss
     *      = revenuePerOrder - merchantPayout - ourDiscountCost
     */
    private CoOrderProfitLossDto calculateProfitLoss(
            CoOrderProfitLossProjection data,
            OutletDetails outletDetails
    ) {

        CoOrderProfitLossDto dto = new CoOrderProfitLossDto();

        // ---------------------------------------------------------
        // Basic order information
        // ---------------------------------------------------------

        dto.setOrderId(data.getOrderId());
        dto.setOutletName(outletDetails != null ? outletDetails.outletName : null);

        // ---------------------------------------------------------
        // Null-safe financial values
        // ---------------------------------------------------------

        BigDecimal revenue = nullSafe(data.getRevenue());

        BigDecimal merchantTotalPrice = nullSafe(data.getMerchantTotalPrice());

        BigDecimal driverDeliveryFee = nullSafe(data.getDriverDeliveryFee());

        BigDecimal discountAmount = nullSafe(data.getDiscountAmount());

        String discountType = data.getDiscountType();

        dto.setRevenue(revenue);
        dto.setMerchantTotalPrice(merchantTotalPrice);
        dto.setDriverDeliveryFee(driverDeliveryFee);
        dto.setDiscountAmount(discountAmount);
        dto.setDiscountType(discountType);

        // ---------------------------------------------------------
        // 1. Calculate revenue per order (order total - delivery charge)
        // ---------------------------------------------------------

        BigDecimal revenuePerOrder =
                revenue
                        .subtract(driverDeliveryFee)
                        .setScale(2, RoundingMode.HALF_UP);

        dto.setRevenuePerOrder(revenuePerOrder);

        // ---------------------------------------------------------
        // 2. Calculate merchant GST = 5% on merchant amount
        // ---------------------------------------------------------

        BigDecimal merchantGst =
                merchantTotalPrice
                        .multiply(MERCHANT_GST_RATE)
                        .setScale(2, RoundingMode.HALF_UP);

        dto.setMerchantGst(merchantGst);

        // ---------------------------------------------------------
        // 3. Determine who bears the discount
        // ---------------------------------------------------------

        BigDecimal merchantPromotionDiscount = BigDecimal.ZERO;

        BigDecimal ourDiscountCost = BigDecimal.ZERO;

        if (discountAmount.compareTo(BigDecimal.ZERO) > 0) {

            if ("MERCHANT_PROMOTION".equalsIgnoreCase(discountType)) {

                // Merchant bears the promotion.
                // Therefore deduct it from merchant payout.
                merchantPromotionDiscount =
                        discountAmount;

            } else {

                // Jippy bears all other discount types.
                ourDiscountCost =
                        discountAmount;
            }
        }

        dto.setMerchantPromotionDiscount(
                merchantPromotionDiscount
        );

        dto.setOurDiscountCost(
                ourDiscountCost
        );

        // ---------------------------------------------------------
        // 4. Calculate merchant payout
        // ---------------------------------------------------------

        BigDecimal merchantPayout =
                merchantTotalPrice
                        .subtract(merchantGst)
                        .subtract(merchantPromotionDiscount)
                        .setScale(2, RoundingMode.HALF_UP);

        dto.setMerchantPayout(merchantPayout);

        // ---------------------------------------------------------
        // 5. Calculate profit/loss
        // ---------------------------------------------------------

        BigDecimal profitOrLoss =
                revenuePerOrder
                        .subtract(merchantPayout)
                        .subtract(ourDiscountCost)
                        .setScale(2, RoundingMode.HALF_UP);

        dto.setProfitOrLoss(profitOrLoss);

        // ---------------------------------------------------------
        // 6. Set P&L status
        // ---------------------------------------------------------

        if (profitOrLoss.compareTo(BigDecimal.ZERO) > 0) {

            dto.setProfitLossStatus("PROFIT");

        } else if (profitOrLoss.compareTo(BigDecimal.ZERO) < 0) {

            dto.setProfitLossStatus("LOSS");

        } else {

            dto.setProfitLossStatus("BREAK_EVEN");
        }

        return dto;
    }


    /**
     * Prevent NullPointerException during calculations.
     */
    private BigDecimal nullSafe(BigDecimal value) {

        return value == null
                ? BigDecimal.ZERO
                : value;
    }

    /**
     * Calculate date range based on the filter option.
     *
     * @param filter The date range filter
     * @return Array containing [fromDate, toDate]
     */
    private LocalDate[] calculateDateRange(DateRangeFilter filter) {
        LocalDate today = LocalDate.now();
        LocalDate fromDate;
        LocalDate toDate;

        switch (filter) {
            case TODAY:
                fromDate = today;
                toDate = today;
                break;

            case YESTERDAY:
                fromDate = today.minusDays(1);
                toDate = today.minusDays(1);
                break;

            case THIS_WEEK:
                fromDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                toDate = today;
                break;

            case LAST_WEEK:
                fromDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1);
                toDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusDays(1);
                break;

            case THIS_MONTH:
                fromDate = today.with(TemporalAdjusters.firstDayOfMonth());
                toDate = today;
                break;

            case LAST_MONTH:
                fromDate = today.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
                toDate = today.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
                break;

            case LAST_3_MONTHS:
                fromDate = today.minusMonths(3).with(TemporalAdjusters.firstDayOfMonth());
                toDate = today;
                break;

            case LAST_6_MONTHS:
                fromDate = today.minusMonths(6).with(TemporalAdjusters.firstDayOfMonth());
                toDate = today;
                break;

            default:
                fromDate = null;
                toDate = null;
        }

        return new LocalDate[]{fromDate, toDate};
    }

    /**
     * Fetch outlet details from FM service using Feign client.
     *
     * @param outletIds List of outlet IDs to fetch details for
     * @return Map of outlet ID to OutletDetails
     */
    private Map<Integer, OutletDetails> fetchOutletDetails(List<Integer> outletIds) {
        Map<Integer, OutletDetails> outletDetailsMap = new HashMap<>();

        if (outletIds == null || outletIds.isEmpty()) {
            return outletDetailsMap;
        }

        try {
            // Use the existing Feign client method to fetch outlet details
            CoOutletDetailsRequestDto request = new CoOutletDetailsRequestDto();
            request.setOutletIds(outletIds);

            List<CoFmOutletDetailsDto> outletDetailsList =
                    fmFeignClient.getOutletDetailsByIds(request);

            if (outletDetailsList != null) {
                for (CoFmOutletDetailsDto outletDto : outletDetailsList) {
                    OutletDetails details = new OutletDetails();
                    details.outletId = outletDto.getOutletId();
                    details.outletName = outletDto.getOutletName();
                    outletDetailsMap.put(details.outletId, details);
                }
            }
        } catch (Exception e) {
            log.error("Error fetching outlet details from FM service: {}", e.getMessage());
            // Return empty map on error, will result in null outlet details in calculations
        }

        return outletDetailsMap;
    }

    /**
     * Inner class to hold outlet details fetched from FM service.
     */
    private static class OutletDetails {
        Integer outletId;
        String outletName;
    }
}