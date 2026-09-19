package com.jippy.driver.schedular;

import com.jippy.driver.dto.uber.UberDispatchRequestDto;
import com.jippy.driver.feignClients.COFeignClient;
import com.jippy.driver.serviceImpl.OrderAcceptedKafkaConsumer;
import com.jippy.driver.serviceImpl.UberDirectClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeliveryDispatchPoller {

    private final StringRedisTemplate redisTemplate;
    private final UberDirectClient uberDirectClient;
    private final COFeignClient coFeignClient;

    @Scheduled(fixedDelay = 5000)
    public void pollAndDispatchDeliveries() {
        long currentEpochMillis = System.currentTimeMillis();

        log.info("======================Uber delivery called");

        Set<String> readyOrderIds = redisTemplate.opsForZSet().rangeByScore(
                OrderAcceptedKafkaConsumer.DISPATCH_QUEUE_KEY,
                0,
                currentEpochMillis
        );

        log.info("======================================readyOrderIds"+readyOrderIds);

        if (readyOrderIds == null || readyOrderIds.isEmpty()) {
            return;
        }

        for (String orderId : readyOrderIds) {
            Long removed = redisTemplate.opsForZSet().remove(OrderAcceptedKafkaConsumer.DISPATCH_QUEUE_KEY, orderId);

            if (removed != null && removed > 0) {
                log.info("Triggering Uber Direct delivery creation for orderId: {}", orderId);
                try {
                    // Pass orderId to createDelivery method
                    UberDispatchRequestDto uberDispatchRequestDto = coFeignClient.getOrderDetailsForDelivery(orderId);
                    uberDirectClient.createDelivery(uberDispatchRequestDto);
                } catch (Exception e) {
                    log.error("Failed to create Uber delivery for orderId: {}. Re-queueing job.", orderId, e);

                    // Fixed double cast with proper parentheses
                    redisTemplate.opsForZSet().add(
                            OrderAcceptedKafkaConsumer.DISPATCH_QUEUE_KEY,
                            orderId,
                            (double) (System.currentTimeMillis() + 30000)
                    );
                }
            }
        }
    }
}
