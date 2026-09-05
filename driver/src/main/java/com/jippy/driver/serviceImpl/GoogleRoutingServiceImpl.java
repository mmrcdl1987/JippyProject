package com.jippy.driver.serviceImpl;

import com.jippy.driver.dto.google.*;
import com.jippy.driver.dto.routing.RouteResult;
import com.jippy.driver.exception.GoogleRouteException;
import com.jippy.driver.service.GoogleRoutesClient;
import com.jippy.driver.service.RoutingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleRoutingServiceImpl implements RoutingService {

    private static final String TRAVEL_MODE = "TWO_WHEELER";
    private static final String ROUTING_PREFERENCE = "TRAFFIC_AWARE_OPTIMAL";

    private final GoogleRoutesClient googleRoutesClient;

    @Override
    public RouteResult calculateRoute(double originLatitude, double originLongitude, double destinationLatitude, double destinationLongitude) {

        log.info("GOOGLE_ROUTE_CALCULATION_START | origin=({}, {}) | destination=({}, {})", originLatitude, originLongitude, destinationLatitude, destinationLongitude);

        GoogleRoutesRequest request = GoogleRoutesRequest.builder().origin(buildLocation(originLatitude, originLongitude)).destination(buildLocation(destinationLatitude, destinationLongitude)).travelMode(TRAVEL_MODE).routingPreference(ROUTING_PREFERENCE).build();

        GoogleRoutesResponse response = googleRoutesClient.calculateRoute(request);

        if (response == null || response.getRoutes() == null || response.getRoutes().isEmpty()) {

            log.error("GOOGLE_ROUTE_CALCULATION_EMPTY_RESPONSE");

            throw new GoogleRouteException("Unable to calculate route");
        }

        GoogleRoute route = response.getRoutes().get(0);

        if (route.getDistanceMeters() == null || route.getDuration() == null) {

            log.error("GOOGLE_ROUTE_CALCULATION_INVALID_RESPONSE");

            throw new GoogleRouteException("Invalid route information received");
        }

        long durationSeconds = parseDuration(route.getDuration());

        BigDecimal distanceKm = BigDecimal.valueOf(route.getDistanceMeters()).divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);

        RouteResult result = RouteResult.builder().distanceMeters(route.getDistanceMeters()).distanceKm(distanceKm).durationSeconds(durationSeconds).build();

        log.info("GOOGLE_ROUTE_CALCULATION_SUCCESS | distanceMeters={} | distanceKm={} | durationSeconds={}", route.getDistanceMeters(), distanceKm, durationSeconds);

        return result;
    }

    private GoogleLocation buildLocation(double latitude, double longitude) {

        return GoogleLocation.builder().location(GoogleWaypoint.builder().latLng(GoogleLatLng.builder().latitude(latitude).longitude(longitude).build()).build()).build();
    }

    private long parseDuration(String duration) {

        try {

            if (duration == null || !duration.endsWith("s")) {
                throw new IllegalArgumentException("Invalid Google duration format");
            }

            return Long.parseLong(duration.substring(0, duration.length() - 1));

        } catch (Exception ex) {

            log.error("GOOGLE_ROUTE_DURATION_PARSE_FAILED | duration={}", duration, ex);

            throw new GoogleRouteException("Invalid route duration received from Google", ex);
        }
    }
}