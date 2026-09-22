package com.jippy.foodandmart.serviceImpl;


import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.entity.BannerSlotDay;
import com.jippy.foodandmart.feignClients.CustomerAndOrderFeignClient;
import com.jippy.foodandmart.mapper.FmSettlementMapper;
import com.jippy.foodandmart.projections.*;
import com.jippy.foodandmart.repository.BannerSlotDayRepository;
import com.jippy.foodandmart.repository.FmOutletRepository;
import com.jippy.foodandmart.repository.FmProductRepository;
import com.jippy.foodandmart.service.FmSettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class FmSettlementServiceImpl implements FmSettlementService {

    private final FmOutletRepository fmOutletRepository;
    private final FmProductRepository fmProductRepository;

    /*
     * Feign client used to communicate with
     * Customer and Order microservice.
     */
    private final CustomerAndOrderFeignClient coSettlementFeignClient;

    private final BannerSlotDayRepository bannerSlotDayRepository;

    /**
     * Fetches settlement details for all outlets
     * belonging to the given merchant.
     * <p>
     * Flow:
     * <p>
     * 1. Receive merchantId.
     * <p>
     * 2. Fetch all outlets belonging to the merchant
     * from FM database.
     * <p>
     * 3. For every outlet, send outletId to
     * Customer and Order microservice.
     * <p>
     * 4. CO calculates:
     * - merchantTotalPrice
     * - promotionDeductedAmount
     * <p>
     * 5. FM mapper calculates:
     * - GST deducted amount
     * - net settlement amount
     * <p>
     * 6. Return settlement details for all outlets.
     *
     * @param merchantId merchant ID
     * @return list of settlement details
     */
    @Override
    public List<FmMerchantSettlementResponseDto> getMerchantSettlementsForOutlets(Integer merchantId) {

        log.info("Starting settlement retrieval for merchantId: {}", merchantId);

        List<FmMerchantSettlementResponseDto> responseList = new ArrayList<>();

        // ---------------------------------------------------------
        // STEP 1:
        // Fetch all outlets belonging to the merchant.
        // ---------------------------------------------------------

        List<FmMerchantSettlementOutletProjection> outlets = fmOutletRepository.findSettlementOutletsByMerchantId(merchantId);

        // ---------------------------------------------------------
        // STEP 2:
        // Check whether outlets are available.
        // ---------------------------------------------------------

        if (outlets == null || outlets.isEmpty()) {

            log.info("No outlets found for merchantId: {}", merchantId);

            return responseList;
        }

        log.info("Found {} outlets for merchantId: {}", outlets.size(), merchantId);

        // ---------------------------------------------------------
        // STEP 3:
        // Process each outlet.
        // ---------------------------------------------------------

        for (FmMerchantSettlementOutletProjection outlet : outlets) {

            if (outlet == null || outlet.getOutletId() == null) {

                log.warn("Skipping outlet because outlet details or " + "outletId is null for merchantId: {}", merchantId);

                continue;
            }

            Integer outletId = outlet.getOutletId();

            log.info("Processing settlement for outletId: {} " + "and merchantId: {}", outletId, merchantId);

            // -----------------------------------------------------
            // STEP 4:
            // Call Customer and Order microservice.
            //
            // CO returns:
            // - merchantTotalPrice
            // - promotionDeductedAmount
            // -----------------------------------------------------

            CoSettlementCalculationDto coSettlement = null;

            try {

                coSettlement = coSettlementFeignClient.calculateSettlementForOutlet(outletId);

                log.info("Settlement calculation received from CO " + "for outletId: {}", outletId);

            } catch (Exception exception) {

                /*
                 * If CO call fails, log the error.
                 *
                 * We continue processing the remaining outlets
                 * instead of failing the complete merchant request.
                 */
                log.error("Failed to fetch settlement calculation " + "from CO for outletId: {}", outletId, exception);
            }

            // -----------------------------------------------------
            // STEP 5:
            // Build final settlement response.
            //
            // FmSettlementMapper calculates:
            //
            // GST:
            // merchantTotalPrice * 5%
            //
            // Net settlement:
            // merchantTotalPrice
            // - GST
            // - promotionDeductedAmount
            // -----------------------------------------------------

            FmMerchantSettlementResponseDto response = FmSettlementMapper.mapToSettlementResponse(outlet, coSettlement);

            responseList.add(response);

            log.info("Settlement response created successfully " + "for outletId: {}", outletId);
        }

        // ---------------------------------------------------------
        // STEP 6:
        // Return all outlet settlement details.
        // ---------------------------------------------------------

        log.info("Settlement retrieval completed for merchantId: {}. " + "Total outlets processed: {}", merchantId, responseList.size());

        return responseList;
    }

//    ====================================================================================
//    ====================================================================================

    /**
     * Fetches merchant settlement details for the given outlet IDs.
     * <p>
     * The repository performs the required joins between:
     * outlets -> merchants -> merchant address -> city.
     * <p>
     * GST is only fetched here.
     * GST calculation will be handled in the CO microservice.
     *
     * @param requestDto settlement request containing outlet IDs and date range
     * @return merchant settlement details
     */
    @Override
    public List<FmMerchantSettlementBetweenDatesResponseDto> getMerchantSettlementBetweenDates(FmMerchantSettlementBetweenDatesRequestDto requestDto) {

        log.info("Fetching merchant settlement. startDate: {}, endDate: {}, weekSlotDaysId: {}", requestDto.getStartDate(), requestDto.getEndDate(), requestDto.getWeekSlotDaysId());

        /*
         * Validate settlement period.
         *
         * Option 1:
         * startDate + endDate
         *
         * Option 2:
         * weekSlotDaysId
         */
        LocalDate[] settlementDates = resolveSettlementDates(requestDto);

        LocalDate startDate = settlementDates[0];
        LocalDate endDate = settlementDates[1];
        /*
         * Get all outlet IDs internally from FM.
         */
        List<Integer> outletIds = fmOutletRepository.findAllOutletIds();

        if (outletIds == null || outletIds.isEmpty()) {

            log.warn("No outlet IDs found for merchant settlement");

            return List.of();
        }

        List<FmOutletGstProjection> gstProjections =
                fmOutletRepository.findGstAppliedByOutletIds(outletIds);

        /*
         * Fetch merchant information for all outlets.
         *
         * This gives the mapping:
         *
         * outletId -> merchantId
         *
         * Example:
         *
         * outlet 3 -> merchant 3
         * outlet 4 -> merchant 3
         * outlet 5 -> merchant 2
         */
        List<FmMerchantSettlementBetweenDatesProjection> projections
                = fmOutletRepository.findMerchantSettlementBetweenDates(outletIds);

        if (projections == null || projections.isEmpty()) {

            log.warn("No merchant/outlet details found");

            return List.of();
        }

        /*
         * Prepare request for CO.
         *
         * CO works with outlet IDs.
         */
        CoMerchantSettlementBetweenDatesRequestDto coRequest = new CoMerchantSettlementBetweenDatesRequestDto();

        coRequest.setOutletIds(outletIds);
        coRequest.setStartDate(startDate);
        coRequest.setEndDate(endDate);

        log.info("Calling CO settlement API. outletIds: {}, startDate: {}, endDate: {}", outletIds, startDate, endDate);

        /*
         * CO returns settlement details per outlet.
         *
         * Example:
         *
         * outlet 3 -> 1393
         * outlet 4 -> 500
         * outlet 5 -> 700
         */
        List<CoMerchantSettlementSummaryDto> coSettlementList = coSettlementFeignClient.getMerchantSettlement(coRequest);

        if (coSettlementList == null || coSettlementList.isEmpty()) {

            log.warn("No settlement data returned from CO for period {} to {}", startDate, endDate);

            return List.of();
        }

        List<FmMerchantSettlementBetweenDatesResponseDto> responseList = new ArrayList<>();

        /*
         * Loop through CO settlement data first.
         *
         * CO gives us the actual outlets that have settlement data.
         */
        for (CoMerchantSettlementSummaryDto coSettlement : coSettlementList) {

            FmMerchantSettlementBetweenDatesProjection merchantProjection = null;

            for (FmMerchantSettlementBetweenDatesProjection projection : projections) {

                if (projection.getOutletId().equals(coSettlement.getOutletId())) {
                    merchantProjection = projection;
                    break;
                }
            }

            if (merchantProjection == null) {

                log.warn("No merchant found for outletId: {}",
                        coSettlement.getOutletId());

                continue;
            }

            FmMerchantSettlementBetweenDatesResponseDto dto =
                    FmSettlementMapper.findOrCreateMerchantSettlement(
                            responseList,
                            merchantProjection
                    );

            FmSettlementMapper.addOutletSettlementToMerchant(
                    dto,
                    coSettlement,
                    gstProjections
            );
        }

        for (FmMerchantSettlementBetweenDatesResponseDto dto : responseList) {

            FmSettlementMapper.calculateMerchantSettlementFinalAmount(dto);
        }
        log.info("Fetched {} merchant settlement records for period {} to {}", responseList.size(), startDate, endDate);

        return responseList;
    }

// ========================================================================================
// ========================================================================================
@Override
public FmOutletAndOrdersCountForSettlementResponseDto getOutletAndOrdersSettlementDetails(
        FmMerchantSettlementBetweenDatesRequestDto requestDto) {

    LocalDate startDate = requestDto.getStartDate();
    LocalDate endDate = requestDto.getEndDate();

    if (requestDto.getWeekSlotDaysId() != null) {

        FmSettlementWeekSlotProjection weekSlot =
                bannerSlotDayRepository.findSettlementWeekSlot(
                        requestDto.getWeekSlotDaysId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Settlement week slot not found for ID: "
                                        + requestDto.getWeekSlotDaysId()
                        )
                );

        startDate = weekSlot.getSlotStartDate();
        endDate = weekSlot.getSlotEndDate();
    }

    if (startDate == null || endDate == null) {
        throw new IllegalArgumentException(
                "Either startDate and endDate or weekSlotDaysId must be provided"
        );
    }
        log.info("Fetching outlet and order settlement details between {} and {}", startDate, endDate);

        // Call CO microservice to get order and outlet settlement details.
        FmOutletAndOrdersCountForSettlementResponseDto coResponse = coSettlementFeignClient.getOutletAndOrdersSettlementDetails(startDate, endDate);

        // Create FM response.
        FmOutletAndOrdersCountForSettlementResponseDto response = new FmOutletAndOrdersCountForSettlementResponseDto();

        // Set settlement period.
        response.setStartDate(startDate);
        response.setEndDate(endDate);

        // Copy CO response values.
        if (coResponse != null) {

            response.setOutletsCountsBetweenDates(coResponse.getOutletsCountsBetweenDates());

            response.setOrderCountsBetweenDates(coResponse.getOrderCountsBetweenDates());

            response.setMerchantTotalPriceBetweenDates(coResponse.getMerchantTotalPriceBetweenDates());

//            response.setPromotionDeductedAmount(coResponse.getPromotionDeductedAmount());

            response.setOutletIds(coResponse.getOutletIds());

            response.setOutletSettlements(coResponse.getOutletSettlements());
        }

        // Fetch GST applicability for the outlets.
        List<Integer> outletIds = response.getOutletIds();

        List<FmOutletGstProjection> gstProjections = List.of();

        if (outletIds != null && !outletIds.isEmpty()) {

            gstProjections = fmOutletRepository.findGstAppliedByOutletIds(outletIds);
        }

        // Calculate GST outlet-wise.
//        BigDecimal gstDeductedAmount = FmSettlementHelper.calculateGstAmount(response.getOutletSettlements(), gstProjections);

        // Set GST details.
//        response.setGstDeductedAmount(gstDeductedAmount);

//        response.setGstApplied(gstDeductedAmount.compareTo(BigDecimal.ZERO) > 0);

        // Get merchant total.
        BigDecimal merchantTotalPrice = response.getMerchantTotalPriceBetweenDates() != null ? response.getMerchantTotalPriceBetweenDates() : BigDecimal.ZERO;

        // Get promotion deduction.
//        BigDecimal promotionDeductedAmount = response.getPromotionDeductedAmount() != null ? response.getPromotionDeductedAmount() : BigDecimal.ZERO;

        // Calculate final settlement.
//        BigDecimal netSettlementAmount = merchantTotalPrice.subtract(promotionDeductedAmount).subtract(gstDeductedAmount);

//        response.setNetTotalSettlementsAmountAfterDeductionsBetweenDates(netSettlementAmount);

        log.info("Settlement calculation completed between {} and {}. " + "Merchant Total: {}, Promotion: {}, GST: {}, Net Settlement: {}",
                startDate, endDate, merchantTotalPrice);

        return response;
    }
