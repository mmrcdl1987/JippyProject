package com.jippy.driver.serviceImpl;

import com.jippy.driver.constants.DConstants;
import com.jippy.driver.dto.uber.UberConfigProperties;
import com.jippy.driver.dto.uber.UberDispatchRequestDto;
import com.jippy.driver.entity.DriverOrder;
import com.jippy.driver.entity.ExternalDriverOrder;
import com.jippy.driver.repositary.DriverOrderRepository;
import com.jippy.driver.repositary.ExternalDriverOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
@Slf4j
public class UberDirectClient {

    private final RestTemplate restTemplate = new RestTemplate();

    private final UberConfigProperties uberConfigProperties;
    private final DriverOrderRepository driverOrderRepository;
    private final ExternalDriverOrderRepository externalDriverOrderRepository;

    private String cachedAccessToken;
    private Instant tokenExpiryTime = Instant.MIN;
    private final ReentrantLock tokenLock = new ReentrantLock();

    /**
     * Dynamically generates OAuth access_token using Client ID and Client Secret
     */
    public String getAccessToken() {
        // 1. Return cached token if valid (using a 60-second buffer before true expiration)
        if (cachedAccessToken != null && Instant.now().isBefore(tokenExpiryTime.minusSeconds(60))) {
            return cachedAccessToken;
        }

        // 2. Lock to ensure only one thread requests a new token when expired
        tokenLock.lock();
        try {
            // Double-check inside lock for concurrent scheduled execution
            if (cachedAccessToken != null && Instant.now().isBefore(tokenExpiryTime.minusSeconds(60))) {
                return cachedAccessToken;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", uberConfigProperties.getClientId());
            body.add("client_secret", uberConfigProperties.getClientSecret());
            body.add("grant_type", "client_credentials");
            body.add("scope", uberConfigProperties.getScope());

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(uberConfigProperties.getAuthUrl(), request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();

                // Extract and update cached token
                this.cachedAccessToken = (String) responseBody.get("access_token");

                // Extract "expires_in" (seconds) and compute target expiry time
                Number expiresIn = (Number) responseBody.get("expires_in");
                long secondsToLive = (expiresIn != null) ? expiresIn.longValue() : 3600;
                this.tokenExpiryTime = Instant.now().plusSeconds(secondsToLive);

                return this.cachedAccessToken;
            }

            throw new RuntimeException("Failed to generate Uber OAuth token");
        } finally {
            tokenLock.unlock();
        }
    }

    /**
     * 2. Creates Delivery Order in Uber Direct API
     */
    public String createDelivery(UberDispatchRequestDto uberDispatchRequestDto) {
        // Automatically fetch token here before calling Uber
        String token = getAccessToken();

        log.info("uber token : {} ",token);

        log.info("uberDispatchRequestDto : {} ",uberDispatchRequestDto);

        String url = String.format("%s/customers/%s/deliveries",
                uberConfigProperties.getBaseUrl(),
                uberConfigProperties.getCustomerId()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token); // Attach the dynamically generated token

        HttpEntity<UberDispatchRequestDto> request = new HttpEntity<>(uberDispatchRequestDto, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            String uberDeliveryId = (String) response.getBody().get("id");
            log.info("===================={}",uberDeliveryId);

            // save uber driver Id to database
            saveUberdetails(uberDispatchRequestDto.getExternalOrderId(),uberDeliveryId);

            return (String) response.getBody().get("id"); // Returns Uber Delivery ID
        }
        throw new RuntimeException("Failed to dispatch order to Uber Direct");
    }

    private void saveUberdetails(String orderId, String uberDeliveryId) {

        DriverOrder driverOrder = new DriverOrder();

        driverOrder.setOrderId(orderId);
        driverOrder.setStatus(DConstants.DRIVER_ORDER_ASSIGNED_STATUS_PENDING);
        driverOrder.setCreatedAt(LocalDateTime.now());
        driverOrder.setDriverType(DConstants.DRIVER_ORDER_DRIVER_TYPE_UBER);

        driverOrderRepository.save(driverOrder);

        ExternalDriverOrder externalDriverOrder = new ExternalDriverOrder();

        externalDriverOrder.setDriverOrder(driverOrder);
        externalDriverOrder.setExternalDeliveryId(uberDeliveryId);
        externalDriverOrder.setProviderName(DConstants.DRIVER_ORDER_UBER_EXTERNAL_PROVIDER);
        externalDriverOrder.setCreatedAt(LocalDateTime.now());

        externalDriverOrderRepository.save(externalDriverOrder);
    }
}
