package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.dto.COOrderEvent;
import com.jippy.customerandorder.entity.CoDriverOrder;
import com.jippy.customerandorder.repository.CoDriverOrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@Slf4j
public class CoDriverLocationRouteService {

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    CoDriverOrderRepository driverOrderRepository;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);


    @KafkaListener(topics = "order-delivered-topic", groupId = "driver-history-group")
    public void processDriverDeliveryHistory(COOrderEvent event) {
        log.info("Kafka consumer received event for Order: {}, Driver: {}, Status: {}", event.getOrderId(), event.getDriverId(), event.getStatus());
        String key = "track:" + event.getOrderId() + ":"  + event.getDriverId();
        List<String> rawCoords = redisTemplate.opsForList().range(key, 0, -1);

        if (rawCoords != null && rawCoords.size() > 1) {
            CoordinateSequence seq = PackedCoordinateSequenceFactory.DOUBLE_FACTORY.create(rawCoords.size(), 3, 1);

            for (int i = 0; i < rawCoords.size(); i++) {
                String[] parts = rawCoords.get(i).split(",");
                double lon = Double.parseDouble(parts[0]);
                double lat = Double.parseDouble(parts[1]);
                double time = Double.parseDouble(parts[2]);

                seq.setOrdinate(i, 0, lon);  // X
                seq.setOrdinate(i, 1, lat);  // Y
                seq.setOrdinate(i, 2, time); // M (Measure)
            }

            LineString lineStringM = geometryFactory.createLineString(seq);

            CoDriverOrder driverOrder = driverOrderRepository.findByDriverIdOrderId(event.getDriverId(),event.getOrderId()).
                    orElseThrow(() -> {
                        log.error("Driver order not found with driver Id: {} and order Id: {}", event.getDriverId(),event.getOrderId());
                        return new ResourceNotFoundException("Driver order not found with driver Id: " + event.getDriverId()+ " and order Id: " + event.getOrderId());
                    });

            log.info("Updating delivery route for driver order id: {}", driverOrder.getDriverOrderId());
            driverOrder.setDeliveryRoute(lineStringM);
            driverOrder.setUpdatedAt(java.time.LocalDateTime.now());
            driverOrder.setUpdatedBy(event.getDriverId());
            driverOrderRepository.save(driverOrder);
            log.info("Delivery route updated successfully for driver order id: {}", driverOrder.getDriverOrderId());


            // Cleanup Redis
            redisTemplate.delete(key);
            log.info("Successfully archived route for Order: {}", event.getOrderId());
        }
    }
}