//    ========================================================================
@Override
public FmMerchantSettlementForOutletResponseDto
getMerchantSettlementForOutletBetweenDates(
        FmMerchantSettlementForOutletRequestDto requestDto) {

    if (requestDto.getOutletId() == null) {
        throw new IllegalArgumentException("Outlet ID is required");
    }

    boolean hasDates =
            requestDto.getStartDate() != null
                    && requestDto.getEndDate() != null;

    boolean hasWeekSlot =
            requestDto.getWeekSlotDaysId() != null;

    if (!hasDates && !hasWeekSlot) {
        throw new IllegalArgumentException(
                "Either startDate and endDate or weekSlotDaysId is required"
        );
    }

    if (hasDates && hasWeekSlot) {
        throw new IllegalArgumentException(
                "Provide either startDate and endDate or weekSlotDaysId, not both"
        );
    }

    LocalDate startDate;
    LocalDate endDate;

    if (hasWeekSlot) {

        Optional<FmSettlementWeekSlotProjection> weekSlot =
                bannerSlotDayRepository.findSettlementWeekSlot(
                        requestDto.getWeekSlotDaysId()
                );

        if (weekSlot.isEmpty()) {
            throw new IllegalArgumentException(
                    "Settlement week slot not found for weekSlotDaysId: "
                            + requestDto.getWeekSlotDaysId()
            );
        }

        startDate = weekSlot.get().getSlotStartDate();
        endDate = weekSlot.get().getSlotEndDate();

    } else {

        startDate = requestDto.getStartDate();
        endDate = requestDto.getEndDate();
    }

    if (endDate.isBefore(startDate)) {
        throw new IllegalArgumentException(
                "End date cannot be before start date"
        );
    }

    CoMerchantSettlementForOutletRequestDto coRequest =
            new CoMerchantSettlementForOutletRequestDto();

    coRequest.setOutletId(requestDto.getOutletId());
    coRequest.setStartDate(startDate);
    coRequest.setEndDate(endDate);

    CoMerchantSettlementSummaryDto coResponse =
            coSettlementFeignClient
                    .getMerchantSettlementForOutletBetweenDates(
                            coRequest
                    );

    if (coResponse == null) {
        return null;
    }

    // CO response → FM response
    FmMerchantSettlementForOutletResponseDto response =
            FmSettlementMapper.toSettlementResponse(coResponse);

    // Fetch GST applicability
    List<FmOutletGstProjection> gstProjection =
            fmOutletRepository.findGstAppliedByOutletIds(
                    List.of(requestDto.getOutletId())
            );

    Boolean gstApplied =
            gstProjection.isEmpty()
                    ? false
                    : gstProjection.get(0).getGstApplied();

    // Existing mapper calculates GST and final settlement
    FmSettlementMapper.calculateOutletSettlementFinalAmount(
            response,
            gstApplied
    );

    return response;
}

    //    ====================================================================================
