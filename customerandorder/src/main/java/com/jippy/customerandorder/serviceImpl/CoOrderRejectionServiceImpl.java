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
import com.jippy.customerandorder.iservice.CoWalletRefundService;
import com.jippy.customerandorder.mapper.CoOrderRejectionMapper;
import com.jippy.customerandorder.repository.CoCustomerDeliveryAddressRepository;
import com.jippy.customerandorder.repository.CoOrderRejectionRepository;
import com.jippy.customerandorder.repository.CoOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoOrderRejectionServiceImpl implements CoOrderRejectionService {

    private final CoOrderRepository orderRepository;

    private final CoOrderRejectionRepository rejectionRepository;

    private final CoCustomerDeliveryAddressRepository addressRepository;

    private final FMFeignClient fmFeignClient;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final CoWalletRefundService walletRefundService;

    @Transactional
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

            order.setOrderStatus(COConstants.ORDER_STATUS_CANCELLED);
            orderRepository.save(order);

            CoOrderRejection rejection = CoOrderRejectionMapper.toEntity(request);
            rejectionRepository.save(rejection);

            log.info(
                    "Customer cancellation saved | orderId={}",
                    order.getOrderId()
            );

            // NO WALLET REFUND

            return rejection;
        }

        /*
         * DRIVER REJECT FLOW
         */
        if ("DRIVER".equalsIgnoreCase(request.getType())
                || "DRIVER_REJECTION".equalsIgnoreCase(request.getType())) {

            order.setOrderStatus(COConstants.ORDER_STATUS_REJECTED);
            orderRepository.save(order);

            CoOrderRejection rejection = CoOrderRejectionMapper.toEntity(request);
            rejectionRepository.save(rejection);

            log.info(
                    "Driver rejection saved | orderId={}",
                    order.getOrderId()
            );

            // REFUND WALLET

            BigDecimal refundAmount =
                    walletRefundService.processWalletRefund(
                            order.getOrderId(),
                            order.getCustomerId(),
                            COConstants.REJECTION_TYPE_DRIVER
                    );

            log.info(
                    "Driver wallet refund completed | " +
                            "orderId={} | refundAmount={}",
                    order.getOrderId(),
                    refundAmount
            );
            return rejection;
        }

        /*
         * OUTLET REJECT FLOW
         */
        order.setOrderStatus(COConstants.ORDER_STATUS_REJECTED);
        orderRepository.save(order);

        CoOrderRejection rejection = CoOrderRejectionMapper.toEntity(request);
        rejectionRepository.save(rejection);

        log.info(
                "Outlet rejection saved | orderId={}",
                order.getOrderId()
        );

// REFUND WALLET

        BigDecimal refundAmount =
                walletRefundService.processWalletRefund(
                        order.getOrderId(),
                        order.getCustomerId(),
                        COConstants.REJECTION_TYPE_OUTLET
                );

        log.info(
                "Outlet wallet refund completed | " +
                        "orderId={} | refundAmount={}",
                order.getOrderId(),
                refundAmount
        );
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

            log.warn(
                    "NO_SPECIALIZED_OUTLETS_FOUND | orderId={} | areaId={}",
                    order.getOrderId(),
                    areaId
            );
            // Order is already rejected and wallet refund is completed.
            // No reassignment is possible.
            return rejection;
        }

        log.info("Total specialized outlets found={}", outlets.size());

        /*
         * SEND NOTIFICATIONS
         */
        boolean reassignmentSent = false;

        for (FmOutletDto outlet : outlets) {

            if (outlet.getOutletId().equals(request.getRejectedById())) {

                log.info(
                        "Skipping rejected outletId={} | orderId={}",
                        outlet.getOutletId(),
                        order.getOrderId()
                );

                continue;
            }

            COOrderEvent event = new COOrderEvent();

            event.setOrderId(order.getOrderId());
            event.setCustomerId(order.getCustomerId());
            event.setOutletId(outlet.getOutletId());
            event.setDriverId(order.getDriverId());
            event.setStatus(COConstants.ORDER_STATUS_REJECTED);
            event.setAreaId(areaId);
            event.setRejectedOutletId(request.getRejectedById());

            kafkaTemplate.send("co-order-events", event);

            reassignmentSent = true;
            log.info("Kafka event sent | orderId={} | outletId={}", order.getOrderId(),
                    outlet.getOutletId());
        }

        if (!reassignmentSent) {

            log.warn(
                    "NO_ALTERNATIVE_OUTLET_FOR_REASSIGNMENT | " +
                            "orderId={} | rejectedOutletId={}",
                    order.getOrderId(),
                    request.getRejectedById()
            );
        } else {
            log.info(
                    "REASSIGNMENT_EVENTS_SENT | orderId={}",
                    order.getOrderId()
            );
        }
        return rejection;
    }
}