package com.jippy.foodandmart.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

/**
 * Calls the Google Maps Distance Matrix API to get road distance
 * and driving duration (delivery time) between a customer and an outlet.
 *
 * Required in application.yml:
 * ----------------------------
 *   google:
 *     maps:
 *       api-key: YOUR_GOOGLE_MAPS_API_KEY
 *
 * The API key must have "Distance Matrix API" enabled in Google Cloud Console.
 *
 * If the key is missing or invalid, roadDistance and deliveryTime will be null
 * and a clear WARNING will appear in the logs.
 */
@Service
public class FmGoogleMapsService {

    private static final Logger logger = LoggerFactory.getLogger(FmGoogleMapsService.class);

    private static final String DISTANCE_MATRIX_URL =
            "https://maps.googleapis.com/maps/api/distancematrix/json";

    private static final String PLACEHOLDER_KEY = "YOUR_GOOGLE_MAPS_API_KEY";

    @Value("${google.maps.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Result for a single origin → destination pair.
     *
     * @param roadDistance human-readable road distance, e.g. "1.4 km"
     * @param deliveryTime human-readable driving duration, e.g. "14 mins"
     */
    public record DistanceResult(
            String roadDistance,
            String deliveryTime
    ) {
        /** Returned when the API call fails or returns no usable data. */
        public static DistanceResult unavailable() {
            return new DistanceResult(null, null);
        }
    }

    /**
     * Fetches road distance and estimated driving duration from
     * Google Maps Distance Matrix API.
     *
     * @param originLat  customer latitude
     * @param originLng  customer longitude
     * @param destLat    outlet latitude
     * @param destLng    outlet longitude
     */
    @SuppressWarnings("unchecked")
    public DistanceResult getDistanceAndDuration(double originLat, double originLng,
                                                 double destLat,   double destLng) {

        // ── Guard: skip API call if key is not configured ─────────────────
        if (apiKey == null || apiKey.isBlank() || apiKey.equals(PLACEHOLDER_KEY)) {
            logger.warn("[GoogleMapsService] google.maps.api-key is not configured. " +
                    "roadDistance and deliveryTime will be null. " +
                    "Set a valid key in application.yml or via GOOGLE_MAPS_API_KEY env variable.");
            return DistanceResult.unavailable();
        }

        try {
            String url = UriComponentsBuilder.fromHttpUrl(DISTANCE_MATRIX_URL)
                    .queryParam("origins",      originLat + "," + originLng)
                    .queryParam("destinations", destLat   + "," + destLng)
                    .queryParam("mode",         "driving")
                    .queryParam("departure_time", "now")   // traffic-aware duration
                    .queryParam("key",          apiKey)
                    .toUriString();

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null) {
                logger.warn("[GoogleMapsService] Null response from Distance Matrix API");
                return DistanceResult.unavailable();
            }

            String topStatus = (String) response.get("status");
            if (!"OK".equals(topStatus)) {
                logger.warn("[GoogleMapsService] Top-level status: {} — check your API key and ensure " +
                        "Distance Matrix API is enabled in Google Cloud Console.", topStatus);
                return DistanceResult.unavailable();
            }

            // rows[0].elements[0]
            var rows = (List<Map<String, Object>>) response.get("rows");
            if (rows == null || rows.isEmpty()) return DistanceResult.unavailable();

            var elements = (List<Map<String, Object>>) rows.get(0).get("elements");
            if (elements == null || elements.isEmpty()) return DistanceResult.unavailable();

            var element = elements.get(0);
            String elementStatus = (String) element.get("status");
            if (!"OK".equals(elementStatus)) {
                logger.warn("[GoogleMapsService] Element status: {} for route ({},{}) -> ({},{})",
                        elementStatus, originLat, originLng, destLat, destLng);
                return DistanceResult.unavailable();
            }

            var distMap = (Map<String, Object>) element.get("distance");
            // Prefer duration_in_traffic (available when departure_time=now)
            var durMap  = (Map<String, Object>) element.getOrDefault(
                    "duration_in_traffic", element.get("duration"));

            String roadDistance = distMap != null ? (String) distMap.get("text") : null;
            String deliveryTime = durMap  != null ? (String) durMap.get("text")  : null;

            logger.debug("[GoogleMapsService] roadDistance={} deliveryTime={}", roadDistance, deliveryTime);
            return new DistanceResult(roadDistance, deliveryTime);

        } catch (Exception e) {
            logger.error("[GoogleMapsService] Distance Matrix API error: {}", e.getMessage(), e);
            return DistanceResult.unavailable();
        }
    }
}
