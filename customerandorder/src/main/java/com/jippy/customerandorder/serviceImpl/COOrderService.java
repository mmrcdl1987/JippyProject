package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.constants.COConstants;
import com.jippy.customerandorder.dto.*;
import com.jippy.customerandorder.entity.*;
import com.jippy.customerandorder.exception.CoBusinessException;
import com.jippy.customerandorder.exception.OrderException;
import com.jippy.customerandorder.feignClients.DivisionFeignClient;
import com.jippy.customerandorder.feignClients.FMFeignClient;
import com.jippy.customerandorder.iservice.CoWalletRefundService;
import com.jippy.customerandorder.iservice.IOrderService;
import com.jippy.customerandorder.iservice.ICoCustomerService;
import com.jippy.customerandorder.mapper.COEventMapper;
import com.jippy.customerandorder.mapper.CoOrderMapper;
import com.jippy.customerandorder.mapper.CoOrderRejectionMapper;
import com.jippy.customerandorder.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.jippy.customerandorder.constants.COConstants.ORDER_TYPE_SCHEDULED_CUSTOM_PLAN;
import static com.jippy.customerandorder.constants.COConstants.ORDER_TYPE_SCHEDULED_RECURRING;

@Service
@RequiredArgsConstructor
@Slf4j
public class COOrderService implements IOrderService {

    private final CoOrderRepository orderRepository;
    private final CoOrderItemRepository orderItemRepository;
    private final CoOrderPriceBreakupRepository priceRepository;
    private final CoCustomerCartRepository cartRepository;
    private final MealSubscriptionRepository subscriptionRepository;
    private final CoOrderMapper orderMapper;
    private final CoOrderSequenceRepository sequenceRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ICoCustomerService customerService;
    private final CoCustomerWalletRepository walletRepository;
    private final CoWalletSettingsRepository walletSettingsRepository;
    private final CoCustomerWalletTransactionsRepository transactionsRepository;
    private final CoCustomerRepository customerRepository;
    private final FMFeignClient fmFeignClient;
    private final CoOrderRejectionRepository rejectionRepository;
    private final CoWalletRefundService walletRefundService;
    private final CoPaymentModeRepository paymentModeRepository;
    private final DivisionFeignClient divisionFeignClient;