//    ================================ Helper Methods ===================================
//    ====================================================================================
    public final class FmSettlementHelper {

        private FmSettlementHelper() {
        }

        /**
         * Calculates GST for each outlet.
         * <p>
         * GST is calculated only when the outlet has
         * is_gst_applied = true.
         * <p>
         * Taxable Amount =
         * Merchant Total - Promotion Deduction
         * <p>
         * GST =
         * Taxable Amount × GST Rate
         */
        public static BigDecimal calculateGstAmount(List<FmOutletSettlementDto> outletSettlements, List<FmOutletGstProjection> gstProjections) {

            if (outletSettlements == null || outletSettlements.isEmpty()) {
                return BigDecimal.ZERO;
            }

            BigDecimal totalGstAmount = BigDecimal.ZERO;

            for (FmOutletSettlementDto outletSettlement : outletSettlements) {

                Integer outletId = outletSettlement.getOutletId();

                boolean outletGstApplied = false;

                // Find GST applicability for this outlet.
                for (FmOutletGstProjection gstProjection : gstProjections) {

                    if (outletId.equals(gstProjection.getOutletId())) {

                        outletGstApplied = Boolean.TRUE.equals(gstProjection.getGstApplied());

                        break;
                    }
                }

                // Calculate GST only for GST-enabled outlets.
                if (outletGstApplied) {

                    BigDecimal merchantTotal = outletSettlement.getMerchantTotalPrice() != null ? outletSettlement.getMerchantTotalPrice() : BigDecimal.ZERO;

                    BigDecimal promotion = outletSettlement.getPromotionDeductedAmount() != null ? outletSettlement.getPromotionDeductedAmount() : BigDecimal.ZERO;

                    // Taxable amount = merchant total - promotion.
                    BigDecimal taxableAmount = merchantTotal.subtract(promotion);

                    // GST = taxable amount × 5%.
                    BigDecimal gstAmount = taxableAmount.multiply(FmAppConstants.GST_RATE);

                    totalGstAmount = totalGstAmount.add(gstAmount);
                }
            }

            return totalGstAmount;
        }
    }
