package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.constants.COConstants;
import com.jippy.customerandorder.dto.*;
import com.jippy.customerandorder.entity.CoMealSubscription;
import com.jippy.customerandorder.entity.CoOrder;
import com.jippy.customerandorder.exception.OrderException;
import com.jippy.customerandorder.iservice.IOrderService;
import com.jippy.customerandorder.mapper.COEventMapper;
import com.jippy.customerandorder.mapper.CoOrderMapper;
import com.jippy.customerandorder.repository.CoOrderItemRepository;
import com.jippy.customerandorder.repository.CoOrderPriceBreakupRepository;
import com.jippy.customerandorder.repository.CoOrderRepository;
import com.jippy.customerandorder.repository.CoOrderSequenceRepository;
import com.jippy.customerandorder.repository.MealSubscriptionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.jippy.customerandorder.constants.COConstants.ORDER_TYPE_SCHEDULED_CUSTOM_PLAN;
import static com.jippy.customerandorder.constants.COConstants.ORDER_TYPE_SCHEDULED_RECURRING;

@Service
@RequiredArgsConstructor
@Slf4j
public class COOrderService implements IOrderService {

    private final CoOrderRepository orderRepository;
    private final CoOrderItemRepository orderItemRepository;
    private final CoOrderPriceBreakupRepository priceRepository;

    private final MealSubscriptionRepository subscriptionRepository;

    private final CoOrderMapper orderMapper;
    private final CoOrderSequenceRepository sequenceRepository;
    private final KafkaTemplate<String, COOrderEvent> kafkaTemplate;

    /*
     * PLACE ORDER
     */
    @Override
    @Transactional
    public CoPlaceOrderResponseDto placeOrder(CoPlaceOrderRequestDto dto) {

        log.info("SERVICE_START | PLACE_ORDER | customerId={} | orderType={}", dto.getCustomerId(), dto.getOrderType());

        try {

            validatePlaceOrderRequest(dto);

            /*
             * NORMAL ORDER
             */
            if (COConstants.ORDER_TYPE_NORMAL.equalsIgnoreCase(dto.getOrderType())) {

                return processNormalOrder(dto);
            }

            /*
             * SCHEDULED ORDER
             */
            if (ORDER_TYPE_SCHEDULED_RECURRING.equalsIgnoreCase(dto.getOrderType())) {

                return processRecurringOrders(dto);
            }

            if (ORDER_TYPE_SCHEDULED_CUSTOM_PLAN.equalsIgnoreCase(dto.getOrderType())) {

                return processCustomPlanOrders(dto);
            }

            log.error("VALIDATION_FAILED | INVALID_ORDER_TYPE | orderType={}", dto.getOrderType());

            throw new OrderException(COConstants.MSG_INVALID_ORDER_TYPE);

        } catch (Exception ex) {

            log.error("EXCEPTION | PLACE_ORDER_FAILED | customerId={} | error={}", dto.getCustomerId(), ex.getMessage(), ex);

            throw ex;
        }
    }

    /*
     * NORMAL ORDER
     */
    private CoPlaceOrderResponseDto processNormalOrder(CoPlaceOrderRequestDto dto) {

        log.info("SERVICE_START | PROCESS_NORMAL_ORDER | customerId={}", dto.getCustomerId());

        String orderId = generateOrderId();

        CoOrder order = orderMapper.mapToOrder(dto);

        order.setOrderId(orderId);

        orderRepository.save(order);

        saveOrderItems(dto.getItems(), orderId);

        priceRepository.save(orderMapper.mapToPrice(dto, orderId));

        publishOrderEvent(order);

        log.info("SERVICE_END | PROCESS_NORMAL_ORDER_SUCCESS | orderId={}", orderId);

        return buildResponse(COConstants.MSG_ORDER_CREATED, null, List.of(orderId), dto);
    }

    private CoPlaceOrderResponseDto processRecurringOrders(CoPlaceOrderRequestDto dto) {

        CoMealSubscription subscription = createMealSubscription(dto);

        return processRecurringOrders(dto, subscription);
    }

    private CoPlaceOrderResponseDto processCustomPlanOrders(CoPlaceOrderRequestDto dto) {

        CoMealSubscription subscription = createMealSubscription(dto);

        return processCustomPlanOrders(dto, subscription);
    }

