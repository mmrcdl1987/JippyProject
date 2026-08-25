package com.jippy.notification.serviceImpl;

import com.jippy.notification.dto.NDeviceTokenRequest;
import com.jippy.notification.dto.NApiResponse;
import com.jippy.notification.entity.NDeviceToken;
import com.jippy.notification.repository.DeviceTokenRepository;
import com.jippy.notification.service.NDeviceTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NDeviceTokenServiceImpl implements NDeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;

    @Override
    public NApiResponse saveDeviceToken(NDeviceTokenRequest request) {

        log.info("Received request to save FCM Token for User Id : {}", request.getUserId());

        NApiResponse response = new NApiResponse();

        try {

            Optional<NDeviceToken> optionalDeviceToken =
                    deviceTokenRepository.findByFcmToken(request.getFcmToken());

            if (optionalDeviceToken.isEmpty()) {

                optionalDeviceToken =
                        deviceTokenRepository.findByUserIdAndUserTypeAndDeviceType(
                                request.getUserId(),
                                request.getUserType(),
                                request.getDeviceType());
            }

            if (optionalDeviceToken.isEmpty()) {

                optionalDeviceToken =
                        deviceTokenRepository.findByUserIdAndUserType(
                                request.getUserId(),
                                request.getUserType());
            }

            if (optionalDeviceToken.isPresent()) {

                log.info("Device Token already exists. Updating token row.");

                NDeviceToken deviceToken = optionalDeviceToken.get();

                deviceToken.setUserId(request.getUserId());
                deviceToken.setUserType(request.getUserType());
                deviceToken.setDeviceType(request.getDeviceType());
                deviceToken.setFcmToken(request.getFcmToken());
                if (deviceToken.getCreatedAt() == null) {
                    deviceToken.setCreatedAt(LocalDateTime.now());
                }

                deviceTokenRepository.save(deviceToken);

                response.setSuccess(true);
                response.setMessage("FCM Token Updated Successfully");

            } else {

                log.info("Creating new Device Token.");

                NDeviceToken deviceToken = new NDeviceToken();

                deviceToken.setUserId(request.getUserId());
                deviceToken.setUserType(request.getUserType());
                deviceToken.setDeviceType(request.getDeviceType());
                deviceToken.setFcmToken(request.getFcmToken());
                deviceToken.setCreatedAt(LocalDateTime.now());

                deviceTokenRepository.save(deviceToken);

                response.setSuccess(true);
                response.setMessage("FCM Token Saved Successfully");
            }

        } catch (Exception exception) {

            log.error("Error while saving Device Token : {}", exception.getMessage(), exception);

            response.setSuccess(false);
            response.setMessage("Failed to Save FCM Token");
        }

        return response;
    }
}
