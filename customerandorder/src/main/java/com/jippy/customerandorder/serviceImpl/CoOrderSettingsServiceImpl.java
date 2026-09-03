package com.jippy.customerandorder.serviceImpl;


import com.jippy.customerandorder.constants.COConstants;
import com.jippy.customerandorder.dto.CoOrderSettingsRequestDto;
import com.jippy.customerandorder.dto.CoOrderSettingsResponseDto;
import com.jippy.customerandorder.dto.CoPaymentModesDto;
import com.jippy.customerandorder.entity.CoOrderSettings;
import com.jippy.customerandorder.entity.CoPaymentModes;
import com.jippy.customerandorder.exception.CoOrderSettingsException;
import com.jippy.customerandorder.iservice.IOrderSettingsService;
import com.jippy.customerandorder.mapper.CoOrderSettingsMapper;
import com.jippy.customerandorder.repository.CoOrderSettingsRepository;
import com.jippy.customerandorder.repository.CoPaymentModeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoOrderSettingsServiceImpl implements IOrderSettingsService {

    private final CoOrderSettingsRepository coOrderSettingsRepository;

    private final CoOrderSettingsMapper orderSettingsMapper;

    private final CoPaymentModeRepository coPaymentModeRepository;

    @Override
    public CoOrderSettingsResponseDto saveOrUpdate(CoOrderSettingsRequestDto requestDto) {

        log.info("SAVE OR UPDATE ORDER SETTINGS SERVICE START");

        if (requestDto == null) {

            log.error("REQUEST DTO IS NULL");

            throw new CoOrderSettingsException("Request body cannot be null");
        }

        boolean isUpdate = requestDto.getOrderSettingsId() != null;

        log.info("REQUEST TYPE : {}", isUpdate ? "UPDATE" : "CREATE");

        CoOrderSettings coOrderSettings;

        if (isUpdate) {

            log.info("FETCHING ORDER SETTINGS | id={}", requestDto.getOrderSettingsId());

            coOrderSettings = coOrderSettingsRepository.findById(requestDto.getOrderSettingsId()).orElseThrow(() -> {

                log.error("ORDER SETTINGS NOT FOUND | id={}", requestDto.getOrderSettingsId());

                return new CoOrderSettingsException(COConstants.MSG_ORDER_SETTINGS_NOT_FOUND);
            });

            if (requestDto.getUpdatedBy() == null) {

                log.error("UPDATED BY IS NULL");

                throw new CoOrderSettingsException("UpdatedBy is required");
            }

            coOrderSettings.setUpdatedBy(requestDto.getUpdatedBy());

            coOrderSettings.setUpdatedAt(LocalDateTime.now());

            log.info("ORDER SETTINGS UPDATE STARTED | id={}", requestDto.getOrderSettingsId());

        } else {

            log.info("CREATE ORDER SETTINGS VALIDATION START");

            if (requestDto.getPlatformFee() == null) {

                log.error("PLATFORM FEE IS NULL");

                throw new CoOrderSettingsException("Platform fee is required");
            }

            if (requestDto.getSurgeFee() == null) {

                log.error("SURGE FEE IS NULL");

                throw new CoOrderSettingsException("Surge fee is required");
            }

            if (requestDto.getPackagingFee() == null) {

                log.error("PACKAGING FEE IS NULL");

                throw new CoOrderSettingsException("Packaging fee is required");
            }

            if (requestDto.getDeliveryFeeTax() == null) {

                log.error("DELIVERY FEE TAX IS NULL");

                throw new CoOrderSettingsException("Delivery fee tax is required");
            }

            if (requestDto.getFoodTotalAmountTax() == null) {

                log.error("FOOD TOTAL AMOUNT TAX IS NULL");

                throw new CoOrderSettingsException("Food total amount tax is required");
            }

            if (requestDto.getCreatedBy() == null) {

                log.error("CREATED BY IS NULL");

                throw new CoOrderSettingsException("CreatedBy is required");
            }

            coOrderSettings = new CoOrderSettings();

            coOrderSettings.setCreatedBy(requestDto.getCreatedBy());

            coOrderSettings.setCreatedAt(LocalDateTime.now());

            log.info("CREATE ORDER SETTINGS OBJECT INITIALIZED");
        }

        if (requestDto.getPlatformFee() != null) {

            if (requestDto.getPlatformFee().doubleValue() < 0) {

                log.error("INVALID PLATFORM FEE | value={}", requestDto.getPlatformFee());

                throw new CoOrderSettingsException("Platform fee cannot be negative");
            }

            coOrderSettings.setPlatformFee(requestDto.getPlatformFee());

            log.info("PLATFORM FEE UPDATED");
        }

        if (requestDto.getSurgeFee() != null) {

            if (requestDto.getSurgeFee().doubleValue() < 0) {

                log.error("INVALID SURGE FEE | value={}", requestDto.getSurgeFee());

                throw new CoOrderSettingsException("Surge fee cannot be negative");
            }

            coOrderSettings.setSurgeFee(requestDto.getSurgeFee());

            log.info("SURGE FEE UPDATED");
        }

        if (requestDto.getPackagingFee() != null) {

            if (requestDto.getPackagingFee().doubleValue() < 0) {

                log.error("INVALID PACKAGING FEE | value={}", requestDto.getPackagingFee());

                throw new CoOrderSettingsException("Packaging fee cannot be negative");
            }

            coOrderSettings.setPackagingFee(requestDto.getPackagingFee());

            log.info("PACKAGING FEE UPDATED");
        }

        if (requestDto.getDeliveryFeeTax() != null) {

            if (requestDto.getDeliveryFeeTax().doubleValue() < 0
                    || requestDto.getDeliveryFeeTax().doubleValue() > 100) {

                log.error("INVALID DELIVERY FEE TAX PERCENTAGE | value={}",
                        requestDto.getDeliveryFeeTax());

                throw new CoOrderSettingsException(
                        "Delivery fee tax percentage must be between 0 and 100");
            }

            coOrderSettings.setDeliveryFeeTax(requestDto.getDeliveryFeeTax());

            log.info("DELIVERY FEE TAX UPDATED");
        }

        if (requestDto.getFoodTotalAmountTax() != null) {

            if (requestDto.getFoodTotalAmountTax().doubleValue() < 0
                    || requestDto.getFoodTotalAmountTax().doubleValue() > 100) {

                log.error("INVALID FOOD TOTAL AMOUNT TAX PERCENTAGE | value={}",
                        requestDto.getFoodTotalAmountTax());

                throw new CoOrderSettingsException(
                        "Food total amount tax percentage must be between 0 and 100");
            }

            coOrderSettings.setFoodTotalAmountTax(requestDto.getFoodTotalAmountTax());

            log.info("FOOD TOTAL AMOUNT TAX UPDATED");
        }

        log.info("SAVING ORDER SETTINGS INTO DATABASE");

        CoOrderSettings savedData = coOrderSettingsRepository.save(coOrderSettings);

        log.info("ORDER SETTINGS SAVED SUCCESSFULLY | id={}", savedData.getOrderSettingsId());

        CoOrderSettingsResponseDto response = orderSettingsMapper.mapToResponse(savedData, isUpdate ? COConstants.MSG_ORDER_SETTINGS_UPDATED : COConstants.MSG_ORDER_SETTINGS_CREATED);

        log.info("SAVE OR UPDATE ORDER SETTINGS SERVICE END");

        return response;
    }

    @Override
    public CoPaymentModesDto getPaymentModeById(Integer paymentModeId) {

        log.info("GET PAYMENT MODE BY ID SERVICE START | paymentModeId={}", paymentModeId);

       Optional<CoPaymentModes> coPaymentModes = coPaymentModeRepository.findById(paymentModeId);
      if(!coPaymentModes.isPresent()) {
            log.error("PAYMENT MODE NOT FOUND | paymentModeId={}", paymentModeId);
           // return new CoOrderSettingsException("Payment mode not found with id: " + paymentModeId);
        }

        CoPaymentModesDto paymentModesDto = new CoPaymentModesDto();

        if (coPaymentModes.isEmpty()) {
            return paymentModesDto;
        }
        paymentModesDto.setPaymentMode(coPaymentModes.get().getPaymentMode());
        paymentModesDto.setPaymentModeId(coPaymentModes.get().getPaymentModeId());
        return  paymentModesDto;

    }

    @Override
    public List<CoPaymentModesDto> getActivePaymentModes() {

        log.info("GET ACTIVE PAYMENT MODES SERVICE START");

        List<CoPaymentModes> activePaymentModes = coPaymentModeRepository.findByIsActive("Y");

        if (activePaymentModes == null) {

            log.error("NO ACTIVE PAYMENT MODE FOUND");

            throw new CoOrderSettingsException("No active payment mode found");
        }

        List<CoPaymentModesDto> paymentModesDtoList = new ArrayList<>();

        for(CoPaymentModes paymentModes: activePaymentModes) {

            CoPaymentModesDto paymentModesDto = new CoPaymentModesDto();
            paymentModesDto.setPaymentMode(paymentModes.getPaymentMode());
            paymentModesDto.setPaymentModeId(paymentModes.getPaymentModeId());
            paymentModesDto.setIsActive(paymentModes.getIsActive());
            paymentModesDtoList.add(paymentModesDto);

        }

        log.info("GET ACTIVE PAYMENT MODES SERVICE END | paymentModeId={}", paymentModesDtoList);

        return paymentModesDtoList;
    }


}