    /*
     * CREATE MEAL SUBSCRIPTION
     */
    private CoMealSubscription createMealSubscription(CoPlaceOrderRequestDto dto) {

        log.info("SERVICE_START | CREATE_MEAL_SUBSCRIPTION | customerId={}", dto.getCustomerId());

        CoMealSubscription subscription = orderMapper.mapToSubscription(dto);

        if (COConstants.ORDER_TYPE_SCHEDULED_CUSTOM_PLAN.equalsIgnoreCase(dto.getOrderType())) {

            LocalDate startDate = dto.getScheduledOrders().stream().map(CoScheduledOrderDto::getDeliveryDate).min(LocalDate::compareTo).orElseThrow(() -> new OrderException(COConstants.MSG_INVALID_SCHEDULE_DATE));

            LocalDate endDate = dto.getScheduledOrders().stream().map(CoScheduledOrderDto::getDeliveryDate).max(LocalDate::compareTo).orElseThrow(() -> new OrderException(COConstants.MSG_INVALID_SCHEDULE_DATE));

            subscription.setSubscriptionStartDate(startDate.atStartOfDay());

            subscription.setSubscriptionEndDate(endDate.atTime(23, 59, 59));
        }

        subscription = subscriptionRepository.save(subscription);

        log.info("SERVICE_END | CREATE_MEAL_SUBSCRIPTION_SUCCESS | subscriptionId={}", subscription.getMealSubscriptionId());

        return subscription;
    }

    /*
     * RECURRING ORDERS
     */
    private CoPlaceOrderResponseDto processRecurringOrders(CoPlaceOrderRequestDto dto, CoMealSubscription subscription) {

        log.info("SERVICE_START | PROCESS_RECURRING_ORDERS | subscriptionId={}", subscription.getMealSubscriptionId());

        List<String> orderIds = new ArrayList<>();

        LocalDate startDate = dto.getSubscriptionStartDate().toLocalDate();

        LocalDate endDate = dto.getSubscriptionEndDate().toLocalDate();

        while (!startDate.isAfter(endDate)) {

            String orderId = createRecurringOrder(dto, subscription, startDate);

            orderIds.add(orderId);

            startDate = startDate.plusDays(1);
        }

        log.info("SERVICE_END | PROCESS_RECURRING_ORDERS_SUCCESS | totalOrders={}", orderIds.size());

        return buildResponse(COConstants.MSG_SCHEDULED_ORDER_CREATED, subscription.getMealSubscriptionId(), orderIds, dto);
    }

    /*
     * CUSTOM PLAN ORDERS
     */
    private CoPlaceOrderResponseDto processCustomPlanOrders(CoPlaceOrderRequestDto dto, CoMealSubscription subscription) {

        log.info("SERVICE_START | PROCESS_CUSTOM_PLAN_ORDERS | subscriptionId={}", subscription.getMealSubscriptionId());

        List<String> orderIds = new ArrayList<>();

        for (CoScheduledOrderDto scheduledOrder : dto.getScheduledOrders()) {

            String orderId = createCustomPlanOrder(dto, subscription, scheduledOrder);

            orderIds.add(orderId);
        }

        log.info("SERVICE_END | PROCESS_CUSTOM_PLAN_ORDERS_SUCCESS | totalOrders={}", orderIds.size());

        return buildResponse(COConstants.MSG_SCHEDULED_ORDER_CREATED, subscription.getMealSubscriptionId(), orderIds, dto);
    }

    /*
     * CREATE RECURRING ORDER
     */
    private String createRecurringOrder(CoPlaceOrderRequestDto dto, CoMealSubscription subscription, LocalDate deliveryDate) {

        String orderId = generateOrderId();

        CoOrder order = orderMapper.mapToOrder(dto);

        order.setOrderId(orderId);

        order.setMealSubscriptionId(subscription.getMealSubscriptionId());

        order.setScheduledDeliveryDateTime(LocalDateTime.of(deliveryDate, dto.getScheduledDeliveryDateTime().toLocalTime()));

        orderRepository.save(order);

        saveOrderItems(dto.getItems(), orderId);

        priceRepository.save(orderMapper.mapToPrice(dto, orderId));

        publishOrderEvent(order);

        log.info("RECURRING_ORDER_CREATED | orderId={} | deliveryDate={}", orderId, deliveryDate);

        return orderId;
    }

