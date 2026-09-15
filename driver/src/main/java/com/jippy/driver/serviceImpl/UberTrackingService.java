package com.jippy.driver.serviceImpl;

import com.jippy.driver.constants.DConstants;
import com.jippy.driver.dto.DriverOrderDto;
import com.jippy.driver.entity.DriverOrder;
import com.jippy.driver.entity.ExternalDriverOrder;
import com.jippy.driver.mapper.DriverMapper;
import com.jippy.driver.repositary.DriverOrderRepository;
import com.jippy.driver.repositary.ExternalDriverOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UberTrackingService {

    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final DriverOrderRepository driverOrderRepository;
    private final ExternalDriverOrderRepository externalDriverOrderRepository;

    // Geometry factory with SRID 4326 (standard WGS 84 GPS coordinates)
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    /**
     * Process incoming live location updates from Uber Webhook
     */
    public void processLocationUpdate(String orderId, String driverId, double lat, double lng,Double bearing) {
        String redisKey = "order:route:" + orderId;
        String locationPayload = String.format("%f,%f,%d", lng, lat, System.currentTimeMillis());

        // 1. Store location point in a Redis List for historical path assembly
        redisTemplate.opsForList().rightPush(redisKey, locationPayload);

        // 2. Save latest location to Redis Spatial Index
        redisTemplate.opsForGeo().add("drivers:locations", new Point(lng, lat), driverId);

        // 3. Save Driver metadata Hash
        String driverMetaKey = "driver:" + driverId + ":meta";
        redisTemplate.opsForHash().putAll(driverMetaKey, Map.of(
                "lat", String.valueOf(lat),
                "lng", String.valueOf(lng),
                "bearing", String.valueOf(bearing),
                "updatedAt", String.valueOf(System.currentTimeMillis())
        ));

        // 4. Push real-time update to Customer frontend via Spring WebSocket (STOMP)
        messagingTemplate.convertAndSend("/topic/orders/" + orderId, Map.of(
                "orderId", orderId,
                "lat", lat,
                "lng", lng,
                "bearing", bearing
        ));
    }

    /**
     * Finalize the route: Read Redis path points -> Build LineString -> Store in PostGIS
     */
    public void finalizeAndStoreRoute(String orderId, String uberDeliveryId, String driverId) {
        String redisKey = "order:route:" + orderId;
        List<String> rawPoints = redisTemplate.opsForList().range(redisKey, 0, -1);

        if (rawPoints == null || rawPoints.size() < 2) {
            log.warn("Not enough GPS points collected to construct a LineString for order {}", orderId);
            redisTemplate.delete(redisKey);
            return;
        }

        List<Coordinate> coordinates = new ArrayList<>();
        for (String pt : rawPoints) {
            String[] parts = pt.split(",");
            double lng = Double.parseDouble(parts[0]);
            double lat = Double.parseDouble(parts[1]);
            coordinates.add(new Coordinate(lng, lat));
        }

        // Create LineString geometry
        LineString lineString = geometryFactory.createLineString(coordinates.toArray(new Coordinate[0]));

        DriverOrderDto driverOrderDto = new DriverOrderDto();

        driverOrderDto.setOrderId(orderId);
        driverOrderDto.setUberDeliveryId(uberDeliveryId);
        driverOrderDto.setUberDriverId(driverId);
        driverOrderDto.setDeliveryRoute(lineString);
        driverOrderDto.setCreatedAt(LocalDateTime.now());

        // Persist in database
        DriverOrder driverOrder = DriverMapper.mapToDriverOrderEntity(driverOrderDto,null);

        driverOrderRepository.save(driverOrder);
        log.info("Successfully persisted LineString route for order {}", orderId);

        // Clean up temporary Redis list
        redisTemplate.delete(redisKey);
    }

    public String assignDriverToOrder(String orderId, String uberDeliveryId, String driverId,
            String driverName, String driverPhoneNumber,String vehiclePlate, String vehicleType) {

        log.info("Driver : {}  is assigned to order : {} ", driverName,orderId);

        Optional<DriverOrder> driverOrderOptional = driverOrderRepository.findByOrderId(orderId);
        DriverOrder driverOrder;
        if(driverOrderOptional.isPresent()){
            driverOrder = driverOrderOptional.get();
        }else{
            log.warn("Uber delivery Id is no found");
            return "Uber delivery Id is no found";
        }

        driverOrder.setStatus(DConstants.DRIVER_ORDER_ASSIGNED_STATUS_ACCEPTED);
        driverOrder.setUpdatedAt(LocalDateTime.now());

        driverOrderRepository.save(driverOrder);

        Optional<ExternalDriverOrder> externalDriverOrderOptional = externalDriverOrderRepository.
                findByExternalDeliveryId(uberDeliveryId);

        ExternalDriverOrder externalDriverOrder = new ExternalDriverOrder();

        if(externalDriverOrderOptional.isPresent()){
            externalDriverOrder = externalDriverOrderOptional.get();
        }

        externalDriverOrder.setCourierId(driverId);
        externalDriverOrder.setCourierName(driverName);
        externalDriverOrder.setCourierPhone(driverPhoneNumber);
        externalDriverOrder.setCourierLicensePlate(vehiclePlate);
        externalDriverOrder.setCourierVehicleType(vehicleType);
        externalDriverOrder.setStatus(DConstants.DRIVER_ORDER_ASSIGNED_STATUS_ACCEPTED);

        return "Driver : "+driverName+" is assigned to order : "+ orderId;
    }

    public String markOrderPickedUp(String orderId, String uberDeliveryId, Double currentLatitude,
            Double currentLongitude, Double bearing,String trackingUrl) {

        log.info("order : {}, is picked up" , orderId);
        Optional<DriverOrder> driverOrderOptional = driverOrderRepository.findByOrderId(orderId);
        DriverOrder driverOrder;

        if(driverOrderOptional.isPresent()){
            driverOrder = driverOrderOptional.get();
        }else{
            log.warn("Uber delivery Id is no found");
            return "Uber delivery Id is no found";
        }

        driverOrder.setStatus(DConstants.DRIVER_ORDER_ASSIGNED_STATUS_PICKUP);
        driverOrderRepository.save(driverOrder);

        Optional<ExternalDriverOrder> externalDriverOrderOptional = externalDriverOrderRepository
                .findByExternalDeliveryId(uberDeliveryId);

        ExternalDriverOrder externalDriverOrder = new ExternalDriverOrder();
        if(externalDriverOrderOptional.isPresent()){
            externalDriverOrder =  externalDriverOrderOptional.get();
        }

        externalDriverOrder.setCourierCurrentLat(BigDecimal.valueOf(currentLatitude));
        externalDriverOrder.setCourierCurrentLng(BigDecimal.valueOf(currentLongitude));
        externalDriverOrder.setBearing(BigDecimal.valueOf(bearing));
        externalDriverOrder.setTrackingUrl(trackingUrl);
        externalDriverOrder.setStatus(DConstants.DRIVER_ORDER_ASSIGNED_STATUS_PICKUP);

        externalDriverOrderRepository.save(externalDriverOrder);

        return "Order status : pickup is updated successfully";

    }

}
