package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.config.SmsCountryProperties;
import com.jippy.foodandmart.dto.SmsCountryRequestDto;
import com.jippy.foodandmart.dto.SmsCountryResponseDto;
import com.jippy.foodandmart.exception.SmsFailedException;
import com.jippy.foodandmart.feignClients.SmsCountryFeignClient;
import com.jippy.foodandmart.service.SmsCountryService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsCountryServiceImpl implements SmsCountryService {
    private static final int OTP_EXPIRY_MINUTES = 5;

    private final SmsCountryFeignClient smsCountryFeignClient;
    private final SmsCountryProperties properties;

    @Override
    public String sendOtp(String mobileNumber, String otp) {

        log.info("SMS_SERVICE | SEND_OTP | mobile={} | START", mobileNumber);

        try {

            SmsCountryRequestDto request = new SmsCountryRequestDto();

            request.setNumber(mobileNumber);
            request.setSenderId(properties.getSenderId());

            String message = String.format(
                    "Your OTP for jippymart login is %s. Please do not share this OTP with anyone. It is valid for the next 10 minutes-jippymart.in.",
                    otp
            );
            request.setText(message);
            request.setTemplateId(properties.getTemplateId());

            SmsCountryResponseDto response = smsCountryFeignClient.sendSms(properties.getAuthKey(), request);

            String referenceId = extractReferenceId(response);

            log.info("SMS_SERVICE | SEND_OTP | mobile={} | SUCCESS | referenceId={}", mobileNumber, referenceId);

            return referenceId;

        } catch (FeignException exception) {

            log.error("SMS_SERVICE | SEND_OTP | mobile={} | FAILED | status={} | response={}", mobileNumber, exception.status(), exception.contentUTF8(), exception);

            throw new SmsFailedException("Unable to send OTP. Please try again later.");

        } catch (Exception exception) {

            log.error("SMS_SERVICE | SEND_OTP | mobile={} | UNEXPECTED_ERROR", mobileNumber, exception);

            throw new SmsFailedException("Unable to send OTP. Please try again later.");
        }
    }

    private String extractReferenceId(SmsCountryResponseDto response) {

        if (response != null && response.getMessageUUID() != null && !response.getMessageUUID().isBlank()) {

            return response.getMessageUUID();
        }

        String referenceId = UUID.randomUUID().toString();

        log.warn("SMS_SERVICE | SMSCOUNTRY_NO_REFERENCE_ID | generatedReferenceId={}", referenceId);

        return referenceId;
    }


}
