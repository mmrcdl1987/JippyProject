package com.jippy.driver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jippy.driver.dto.uber.UberConfigProperties;
import com.jippy.driver.dto.uber.UberWebhookPayload;
import com.jippy.driver.serviceImpl.UberTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/api/driver/webhooks/uber")
@RequiredArgsConstructor
@Slf4j
public class UberWebhookController {

    private final UberTrackingService uberTrackingService;
    private final UberConfigProperties uberConfigProperties;
    private final ObjectMapper objectMapper;

    @PostMapping("/handleUberWebhook")
    public ResponseEntity<Void> handleUberWebhook(@RequestHeader(value = "X-Uber-Signature", required = false) String uberSignature,
            @RequestBody String rawJsonPayload) {

       log.info("Received Uber Webhook raw json: {} ", rawJsonPayload);

//        // 1. Validate signature using HMAC-SHA256 with your Client Secret
//        boolean isValid = isValidUberSignature(rawJsonPayload, uberSignature);
//        log.info("Received signature status isValid: {} ",isValid);
//
//
//        if (!isValid) {
//            log.warn("Invalid webhook signature received!");
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        }

        try{
            // 2. Deserialize JSON payload manually after verification
            UberWebhookPayload payload = objectMapper.readValue(rawJsonPayload, UberWebhookPayload.class);

            String eventType = payload.getEventType();
            String orderId = payload.getExternalOrderId();
            String uberDeliveryId = payload.getDeliveryId();

            log.info("eventType : {} , orderId: {} , uberDeliveryId : {} ",eventType,orderId,uberDeliveryId);

            switch (eventType) {
                case "event.delivery_status":
                case "delivery.status_update": // Handle status transitions
                    String status = payload.getData().getStatus();

                    if ("pickup".equalsIgnoreCase(status)) {
                        // Driver accepted the request
                        String driverId = payload.getData().getCourier().getId();
                        String driverName = payload.getData().getCourier().getName();
                        String phoneNumber = payload.getData().getCourier().getPhoneNumber();
                        String vehiclePlate = payload.getData().getCourier().getVehicleLicensePlate();
                        String vehicleType =  payload.getData().getCourier().getVehicleType();

                        uberTrackingService.assignDriverToOrder(orderId, uberDeliveryId, driverId, driverName,phoneNumber,
                                vehiclePlate,vehicleType);

                        // Record current estimated fee
                        recordOrUpdateCharges(orderId, uberDeliveryId, payload);
                    } if ("dropoff".equalsIgnoreCase(status) || "pickup_complete".equalsIgnoreCase(status)) {

                        Double currentLatitude =  payload.getData().getLocation().getLat();
                        Double currentLongitude = payload.getData().getLocation().getLng();
                        Double bearing = payload.getData().getLocation().getBearing();
                        String trackingUrl = payload.getTrackingUrl();

                            uberTrackingService.markOrderPickedUp(orderId, uberDeliveryId,
                                    currentLatitude,currentLongitude,bearing,trackingUrl);
                    }
                    //else if ("canceled".equalsIgnoreCase(status)) {
//                    uberTrackingService.handleCancellation(orderId, uberDeliveryId);
//                }
                    break;

                case "delivery.location_update":
                    UberWebhookPayload.Location loc = payload.getData().getLocation();
                    String driverId = payload.getData().getCourier().getId();
                    uberTrackingService.processLocationUpdate(orderId, driverId, loc.getLat(), loc.getLng(),loc.getBearing());
                    break;

                case "delivery.completed":
                    String completedDriverId = payload.getData().getCourier().getId();
                    uberTrackingService.finalizeAndStoreRoute(orderId, uberDeliveryId, completedDriverId);

                    // Lock in final charged fee
                    recordOrUpdateCharges(orderId, uberDeliveryId, payload);
                    break;

                case "delivery.canceled":
                case "delivery.unfulfilled":
                case "delivery.expired":
                    // Direct event types for cancellation and unfulfilled orders
                    String failStatus = payload.getData() != null && payload.getData().getStatus() != null
                            ? payload.getData().getStatus()
                            : eventType.replace("delivery.", "");

                    uberTrackingService.handleCancellationOrFailure(orderId, uberDeliveryId, failStatus);
                    break;

                default:
                    log.debug("Unhandled event type: {}", eventType);
                    break;
            }



        }catch (Exception e){
            log.info("An exception occured : "+e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    private void recordOrUpdateCharges(String orderId, String uberDeliveryId, UberWebhookPayload payload) {
        if (payload.getData() != null && payload.getData().getFee() != null) {
            Double deliveryFee = payload.getData().getFee().getFee() / 100.0; // convert cents/paise
            String currency = payload.getData().getFee().getCurrency();
            Double tipAmount = (payload.getData().getTip() != null)
                    ? payload.getData().getTip().getAmount() / 100.0
                    : 0.0;

            uberTrackingService.saveDeliveryCharges(orderId, uberDeliveryId, deliveryFee, tipAmount, currency);
        }
    }

    private boolean isValidUberSignature(String rawJsonPayload, String uberSignature) {
        if (uberSignature == null || uberSignature.isBlank() || rawJsonPayload == null) {
            return false;
        }

        try {
            // 1. Create SecretKeySpec using your client secret
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    uberConfigProperties.getClientSecret().getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );

            // 2. Initialize HMAC-SHA256 Mac instance
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKeySpec);

            // 3. Compute hash on the raw string body
            byte[] hmacBytes = mac.doFinal(rawJsonPayload.getBytes(StandardCharsets.UTF_8));

            // 4. Convert bytes to lowercase hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hmacBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            String calculatedSignature = hexString.toString();

            // 5. Compare using constant-time check to prevent timing attacks
            return MessageDigest.isEqual(
                    calculatedSignature.getBytes(StandardCharsets.UTF_8),
                    uberSignature.trim().getBytes(StandardCharsets.UTF_8)
            );

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error computing Uber HMAC signature", e);
            return false;
        }
    }
}
