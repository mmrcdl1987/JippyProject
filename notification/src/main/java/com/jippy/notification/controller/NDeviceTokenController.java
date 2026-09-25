package com.jippy.notification.controller;

import com.jippy.notification.dto.NApiResponse;
import com.jippy.notification.dto.NDeviceTokenRequest;
import com.jippy.notification.service.NDeviceTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
@Slf4j
public class NDeviceTokenController {

    private final NDeviceTokenService deviceTokenService;

    // REGISTER / UPDATE DEVICE TOKEN

    @PostMapping("/device-token")
    public ResponseEntity<NApiResponse> saveDeviceToken(@Valid @RequestBody NDeviceTokenRequest request) {

        log.info("DEVICE_TOKEN_REGISTER_REQUEST | userId={} | userType={} | deviceType={}", request.getUserId(), request.getUserType(), request.getDeviceType());

        NApiResponse response = deviceTokenService.saveDeviceToken(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }


    // DELETE DEVICE TOKEN - LOGOUT

    @DeleteMapping("/device-token")
    public ResponseEntity<NApiResponse> deleteDeviceToken(@RequestParam String fcmToken) {

        log.info("DEVICE_TOKEN_DELETE_REQUEST | logout device token received");

        NApiResponse response = deviceTokenService.deleteDeviceToken(fcmToken);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}