    /*
     * CREATE CUSTOM PLAN ORDER
     */
    private String createCustomPlanOrder(CoPlaceOrderRequestDto dto, CoMealSubscription subscription, CoScheduledOrderDto scheduledOrder) {

        String orderId = generateOrderId();

        CoOrder order = orderMapper.mapToOrder(dto);
        order.setOrderId(orderId);

        order.setMealSubscriptionId(subscription.getMealSubscriptionId());

        order.setScheduledDeliveryDateTime(LocalDateTime.of(scheduledOrder.getDeliveryDate(), scheduledOrder.getScheduledDeliveryTime()));

        orderRepository.save(order);

        saveOrderItems(scheduledOrder.getItems(), orderId);

        priceRepository.save(orderMapper.mapToPrice(dto, orderId));

        publishOrderEvent(order);

        log.info("CUSTOM_PLAN_ORDER_CREATED | orderId={} | deliveryDate={}", orderId, scheduledOrder.getDeliveryDate());

        return orderId;
    }

    /*
     * SAVE ORDER ITEMS
     */
    private void saveOrderItems(List<CoOrderItemDto> items, String orderId) {

        for (CoOrderItemDto item : items) {

            orderItemRepository.save(orderMapper.mapToItem(item, orderId));
        }

        log.info("ORDER_ITEMS_SAVED | orderId={} | itemCount={}", orderId, items.size());
    }

    /*
     * PUBLISH EVENT
     */
    private void publishOrderEvent(CoOrder order) {

        COOrderEvent event = COEventMapper.mapToOrderEvent(order);

        boolean scheduledOrder = COConstants.ORDER_TYPE_SCHEDULED_RECURRING.equalsIgnoreCase(order.getOrderType()) || COConstants.ORDER_TYPE_SCHEDULED_CUSTOM_PLAN.equalsIgnoreCase(order.getOrderType());

        event.setScheduledOrder(scheduledOrder);

        event.setMorningReminder(false);

        event.setOneHourReminder(false);

        event.setNotificationType(scheduledOrder ? COConstants.NOTIFICATION_TYPE_SCHEDULED_ORDER_CREATED : COConstants.NOTIFICATION_TYPE_CREATED);

        kafkaTemplate.send("new-orders", order.getOrderId(), event);

        log.info("KAFKA_EVENT_PUBLISHED | orderId={} | orderType={}", order.getOrderId(), order.getOrderType());
    }

    /*
     * VALIDATE REQUEST
     */
    private void validatePlaceOrderRequest(CoPlaceOrderRequestDto dto) {

        log.info("SERVICE_START | VALIDATE_PLACE_ORDER_REQUEST");

        if (dto == null) {

            log.error("VALIDATION_FAILED | REQUEST_NULL");

            throw new OrderException("Place order request cannot be null");
        }

        if (dto.getOrderType() == null || dto.getOrderType().isBlank()) {

            log.error("VALIDATION_FAILED | ORDER_TYPE_REQUIRED");

            throw new OrderException(COConstants.MSG_INVALID_ORDER_TYPE);
        }

        if (COConstants.ORDER_TYPE_NORMAL.equalsIgnoreCase(dto.getOrderType())) {

            if (dto.getItems() == null || dto.getItems().isEmpty()) {

                log.error("VALIDATION_FAILED | ORDER_ITEMS_EMPTY");

                throw new OrderException(COConstants.MSG_ORDER_ITEMS_EMPTY);
            }
        }

        if (COConstants.ORDER_TYPE_SCHEDULED_RECURRING.equalsIgnoreCase(dto.getOrderType()) || COConstants.ORDER_TYPE_SCHEDULED_CUSTOM_PLAN.equalsIgnoreCase(dto.getOrderType())) {

            validateScheduledOrder(dto);
        }

        log.info("SERVICE_END | VALIDATE_PLACE_ORDER_REQUEST_SUCCESS");
    }


