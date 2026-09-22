package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.dto.*;
import com.jippy.customerandorder.feignClients.FMFeignClient;
import com.jippy.customerandorder.iservice.CoSettlementService;
import com.jippy.customerandorder.mapper.CoMerchantSettlementMapper;
import com.jippy.customerandorder.projection.*;
import com.jippy.customerandorder.repository.CoOrderRepository;
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
public class CoSettlementServiceImpl implements CoSettlementService {

    private final CoOrderRepository coOrderRepository;

    private final FMFeignClient fmFeignClient;


    /**
     * Calculates settlement amounts for an outlet.
     * <p>
     * The repository performs one query and returns:
     * <p>
     * 1. Total merchant price from order items.
     * 2. Total merchant promotion discount.
     * <p>
     * GST is not calculated here because GST information
     * belongs to the FM outlet.
     *
     * @param outletId outlet ID
     * @return settlement calculation details
     */
    @Override
    public CoSettlementCalculationDto calculateSettlementForOutlet(Integer outletId) {

        log.info("Starting settlement calculation for outletId: {}", outletId);

        // Create response object
        CoSettlementCalculationDto response = new CoSettlementCalculationDto();

        // Default values
        response.setMerchantTotalPrice(BigDecimal.ZERO);
        response.setPromotionDeductedAmount(BigDecimal.ZERO);

        // ---------------------------------------------------------
        // Fetch all settlement amounts using ONE query
        // ---------------------------------------------------------

        CoMerchantSettlementProjection projection = coOrderRepository.getMerchantSettlementDetails(outletId);

        // Check whether projection contains data
        if (projection == null) {

            log.info("No settlement data found for outletId: {}", outletId);

            return response;
        }

        // ---------------------------------------------------------
        // Get merchant total price
        // ---------------------------------------------------------

        if (projection.getMerchantTotalPrice() != null) {

            response.setMerchantTotalPrice(projection.getMerchantTotalPrice());
        }

        // ---------------------------------------------------------
        // Get merchant promotion discount
        // ---------------------------------------------------------

        if (projection.getPromotionDiscount() != null) {

            response.setPromotionDeductedAmount(projection.getPromotionDiscount());
        }

        log.info("Merchant total price for outletId {}: {}", outletId, response.getMerchantTotalPrice());

        log.info("Promotion deducted amount for outletId {}: {}", outletId, response.getPromotionDeductedAmount());

        log.info("Settlement calculation completed for outletId: {}", outletId);

        return response;
    }

//    ====================================================================================
//    ====================================================================================

    @Override
    public List<CoMerchantSettlementSummaryDto> getMerchantSettlement(CoMerchantSettlementBetweenDatesRequestDto requestDto) {

        log.info("Fetching merchant settlement. Outlet IDs: {}, startDate: {}, endDate: {}, weekSlotDaysId: {}", requestDto.getOutletIds(), requestDto.getStartDate(), requestDto.getEndDate(), requestDto.getWeekSlotDaysId());

        LocalDate startDate = requestDto.getStartDate();
        LocalDate endDate = requestDto.getEndDate();

        /*
         * If weekSlotDaysId is provided,
         * fetch the settlement dates from FM.
         */
        if (requestDto.getWeekSlotDaysId() != null) {

            CoSettlementWeekSlotResponseDto weekSlot = fmFeignClient.getSettlementWeekSlot(requestDto.getWeekSlotDaysId());

            startDate = weekSlot.getSlotStartDate();
            endDate = weekSlot.getSlotEndDate();
        }

        if (startDate == null || endDate == null) {
            log.warn("Settlement start date or end date is missing");
            return List.of();
        }

        if (requestDto.getOutletIds() == null || requestDto.getOutletIds().isEmpty()) {

            log.warn("No outlet IDs provided for merchant settlement");
            return List.of();
        }

        List<CoMerchantSettlementForOutletProjection> projections = coOrderRepository.findMerchantSettlementForOutlets(requestDto.getOutletIds(), startDate, endDate);

        List<CoMerchantSettlementSummaryDto> responseList = new ArrayList<>();

        for (CoMerchantSettlementForOutletProjection projection : projections) {

            CoMerchantSettlementSummaryDto dto = new CoMerchantSettlementSummaryDto();

            dto.setOutletId(projection.getOutletId());

            dto.setOrderCount(projection.getOrderCount());

            dto.setMerchantTotalAmount(projection.getMerchantTotalAmount());

            dto.setPromotionDeductedAmount(projection.getPromotionDeductedAmount());

            responseList.add(dto);
        }

        log.info("Fetched {} merchant settlement records", responseList.size());

        return responseList;
    }

//    ====================================================================================
//    ====================================================================================

