package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.Constants.COConstants;
import com.jippy.customerandorder.dto.COOrderEvent;
import com.jippy.customerandorder.dto.CoPlaceOrderRequestDto;
import com.jippy.customerandorder.dto.CoPlaceOrderResponseDto;
import com.jippy.customerandorder.entity.CoOrder;
import com.jippy.customerandorder.exception.OrderException;
import com.jippy.customerandorder.mapper.COEventMapper;
import com.jippy.customerandorder.mapper.CoOrderMapper;
import com.jippy.customerandorder.repository.CoOrderItemRepository;
import com.jippy.customerandorder.repository.CoOrderPriceBreakupRepository;
import com.jippy.customerandorder.repository.CoOrderRepository;
import com.jippy.customerandorder.repository.CoOrderSequenceRepository;
import com.jippy.customerandorder.service.IOrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class COOrderService implements IOrderService {

    private final CoOrderRepository orderRepository;
    private final CoOrderItemRepository orderItemRepository;
    private final CoOrderPriceBreakupRepository priceRepository;
    private final CoOrderMapper orderMapper;
    private final CoOrderSequenceRepository sequenceRepository;
    private final KafkaTemplate<String, COOrderEvent> kafkaTemplate;

    @Transactional
    public CoPlaceOrderResponseDto placeOrder(CoPlaceOrderRequestDto dto) {

        log.info("Start placeOrder | customerId={}, outletId={}",
                dto.getCustomerId(), dto.getOutletId());

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new OrderException(COConstants.MSG_ORDER_ITEMS_EMPTY);
        }

        log.info("Order validation passed | itemCount={}", dto.getItems().size());

        // 1. Generate ID
        String orderId = generateOrderId();

        // 2. Save Order
        CoOrder order = orderMapper.mapToOrder(dto);
        order.setOrderId(orderId);
        orderRepository.save(order);

        log.info("Order saved | orderId={}, customerId={}, outletId={}",
                orderId, dto.getCustomerId(), dto.getOutletId());

        // 3. Save Items
        dto.getItems().forEach(i ->
                orderItemRepository.save(orderMapper.mapToItem(i, orderId))
        );

        log.info("Order items saved | orderId={}, itemCount={}", orderId, dto.getItems().size());

        // 4. Save Price
        priceRepository.save(orderMapper.mapToPrice(dto, orderId));

        log.info("Order price breakup saved | orderId={}", orderId);

        // 5. Publish Kafka (AFTER DB)
        COOrderEvent event = COEventMapper.mapToOrderEvent(order);
        kafkaTemplate.send("new-orders", orderId, event);

        log.info("Kafka published | orderId={}", orderId);

        // 6. Response
        CoPlaceOrderResponseDto response = new CoPlaceOrderResponseDto();
        response.setOrderId(orderId);
        response.setMessage(COConstants.MSG_ORDER_CREATED);

        log.info("Order placed successfully | orderId={}, customerId={}, outletId={}",
                orderId, dto.getCustomerId(), dto.getOutletId());

        return response;
    }

    @Transactional
    public String generateOrderId() {

        log.info("Generating order ID");

        LocalDate today = LocalDate.now();

        Object[] row = sequenceRepository.getSequenceForUpdate();

        if (row == null) {
            log.info("Sequence row not found, inserting initial sequence for date: {}", today);
            sequenceRepository.insertInitial(today);
            row = sequenceRepository.getSequenceForUpdate();
        }

        LocalDate lastDate = ((java.sql.Date) row[0]).toLocalDate();
        Long currentSeq = row[1] instanceof Number ? ((Number) row[1]).longValue() : Long.valueOf(row[1].toString());

        log.info("Retrieved sequence | lastDate={}, currentSeq={}", lastDate, currentSeq);

        Long nextSeq = (!today.equals(lastDate)) ? 1 : currentSeq + 1;

        log.info("Calculated next sequence | nextSeq={}", nextSeq);

        sequenceRepository.updateSequence(today, nextSeq);

        String orderId = "jippy" + today.format(DateTimeFormatter.BASIC_ISO_DATE) + nextSeq;

        log.info("Order ID generated | orderId={}", orderId);

        return orderId;
    }
}