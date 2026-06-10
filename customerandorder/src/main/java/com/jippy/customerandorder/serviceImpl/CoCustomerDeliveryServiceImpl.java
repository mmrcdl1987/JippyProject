package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.dto.*;
import com.jippy.customerandorder.entity.CoCustomerDeliveryAddress;
import com.jippy.customerandorder.entity.CoOrder;
import com.jippy.customerandorder.entity.CoOrderRejection;
import com.jippy.customerandorder.entity.CoOrderWaitingPeriod;
import com.jippy.customerandorder.exception.CoBusinessException;
import com.jippy.customerandorder.feignClients.FMFeignClient;
import com.jippy.customerandorder.iservice.CoCustomerDeliveryService;
import com.jippy.customerandorder.mapper.CoCustomerDeliveryMapper;
import com.jippy.customerandorder.repository.*;
import com.jippy.customerandorder.constants.COConstants;

import java.time.Duration;


import com.jippy.foodandmart.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoCustomerDeliveryServiceImpl implements CoCustomerDeliveryService {

    private final CoOrderRepository coOrderRepository;

    private final FMFeignClient fmFeignClient;

    private final CoCustomerRepository customerRepository;

    private final CoOrderRejectionRepository coOrderRejectionRepository;

    private final CoLocationValidationRepository locationValidationRepository;

    private final CoOrderWaitingPeriodRepository coOrderWaitingPeriodRepository;

    private final CoCustomerDeliveryAddressRepository customerDeliveryAddressRepository;


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

    @Override
    public CoCustomerDeliveryAddressResponseDto createCustomerDeliveryAddress(CoCustomerDeliveryAddressRequestDto requestDto) {

        log.info("CREATE_CUSTOMER_ADDRESS_SERVICE_START | customerId={}", requestDto.getCustomerId());

        customerRepository.findById(requestDto.getCustomerId()).orElseThrow(() -> new ResourceNotFoundException("Customer not found with customerId : " + requestDto.getCustomerId()));

//        mapping the request DTO to the entity class CoCustomerDeliveryAddress
//        using the CoCustomerDeliveryMapper
        CoCustomerDeliveryAddress customerDeliveryAddress = CoCustomerDeliveryMapper.mapToEntity(requestDto);

//        saving the customer delivery address details to the database
        CoCustomerDeliveryAddress savedCustomerDeliveryAddress = customerDeliveryAddressRepository.save(customerDeliveryAddress);

        log.info("CREATE_CUSTOMER_ADDRESS_SERVICE_SUCCESS | customerAddressId={}", savedCustomerDeliveryAddress.getCustomerAddressId());

//        getting the saved customer delivery address details
//        and mapping it to the response DTO
        return CoCustomerDeliveryMapper.mapToResponseDto(savedCustomerDeliveryAddress);

    }

    @Override
    public List<CoCustomerDeliveryAddressResponseDto> getCustomerDeliveryAddresses(Integer customerId) {

        log.info("GET_CUSTOMER_DELIVERY_ADDRESSES_SERVICE_START | customerId={}", customerId);
        log.info("Validating customer existence for customerId={}", customerId);


        customerRepository.findById(customerId).orElseThrow(() -> new ResourceNotFoundException("Customer not found with customerId : " + customerId));

        List<CoCustomerDeliveryAddress> customerDeliveryAddresses = customerDeliveryAddressRepository.findByCustomerId(customerId);

        if (customerDeliveryAddresses.isEmpty()) {

            throw new ResourceNotFoundException("No delivery addresses found for customerId : " + customerId);
        }

        List<CoCustomerDeliveryAddressResponseDto> addressResponseDtoList = new ArrayList<>();

//        looping through the list of customer delivery addresses and mapping each address to the response DTO
        for (CoCustomerDeliveryAddress customerDeliveryAddress : customerDeliveryAddresses) {

            CoCustomerDeliveryAddressResponseDto coCustomerDeliveryAddressResponseDto = CoCustomerDeliveryMapper.mapToResponseDto(customerDeliveryAddress);
            addressResponseDtoList.add(coCustomerDeliveryAddressResponseDto);
        }

        log.info("GET_CUSTOMER_DELIVERY_ADDRESSES_SERVICE_SUCCESS | customerId={} | addressCount={}", customerId, addressResponseDtoList.size());

        return addressResponseDtoList;

    }

//     to delete a delivery address based on the customer_address_id
    @Override
    public void deleteCustomerDeliveryAddress(Integer customerAddressId) {

        log.info("DELETE_CUSTOMER_DELIVERY_ADDRESS_SERVICE_START | customerAddressId={}", customerAddressId);
        log.info("Validating existence of customer delivery address for customerAddressId={}", customerAddressId);

        CoCustomerDeliveryAddress customerDeliveryAddress =
                customerDeliveryAddressRepository.findById(customerAddressId).orElseThrow(
                        () -> new ResourceNotFoundException("Customer delivery address not found with customerAddressId : " + customerAddressId));

//        if you use deleteById method of the repository, it will directly delete the record
//        without checking if it exists or not.
//        So we first fetch the record using findById and
//        if it does not exist, we throw a ResourceNotFoundException. If it exists, then we proceed to delete
//        it using the delete method of the repository which takes the entity as a parameter.
//        if you use deleteById ,if ID doesn't exist, depending on JPA implementation,
//        it may not give you the custom error you want: so use delete() method.

        customerDeliveryAddressRepository.delete(customerDeliveryAddress);

        log.info("DELETE_CUSTOMER_DELIVERY_ADDRESS_SERVICE_SUCCESS | customerAddressId={}", customerAddressId);

    }


}