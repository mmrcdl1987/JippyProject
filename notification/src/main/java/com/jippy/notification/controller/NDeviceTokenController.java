package com.jippy.notification.controller;

import com.jippy.notification.dto.NDeviceTokenRequest;
import com.jippy.notification.dto.NApiResponse;
import com.jippy.notification.service.NDeviceTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Device Token Controller", description = "APIs for Managing FCM Device Tokens")
public class NDeviceTokenController {

    private final NDeviceTokenService deviceTokenService;

    @Operation(summary = "Save or Update FCM Device Token")
    @PostMapping("/device-token")
    public ResponseEntity<NApiResponse> saveDeviceToken(
            @Valid @RequestBody NDeviceTokenRequest request) {

        log.info("Received request to save/update FCM Device Token.");

        NApiResponse response = deviceTokenService.saveDeviceToken(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}