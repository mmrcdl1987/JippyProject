package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.dto.CoPaymentModeResponse;
import com.jippy.customerandorder.dto.CoPaymentRequest;
import com.jippy.customerandorder.entity.CoPaymentModes;
import com.jippy.customerandorder.exception.CoBusinessException;
import com.jippy.customerandorder.iservice.OrderSettingsService;

import com.jippy.customerandorder.repository.CoOrderSettingsRepository;
import com.jippy.customerandorder.repository.CoPaymentModeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CoOrderSettingsServiceImpl implements OrderSettingsService {

    private final CoPaymentModeRepository paymentModeRepository;

    private final CoOrderSettingsRepository coOrderSettingsRepository;


    @Override
    public CoPaymentModeResponse getPaymentModeById(Integer paymentModeId) {

        Optional<CoPaymentModes> paymentModesOptional = paymentModeRepository.findByPaymentModeId(paymentModeId);
        CoPaymentModeResponse paymentModeResponse = new CoPaymentModeResponse();

        if (paymentModesOptional.isPresent()) {
            CoPaymentModes paymentModes = paymentModesOptional.get();

            paymentModeResponse.setPaymentModeId(paymentModes.getPaymentModeId());
            paymentModeResponse.setPaymentMode(paymentModes.getPaymentMode());

            return paymentModeResponse;
        }
        return paymentModeResponse;

    }

    @Override
    public List<CoPaymentModeResponse> getActivePaymentModes() {
        String isActive = "Y";
        List<CoPaymentModes> paymentModesList = paymentModeRepository.findByIsActive(isActive);

        List<CoPaymentModeResponse> paymentModeResponseList = new ArrayList<>();
        for (CoPaymentModes paymentModes : paymentModesList) {

            CoPaymentModeResponse coPaymentModeResponse = new CoPaymentModeResponse();

            coPaymentModeResponse.setPaymentModeId(paymentModes.getPaymentModeId());
            coPaymentModeResponse.setPaymentMode(paymentModes.getPaymentMode());
            coPaymentModeResponse.setIsActive(paymentModes.getIsActive());

            paymentModeResponseList.add(coPaymentModeResponse);
        }
        return paymentModeResponseList;
    }

    /*
     * CREATE
     */
    public CoPaymentModeResponse create(CoPaymentRequest request, Integer userId) {

        String paymentMode = request.getPaymentMode().trim().toUpperCase();

        /*
         * Check duplicate active payment mode
         */
        if (paymentModeRepository.existsByPaymentModeAndIsActive(paymentMode, "Y")) {

            throw new RuntimeException("Payment mode already exists");
        }

        CoPaymentModes paymentModeEntity = CoPaymentModes.builder().paymentMode(paymentMode).isActive("Y").createdAt(LocalDateTime.now()).createdBy(userId).build();

        CoPaymentModes saved = paymentModeRepository.save(paymentModeEntity);

        return mapToResponse(saved);
    }


    @Override
    @Transactional
    public CoPaymentModeResponse update(Integer paymentModeId, CoPaymentRequest request, Integer userId) {

        CoPaymentModes paymentMode = paymentModeRepository.findById(paymentModeId).orElseThrow(() -> new CoBusinessException("Payment mode not found"));

        String newPaymentMode = request.getPaymentMode().trim().toUpperCase();

        String activeStatus = request.getIsActive().trim().toUpperCase();

        if (!activeStatus.equals("Y") && !activeStatus.equals("N")) {
            throw new CoBusinessException("isActive must be Y or N");
        }

        /*
         * Check duplicate active payment mode
         */
        if ("Y".equals(activeStatus)) {

            paymentModeRepository.findByPaymentModeAndIsActive(newPaymentMode, "Y").ifPresent(existing -> {

                if (!existing.getPaymentModeId().equals(paymentModeId)) {

                    throw new CoBusinessException("Payment mode already exists");
                }
            });
        }

        /*
         * UPDATE
         */
        paymentMode.setPaymentMode(newPaymentMode);
        paymentMode.setIsActive(activeStatus);
        paymentMode.setUpdatedAt(LocalDateTime.now());
        paymentMode.setUpdatedBy(userId);

        CoPaymentModes updated = paymentModeRepository.save(paymentMode);

        return mapToResponse(updated);
    }

    /*
     * SOFT DELETE
     */
    public void softDelete(Integer paymentModeId, Integer userId) {

        CoPaymentModes paymentMode = paymentModeRepository.findById(paymentModeId).orElseThrow(() -> new RuntimeException("Payment mode not found"));

        /*
         * Already inactive
         */
        if ("N".equals(paymentMode.getIsActive())) {

            throw new RuntimeException("Payment mode is already inactive");
        }

        /*
         * SOFT DELETE
         */
        paymentMode.setIsActive("N");
        paymentMode.setUpdatedAt(LocalDateTime.now());
        paymentMode.setUpdatedBy(userId);

        paymentModeRepository.save(paymentMode);
    }

    @Transactional
    public List<CoPaymentModeResponse> getAllPaymentModes() {

        return paymentModeRepository.findAll().stream().map(this::mapToResponse).toList();
    }


    /*
     * MAPPER
     */
    private CoPaymentModeResponse mapToResponse(CoPaymentModes paymentMode) {

        return new CoPaymentModeResponse(paymentMode.getPaymentModeId(), paymentMode.getPaymentMode(), paymentMode.getIsActive(), paymentMode.getCreatedAt(), paymentMode.getCreatedBy(), paymentMode.getUpdatedAt(), paymentMode.getUpdatedBy());
    }
}