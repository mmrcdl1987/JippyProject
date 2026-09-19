package com.jippy.driver.serviceImpl;

import com.jippy.driver.constants.DConstants;
import com.jippy.driver.dto.DriverIncentiveHistoryPageResponseDto;
import com.jippy.driver.projection.DriverIncentiveHistoryPageProjection;
import com.jippy.driver.repositary.DriverIncentiveHistoryRepository;
import com.jippy.driver.service.DriverIncentiveSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DriverIncentiveSettingsServiceImpl implements DriverIncentiveSettingsService {

    private final DriverIncentiveHistoryRepository repository;

    @Override
    public Page<DriverIncentiveHistoryPageResponseDto> getIncentiveHistoryPage(Integer driverId, String filter, LocalDate startDate, LocalDate endDate, Integer page, Integer size) {

        int pageNumber = page == null || page < 0 ? DConstants.DEFAULT_PAGE : page;

        int pageSize = size == null || size <= 0 ? DConstants.DEFAULT_PAGE_SIZE : Math.min(size, 100);

        log.info("Fetching incentive history: driverId={}, filter={}, " + "startDate={}, endDate={}, page={}, size={}", driverId, filter, startDate, endDate, pageNumber, pageSize);

        LocalDate finalStartDate = startDate;
        LocalDate finalEndDate = endDate;

        if (StringUtils.hasText(filter)) {

            String selectedFilter = filter.trim().toUpperCase(Locale.ROOT);

            LocalDate today = LocalDate.now();

            switch (selectedFilter) {

                case DConstants.ALL:
                    break;

                case DConstants.FILTER_DAILY:
                case "FILTER_DAILY":
                    finalStartDate = today;
                    finalEndDate = today;
                    break;

                case DConstants.FILTER_WEEKLY:
                case "FILTER_WEEKLY":
                    finalStartDate = today.minusDays(6);
                    finalEndDate = today;
                    break;

                case DConstants.FILTER_MONTHLY:
                case "FILTER_MONTHLY":
                    finalStartDate = today.withDayOfMonth(1);
                    finalEndDate = today;
                    break;

                default:
                    log.warn("Invalid incentive history filter: {}", filter);

                    throw new IllegalArgumentException(DConstants.INVALID_INCENTIVE_HISTORY_FILTER + filter);
            }
        }

        validateDateRange(finalStartDate, finalEndDate);

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Order.desc(DConstants.SORT_BY_CURR_DATE), Sort.Order.desc(DConstants.SORT_BY_HISTORY_ID)));

        Page<DriverIncentiveHistoryPageProjection> historyPage = repository.searchIncentiveHistory(driverId, finalStartDate, finalEndDate, pageable);

        log.info("Incentive history fetched: totalElements={}, " + "totalPages={}, currentPage={}, pageSize={}", historyPage.getTotalElements(), historyPage.getTotalPages(), historyPage.getNumber(), historyPage.getSize());

        return historyPage.map(this::mapToResponseDto);
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {

            log.warn("Invalid incentive history date range: " + "startDate={}, endDate={}", startDate, endDate);

            throw new IllegalArgumentException("Start date cannot be after end date");
        }
    }

    private DriverIncentiveHistoryPageResponseDto mapToResponseDto(DriverIncentiveHistoryPageProjection projection) {

        DriverIncentiveHistoryPageResponseDto dto = new DriverIncentiveHistoryPageResponseDto();

        dto.setDriverIncentiveHistoryId(projection.getDriverIncentiveHistoryId());

        dto.setDriverId(projection.getDriverId());

        dto.setDriverName(projection.getDriverName());

        dto.setCurrDate(projection.getCurrDate());

        dto.setIncentiveAmount(projection.getIncentiveAmount());

        dto.setCompletedOrdersCount(projection.getCompletedOrdersCount());

        dto.setCreatedAt(projection.getCreatedAt());

        return dto;
    }
}