package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.dto.CoWalletSettingsRequestDto;
import com.jippy.customerandorder.dto.CoWalletSettingsResponseDto;
import com.jippy.customerandorder.entity.CoWalletSettings;
import com.jippy.customerandorder.iservice.CoWalletSettingsService;
import com.jippy.customerandorder.mapper.CoWalletSettingsMapper;
import com.jippy.customerandorder.repository.CoWalletSettingsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional
public class CoWalletSettingsServiceImpl
        implements CoWalletSettingsService {

    private static final int MAX_PAGE_SIZE = 100;

    @Autowired
    private CoWalletSettingsRepository walletSettingsRepository;

    // ============================================================
    // GET ALL WALLET SETTINGS
    // ============================================================

    /**
     * Get all wallet settings without pagination.
     *
     * @return list of wallet settings
     */
    @Override
    @Transactional(readOnly = true)
    public List<CoWalletSettings> getWalletSettings() {

        log.info("Fetching all wallet settings");

        List<CoWalletSettings> walletSettings =
                walletSettingsRepository.findAll();

        log.info(
                "Wallet settings fetched successfully. Total elements={}",
                walletSettings.size()
        );

        return walletSettings;
    }

    // ============================================================
    // CREATE / UPDATE WALLET SETTINGS
    // ============================================================

    /**
     * Create or update wallet settings.
     *
     * CREATE:
     * walletSettingsId is null
     *
     * UPDATE:
     * walletSettingsId is provided
     *
     * @param requestDto wallet settings request
     * @return saved wallet settings response
     */
    @Override
    public CoWalletSettingsResponseDto saveWalletSettings(
            CoWalletSettingsRequestDto requestDto) {

        log.info("Entering saveWalletSettings");

        // ========================================================
        // VALIDATE REQUEST
        // ========================================================

        validateSaveRequest(requestDto);

        CoWalletSettings walletSettings;

        // ========================================================
        // UPDATE
        // ========================================================

        if (requestDto.getWalletSettingsId() != null) {

            Integer walletSettingsId =
                    requestDto.getWalletSettingsId();

            log.info(
                    "Updating wallet settings. walletSettingsId={}",
                    walletSettingsId
            );

            Optional<CoWalletSettings> optionalWalletSettings =
                    walletSettingsRepository.findById(walletSettingsId);

            if (optionalWalletSettings.isEmpty()) {

                log.error(
                        "Wallet settings not found. walletSettingsId={}",
                        walletSettingsId
                );

                throw new IllegalArgumentException(
                        "Wallet settings not found with ID: "
                                + walletSettingsId
                );
            }

            walletSettings = optionalWalletSettings.get();

            // ====================================================
            // UPDATE BUSINESS FIELDS
            // ====================================================

            walletSettings.setSettingType(
                    requestDto.getSettingType().trim()
            );

            /*
             * settingValue is Integer.
             *
             * Do NOT use trim().
             */
            walletSettings.setSettingValue(
                    requestDto.getSettingValue()
            );

            // ====================================================
            // UPDATE AUDIT FIELDS
            // ====================================================

            walletSettings.setUpdatedBy(
                    requestDto.getUpdatedBy()
            );

            walletSettings.setUpdatedAt(
                    LocalDateTime.now()
            );

            log.info(
                    "Wallet settings updated successfully. " +
                            "walletSettingsId={}",
                    walletSettingsId
            );

        } else {

            // ====================================================
            // CREATE
            // ====================================================

            log.info("Creating new wallet settings");

            walletSettings =
                    CoWalletSettingsMapper.mapToEntity(
                            requestDto
                    );

            /*
             * Mapper already sets createdAt.
             *
             * Do not overwrite it here.
             */

            log.info("New wallet settings entity created");
        }

        // ========================================================
        // SAVE ENTITY
        // ========================================================

        CoWalletSettings savedEntity =
                walletSettingsRepository.save(walletSettings);

        log.info(
                "Wallet settings saved successfully. walletSettingsId={}",
                savedEntity.getWalletSettingsId()
        );

        // ========================================================
        // ENTITY -> RESPONSE DTO
        // ========================================================

        return CoWalletSettingsMapper.mapToResponseDto(
                savedEntity
        );
    }

    // ============================================================
    // GET WALLET SETTINGS WITH PAGINATION
    // ============================================================

    /**
     * Get wallet settings with pagination.
     *
     * Latest records are returned first.
     *
     * @param page page number starting from 0
     * @param size number of records per page
     * @return paginated wallet settings
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CoWalletSettingsResponseDto> getWalletSettings(
            int page,
            int size) {

        log.info(
                "Entering getWalletSettings with pagination. " +
                        "page={}, size={}",
                page,
                size
        );

        // ========================================================
        // VALIDATE PAGINATION
        // ========================================================

        validatePagination(page, size);

        // ========================================================
        // CREATE PAGEABLE
        // ========================================================

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "walletSettingsId"
                        )
                );

        // ========================================================
        // FETCH FROM DATABASE
        // ========================================================

        Page<CoWalletSettings> walletSettingsPage =
                walletSettingsRepository.findAll(pageable);

        log.info(
                "Wallet settings fetched successfully. " +
                        "Total elements={}, total pages={}, current page={}",
                walletSettingsPage.getTotalElements(),
                walletSettingsPage.getTotalPages(),
                page
        );

        // ========================================================
        // ENTITY -> RESPONSE DTO
        // ========================================================

        return walletSettingsPage.map(
                CoWalletSettingsMapper::mapToResponseDto
        );
    }

    // ============================================================
    // VALIDATE CREATE / UPDATE REQUEST
    // ============================================================

    /**
     * Validate create/update request.
     */
    private void validateSaveRequest(
            CoWalletSettingsRequestDto requestDto) {

        // ========================================================
        // REQUEST NULL CHECK
        // ========================================================

        if (requestDto == null) {

            throw new IllegalArgumentException(
                    "Wallet settings request cannot be null"
            );
        }

        // ========================================================
        // WALLET SETTINGS ID
        // ========================================================

        if (requestDto.getWalletSettingsId() != null
                && requestDto.getWalletSettingsId() <= 0) {

            throw new IllegalArgumentException(
                    "Wallet settings ID must be greater than 0"
            );
        }

        // ========================================================
        // SETTING TYPE
        // ========================================================

        if (requestDto.getSettingType() == null
                || requestDto.getSettingType().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Setting type is required"
            );
        }

        // ========================================================
        // SETTING VALUE
        // ========================================================

        /*
         * settingValue is Integer.
         *
         * Therefore:
         * - null check is required
         * - trim() must NOT be used
         */
        if (requestDto.getSettingValue() == null) {

            throw new IllegalArgumentException(
                    "Setting value is required"
            );
        }

        // ========================================================
        // CREATE VALIDATION
        // ========================================================

        if (requestDto.getWalletSettingsId() == null) {

            if (requestDto.getCreatedBy() == null
                    || requestDto.getCreatedBy().trim().isEmpty()) {

                throw new IllegalArgumentException(
                        "Created by is required"
                );
            }
        }

        // ========================================================
        // UPDATE VALIDATION
        // ========================================================

        if (requestDto.getWalletSettingsId() != null) {

            if (requestDto.getUpdatedBy() == null
                    || requestDto.getUpdatedBy().trim().isEmpty()) {

                throw new IllegalArgumentException(
                        "Updated by is required"
                );
            }
        }
    }

    // ============================================================
    // VALIDATE PAGINATION
    // ============================================================

    /**
     * Validate pagination parameters.
     */
    private void validatePagination(
            int page,
            int size) {

        // ========================================================
        // PAGE
        // ========================================================

        if (page < 0) {

            throw new IllegalArgumentException(
                    "Page number cannot be negative"
            );
        }

        // ========================================================
        // SIZE
        // ========================================================

        if (size <= 0) {

            throw new IllegalArgumentException(
                    "Page size must be greater than 0"
            );
        }

        // ========================================================
        // MAX SIZE
        // ========================================================

        if (size > MAX_PAGE_SIZE) {

            throw new IllegalArgumentException(
                    "Page size cannot be greater than "
                            + MAX_PAGE_SIZE
            );
        }
    }
}