            package com.jippy.division.dto;

            import com.jippy.foodandmart.dto.FmActiveDiscountsDto;
            import io.swagger.v3.oas.annotations.media.Schema;
            import lombok.AllArgsConstructor;
            import lombok.Data;
            import lombok.NoArgsConstructor;

            @Data
            @AllArgsConstructor
            @NoArgsConstructor
            @Schema(name = "NearbyOutlet", description = "Outlet details returned for the Customer App nearby search")
            public class FmNearbyOutletDto {

                @Schema(description = "Unique outlet identifier", example = "101")
                private Integer outletId;

                @Schema(description = "Name of the outlet", example = "Jippy Kitchen - Madhapur")
                private String outletName;

                @Schema(description = "Type of cuisine served", example = "Indian")
                private String cuisineType;

                @Schema(description = "Outlet contact number", example = "+919876543210")
                private String outletPhone;

                @Schema(description = "Delivery radius configured for this outlet (km)", example = "3.0")
                private Double radius;

                @Schema(description = "Customer review rating", example = "4.5")
                private Double review;

                @Schema(description = "Subscription status", example = "subscribed")
                private String subscriptionStatus;

                @Schema(description = "Promotion status", example = "promoted")
                private String promotionStatus;

                // ── Straight-line distance (PostGIS) ─────────────────────────────────

                @Schema(description = "Straight-line distance from customer in km", example = "0.85")
                private Double distanceKm;

                // ── Google Maps Distance Matrix ───────────────────────────────────────

                @Schema(description = "Road distance from Google Maps (e.g. '1.4 km')", example = "1.4 km")
                private String roadDistance;

                @Schema(description = "Estimated delivery time from Google Maps (e.g. '14 mins')", example = "14 mins")
                private String deliveryTime;

                // ── Operating hours from outlet_days table ────────────────────────────

                @Schema(description = "Today's opening time in HH:mm (null if closed today)", example = "09:00")
                private String openingTime;

                @Schema(description = "Today's closing time in HH:mm (null if closed today)", example = "22:00")
                private String closingTime;

                @Schema(description = "Whether the outlet is open right now based on outlet_days hours", example = "true")
                private Boolean openNow;

                private String outletPicUrl;

                private Boolean isVegOutlet;

                private Boolean isBestRestaurant;

                private FmActiveDiscountsDto activeDiscountsDto;
            }