    /*
     * VALIDATE SCHEDULED ORDER
     */
    private void validateScheduledOrder(CoPlaceOrderRequestDto dto) {

        log.info("SERVICE_START | VALIDATE_SCHEDULED_ORDER");

        if (dto.getMealPreference() == null || dto.getMealPreference().isBlank()) {

            throw new OrderException(COConstants.MSG_MEAL_PREFERENCE_REQUIRED);
        }

        if (COConstants.ORDER_TYPE_SCHEDULED_RECURRING.equalsIgnoreCase(dto.getOrderType())) {

            if (dto.getItems() == null || dto.getItems().isEmpty()) {

                throw new OrderException(COConstants.MSG_ORDER_ITEMS_EMPTY);
            }

            if (dto.getScheduledDeliveryDateTime() == null) {

                throw new OrderException(COConstants.MSG_SCHEDULED_TIME_REQUIRED);
            }

            if (dto.getScheduledDeliveryDateTime().isBefore(LocalDateTime.now())) {

                throw new OrderException("Scheduled delivery date time cannot be in the past");
            }

            if (dto.getSubscriptionStartDate() == null || dto.getSubscriptionEndDate() == null) {

                throw new OrderException(COConstants.MSG_SUBSCRIPTION_DATES_REQUIRED);
            }

            if (dto.getSubscriptionStartDate().isBefore(LocalDateTime.now())) {

                throw new OrderException("Subscription start date cannot be in the past");
            }

            if (dto.getSubscriptionEndDate().isBefore(dto.getSubscriptionStartDate())) {

                throw new OrderException(COConstants.MSG_INVALID_SUBSCRIPTION_DATE);
            }
        }

        if (COConstants.ORDER_TYPE_SCHEDULED_CUSTOM_PLAN.equalsIgnoreCase(dto.getOrderType())) {

            if (dto.getScheduledOrders() == null || dto.getScheduledOrders().isEmpty()) {

                throw new OrderException(COConstants.MSG_SCHEDULED_ORDERS_REQUIRED);
            }

            for (CoScheduledOrderDto scheduledOrder : dto.getScheduledOrders()) {

                LocalDateTime scheduledDateTime = LocalDateTime.of(scheduledOrder.getDeliveryDate(), scheduledOrder.getScheduledDeliveryTime());

                if (scheduledDateTime.isBefore(LocalDateTime.now())) {

                    throw new OrderException("Scheduled delivery date time cannot be in the past");
                }
            }
        }

        log.info("SERVICE_END | VALIDATE_SCHEDULED_ORDER_SUCCESS");
    }

    /*
     * BUILD RESPONSE
     */
    private CoPlaceOrderResponseDto buildResponse(String message, Integer subscriptionId, List<String> orderIds, CoPlaceOrderRequestDto dto) {

        CoPlaceOrderResponseDto response = new CoPlaceOrderResponseDto();

        response.setMessage(message);

        response.setMealSubscriptionId(subscriptionId);

        response.setOrderIds(orderIds);

        response.setOrderType(dto.getOrderType());

        response.setTotalOrdersCreated(orderIds.size());

        response.setCreatedAt(LocalDateTime.now());

        return response;
    }

    /*
     * GENERATE ORDER ID
     */
    @Transactional
    public String generateOrderId() {

        LocalDate today = LocalDate.now();

        Object[] row = sequenceRepository.getSequenceForUpdate();

        if (row == null) {

            sequenceRepository.insertInitial(today);
            row = sequenceRepository.getSequenceForUpdate();
        }

        LocalDate lastDate = ((java.sql.Date) row[0]).toLocalDate();
        Long currentSeq = row[1] instanceof Number ? ((Number) row[1]).longValue() : Long.valueOf(row[1].toString());

        Long nextSeq = (!today.equals(lastDate)) ? 1 : currentSeq + 1;

        sequenceRepository.updateSequence(today, nextSeq);

        String orderId = "jippy" + today.format(DateTimeFormatter.BASIC_ISO_DATE) + nextSeq;

        log.info("ORDER_ID_GENERATED | orderId={}", orderId);

        return orderId;
    }

    /*
     * FREQUENT OUTLETS
     */
    @Override
    public List<Integer> getFrequentOutlets(Integer customerId) {

        log.info("SERVICE_START | GET_FREQUENT_OUTLETS | customerId={}", customerId);

        List<Integer> result = orderRepository.findFrequentOutlets(customerId);

        if (result == null) {
            return new ArrayList<>();
        }

        log.info("SERVICE_END | GET_FREQUENT_OUTLETS_SUCCESS | customerId={} | outletCount={}", customerId, result.size());

        return result;
    }

    @Override
    public Integer getRecentOutlet(Integer customerId) {

        log.info("SERVICE_START | GET_RECENT_OUTLET | customerId={}", customerId);

        Integer outlet = orderRepository.findRecentOutlet(customerId);

        if (outlet == null) {

            log.error("VALIDATION_FAILED | NO_RECENT_OUTLET_FOUND | customerId={}", customerId);

            throw new OrderException("No orders found for customerId: " + customerId);
        }

        log.info("SERVICE_END | GET_RECENT_OUTLET_SUCCESS | customerId={} | outletId={}", customerId, outlet);

        return outlet;

    }
}