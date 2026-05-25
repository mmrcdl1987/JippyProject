package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.constants.COConstants;
import com.jippy.customerandorder.dto.COOrderEvent;
import com.jippy.customerandorder.dto.CoOrderRejectionRequestDto;
import com.jippy.customerandorder.dto.FmNearbyOutletResponseDto;
import com.jippy.customerandorder.dto.FmOutletDto;
import com.jippy.customerandorder.entity.CoCustomerDeliveryAddress;
import com.jippy.customerandorder.entity.CoOrder;
import com.jippy.customerandorder.entity.CoOrderRejection;
import com.jippy.customerandorder.exception.CoBusinessException;
import com.jippy.customerandorder.feignClients.FMFeignClient;
import com.jippy.customerandorder.iservice.CoOrderRejectionService;
import com.jippy.customerandorder.mapper.CoOrderRejectionMapper;
import com.jippy.customerandorder.repository.CoCustomerDeliveryAddressRepository;
import com.jippy.customerandorder.repository.CoOrderRejectionRepository;
import com.jippy.customerandorder.repository.CoOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoOrderRejectionServiceImpl implements CoOrderRejectionService {

    private final CoOrderRepository orderRepository;

    private final CoOrderRejectionRepository rejectionRepository;

    private final CoCustomerDeliveryAddressRepository addressRepository;

    private final FMFeignClient fmFeignClient;

    private final KafkaTemplate<String, COOrderEvent> kafkaTemplate;

    @Override
    public CoOrderRejection rejectOrder(CoOrderRejectionRequestDto request) {

        log.info("Reject order request received orderId={}", request.getOrderId());

        /*
         * PREVENT DUPLICATE REJECTION
         */
        boolean alreadyRejected = rejectionRepository.existsByOrderId(request.getOrderId());

        if (alreadyRejected) {

            log.error("Order already rejected orderId={}", request.getOrderId());

            throw new CoBusinessException("Order already rejected");
        }

        /*
         * FETCH ORDER
         */
        CoOrder order = orderRepository.findById(request.getOrderId()).orElseThrow(() -> new CoBusinessException("Order not found"));

        /*
         * CUSTOMER CANCEL FLOW
         */
        if (COConstants.CUSTOMER.equalsIgnoreCase(request.getType())) {

            /*
             * UPDATE STATUS -> CANCELLED
             */
            order.setOrderStatus(COConstants.ORDER_STATUS_CANCELLED);

            orderRepository.save(order);

            log.info("Order cancelled by customer orderId={}", order.getOrderId());

            /*
             * SAVE REJECTION ENTRY
             */
            CoOrderRejection rejection = CoOrderRejectionMapper.toEntity(request);

            rejectionRepository.save(rejection);

            log.info("Customer cancellation saved orderId={}", order.getOrderId());

            return rejection;
        }

        /*
         * OUTLET REJECT FLOW
         */
        order.setOrderStatus(COConstants.ORDER_STATUS_REJECTED);

        orderRepository.save(order);

        log.info("Order status updated REJECTED orderId={}", order.getOrderId());

        /*
         * SAVE REJECTION ENTRY
         */
        CoOrderRejection rejection = CoOrderRejectionMapper.toEntity(request);

        rejectionRepository.save(rejection);

        log.info("Rejection entry saved orderId={}", order.getOrderId());

        /*
         * FETCH CUSTOMER ADDRESS
         */
        CoCustomerDeliveryAddress address = addressRepository.findById(order.getCustomerDeliveryAddressId()).orElseThrow(() -> new CoBusinessException("Customer address not found"));

        /*
         * AREA ID
         */
        Integer areaId = address.getArea();

        if (areaId == null) {

            throw new CoBusinessException("Area id not found");
        }

        log.info("Customer areaId={}", areaId);

        /*
         * FETCH SPECIALIZED OUTLETS
         */
        FmNearbyOutletResponseDto response = fmFeignClient.fetchSpecializedOutletsByAreaId(areaId);

        /*
         * OUTLET LIST
         */
        List<FmOutletDto> outlets = response.getOutlets();

        if (outlets == null || outlets.isEmpty()) {

            throw new CoBusinessException("No specialized outlets found");
        }

        log.info("Total specialized outlets found={}", outlets.size());

        /*
         * SEND NOTIFICATIONS
         */
        for (FmOutletDto outlet : outlets) {

            /*
             * SKIP REJECTED OUTLET
             */
            if (outlet.getOutletId().equals(request.getRejectedById())) {

                log.info("Skipping rejected outletId={}", outlet.getOutletId());

                continue;
            }

            /*
             * CREATE EVENT
             */
            COOrderEvent event = new COOrderEvent();

            event.setOrderId(order.getOrderId());

            event.setCustomerId(order.getCustomerId());

            event.setOutletId(outlet.getOutletId());

            event.setDriverId(order.getDriverId());

            event.setStatus(COConstants.ORDER_STATUS_REJECTED);

            event.setAreaId(areaId);

            event.setRejectedOutletId(request.getRejectedById());

            /*
             * SEND KAFKA EVENT
             */
            kafkaTemplate.send("co-order-events", event);

            log.info("Kafka event sent orderId={} outletId={}", order.getOrderId(), outlet.getOutletId());
        }

        log.info("Rejected order reassignment completed orderId={}", order.getOrderId());

        return rejection;
    }
}