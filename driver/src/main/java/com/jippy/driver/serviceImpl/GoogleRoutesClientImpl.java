package com.jippy.driver.serviceImpl;
import com.jippy.driver.dto.google.GoogleRoutesRequest;
import com.jippy.driver.dto.google.GoogleRoutesResponse;
import com.jippy.driver.exception.GoogleRouteException;
import com.jippy.driver.service.GoogleRoutesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleRoutesClientImpl implements GoogleRoutesClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${google.maps.routes.url}")
    private String routesUrl;

    @Value("${google.maps.routes.api-key}")
    private String apiKey;

    @Override
    public GoogleRoutesResponse calculateRoute(GoogleRoutesRequest request) {

        log.info("GOOGLE_ROUTES_API_START | travelMode={} | routingPreference={}", request.getTravelMode(), request.getRoutingPreference());

        try {
            GoogleRoutesResponse response = webClientBuilder.build().post().uri(routesUrl).header("X-Goog-Api-Key", apiKey).header("X-Goog-FieldMask", "routes.distanceMeters,routes.duration").header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).bodyValue(request).retrieve().bodyToMono(GoogleRoutesResponse.class).block();

            if (response == null || response.getRoutes() == null || response.getRoutes().isEmpty()) {

                log.error("GOOGLE_ROUTES_API_EMPTY_RESPONSE");

                throw new GoogleRouteException("Unable to calculate route distance");
            }

            log.info("GOOGLE_ROUTES_API_SUCCESS | routeCount={}", response.getRoutes().size());

            return response;

        } catch (GoogleRouteException ex) {
            throw ex;

        } catch (Exception ex) {

            log.error("GOOGLE_ROUTES_API_FAILED | message={}", ex.getMessage(), ex);

            throw new GoogleRouteException("Unable to calculate route distance",ex);
        }
    }
}