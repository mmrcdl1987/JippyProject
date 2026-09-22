package com.jippy.driver.serviceImpl;

import com.jippy.driver.constants.DConstants;
import com.jippy.driver.dto.DriverOrderDto;
import com.jippy.driver.dto.uber.UberDispatchRequestDto;
import com.jippy.driver.entity.DriverOrder;
import com.jippy.driver.entity.ExternalDriverOrder;
import com.jippy.driver.feignClients.COFeignClient;
import com.jippy.driver.mapper.DriverMapper;
import com.jippy.driver.repositary.DriverOrderRepository;
import com.jippy.driver.repositary.ExternalDriverOrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Value;
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
    private final COFeignClient coFeignClient;
    private final UberDirectClient uberDirectClient;

    // Geometry factory with SRID 4326 (standard WGS 84 GPS coordinates)
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Value("${uber.direct.max-retries:2}")
    private int maxRetryLimit;

    @Value("${uber.direct.webhook-url}")
    private String uberWebhookUrl;

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

    @Transactional
    public void saveDeliveryCharges(String externalOrderId, String uberDeliveryId, Double finalFee, Double tipAmount, String currency) {

        // Find by uberDeliveryId
        Optional<ExternalDriverOrder>  uberDeliveryOptional = externalDriverOrderRepository.findByExternalDeliveryId(uberDeliveryId);
        log.info("========================"+uberDeliveryOptional);
        ExternalDriverOrder externalDriverOrder;
        if(uberDeliveryOptional.isPresent()){
            externalDriverOrder = uberDeliveryOptional.get();
        }else{
            log.warn("UberDelivery record not found for externalOrderId: " + externalOrderId + "  deliveryId: " + uberDeliveryId);
            throw new RuntimeException("UberDelivery record not found for externalOrderId: " + externalOrderId + " / deliveryId: " + uberDeliveryId);
        }
        if (finalFee != null) {
            externalDriverOrder.setUber_delivery_charges(BigDecimal.valueOf(finalFee));
        }
        if (tipAmount != null) {
            externalDriverOrder.setUberTip(BigDecimal.valueOf(tipAmount));
        }

        externalDriverOrderRepository.save(externalDriverOrder);
        log.info("Updated final delivery charges for order: {}, finalFee: {}, tip: {} {}",
                externalOrderId, finalFee, tipAmount, currency);
    }

    @Transactional
    public void handleCancellationOrFailure(String orderId, String uberDeliveryId, String failureReason) {
        ExternalDriverOrder order = externalDriverOrderRepository.findByExternalDeliveryId(uberDeliveryId)
                .orElseThrow(() -> new RuntimeException("Order not found for ID: " + orderId));

        String reason = failureReason.toUpperCase();
        order.setStatus(reason);

        // Also update main driver_orders status if linked
        if (order.getDriverOrder() != null) {
            order.getDriverOrder().setStatus(reason);
            driverOrderRepository.save(order.getDriverOrder());
        }

        if ("UNFULFILLED".equals(reason) || "EXPIRED".equals(reason)) {
            int currentRetries = order.getRetryCount() != null ? order.getRetryCount() : 0;

            if (currentRetries < maxRetryLimit) {
                order.setRetryCount(currentRetries + 1);
                externalDriverOrderRepository.save(order);

                log.info("Order {} failed with status {}. Attempting retry {} of {}",
                        orderId, reason, order.getRetryCount(), maxRetryLimit);

                redispatchToUber(order);
            } else {
                // Max retries reached -> Mark status as failed in driver_orders so local drivers can pick it up
                if (order.getDriverOrder() != null) {
                    order.getDriverOrder().setStatus("UBER_DISPATCH_FAILED");
                    driverOrderRepository.save(order.getDriverOrder());
                }

                externalDriverOrderRepository.save(order);
                log.error("Order {} reached max retries. Triggering fallback.", orderId);

            }
        } else {
            externalDriverOrderRepository.save(order);
        }
    }


    public void redispatchToUber(ExternalDriverOrder externalOrder) {
        try {
            DriverOrder driverOrder = externalOrder.getDriverOrder();
            if (driverOrder == null) {
                throw new IllegalStateException("Cannot re-dispatch: DriverOrder entity reference is missing.");
            }

            String orderId = driverOrder.getOrderId(); // Get core order identifier (e.g., ORD-2026-9921)
            log.info("Initiating re-dispatch attempt #{} for order {}", externalOrder.getRetryCount(), orderId);

            // 1. Reconstruct the request payload DTO using existing order details
            UberDispatchRequestDto uberDispatchRequestDto = coFeignClient.getOrderDetailsForDelivery(orderId);
            uberDispatchRequestDto.setUberWebhookUrl(uberWebhookUrl);

            // 2. Clear previous Uber tracking state on the external order entity
            externalOrder.setExternalDeliveryId(null);
            externalOrder.setExternalQuoteId(null);
            externalOrder.setTrackingUrl(null);
            externalOrder.setStatus("RE_DISPATCHING");
            externalDriverOrderRepository.save(externalOrder);

            // Update main driver_orders status to reflect re-dispatch phase
            driverOrder.setStatus("RE_DISPATCHING");
            driverOrderRepository.save(driverOrder);

            // 3. Re-invoke createDelivery API
            // This generates a new Uber delivery job, fetches a new quote/fee, and calls saveUberDetails
            String newUberDeliveryId = uberDirectClient.createDelivery(uberDispatchRequestDto);

            log.info("Successfully re-dispatched order {} to Uber Direct. New Uber Delivery ID: {}",
                    orderId, newUberDeliveryId);

        } catch (Exception e) {
            log.error("Failed to re-dispatch order to Uber Direct on retry #{}: {}",
                    externalOrder.getRetryCount(), e.getMessage(), e);

            // Fallback if the Uber API call itself fails immediately during re-dispatch
            //triggerFallbackToLocalDrivers(externalOrder);
        }
    }
}
