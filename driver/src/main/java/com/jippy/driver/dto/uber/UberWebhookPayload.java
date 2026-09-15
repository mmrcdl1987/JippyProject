package com.jippy.driver.dto.uber;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UberWebhookPayload {

    @JsonProperty("event_type")
    private String eventType;

    @JsonProperty("delivery_id")
    private String deliveryId;

    @JsonProperty("external_order_id")
    private String externalOrderId;

    private EventData data;

    private String trackingUrl;

    @Data
    public static class EventData {
        private Courier courier;
        private Location location;
        private String status;
    }

    @Data
    public static class Courier {
        private String id;
        private String name;

        @JsonProperty("phone_number")
        private String phoneNumber;

        @JsonProperty("vehicle_license_plate")
        private String vehicleLicensePlate;

        @JsonProperty("vehicle_type")
        private String vehicleType;
    }

    @Data
    public static class Location {
        private double lat;
        private double lng;
        private double bearing;
    }
}
