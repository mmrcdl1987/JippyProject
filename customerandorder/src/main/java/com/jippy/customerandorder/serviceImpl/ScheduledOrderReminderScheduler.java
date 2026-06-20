package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.constants.COConstants;
import com.jippy.customerandorder.dto.COOrderEvent;
import com.jippy.customerandorder.entity.CoOrder;
import com.jippy.customerandorder.mapper.COEventMapper;
import com.jippy.customerandorder.repository.CoOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledOrderReminderScheduler {

    private final CoOrderRepository orderRepository;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /*
     * MORNING REMINDER
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void sendMorningReminder() {

        log.info("SCHEDULER_START | MORNING_REMINDER");

        try {

            LocalDateTime start = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);

            LocalDateTime end = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);

            List<CoOrder> scheduledOrders = orderRepository.findByOrderTypeInAndScheduledDeliveryDateTimeBetween(List.of(COConstants.ORDER_TYPE_SCHEDULED_RECURRING, COConstants.ORDER_TYPE_SCHEDULED_CUSTOM_PLAN), start, end);

            for (CoOrder order : scheduledOrders) {

                COOrderEvent event = COEventMapper.mapToOrderEvent(order);

                event.setScheduledOrder(true);

                event.setMorningReminder(true);

                event.setOneHourReminder(false);

                event.setNotificationType(COConstants.NOTIFICATION_TYPE_SCHEDULED_ORDER_TODAY);

                kafkaTemplate.send("new-orders", order.getOrderId(), event);

                log.info("MORNING_REMINDER_SENT | orderId={}", order.getOrderId());
            }

            log.info("SCHEDULER_END | MORNING_REMINDER_SUCCESS | totalOrders={}", scheduledOrders.size());

        } catch (Exception ex) {

            log.error("EXCEPTION | MORNING_REMINDER_FAILED | error={}", ex.getMessage(), ex);
        }
    }

    /*
     * ONE HOUR REMINDER
     */
    @Scheduled(cron = "0 */10 * * * *")
    public void sendOneHourReminder() {

        log.info("SCHEDULER_START | ONE_HOUR_REMINDER");

        try {

            LocalDateTime now = LocalDateTime.now();

            LocalDateTime nextHour = now.plusHours(1);

            List<CoOrder> scheduledOrders = orderRepository.findByOrderTypeInAndScheduledDeliveryDateTimeBetween(List.of(COConstants.ORDER_TYPE_SCHEDULED_RECURRING, COConstants.ORDER_TYPE_SCHEDULED_CUSTOM_PLAN), now, nextHour);

            for (CoOrder order : scheduledOrders) {

                COOrderEvent event = COEventMapper.mapToOrderEvent(order);

                event.setScheduledOrder(true);

                event.setMorningReminder(false);

                event.setOneHourReminder(true);

                event.setNotificationType(COConstants.NOTIFICATION_TYPE_SCHEDULED_ORDER_1_HOUR_BEFORE);

                kafkaTemplate.send("new-orders", order.getOrderId(), event);

                log.info("ONE_HOUR_REMINDER_SENT | orderId={}", order.getOrderId());
            }

            log.info("SCHEDULER_END | ONE_HOUR_REMINDER_SUCCESS | totalOrders={}", scheduledOrders.size());

        } catch (Exception ex) {

            log.error("EXCEPTION | ONE_HOUR_REMINDER_FAILED | error={}", ex.getMessage(), ex);
        }
    }
}