    /*
     * PLACE ORDER
     */
    @Override
    @Transactional
    public CoPlaceOrderResponseDto placeOrder(CoPlaceOrderRequestDto dto) {

        log.info("SERVICE_START | PLACE_ORDER | customerId={} | orderType={}", dto.getCustomerId(), dto.getOrderType());

        try {

            validatePlaceOrderRequest(dto);
            // Only cart-based order types should validate the cart
            if (COConstants.ORDER_TYPE_NORMAL.equalsIgnoreCase(dto.getOrderType())) {
                return processNormalOrder(dto);
            }

            if (COConstants.GROUP_ORDER_ORDER_TYPE.equalsIgnoreCase(dto.getOrderType()) || COConstants.COMMUNITY_ORDER_ORDER_TYPE.equals(dto.getOrderType()) || COConstants.COMMUNITY_GROUP_ORDER_ORDER_TYPE.equals(dto.getOrderType())) {

                validateCartOutlet(dto);
                return processNormalOrder(dto);
            }

            if (ORDER_TYPE_SCHEDULED_RECURRING.equalsIgnoreCase(dto.getOrderType())) {
                validateCartOutlet(dto);
                return processRecurringOrders(dto);
            }

            if (ORDER_TYPE_SCHEDULED_CUSTOM_PLAN.equalsIgnoreCase(dto.getOrderType())) {
                validateCartOutlet(dto);
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
        CoOrder savedOrder = orderRepository.save(order);

        // Save items directly from request
        saveOrderItems(dto.getItems(), savedOrder);

        BigDecimal currentTotal = dto.getOrderTotalAmount() != null ? dto.getOrderTotalAmount() : BigDecimal.ZERO;
        log.info("ORDER_AMOUNT_DISCOUNTED | orderId={} | itemAmount={} | couponDiscount={} | orderAmountDiscounted={}", orderId, dto.getOrderAmount(), dto.getCouponDiscount(), dto.getOrderAmountDiscounted());
        BigDecimal walletDeduction = processWalletDeduction(dto, orderId, currentTotal);

        BigDecimal finalOrderTotal = currentTotal.subtract(walletDeduction).setScale(2, RoundingMode.HALF_UP);
        if (finalOrderTotal.compareTo(BigDecimal.ZERO) < 0) {
            finalOrderTotal = BigDecimal.ZERO;
        }

        // Set wallet amount discounted and updated total on DTO before mapping to Price
        dto.setWalletAmount(walletDeduction);
        dto.setOrderTotalAmount(finalOrderTotal);

        CoOrderPriceBreakup savedOrderPriceBreakUp = priceRepository.save(orderMapper.mapToPrice(dto, savedOrder));
        // DO NOT clear cart for direct NORMAL order
        // Qualify referral if this is the first order
        try {
            customerService.qualifyReferralOnFirstOrder(dto.getCustomerId());
        } catch (Exception ex) {
            log.error("REFERRAL_QUALIFICATION_FAILED | " + "orderId={} | customerId={} | error={}", orderId, dto.getCustomerId(), ex.getMessage());
        }

        CoPlaceOrderRequestDto updatedDto = new CoPlaceOrderRequestDto();

        updatedDto.setOrderId(savedOrder.getOrderId());
        updatedDto.setOrderTotalAmount(savedOrderPriceBreakUp.getOrderTotalAmount());
        updatedDto.setOutletId(savedOrder.getOutletId());
        updatedDto.setCustomerId(savedOrder.getCustomerId());
        updatedDto.setOrderType(savedOrder.getOrderType());
        updatedDto.setOrderStatus(savedOrder.getOrderStatus());
        updatedDto.setPaymentModeId(savedOrder.getPaymentModeId());
        updatedDto.setWalletAmount(walletDeduction);
        publishOrderEvent(order);
        clearCustomerCart(dto.getCustomerId(), orderId);

        log.info("SERVICE_END | PROCESS_NORMAL_ORDER_SUCCESS | orderId={}", orderId);

        CoPlaceOrderResponseDto response = buildResponse(COConstants.MSG_ORDER_CREATED, null, List.of(orderId), updatedDto);
        response.setWalletDiscount(walletDeduction);
        return response;
    }

    /*
     * WALLET DEDUCTION (Up to 25% of wallet balance amount)
     */
    /*
     * WALLET DEDUCTION (Up to 25% of wallet balance amount)
     */
    private BigDecimal processWalletDeduction(CoPlaceOrderRequestDto dto, String orderId, BigDecimal currentTotalAmount) {

        // Customer did not request wallet usage
        if ((dto.getUseWallet() == null || !dto.getUseWallet()) && (dto.getWalletAmount() == null || dto.getWalletAmount().compareTo(BigDecimal.ZERO) <= 0)) {

            return BigDecimal.ZERO;
        }

        // ==========================================
        // GET CUSTOMER WALLET
        // ==========================================

        CoCustomerWallet wallet = walletRepository.findByCustomerCustomerId(dto.getCustomerId()).orElseThrow(() -> new OrderException(COConstants.WALLET_NOT_FOUND));

        BigDecimal walletBalance = wallet.getBalanceAmount() != null ? wallet.getBalanceAmount() : BigDecimal.ZERO;

        if (walletBalance.compareTo(BigDecimal.ZERO) <= 0) {

            throw new OrderException("Insufficient wallet balance");
        }

        // ==========================================
        // GET MAX WALLET USAGE SETTINGS
        // ==========================================

        CoWalletSettings walletUsageSettings = walletSettingsRepository.findBySettingType(COConstants.MAX_WALLET_USAGE_PER_ORDER).orElseThrow(() -> new CoBusinessException("Maximum wallet usage percentage not configured"));

        Integer maxWalletUsagePercentage = walletUsageSettings.getSettingValue();

        // ==========================================
        // MAXIMUM WALLET USAGE
        // ==========================================
        log.info("current total amount :{} max wallet usage percentage : {} ", currentTotalAmount, maxWalletUsagePercentage);
        BigDecimal maxAllowedFromWallet = currentTotalAmount.multiply(BigDecimal.valueOf(maxWalletUsagePercentage).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)).setScale(2, RoundingMode.HALF_UP);

        log.info("WALLET_LIMIT_CALCULATED | customerId={} | " + "orderId={} | orderAmount={} | walletBalance={} | " + "maxWalletUsage={}", dto.getCustomerId(), orderId, currentTotalAmount, walletBalance, maxAllowedFromWallet);

        // ==========================================
        // DETERMINE DEDUCTION
        // ==========================================

        BigDecimal deductionAmount;

        if (dto.getWalletAmount() != null && dto.getWalletAmount().compareTo(BigDecimal.ZERO) > 0) {

            BigDecimal requestedWalletAmount = dto.getWalletAmount().setScale(2, RoundingMode.HALF_UP);

            // Cannot exceed 25% of order value
            if (requestedWalletAmount.compareTo(maxAllowedFromWallet) > 0) {

                throw new OrderException("Wallet utilization cannot exceed 25% " + "of order value. Maximum allowed: ₹" + maxAllowedFromWallet);
            }

            // Cannot exceed wallet balance
            if (requestedWalletAmount.compareTo(walletBalance) > 0) {

                throw new OrderException("Insufficient wallet balance. " + "Available balance: ₹" + walletBalance);
            }

            // Cannot exceed order amount
            deductionAmount = requestedWalletAmount.min(currentTotalAmount);

        } else {
            // useWallet = true
            // Automatically use maximum allowed amount
            deductionAmount = walletBalance.min(maxAllowedFromWallet).min(currentTotalAmount);
        }

        // ==========================================
        // DEDUCT WALLET
        // ==========================================

//        if (deductionAmount.compareTo(BigDecimal.ZERO) > 0) {
//
//            BigDecimal newBalance =
//                    walletBalance
//                            .subtract(deductionAmount)
//                            .setScale(2, RoundingMode.HALF_UP);
//
//            wallet.setBalanceAmount(newBalance);
//            wallet.setUpdatedAt(LocalDateTime.now());
//            wallet.setUpdatedBy(dto.getCustomerId());
//
//            walletRepository.save(wallet);
//
//            log.info(
//                    "WALLET_DEDUCTED | customerId={} | " +
//                            "orderId={} | deductedAmount={} | " +
//                            "remainingBalance={}",
//                    dto.getCustomerId(),
//                    orderId,
//                    deductionAmount,
//                    newBalance
//            );
//
//            // ==========================================
//            // SAVE WALLET TRANSACTION
//            // ==========================================
//
//            CoCustomerWalletTransactions transaction = new CoCustomerWalletTransactions();
//
//            transaction.setWalletId(wallet.getWalletId());
//            transaction.setOrderId(orderId);
//            transaction.setTransactionType(COConstants.WALLET_DEBIT);
//            transaction.setAmount(deductionAmount.negate());
//            transaction.setCreatedAt(LocalDateTime.now());
//            transaction.setCreatedBy(dto.getCustomerId());
//            transactionsRepository.save(transaction);
//
//            log.info(
//                    "WALLET_TRANSACTION_SAVED | orderId={} | " +
//                            "walletId={} | transactionPoints={}",
//                    orderId,
//                    wallet.getWalletId(),
//                    transaction.getPoints()
//            );
//        }
        return deductionAmount;
    }


    void customerPostOrderWalletTransactions(CoOrder order) {

        CoCustomerWallet wallet = walletRepository.findByCustomerCustomerId(order.getCustomerId()).orElseThrow(() -> new OrderException(COConstants.WALLET_NOT_FOUND));

        CoOrderPriceBreakup orderPriceBreakUp = priceRepository.findByOrder_OrderId(order.getOrderId());
        BigDecimal walletAmountUsed = orderPriceBreakUp.getWalletAmount();

        if (walletAmountUsed.compareTo(BigDecimal.ZERO) > 0) {

            BigDecimal newBalance = wallet.getBalanceAmount().subtract(walletAmountUsed).setScale(2, RoundingMode.HALF_UP);

            wallet.setBalanceAmount(newBalance);
            wallet.setUpdatedAt(LocalDateTime.now());

            walletRepository.save(wallet);

            log.info("WALLET_DEDUCTED | customerId={} | " + "orderId={} | deductedAmount={} | " + "remainingBalance={}", order.getCustomerId(), order.getOrderId(), walletAmountUsed, newBalance);

            // ==========================================
            // SAVE WALLET TRANSACTION
            // ==========================================

            CoCustomerWalletTransactions transaction = new CoCustomerWalletTransactions();

            transaction.setWalletId(wallet.getWalletId());
            transaction.setOrderId(order.getOrderId());
            transaction.setTransactionType(COConstants.WALLET_DEBIT);
            transaction.setAmount(walletAmountUsed.negate());
            transaction.setCreatedAt(LocalDateTime.now());
            transactionsRepository.save(transaction);

            log.info("WALLET_TRANSACTION_SAVED | orderId={} | " + "walletId={} | transactionPoints={}", order.getOrderId(), wallet.getWalletId(), transaction.getPoints());
        }

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

        saveOrderItems(dto.getItems(), order);

        priceRepository.save(orderMapper.mapToPrice(dto, order));

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

        saveOrderItems(scheduledOrder.getItems(), order);

        priceRepository.save(orderMapper.mapToPrice(dto, order));

        publishOrderEvent(order);

        log.info("CUSTOM_PLAN_ORDER_CREATED | orderId={} | deliveryDate={}", orderId, scheduledOrder.getDeliveryDate());

        return orderId;
    }

    /*
     * SAVE ORDER ITEMS
     */
    private void saveOrderItems(List<CoOrderItemDto> items, CoOrder order) {

        for (CoOrderItemDto item : items) {

            log.info("ORDER_ITEM_SAVE | orderId={} | productId={} | variantOptionId={} | quantity={}", order.getOrderId(), item.getProductId(), item.getVariantOptionId(), item.getQuantity());

            orderItemRepository.save(orderMapper.mapToItem(item, order));
        }

        log.info("ORDER_ITEMS_SAVED | orderId={} | itemCount={}", order.getOrderId(), items.size());
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

        if (dto.getOrderType().equals("NORMAL")) {
            response.setOrderTotalAmount(dto.getOrderTotalAmount());
            response.setOrderId(dto.getOrderId());
            response.setOutletId(dto.getOutletId());
            response.setPaymentModeId(dto.getPaymentModeId());
            response.setOrderStatus(dto.getOrderStatus());
            response.setCustomerId(dto.getCustomerId());
        }

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

    @Override
    public CoOrderDto getOrder(String orderId) {

        log.info("SERVICE_START | GET_ORDER | orderId={}", orderId);

        CoOrder order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        CoOrderDto dto = new CoOrderDto();

        dto.setOrderId(order.getOrderId());

        dto.setDriverId(order.getDriverId());

        dto.setOrderStatus(order.getOrderStatus());

        dto.setPaymentModeId(order.getPaymentModeId());

        dto.setOutletId(order.getOutletId());

        return dto;
    }

    @Override
    @Transactional
    public void updateOrderStatus(CoOrderDto orderDto) {

        log.info("SERVICE_START | UPDATE_ORDER_STATUS | orderId={} | newStatus={}", orderDto.getOrderId(), orderDto.getOrderStatus());

        CoOrder order = orderRepository.findById(orderDto.getOrderId()).orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Update order status
        order.setOrderStatus(orderDto.getOrderStatus());
        order.setUpdatedAt(LocalDateTime.now());

        orderRepository.save(order);

        if ("ORDER_PLACED".equalsIgnoreCase(orderDto.getOrderStatus())) {

            Optional<CoCustomer> optionalCoCustomer = customerRepository.findByCustomerId(order.getCustomerId());
            CoCustomer customer = new CoCustomer();

            if (optionalCoCustomer.isPresent()) {
                customer = optionalCoCustomer.get();
            }

            //After Payment successful publish order event
            publishNewOrderEvent(order, customer);

            //If customer uses wallet make those customer wallet changes and insert record in wallet transaction table
            customerPostOrderWalletTransactions(order);


        }

        // ==========================================
        // PROCESS REFERRAL REWARD AFTER DELIVERY
        // ==========================================

        if ("DELIVERED".equalsIgnoreCase(orderDto.getOrderStatus())) {

            log.info("REFERRAL_REWARD_TRIGGER | orderId={} | customerId={}", order.getOrderId(), order.getCustomerId());
            try {
                customerService.processReferralReward(order.getCustomerId(), order.getOrderId());
                log.info("REFERRAL_REWARD_SUCCESS | orderId={} | customerId={}", order.getOrderId(), order.getCustomerId());
            } catch (Exception ex) {
                log.error("REFERRAL_REWARD_FAILED | orderId={} | customerId={} | error={}", order.getOrderId(), order.getCustomerId(), ex.getMessage(), ex);
                throw ex;
            }
        }
        log.info("SERVICE_END | UPDATE_ORDER_STATUS_SUCCESS | orderId={} | newStatus={}", orderDto.getOrderId(), orderDto.getOrderStatus());
    }


    private void publishNewOrderEvent(CoOrder order, CoCustomer customer) {

        List<Integer> productIds = new ArrayList<>();
        List<Integer> productVariantIds = new ArrayList<>();

        List<CoOrderItem> orderItems = order.getOrderItems();
        for (CoOrderItem item : orderItems) {
            productIds.add(item.getProductId());
            productVariantIds.add(item.getVariantOptionId());
        }

        ResponseEntity<List<CoOrderItemsEvent>> orderItemsEventListResponse = fmFeignClient.getOrderProductItemsForMerchant(productIds, productVariantIds);
        List<CoOrderItemsEvent> orderItemsEventList = orderItemsEventListResponse.getBody();

        CoNewOrderEvent event = COEventMapper.mapToOrderNewOrderEvent(order, customer, orderItemsEventList);

        kafkaTemplate.send("new-orders-for-outlet", order.getOrderId(), event);

        log.info("KAFKA_EVENT_PUBLISHED | orderId={} | orderType={}", order.getOrderId(), order.getOrderType());
    }


    private void validateCartOutlet(CoPlaceOrderRequestDto dto) {

        log.info("SERVICE_START | VALIDATE_CART_OUTLET | customerId={} | requestedOutletId={}", dto.getCustomerId(), dto.getOutletId());

        List<CoCustomerCart> cartItems = cartRepository.findByCustomerId(dto.getCustomerId());

        if (cartItems == null || cartItems.isEmpty()) {

            log.error("VALIDATION_FAILED | CART_EMPTY | customerId={}", dto.getCustomerId());

            throw new OrderException(COConstants.MSG_CART_EMPTY);
        }

        Integer cartOutletId = cartItems.get(0).getOutletId();

        if (cartOutletId == null) {

            log.error("VALIDATION_FAILED | CART_OUTLET_ID_MISSING | customerId={}", dto.getCustomerId());

            throw new OrderException("Cart outlet information not found");
        }

        /*
         * Verify that every cart item belongs
         * to the same outlet.
         */
        for (CoCustomerCart cartItem : cartItems) {

            if (cartItem.getOutletId() == null) {

                log.error("VALIDATION_FAILED | CART_ITEM_OUTLET_ID_MISSING | customerId={} | cartId={} | productId={}", dto.getCustomerId(), cartItem.getCartId(), cartItem.getProductId());

                throw new OrderException("Cart item outlet information not found");
            }

            if (!cartOutletId.equals(cartItem.getOutletId())) {

                log.error("VALIDATION_FAILED | MULTIPLE_OUTLETS_IN_CART | customerId={} | expectedOutletId={} | actualOutletId={} | productId={}", dto.getCustomerId(), cartOutletId, cartItem.getOutletId(), cartItem.getProductId());

                throw new OrderException("Cart contains items from multiple outlets");
            }
        }

        /*
         * Compare cart outlet with outlet
         * requested for placing the order.
         */
        if (!cartOutletId.equals(dto.getOutletId())) {

            log.error("VALIDATION_FAILED | CART_ORDER_OUTLET_MISMATCH | customerId={} | cartOutletId={} | orderOutletId={}", dto.getCustomerId(), cartOutletId, dto.getOutletId());

            throw new OrderException("Selected outlet does not match the cart outlet");
        }

        log.info("SERVICE_END | VALIDATE_CART_OUTLET_SUCCESS | customerId={} | outletId={}", dto.getCustomerId(), cartOutletId);
    }

    private void clearCustomerCart(Integer customerId, String orderId) {

        log.info("CART_CLEAR_START | customerId={} | orderId={}", customerId, orderId);

        List<CoCustomerCart> cartItems = cartRepository.findByCustomerId(customerId);

        if (cartItems == null || cartItems.isEmpty()) {

            log.info("CART_ALREADY_EMPTY | customerId={} | orderId={}", customerId, orderId);

            return;
        }

        cartRepository.deleteAll(cartItems);

        log.info("CART_CLEARED_SUCCESS | customerId={} | orderId={} | itemCount={}", customerId, orderId, cartItems.size());
    }

    @Override
    public CoOrderPriceBreakupDto getOrderPriceBreakup(String orderId) {

        log.info("SERVICE_START | GET_ORDER_PRICE_BREAKUP | orderId={}", orderId);

        CoOrderPriceBreakup breakup = priceRepository.findByOrder_OrderId(orderId);

        if (breakup == null) {

            log.error("PRICE_BREAKUP_NOT_FOUND | orderId={}", orderId);

            throw new OrderException("Order price breakup not found for orderId: " + orderId);
        }

        CoOrderPriceBreakupDto dto = new CoOrderPriceBreakupDto();

        dto.setOrderId(orderId);

        // ================= ORDER =================

        dto.setOrderAmount(breakup.getOrderAmount());

        dto.setOrderAmountDiscounted(breakup.getOrderAmountDiscounted());

        dto.setCouponDiscount(breakup.getCouponDiscount());

        // ================= DRIVER DELIVERY =================

        dto.setPickUpDistanceKms(breakup.getPickUpDistanceKms());

        dto.setDeliveryDistanceKms(breakup.getDeliveryDistanceKms());

        dto.setPickUpCharges(breakup.getPickUpCharges());

        dto.setDriverDeliveryFee(breakup.getDriverDeliveryFee());

        // ================= CUSTOMER DELIVERY =================

        dto.setCustomerDeliveryFee(breakup.getCustomerDeliveryFee());

        dto.setCustomerDeliveryFeeTax(breakup.getCustomerDeliveryFeeTax());

        // ================= TOTAL DELIVERY =================

        dto.setTotalDeliveryFee(breakup.getTotalDeliveryFee());

        // ================= PLATFORM =================

        dto.setPlatformFee(breakup.getPlatformFee());

        dto.setPlatformFeeTax(breakup.getPlatformFeeTax());

        // ================= SURGE =================

        dto.setSurgeFee(breakup.getSurgeFee());

        dto.setSurgeFeeTax(breakup.getSurgeFeeTax());

        // ================= PACKAGING =================

        dto.setPackagingFee(breakup.getPackagingFee());

        dto.setPackagingFeeTax(breakup.getPackagingFeeTax());

        // ================= TAX =================

        dto.setFoodTax(breakup.getFoodTax());

        dto.setTotalTax(breakup.getTotalTax());

        // ================= PAYMENT =================

        dto.setTip(breakup.getTip());

        dto.setWalletAmount(breakup.getWalletAmount());

        // ================= FINAL =================

        dto.setOrderTotalAmount(breakup.getOrderTotalAmount());

        log.info("SERVICE_END | GET_ORDER_PRICE_BREAKUP_SUCCESS | orderId={} | driverDeliveryFee={} | customerDeliveryFee={} | customerDeliveryFeeTax={} | totalDeliveryFee={} | totalTax={} | orderTotalAmount={}", orderId, dto.getDriverDeliveryFee(), dto.getCustomerDeliveryFee(), dto.getCustomerDeliveryFeeTax(), dto.getTotalDeliveryFee(), dto.getTotalTax(), dto.getOrderTotalAmount());

        return dto;
    }

    @Override
    public String acceptOrRejectOrderByOutlet(AcceptOrRejectOrderByOutletDto acceptOrRejectOrderByOutletDto) {

        log.info("SERVICE_START | ACCEPT_OR_REJECT_ORDER_BY_OUTLET | orderId={} | outletId={} | newStatus={}",
                acceptOrRejectOrderByOutletDto.getOrderId(),
                acceptOrRejectOrderByOutletDto.getOutletId(),
                acceptOrRejectOrderByOutletDto.getOrderStatus()
        );

        CoOrder order = orderRepository
                .findById(acceptOrRejectOrderByOutletDto.getOrderId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found")
                );

        if (!order.getOutletId().equals(acceptOrRejectOrderByOutletDto.getOutletId())) {
            throw new OrderException("Outlet ID does not match the order's outlet");
        }

        if("REJECT".equalsIgnoreCase(acceptOrRejectOrderByOutletDto.getOrderStatus())) {

            boolean isReasonMissing = acceptOrRejectOrderByOutletDto.getRejectionReason() == null
                    || acceptOrRejectOrderByOutletDto.getRejectionReason().isBlank();

            if(isReasonMissing){
                return "Rejection reason is required when rejecting an order";
            }
            order.setOrderStatus(COConstants.ORDER_STATUS_REJECTED);

            //Insert record in order rejection table if order is rejected by outlet

            CoOrderRejectionRequestDto coOrderRejectionRequestDto = new CoOrderRejectionRequestDto();
            coOrderRejectionRequestDto.setOrderId(acceptOrRejectOrderByOutletDto.getOrderId());
            coOrderRejectionRequestDto.setType(COConstants.OUTLET);
            coOrderRejectionRequestDto.setReason(acceptOrRejectOrderByOutletDto.getRejectionReason());
            coOrderRejectionRequestDto.setRejectedById(acceptOrRejectOrderByOutletDto.getOutletId());

            CoOrderRejection rejection = CoOrderRejectionMapper.toEntity(coOrderRejectionRequestDto);
            rejectionRepository.save(rejection);

            log.info(
                    "Outlet rejection saved | orderId={}",
                    order.getOrderId()
            );

            //Refund Customer
            Optional<CoPaymentModes> optionalCoPaymentModes = paymentModeRepository.findByPaymentModeId(order.getPaymentModeId());
            if(optionalCoPaymentModes.isPresent()){

                CoPaymentModes coPaymentModes = optionalCoPaymentModes.get();
                if(!coPaymentModes.getPaymentMode().equalsIgnoreCase(COConstants.PAYMENT_TYPE_COD)){
                    ResponseEntity<String> refundResponseEntity = divisionFeignClient.orderRefund(order.getOrderId(),
                            acceptOrRejectOrderByOutletDto.getRejectionReason());

                    if(refundResponseEntity.getStatusCode().is2xxSuccessful()){
                        log.info("Refund initiated successfully for orderId={} | refundResponse={}", order.getOrderId(), refundResponseEntity.getBody());
                    }else{
                        log.error("Refund initiation failed for orderId={} | refundResponse={}", order.getOrderId(), refundResponseEntity.getBody());
                    }
                }
            }


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


        }
        if("ACCEPT".equalsIgnoreCase(acceptOrRejectOrderByOutletDto.getOrderStatus())) {
            // Update order status
            order.setOrderStatus(COConstants.ORDER_STATUS_ACCEPTED);
            if(acceptOrRejectOrderByOutletDto.getPreparationTimeInMins() > 15){
                return "Preparation time cannot exceed 15 minutes";
            }
            order.setPreparationTime(acceptOrRejectOrderByOutletDto.getPreparationTimeInMins());
        }

        order.setUpdatedAt(LocalDateTime.now());
        order.setUpdatedBy(acceptOrRejectOrderByOutletDto.getOutletId());
        orderRepository.save(order);

        log.info("SERVICE_END | ACCEPT_OR_REJECT_ORDER_BY_OUTLET_SUCCESS | orderId={} | outletId={} | newStatus={}",
                acceptOrRejectOrderByOutletDto.getOrderId(),
                acceptOrRejectOrderByOutletDto.getOutletId(),
                acceptOrRejectOrderByOutletDto.getOrderStatus()
        );
        return "Order status updated successfully";
    }




}
