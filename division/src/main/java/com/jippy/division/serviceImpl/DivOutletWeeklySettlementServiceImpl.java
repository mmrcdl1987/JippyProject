package com.jippy.division.serviceImpl;


import com.jippy.division.constants.DivAppConstants;
import com.jippy.division.dto.DivFmApiResponse;
import com.jippy.division.dto.DivFmOutletDto;
import com.jippy.division.dto.DivOutletWeeklySettlementResponseDto;
import com.jippy.division.entity.DivOutletWeeklySettlement;
import com.jippy.division.enums.DivSettlementFilter;
import com.jippy.division.exception.ResourceNotFoundException;
import com.jippy.division.mapper.DivOutletWeeklySettlementMapper;
import com.jippy.division.repositary.DivOutletWeeklySettlementRepository;
import com.jippy.division.service.DivOutletWeeklySettlementService;
import lombok.RequiredArgsConstructor;
import com.jippy.division.feignClient.FMFeignClient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DivOutletWeeklySettlementServiceImpl implements DivOutletWeeklySettlementService {

    private final DivOutletWeeklySettlementRepository divOutletWeeklySettlementRepository;

    private final DivOutletWeeklySettlementMapper divOutletWeeklySettlementMapper;

    private final FMFeignClient fmFeignClient;

    private final EmailService emailService;

    @Override
    public DivOutletWeeklySettlementResponseDto getOutletWeeklySettlement(Integer weeklySettlementId) {

        log.info("Fetching outlet weekly settlement details for weeklySettlementId : {}", weeklySettlementId);

        DivOutletWeeklySettlement settlement = divOutletWeeklySettlementRepository.findById(weeklySettlementId).orElseThrow(() -> {

            log.error("Outlet weekly settlement not found with weeklySettlementId : {}", weeklySettlementId);

            return new ResourceNotFoundException("Outlet weekly settlement not found with weeklySettlementId : " + weeklySettlementId);
        });

        log.info("Successfully fetched outlet weekly settlement details for weeklySettlementId : {}", weeklySettlementId);

        return divOutletWeeklySettlementMapper.convertToResponseDto(settlement);
    }

    @Override
    public List<DivOutletWeeklySettlementResponseDto> getWeeklySettlements(Integer merchantId, Integer outletId, DivSettlementFilter filter) {

        log.info("WEEKLY_SETTLEMENT_START | merchantId={} | outletId={} | filter={}", merchantId, outletId, filter);

        try {

            // Step 1 : Validate Request
            validateRequest(merchantId, outletId, filter);

            // Step 2 : Validate Merchant & Fetch Merchant Outlets
            List<DivFmOutletDto> merchantOutlets = validateMerchantAndGetOutlets(merchantId);

            // Step 3 : Calculate Filter Dates
            LocalDate fromDate = null;
            LocalDate toDate = null;

            switch (filter) {

                case ALL -> {

                    log.info("FILTER_APPLIED | filter=ALL");
                }

                case LAST_WEEK -> {

                    LocalDate currentWeekMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

                    fromDate = currentWeekMonday.minusWeeks(1);
                    toDate = currentWeekMonday.minusDays(1);

                    log.info("FILTER_APPLIED | LAST_WEEK | fromDate={} | toDate={}", fromDate, toDate);
                }

                case LAST_MONTH -> {

                    YearMonth lastMonth = YearMonth.now().minusMonths(1);

                    fromDate = lastMonth.atDay(1);
                    toDate = lastMonth.atEndOfMonth();

                    log.info("FILTER_APPLIED | LAST_MONTH | fromDate={} | toDate={}", fromDate, toDate);
                }

                default -> throw new IllegalArgumentException("Invalid filter");
            }

            List<DivOutletWeeklySettlement> settlements;

            if (outletId != null) {

                // Validate outlet belongs to merchant
                validateOutletBelongsToMerchant(merchantId, outletId, merchantOutlets);

                log.info("FETCHING_SETTLEMENTS | outletId={} | filter={}", outletId, filter);

                if (filter == DivSettlementFilter.ALL) {

                    settlements = divOutletWeeklySettlementRepository.findByOutletId(outletId);

                } else {

                    settlements = divOutletWeeklySettlementRepository.findByOutletIdAndWeekEndDateBetween(outletId, fromDate, toDate);
                }

            } else {

                List<Integer> outletIds = merchantOutlets.stream().map(DivFmOutletDto::getOutletId).toList();

                log.info("FETCHING_SETTLEMENTS | merchantId={} | outletCount={} | filter={}", merchantId, outletIds.size(), filter);

                if (filter == DivSettlementFilter.ALL) {

                    settlements = divOutletWeeklySettlementRepository.findByOutletIdIn(outletIds);

                } else {

                    settlements = divOutletWeeklySettlementRepository.findByOutletIdInAndWeekEndDateBetween(outletIds, fromDate, toDate);
                }
            }

            if (settlements == null || settlements.isEmpty()) {

                log.info("NO_SETTLEMENTS_FOUND | merchantId={} | outletId={}", merchantId, outletId);

                return List.of();
            }

            log.info("SETTLEMENTS_FETCHED_SUCCESSFULLY | count={}", settlements.size());

            return settlements.stream().map(divOutletWeeklySettlementMapper::convertToResponseDto).toList();

        } catch (Exception ex) {

            log.error("SERVICE_ERROR | WEEKLY_SETTLEMENT_FAILED | merchantId={} | outletId={}", merchantId, outletId, ex);

            throw ex;
        }
    }

    private void validateRequest(Integer merchantId, Integer outletId, DivSettlementFilter filter) {

        log.debug("VALIDATION_START | merchantId={} | outletId={} | filter={}", merchantId, outletId, filter);

        if (merchantId == null || merchantId <= 0) {

            log.warn("VALIDATION_FAILED | INVALID_MERCHANT_ID | merchantId={}", merchantId);

            throw new IllegalArgumentException("Merchant id is required");
        }

        if (outletId != null && outletId <= 0) {

            log.warn("VALIDATION_FAILED | INVALID_OUTLET_ID | outletId={}", outletId);

            throw new IllegalArgumentException("Invalid outlet id");
        }

        if (filter == null) {

            log.warn("VALIDATION_FAILED | FILTER_REQUIRED");

            throw new IllegalArgumentException("Filter is required");
        }

        log.debug("VALIDATION_SUCCESS");
    }

    private List<DivFmOutletDto> validateMerchantAndGetOutlets(Integer merchantId) {

        log.info("VALIDATION_START | MERCHANT_ID={}", merchantId);

        DivFmApiResponse<List<DivFmOutletDto>> response = fmFeignClient.getOutletsByMerchantId(merchantId);

        if (response == null) {

            log.error("FM_RESPONSE_NULL");

            throw new ResourceNotFoundException("Unable to fetch merchant outlets");
        }

        if (Boolean.FALSE.equals(response.getSuccess())) {

            log.warn("MERCHANT_VALIDATION_FAILED");

            throw new ResourceNotFoundException(response.getMessage());
        }

        List<DivFmOutletDto> outlets = response.getData();

        if (outlets == null || outlets.isEmpty()) {

            log.warn("NO_OUTLETS_FOUND");

            throw new ResourceNotFoundException("Invalid merchant id");
        }

        log.info("VALIDATION_SUCCESS | outletCount={}", outlets.size());

        return outlets;
    }

    private void validateOutletBelongsToMerchant(Integer merchantId, Integer outletId, List<DivFmOutletDto> merchantOutlets) {

        log.debug("VALIDATING_OUTLET | merchantId={} | outletId={}", merchantId, outletId);

        boolean validOutlet = merchantOutlets.stream().anyMatch(outlet -> outlet.getOutletId().equals(outletId));

        if (!validOutlet) {

            log.warn("OUTLET_MERCHANT_MISMATCH");

            throw new ResourceNotFoundException("Outlet does not belong to merchant");
        }

        log.info("VALIDATION_SUCCESS | outlet belongs to merchant");
    }


//    @Override
//    public void sendOutletSettlementMail(Integer weeklySettlementId) {
//        System.out.println("schedular for every 1 min");
//        DivOutletWeeklySettlement settlement =
//                divOutletWeeklySettlementRepository.
//                        findById(weeklySettlementId).orElseThrow(
//                                () -> new ResourceNotFoundException("Settlement Not Found"));
//
//        emailService.sendSettlementMail(settlement);
//       }
//        ----------------------------------------------------------------


    //     Instead of controller [Schedular] is calling this service
    @Override
    public void sendOutletSettlementMail() {

        List<DivOutletWeeklySettlement> settlements =
                divOutletWeeklySettlementRepository.
                        findByPaymentStatusAndEmailStatus(DivAppConstants.PAYMENT_STATUS_PAID,
                                DivAppConstants.EMAIL_STATUS_PENDING);

        if (settlements.isEmpty()) {
            log.info("No eligible settlements found");
            return;
        }

        for (DivOutletWeeklySettlement settlement : settlements) {

            try {

//                 sending mail / calling mail service
                emailService.sendSettlementMail(settlement);

//                settlement.setEmailStatus("EMAIL_SENT");
                settlement.setEmailStatus(DivAppConstants.EMAIL_STATUS_SENT);

                divOutletWeeklySettlementRepository.save(settlement);

            } catch (Exception e) {
                log.error("Failed to send mail for settlementId : {}", settlement.getWeeklySettlementId(), e);
            }
        }

    }


//    @Scheduled(cron = "0 * * * * *")
//    public void scheduleSettlementMail() {
//        System.out.println("schedular for every minute");
//        sendOutletSettlementMail(1);
//
// }
//
}