//    ======================================================================================
//    ======================================================================================
    /**
     * Resolves the settlement start and end dates.
     *
     * The settlement period can be provided in one of two ways:
     *
     * 1. Custom date range:
     *    startDate + endDate
     *
     * 2. Week slot:
     *    weekSlotDaysId
     *
     * If weekSlotDaysId is provided, the corresponding start and end
     * dates are fetched from the settlement week slot table.
     */
    private LocalDate[] resolveSettlementDates(
            FmMerchantSettlementBetweenDatesRequestDto requestDto) {

        /*
         * Check whether a custom date range was provided.
         */
        boolean customDateRange =
                requestDto.getStartDate() != null &&
                        requestDto.getEndDate() != null;

        /*
         * Check whether a settlement week slot was provided.
         */
        boolean weekSlot =
                requestDto.getWeekSlotDaysId() != null;

        /*
         * At least one settlement period option is required.
         */
        if (!customDateRange && !weekSlot) {
            throw new IllegalArgumentException(
                    "Either startDate and endDate or weekSlotDaysId is required"
            );
        }

        /*
         * Both options cannot be provided at the same time.
         */
        if (customDateRange && weekSlot) {
            throw new IllegalArgumentException(
                    "Provide either startDate and endDate or weekSlotDaysId, not both"
            );
        }

        /*
         * Resolve the settlement dates from the selected week slot.
         */
        if (weekSlot) {

            BannerSlotDay weekSlotData =
                    bannerSlotDayRepository
                            .findById(requestDto.getWeekSlotDaysId())
                            .orElse(null);

            /*
             * Stop processing if the requested week slot does not exist.
             */
            if (weekSlotData == null) {
                throw new IllegalArgumentException(
                        "Settlement week slot not found for weekSlotDaysId: "
                                + requestDto.getWeekSlotDaysId()
                );
            }

            /*
             * Return the dates configured for the selected week slot.
             */
            return new LocalDate[]{
                    weekSlotData.getSlotStartDate(),
                    weekSlotData.getSlotEndDate()
            };
        }

        /*
         * Custom date range was provided directly in the request.
         */
        return new LocalDate[]{
                requestDto.getStartDate(),
                requestDto.getEndDate()
        };
    }
