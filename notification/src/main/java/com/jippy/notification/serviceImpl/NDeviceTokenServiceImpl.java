package com.jippy.notification.serviceImpl;

import com.jippy.notification.dto.NApiResponse;
import com.jippy.notification.dto.NDeviceTokenRequest;
import com.jippy.notification.entity.NDeviceToken;
import com.jippy.notification.repository.DeviceTokenRepository;
import com.jippy.notification.service.NDeviceTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NDeviceTokenServiceImpl implements NDeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;

    @Override
    @Transactional
    public NApiResponse saveDeviceToken(NDeviceTokenRequest request) {

        log.info("Saving FCM token for userId={}, userType={}, deviceType={}", request.getUserId(), request.getUserType(), request.getDeviceType());

        NApiResponse response = new NApiResponse();

        try {

            Optional<NDeviceToken> existingToken = deviceTokenRepository.findByFcmToken(request.getFcmToken());

            if (existingToken.isPresent()) {

                NDeviceToken deviceToken = existingToken.get();

                /*
                 * The FCM token already exists.
                 *
                 * This can happen when:
                 * - Same user logs in again
                 * - User logs out and logs in again
                 * - Firebase returns the same token
                 * - Same device/browser is reused by another user
                 */
                deviceToken.setUserId(request.getUserId());
                deviceToken.setUserType(request.getUserType());
                deviceToken.setDeviceType(request.getDeviceType());

                if (deviceToken.getCreatedAt() == null) {
                    deviceToken.setCreatedAt(LocalDateTime.now());
                }

                deviceTokenRepository.save(deviceToken);

                log.info("Existing FCM token updated successfully for userId={}", request.getUserId());

                response.setSuccess(true);
                response.setMessage("FCM Token Updated Successfully");

            } else {

                /*
                 * New device/browser installation.
                 */
                NDeviceToken deviceToken = new NDeviceToken();

                deviceToken.setUserId(request.getUserId());
                deviceToken.setUserType(request.getUserType());
                deviceToken.setFcmToken(request.getFcmToken());
                deviceToken.setDeviceType(request.getDeviceType());
                deviceToken.setCreatedAt(LocalDateTime.now());

                deviceTokenRepository.save(deviceToken);

                log.info("New FCM token saved successfully for userId={}", request.getUserId());

                response.setSuccess(true);
                response.setMessage("FCM Token Saved Successfully");
            }

        } catch (Exception exception) {

            log.error("Error while saving FCM token for userId={}", request.getUserId(), exception);

            response.setSuccess(false);
            response.setMessage("Failed to Save FCM Token");
        }

        return response;
    }

    @Override
    @Transactional
    public NApiResponse deleteDeviceToken(String fcmToken) {

        log.info("Deleting FCM token during logout");

        NApiResponse response = new NApiResponse();

        try {

            Optional<NDeviceToken> existingToken = deviceTokenRepository.findByFcmToken(fcmToken);

            if (existingToken.isEmpty()) {

                response.setSuccess(true);
                response.setMessage("FCM Token Already Removed");

                return response;
            }

            deviceTokenRepository.deleteByFcmToken(fcmToken);

            log.info("FCM token deleted successfully during logout");

            response.setSuccess(true);
            response.setMessage("FCM Token Deleted Successfully");

        } catch (Exception exception) {

            log.error("Error while deleting FCM token during logout", exception);

            response.setSuccess(false);
            response.setMessage("Failed to Delete FCM Token");
        }

        return response;
    }
}