    /**
     * Fetches settlement details for all delivered orders
     * within the selected settlement period.
     * <p>
     * The repository query calculates:
     * <p>
     * 1. Number of unique outlets.
     * 2. Number of delivered orders.
     * 3. Total merchant amount.
     * 4. Total merchant promotion deduction.
     * <p>
     * GST is not calculated here because GST information
     * belongs to the FM service.
     */
    @Override
    public CoOutletAndOrdersSettlementResponseDto getOutletAndOrdersSettlementDetails
                                        (LocalDate startDate, LocalDate endDate) {

        log.info("Fetching outlet and order settlement details between {} and {}", startDate, endDate);

        /*
         * Fetch settlement calculations from CO database.
         *
         * The repository returns:
         * 1. Unique outlet count
         * 2. Delivered order count
         * 3. Total merchant amount
         * 4. Merchant promotion deduction
         */
        CoOutletAndOrdersSettlementProjection projection
                = coOrderRepository.getOutletAndOrdersSettlementDetails(startDate, endDate);

        /*
         * Create response DTO.
         *
         * This DTO is returned by the CO API.
         */
        CoOutletAndOrdersSettlementResponseDto response = new CoOutletAndOrdersSettlementResponseDto();

        /*
         * Set default values.
         *
         * This prevents null values when no delivered
         * orders are found for the selected period.
         */
        response.setOutletsCountsBetweenDates(0);
        response.setOrderCountsBetweenDates(0);
        response.setMerchantTotalPriceBetweenDates(BigDecimal.ZERO);
        response.setPromotionDeductedAmount(BigDecimal.ZERO);

        /*
         * Copy calculated values from the projection
         * when repository data is available.
         */
        if (projection != null) {

            response.setOutletsCountsBetweenDates(projection.getOutletsCountsBetweenDates());

            response.setOrderCountsBetweenDates(projection.getOrderCountsBetweenDates());

            response.setMerchantTotalPriceBetweenDates(projection.getMerchantTotalPriceBetweenDates());

            response.setPromotionDeductedAmount(projection.getPromotionDeductedAmount());
        }
        // Get the actual outlet IDs from delivered orders
        List<Integer> outletIds =
                coOrderRepository.findDeliveredOrderOutletIdsBetweenDates(
                        startDate,
                        endDate
                );

        response.setOutletIds(outletIds);

        log.info(
                "Found {} outlets for settlement between {} and {}",
                outletIds.size(),
                startDate,
                endDate
        );
        // Fetch outlet-wise settlement details from CO database.
        List<CoOutletSettlementProjection> outletSettlementProjections =
                coOrderRepository.getOutletSettlementDetailsBetweenDates(
                        startDate,
                        endDate
                );

        // Convert outlet settlement projections to response DTOs.
        List<CoOutletSettlementDto> outletSettlements =
                CoMerchantSettlementMapper.toOutletSettlementDtoList(
                        outletSettlementProjections
                );

        // Set outlet-wise settlement details in the response.
        response.setOutletSettlements(outletSettlements);
        log.info("Successfully fetched outlet and order settlement details between {} and {}", startDate, endDate);

        return response;
    }
//    ==================================================================================
//    ==================================================================================
@Override
public CoMerchantSettlementSummaryDto getMerchantSettlementForOutlet(
        CoMerchantSettlementForOutletRequestDto requestDto) {

    Integer outletId = requestDto.getOutletId();
    LocalDate startDate = requestDto.getStartDate();
    LocalDate endDate = requestDto.getEndDate();
    Integer weekSlotDaysId = requestDto.getWeekSlotDaysId();

    /*
     * If weekSlotDaysId is provided,
     * CO calls FM to get the corresponding
     * settlement start date and end date.
     */
    if (weekSlotDaysId != null) {

        log.info(
                "Week slot ID provided: {}. Fetching settlement dates from FM.",
                weekSlotDaysId
        );

        CoSettlementWeekSlotResponseDto weekSlot =
                fmFeignClient.getSettlementWeekSlot(weekSlotDaysId);

        if (weekSlot == null) {

            log.warn(
                    "No settlement week slot found for weekSlotDaysId: {}",
                    weekSlotDaysId
            );

            return null;
        }

        startDate = weekSlot.getSlotStartDate();
        endDate = weekSlot.getSlotEndDate();

        log.info(
                "Settlement dates resolved from weekSlotDaysId: {}. " +
                        "startDate: {}, endDate: {}",
                weekSlotDaysId,
                startDate,
                endDate
        );
    }
    log.info(
            "Fetching merchant settlement for outletId: {}, startDate: {}, endDate: {}",
            outletId,
            startDate,
            endDate
    );

    Optional<CoMerchantSettlementForOutletProjection> projection =
            coOrderRepository.findMerchantSettlementForOutlet(
                    outletId,
                    startDate,
                    endDate
            );

    if (projection.isEmpty()) {

        log.warn(
                "No delivered settlement orders found for outletId: {} between {} and {}",
                outletId,
                startDate,
                endDate
        );

        return null;
    }

    CoMerchantSettlementForOutletProjection data =
            projection.get();

    /*
     * Convert the repository projection into
     * the CO response DTO.
     */
    CoMerchantSettlementSummaryDto response =
            new CoMerchantSettlementSummaryDto();

    response.setOutletId(data.getOutletId());

    response.setOrderCount(
            data.getOrderCount() == null
                    ? 0L
                    : data.getOrderCount()
    );

    response.setMerchantTotalAmount(
            data.getMerchantTotalAmount() == null
                    ? BigDecimal.ZERO
                    : data.getMerchantTotalAmount()
    );

    response.setPromotionDeductedAmount(
            data.getPromotionDeductedAmount() == null
                    ? BigDecimal.ZERO
                    : data.getPromotionDeductedAmount()
    );

    log.info(
            "Merchant settlement fetched for outletId: {}. " +
                    "OrderCount: {}, MerchantTotalAmount: {}, PromotionDeductedAmount: {}",
            response.getOutletId(),
            response.getOrderCount(),
            response.getMerchantTotalAmount(),
            response.getPromotionDeductedAmount()
    );

    return response;
}
//========================================================================================
//========================================================================================
@Override
public List<CoMerchantSettlementOrderItemDto> getMerchantSettlementOrderDetails(
        Integer outletId,
        LocalDateTime startDate,
        LocalDateTime endDate) {

    log.info(
            "Fetching merchant settlement order details for outletId: {}, startDate: {}, endDate: {}",
            outletId,
            startDate,
            endDate
    );

    List<CoMerchantSettlementOrderDetailsProjection> projections =
            coOrderRepository.getMerchantSettlementOrderDetails(
                    outletId,
                    startDate,
                    endDate
            );

    List<CoMerchantSettlementOrderItemDto> response = new ArrayList<>();

    if (projections == null || projections.isEmpty()) {
        return response;
    }

    for (CoMerchantSettlementOrderDetailsProjection projection : projections) {

        CoMerchantSettlementOrderItemDto dto =
                CoMerchantSettlementMapper.toMerchantSettlementOrderItemDto(projection);

        response.add(dto);
    }

    log.info(
            "Fetched {} merchant settlement order items for outletId: {}",
            response.size(),
            outletId
    );

    return response;
}
}