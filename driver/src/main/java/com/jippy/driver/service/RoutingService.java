package com.jippy.driver.service;

import com.jippy.driver.dto.routing.RouteResult;

public interface RoutingService {

    RouteResult calculateRoute(
            double originLatitude,
            double originLongitude,
            double destinationLatitude,
            double destinationLongitude
    );
}