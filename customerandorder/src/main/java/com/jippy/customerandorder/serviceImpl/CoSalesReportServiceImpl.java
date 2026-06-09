package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.dto.CoDailySalesReportDto;
import com.jippy.customerandorder.dto.CoFmApiResponse;
import com.jippy.customerandorder.dto.CoFmOutletDto;
import com.jippy.customerandorder.dto.CoSalesReportResponseDto;
import com.jippy.customerandorder.enums.CoSalesReportFilter;
import com.jippy.customerandorder.exception.CoBadRequestException;
import com.jippy.customerandorder.feignClients.FMFeignClient;
import com.jippy.customerandorder.iservice.CoSalesReportService;
import com.jippy.customerandorder.mapper.CoSalesReportMapper;
import com.jippy.customerandorder.projection.CoSalesReportProjection;
import com.jippy.customerandorder.repository.CoOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CoSalesReportServiceImpl implements CoSalesReportService {

    private final CoOrderRepository coOrderRepository;

    private final FMFeignClient coFmFeignClient;

    private final CoSalesReportMapper coSalesReportMapper;

    @Override
    public CoSalesReportResponseDto getSalesReport(Integer merchantId, Integer outletId, CoSalesReportFilter filter) {

        log.info("SALES_REPORT_START | merchantId={} | outletId={} | filter={}", merchantId, outletId, filter);

        try {

            validateRequest(merchantId, outletId, filter);

            List<CoFmOutletDto> merchantOutlets = validateMerchantAndGetOutlets(merchantId);

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

                    log.info("FILTER_APPLIED | filter=LAST_WEEK | fromDate={} | toDate={}", fromDate, toDate);
                }

                case LAST_MONTH -> {

                    YearMonth lastMonth = YearMonth.now().minusMonths(1);

                    fromDate = lastMonth.atDay(1);
                    toDate = lastMonth.atEndOfMonth();

                    log.info("FILTER_APPLIED | filter=LAST_MONTH | fromDate={} | toDate={}", fromDate, toDate);
                }
                default -> {

                    log.warn("VALIDATION_FAILED | INVALID_FILTER | filter={}", filter);

                    throw new CoBadRequestException("Invalid sales report filter");
                }
            }
            List<CoSalesReportProjection> report;

            if (outletId != null) {

                validateOutletBelongsToMerchant(merchantId, outletId, merchantOutlets);

                log.info("DB_OPERATION | FETCH_OUTLET_SALES_REPORT | merchantId={} | outletId={}", merchantId, outletId);
                if (filter == CoSalesReportFilter.ALL) {

                    report = coOrderRepository.getSalesReportByOutlet(outletId);

                } else {

                    report = coOrderRepository.getSalesReportByOutletAndDateRange(outletId, fromDate, toDate);
                }

            } else {

                List<Integer> outletIds = merchantOutlets.stream().map(CoFmOutletDto::getOutletId).toList();

                log.info("DB_OPERATION | FETCH_MERCHANT_SALES_REPORT | merchantId={} | outletCount={}", merchantId, outletIds.size());
                if (filter == CoSalesReportFilter.ALL) {

                    report = coOrderRepository.getSalesReport(outletIds);

                } else {

                    report = coOrderRepository.getSalesReportByDateRange(outletIds, fromDate, toDate);
                }
            }
            log.info("DB_SUCCESS | SALES_REPORT_FETCHED | recordCount={}", report == null ? 0 : report.size());


            if (report == null || report.isEmpty()) {

                log.info("NO_SALES_FOUND | merchantId={} | outletId={}", merchantId, outletId);

                CoSalesReportResponseDto response = new CoSalesReportResponseDto();

                response.setTotalOrders(0L);
                response.setTotalEarnings(BigDecimal.ZERO);
                response.setDailyBreakdown(List.of());

                log.info("SERVICE_SUCCESS | EMPTY_SALES_REPORT | merchantId={} | outletId={}", merchantId, outletId);

                return response;
            }

            Long totalOrders = report.stream().mapToLong(CoSalesReportProjection::getTotalOrders).sum();

            BigDecimal totalEarnings = report.stream().map(CoSalesReportProjection::getTotalEarnings).reduce(BigDecimal.ZERO, BigDecimal::add);

            List<CoDailySalesReportDto> dailyBreakdown = report.stream().map(coSalesReportMapper::mapToDailySalesDto).toList();
            log.debug("REPORT_SUMMARY | totalOrders={} | totalEarnings={} | breakdownCount={}", totalOrders, totalEarnings, dailyBreakdown.size());
            CoSalesReportResponseDto response = new CoSalesReportResponseDto();

            response.setTotalOrders(totalOrders);

            response.setTotalEarnings(totalEarnings);

            response.setDailyBreakdown(dailyBreakdown);
            log.info("SERVICE_SUCCESS | SALES_REPORT_GENERATED | merchantId={} | outletId={} | totalOrders={} | totalEarnings={}", merchantId, outletId, totalOrders, totalEarnings);

            return response;

        } catch (CoBadRequestException ex) {

            log.warn("SERVICE_ERROR | BUSINESS_VALIDATION_FAILED | merchantId={} | outletId={} | message={}", merchantId, outletId, ex.getMessage());

            throw ex;

        } catch (Exception ex) {

            log.error("SERVICE_ERROR | SALES_REPORT_GENERATION_FAILED | merchantId={} | outletId={}", merchantId, outletId, ex);

            throw ex;
        }
    }

    private void validateRequest(Integer merchantId, Integer outletId, CoSalesReportFilter filter) {

        log.debug("VALIDATION_START | merchantId={} | outletId={} | filter={}", merchantId, outletId, filter);

        if (merchantId == null || merchantId <= 0) {

            log.warn("VALIDATION_FAILED | INVALID_MERCHANT_ID | merchantId={}", merchantId);

            throw new CoBadRequestException("Merchant id is required");
        }

        if (outletId != null && outletId <= 0) {

            log.warn("VALIDATION_FAILED | INVALID_OUTLET_ID | outletId={}", outletId);

            throw new CoBadRequestException("Invalid outlet id");
        }

        if (filter == null) {

            log.warn("VALIDATION_FAILED | FILTER_REQUIRED");

            throw new CoBadRequestException("Filter is required");
        }

        log.debug("VALIDATION_SUCCESS | merchantId={} | outletId={} | filter={}", merchantId, outletId, filter);
    }

    private List<CoFmOutletDto> validateMerchantAndGetOutlets(Integer merchantId) {

        log.info("VALIDATION_START | MERCHANT_VALIDATION | merchantId={}", merchantId);

        CoFmApiResponse<List<CoFmOutletDto>> response = coFmFeignClient.getOutletsByMerchantId(merchantId);

        if (response == null) {

            log.error("FM_RESPONSE_NULL | merchantId={}", merchantId);

            throw new CoBadRequestException("Unable to fetch merchant outlets");
        }

        if (Boolean.FALSE.equals(response.getSuccess())) {

            log.warn("MERCHANT_VALIDATION_FAILED | merchantId={} | message={}", merchantId, response.getMessage());

            throw new CoBadRequestException(response.getMessage());
        }

        List<CoFmOutletDto> outlets = response.getData();

        if (outlets == null || outlets.isEmpty()) {

            log.warn("MERCHANT_NOT_FOUND | merchantId={}", merchantId);

            throw new CoBadRequestException("Invalid merchant id");
        }

        log.info("VALIDATION_SUCCESS | merchantId={} | outletCount={}", merchantId, outlets.size());

        return outlets;
    }

    private void validateOutletBelongsToMerchant(Integer merchantId, Integer outletId, List<CoFmOutletDto> merchantOutlets) {

        log.debug("VALIDATING_OUTLET_MERCHANT_MAPPING | merchantId={} | outletId={}", merchantId, outletId);

        boolean validOutlet = merchantOutlets.stream().anyMatch(outlet -> outlet.getOutletId().equals(outletId));

        if (!validOutlet) {

            log.warn("OUTLET_MERCHANT_MISMATCH | merchantId={} | outletId={}", merchantId, outletId);

            throw new CoBadRequestException("Outlet does not belong to merchant");
        }

        log.info("VALIDATION_SUCCESS | outletId={} belongsTo merchantId={}", outletId, merchantId);
    }
}