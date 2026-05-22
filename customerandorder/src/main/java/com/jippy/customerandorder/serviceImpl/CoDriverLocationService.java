/*
package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.dto.COOrderEvent;
import com.jippy.customerandorder.dto.CoDriverOrderDto;
import com.jippy.customerandorder.dto.CoUpdateDriverLocationDto;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class CoDriverLocationService {

    private final KafkaTemplate<String, COOrderEvent> kafkaTemplate;
    private final RedisTemplate<String, String> redisTemplate;

    public CoDriverLocationService(KafkaTemplate<String, COOrderEvent> kafkaTemplate, RedisTemplate<String, String> redisTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.redisTemplate = redisTemplate;
    }

    // Every 5 seconds
    public String updateLiveLocation(CoUpdateDriverLocationDto updateDriverLocationDto) {
        String message = "";
        // New Key Format: track:orderId:driverId
        String key = "track:" + updateDriverLocationDto.getOrderId() + ":"  + updateDriverLocationDto.getDriverId();

        String val = updateDriverLocationDto.getLongitude() + "," + updateDriverLocationDto.getLatitude() + "," +
                System.currentTimeMillis();
       Long listSize =  redisTemplate.opsForList().rightPush(key, val);
        if (listSize != null) {
            message = "Updated location for Order: " + updateDriverLocationDto.getOrderId() +
                    ", Driver: " + updateDriverLocationDto.getDriverId() + " | Total Points: " + listSize;
        }
        return  message;
    }

    // When driver clicks "Deliver"
    public void pushCompleteOrderEvent(CoDriverOrderDto driverOrderDto) {

       COOrderEvent event = new COOrderEvent();
       event.setOrderId(driverOrderDto.getOrderId());
       event.setDriverId(driverOrderDto.getDriverId());
       event.setStatus("DELIVERED");

        // Push to Kafka and forget about it (Asynchronous)
        kafkaTemplate.send("order-delivered-topic", driverOrderDto.getOrderId(), event);
        System.out.println("Sent OrderDeliveredEvent to Kafka for Order: " + driverOrderDto.getOrderId());
    }


}
*/
