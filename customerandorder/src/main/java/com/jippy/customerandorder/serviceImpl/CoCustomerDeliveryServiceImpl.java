package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.dto.*;
import com.jippy.customerandorder.entity.CoOrder;
import com.jippy.customerandorder.entity.CoOrderRejection;
import com.jippy.customerandorder.entity.CoOrderWaitingPeriod;
import com.jippy.customerandorder.exception.CoBusinessException;
import com.jippy.customerandorder.feignClients.FMFeignClient;
import com.jippy.customerandorder.iservice.CoCustomerDeliveryService;
import com.jippy.customerandorder.mapper.CoCustomerDeliveryMapper;
import com.jippy.customerandorder.repository.CoLocationValidationRepository;
import com.jippy.customerandorder.repository.CoOrderRejectionRepository;
import com.jippy.customerandorder.repository.CoOrderRepository;
import com.jippy.customerandorder.repository.CoOrderWaitingPeriodRepository;
import com.jippy.customerandorder.constants.COConstants;

import java.time.Duration;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoCustomerDeliveryServiceImpl implements CoCustomerDeliveryService {

    private final CoOrderRepository coOrderRepository;

    private final FMFeignClient fmFeignClient;


    private final CoOrderRejectionRepository coOrderRejectionRepository;

    private final CoLocationValidationRepository locationValidationRepository;

    private final CoOrderWaitingPeriodRepository coOrderWaitingPeriodRepository;

    @Override
    public CoCustomerUnreachableResponseDto customerUnreachable(CoCustomerUnreachableRequestDto requestDto) {

        log.info("Customer unreachable API started for orderId : {}", requestDto.getOrderId());

        // FETCH ORDER

        CoOrder order = coOrderRepository.findByOrderIdAndDriverId(requestDto.getOrderId(), requestDto.getDriverId()).orElseThrow(() -> {

            log.error("Order not found for orderId : {} and driverId : {}", requestDto.getOrderId(), requestDto.getDriverId());

            return new CoBusinessException("Order not found");
        });

        log.info("Order fetched successfully for orderId : {}", order.getOrderId());

        // VALIDATE ORDER STATUS

//        if (!"OUT_FOR_DELIVERY".equals(order.getOrderStatus())) {
        if (!COConstants.ORDER_STATUS_OUT_FOR_DELIVERY.equals(order.getOrderStatus())) {

            log.error("Invalid order status for orderId : {}, status : {}", order.getOrderId(), order.getOrderStatus());

            throw new CoBusinessException("Order not eligible for unreachable flow");
        }

        log.info("Order status validation success for orderId : {}", order.getOrderId());

        // CHECK REJECTION ALREADY EXISTS

        // boolean alreadyRejected = coOrderRejectionRepository.existsByOrderIdAndType(requestDto.getOrderId(), "CUSTOMER");
        boolean alreadyRejected = coOrderRejectionRepository.existsByOrderIdAndType(requestDto.getOrderId(), COConstants.REJECTION_TYPE_CUSTOMER);
        if (alreadyRejected) {

            log.error("Customer rejection already exists for orderId : {}", requestDto.getOrderId());

            throw new CoBusinessException("Customer rejection already exists");
        }

        log.info("No existing rejection found for orderId : {}", requestDto.getOrderId());

        // VALIDATE DISTANCE

        Double distance = locationValidationRepository.calculateDistance(requestDto.getOrderId(), requestDto.getLatitude(), requestDto.getLongitude());

        log.info("Driver distance from customer : {} meters for orderId : {}", distance, requestDto.getOrderId());

        if (distance == null) {

            log.error("Unable to calculate distance for orderId : {}", requestDto.getOrderId());

            throw new CoBusinessException("Unable to validate driver location");
        }

        // DRIVER LOCATION VALIDATION

        if (distance > COConstants.CUSTOMER_LOCATION_RADIUS_METERS) {

            log.error("Driver not reached customer location for orderId : {}", requestDto.getOrderId());

            throw new CoBusinessException(COConstants.DRIVER_NOT_AT_CUSTOMER_LOCATION);
        }
        log.info("Driver location validation success for orderId : {}", requestDto.getOrderId());

        // UPDATE ORDER STATUS

        //order.setOrderStatus("CUSTOMER_NOT_REACHABLE");
        order.setOrderStatus(COConstants.ORDER_STATUS_CUSTOMER_NOT_REACHABLE);
        order.setUpdatedAt(LocalDateTime.now());

        coOrderRepository.save(order);

        log.info("Order status updated successfully for orderId : {}", order.getOrderId());

        // SAVE REJECTION ENTRY

        //CoOrderRejection rejectionEntity = CoCustomerDeliveryMapper.mapToOrderRejectionEntity(requestDto);
        CoOrderRejection rejectionEntity = CoCustomerDeliveryMapper.mapToOrderRejectionEntity(requestDto, order);
        coOrderRejectionRepository.save(rejectionEntity);

        log.info("Order rejection entry saved successfully for orderId : {}", order.getOrderId());

        // CREATE WAITING PERIOD

        CoOrderWaitingPeriod waitingPeriod = new CoOrderWaitingPeriod();

        waitingPeriod.setOrderRejectionId(rejectionEntity.getOrderRejectionId());

        waitingPeriod.setAllowsRejection(false);

        waitingPeriod.setCreatedAt(LocalDateTime.now());

        coOrderWaitingPeriodRepository.save(waitingPeriod);

        log.info("Waiting period created successfully for orderId : {}", order.getOrderId());


        // TODO:
        // CALL NOTIFICATION MS

        log.info("Customer unreachable notification triggered for orderId : {}", order.getOrderId());

        log.info("Customer unreachable API completed successfully for orderId : {}", order.getOrderId());

        // RETURN RESPONSE

        return CoCustomerUnreachableResponseDto.builder().success(true).message("Customer unreachable flow started").distanceInMeters(distance).rejectionAllowed(false).build();
    }

    @Override
    public CoFinalRejectResponseDto finalRejectOrder(CoFinalRejectRequestDto requestDto) {

        log.info("Final reject started for orderId : {}", requestDto.getOrderId());

        CoOrder order = coOrderRepository.findByOrderIdAndDriverId(requestDto.getOrderId(), requestDto.getDriverId()).orElseThrow(() -> new CoBusinessException("Order not found"));

        log.info("Order fetched successfully for orderId : {}", order.getOrderId());

        CoOrderRejection rejection = coOrderRejectionRepository.findByOrderId(requestDto.getOrderId()).orElseThrow(() -> new CoBusinessException("Rejection entry not found"));

        log.info("Rejection entry fetched successfully for orderId : {}", requestDto.getOrderId());

        CoOrderWaitingPeriod waitingPeriod = coOrderWaitingPeriodRepository.findByOrderRejectionId(rejection.getOrderRejectionId()).orElseThrow(() -> new CoBusinessException("Waiting period not found"));

        log.info("Waiting period fetched successfully for orderId : {}", requestDto.getOrderId());

        // WAITING PERIOD VALIDATION USING CREATED_AT

        LocalDateTime allowedTime = waitingPeriod.getCreatedAt().plusMinutes(COConstants.FINAL_REJECTION_WAIT_MINUTES);

        if (LocalDateTime.now().isBefore(allowedTime)) {

            long secondsRemaining = Duration.between(LocalDateTime.now(), allowedTime).getSeconds();

            throw new CoBusinessException("Please wait " + secondsRemaining + " seconds before final rejection");
        }

        log.info("Waiting period validation success for orderId : {}", requestDto.getOrderId());

        order.setOrderStatus(COConstants.ORDER_STATUS_FINAL_REJECTED);

        order.setUpdatedAt(LocalDateTime.now());

        coOrderRepository.save(order);

        log.info("Order final rejected successfully");

        CoNearbyOutletResponseDto outletResponse = fmFeignClient.fetchNearbySpecializedOutlets(requestDto.getLatitude(), requestDto.getLongitude());

        log.info("Fetched {} specialized outlets", outletResponse.getTotalOutlets());

        return CoFinalRejectResponseDto.builder().success(true).message("Order final rejected successfully").orderStatus(order.getOrderStatus()).totalOutlets(outletResponse.getTotalOutlets()).outlets(outletResponse.getOutlets()).build();
    }
}