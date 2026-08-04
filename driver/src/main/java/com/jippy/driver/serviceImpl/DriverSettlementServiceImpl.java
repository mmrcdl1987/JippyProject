package com.jippy.driver.serviceImpl;

import com.jippy.driver.constants.DConstants;
import com.jippy.driver.dto.DriverIncentiveDetailDto;
import com.jippy.driver.dto.DriverIncentiveSettlementResponseDto;
import com.jippy.driver.dto.DriverOrderSettlementDto;
import com.jippy.driver.dto.DriverSettlementResponseDto;
import com.jippy.driver.exception.DriverBadRequestException;
import com.jippy.driver.exception.ResourceNotFoundException;
import com.jippy.driver.mapper.DriverSettlementMapper;
import com.jippy.driver.projection.DriverIncentiveDetailProjection;
import com.jippy.driver.projection.DriverIncentiveSettlementProjection;
import com.jippy.driver.projection.DriverOrderSettlementProjection;
import com.jippy.driver.projection.DriverSettlementProjection;
import com.jippy.driver.repositary.DriverIncentiveHistoryRepository;
import com.jippy.driver.repositary.DriverOrderRepository;
import com.jippy.driver.service.DriverSettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverSettlementServiceImpl implements DriverSettlementService {

    private final DriverOrderRepository driverOrdersRepository;

    private final DriverIncentiveHistoryRepository driverIncentiveHistoryRepository;


    //    Fetch settlements grouped by driver.
    @Override
    public List<DriverSettlementResponseDto> getDriversSettlements(LocalDate startDate, LocalDate endDate) {

        log.info("Fetching driver settlements from {} to {}", startDate, endDate);

        // Validate date range
        if (startDate.isAfter(endDate)) {
            log.error("Invalid date range. Start date {} is after end date {}", startDate, endDate);
            throw new IllegalArgumentException("Start date cannot be greater than end date");
        }

//        fetching the settlement calculations for each driver within the specified date range.
        List<DriverSettlementProjection> driverCalculations = driverOrdersRepository.getDriversSettlementCalculation(startDate, endDate);

        // No settlements found handling: If there are no settlements found for the given date range,
        // we log an error and Throw a ResourceNotFoundException to indicate that
        // no driver settlements were found.
        if (driverCalculations.isEmpty()) {
            log.error("No driver settlements found between {} and {}", startDate, endDate);
            throw new ResourceNotFoundException("No driver settlements found for the given date range");
        }
//          fetching the order details for each driver within the specified date range.
        List<DriverOrderSettlementProjection> orderDetails =
                driverOrdersRepository.getDriverOrderSettlements(startDate, endDate);


//        the response will be a list of driver settlements,
//        each containing the Calculation and the list of orders for that driver.
        List<DriverSettlementResponseDto> response = new ArrayList<>();

        for (DriverSettlementProjection calculate : driverCalculations) {

            DriverSettlementResponseDto driverDto =
                    DriverSettlementMapper.toDriverSettlementResponseDto(calculate);

//            for each driver, we filter the order details
//            to include only those that belong to the current driver,
//            and then map those order details to a list of
//            DriverOrderSettlementDto objects, which are then set in the driverDto.
            List<DriverOrderSettlementDto> orders = new ArrayList<>();

            for (DriverOrderSettlementProjection order : orderDetails) {

                if (order.getDriverId().equals(calculate.getDriverId())) {

                    orders.add(DriverSettlementMapper.toDriverOrderSettlementDto(order));
                }
            }

            driverDto.setOrders(orders);

            response.add(driverDto);
        }

        log.info("Successfully fetched settlements for {} drivers", response.size());

        return response;
    }

    @Override
    public List<DriverIncentiveSettlementResponseDto> getDriversIncentivesForSettlements(String filter) {

        log.info("Fetching driver incentive settlements for filter : {}", filter);

        if (!DConstants.current_Month.equalsIgnoreCase(filter)) {
            throw new DriverBadRequestException(
                    "Invalid filter. Supported value is currentMonth.");
        }
//        this fetches previous month incentive settlements for all drivers.
//        We calculate the start and end dates for the previous month,
        LocalDate startDate = LocalDate.now().minusMonths(1).withDayOfMonth(1);

//        this fetches the last day of the previous month by using the lengthOfMonth() method on the start date,
//        which gives us the number of days in that month, and then we set the end date to that day.
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<DriverIncentiveSettlementProjection> incentiveCalculations = driverIncentiveHistoryRepository.
                getDriverIncentiveSettlementCalculation(startDate, endDate);

        if (incentiveCalculations.isEmpty()) {

            throw new ResourceNotFoundException("No driver incentives found for previous month");
        }

        List<DriverIncentiveDetailProjection> incentiveDetails =
                driverIncentiveHistoryRepository.getDriverIncentiveDetails(startDate, endDate);

        List<DriverIncentiveSettlementResponseDto> response = new ArrayList<>();

        for (DriverIncentiveSettlementProjection calculation : incentiveCalculations) {

            DriverIncentiveSettlementResponseDto dto =
                    DriverSettlementMapper.toDriverIncentiveSettlementResponseDto(calculation);

            List<DriverIncentiveDetailDto> incentives = new ArrayList<>();

            for (DriverIncentiveDetailProjection detail : incentiveDetails) {

                if (detail.getDriverId().equals(calculation.getDriverId())) {

                    DriverIncentiveDetailDto driverIncentiveDetailDto =
                            DriverSettlementMapper.toDriverIncentiveDetailDto(detail);
                    incentives.add(driverIncentiveDetailDto);
                }
            }

//            setting for DriverIncentiveSettlementResponseDto the list of incentives
//            for each driver, which includes the date, incentive amount, and
//            the number of completed orders for that day.
            dto.setIncentives(incentives);

            response.add(dto);
        }

        log.info("Successfully fetched incentive settlements for {} drivers", response.size());

        return response;
    }
    }