//    ==========================================================================
//    ==========================================================================
@Override
public FmMerchantSettlementForOutletOrderDetailsResponseDto
getMerchantSettlementForOutletOrderDetails(
        FmMerchantSettlementForOutletBetweenDatesRequestDto requestDto) {

    log.info(
            "Fetching merchant settlement order details for outletId: {}, weekSlotDaysId: {}, startDate: {}, endDate: {}",
            requestDto.getOutletId(),
            requestDto.getWeekSlotDaysId(),
            requestDto.getStartDate(),
            requestDto.getEndDate()
    );


    // ---------------------------------------------------------
    // 1. Date range
    // ---------------------------------------------------------

    boolean hasDates =
            requestDto.getStartDate() != null
                    && requestDto.getEndDate() != null;

    boolean hasWeekSlot =
            requestDto.getWeekSlotDaysId() != null;

    if (!hasDates && !hasWeekSlot) {
        throw new IllegalArgumentException(
                "Either startDate and endDate or weekSlotDaysId is required"
        );
    }

    if (hasDates && hasWeekSlot) {
        throw new IllegalArgumentException(
                "Provide either startDate and endDate or weekSlotDaysId, not both"
        );
    }

    LocalDate startDate = requestDto.getStartDate();
    LocalDate endDate = requestDto.getEndDate();

    if (requestDto.getWeekSlotDaysId() != null) {

        FmSettlementWeekSlotProjection weekSlot =
                bannerSlotDayRepository.findSettlementWeekSlot(
                        requestDto.getWeekSlotDaysId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Settlement week slot not found for weekSlotDaysId: "
                                        + requestDto.getWeekSlotDaysId()
                        )
                );

        startDate = weekSlot.getSlotStartDate();
        endDate = weekSlot.getSlotEndDate();
    }

    LocalDateTime startDateTime = startDate.atStartOfDay();

    LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

    // ---------------------------------------------------------
    // 2. Get order items from CO
    // ---------------------------------------------------------

    List<FmMerchantSettlementOrderDetailsDto> coOrders =
            coSettlementFeignClient.getMerchantSettlementOrderDetails(
                    requestDto.getOutletId(),
                    startDateTime,
                    endDateTime
            );
    // ---------------------------------------------------------
    // 2.1 Fetch GST applicability from OUTLET
    // ---------------------------------------------------------

    List<FmOutletGstProjection> gstProjection =
            fmOutletRepository.findGstAppliedByOutletIds(
                    List.of(requestDto.getOutletId())
            );

    Boolean gstApplied =
            gstProjection.isEmpty()
                    ? false
                    : gstProjection.get(0).getGstApplied();

    // ---------------------------------------------------------
    // 3. Main response
    // ---------------------------------------------------------

    FmMerchantSettlementForOutletOrderDetailsResponseDto response =
            new FmMerchantSettlementForOutletOrderDetailsResponseDto();

    response.setOutletId(requestDto.getOutletId());
    response.setStartDate(startDate);
    response.setEndDate(endDate);

    List<FmMerchantSettlementOrderDetailsResponseDto> orders =
            new ArrayList<>();

    // ---------------------------------------------------------
    // 4. No orders
    // ---------------------------------------------------------

    if (coOrders == null || coOrders.isEmpty()) {

        response.setOrders(orders);

        log.info(
                "No delivered settlement orders found for outletId: {}",
                requestDto.getOutletId()
        );

        return response;
    }

    // ---------------------------------------------------------
    // 5. Process orders
    // ---------------------------------------------------------

    int index = 0;

    while (index < coOrders.size()) {

        FmMerchantSettlementOrderDetailsDto firstOrder =
                coOrders.get(index);

        String orderId = firstOrder.getOrderId();

        List<FmMerchantSettlementProductDetailsDto> products =
                new ArrayList<>();

//        Boolean gstApplied = false;

        BigDecimal merchantTotalPrice = BigDecimal.ZERO;

        BigDecimal promotionDeductedAmount =
                firstOrder.getPromotionDeductedAmount() == null
                        ? BigDecimal.ZERO
                        : firstOrder.getPromotionDeductedAmount();

        // -----------------------------------------------------
        // Process all items belonging to this order
        // -----------------------------------------------------

        while (index < coOrders.size()
                && coOrders.get(index)
                .getOrderId()
                .equals(orderId)) {

            FmMerchantSettlementOrderDetailsDto coOrder =
                    coOrders.get(index);
            merchantTotalPrice = merchantTotalPrice.add(
                    coOrder.getMerchantTotalPrice() == null
                            ? BigDecimal.ZERO
                            : coOrder.getMerchantTotalPrice()
            );

            log.debug(
                    "Processing order item. orderId: {}, productId: {}, variantOptionId: {}",
                    coOrder.getOrderId(),
                    coOrder.getProductId(),
                    coOrder.getVariantOptionId()
            );

            // -------------------------------------------------
            // Get FM product/variant details
            // -------------------------------------------------

            FmMerchantSettlementProductProjection fmDetails =
                    fmProductRepository.getMerchantSettlementProductDetails(
                            requestDto.getOutletId(),
                            coOrder.getProductId(),
                            coOrder.getVariantOptionId()
                    );

            if (fmDetails == null) {

                log.warn(
                        "FM product details not found. productId: {}, variantOptionId: {}",
                        coOrder.getProductId(),
                        coOrder.getVariantOptionId()
                );

                index++;
                continue;
            }

            // -------------------------------------------------
            // GST
            // -------------------------------------------------
//
//            gstApplied =
//                    Boolean.TRUE.equals(fmDetails.getGstApplied());

            // -------------------------------------------------
            // Find existing product
            // -------------------------------------------------

            FmSettlementMapper.addProductVariant(
                    products,
                    fmDetails,
                    coOrder.getQuantity()
            );

            index++;
        }

        // -----------------------------------------------------
        // Create final order response
        // -----------------------------------------------------

        FmMerchantSettlementOrderDetailsResponseDto dto =
                FmSettlementMapper.toOrderDetailsResponse(
                        firstOrder,
                        products,
                        gstApplied,
                        merchantTotalPrice,
                        promotionDeductedAmount
                );

        orders.add(dto);
    }

    // ---------------------------------------------------------
    // 6. Set orders
    // ---------------------------------------------------------

    response.setOrders(orders);

    log.info(
            "Merchant settlement order details completed successfully. " +
                    "outletId: {}, totalOrders: {}",
            requestDto.getOutletId(),
            orders.size()
    );

    return response;
}
}