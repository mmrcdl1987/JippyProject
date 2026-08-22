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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CoWalletSettingsServiceImpl implements CoWalletSettingsService {

    private static final int MAX_PAGE_SIZE = 100;

    @Autowired
    private CoWalletSettingsRepository walletSettingsRepository;


    // ============================================================
    // CREATE / UPDATE WALLET SETTINGS
    // ============================================================

    /**
     * Create / Update Wallet Settings.
     */
    @Override
    public CoWalletSettingsResponseDto saveWalletSettings(
            CoWalletSettingsRequestDto requestDto) {

        log.info("Entering saveWalletSettings");

        // Validate request
        validateSaveRequest(requestDto);

        CoWalletSettings walletSettings;

        /*
         * ========================================================
         * UPDATE
         * ========================================================
         */
        if (requestDto.getWalletSettingsId() != null) {

            log.info(
                    "Updating wallet settings with ID: {}",
                    requestDto.getWalletSettingsId()
            );

            Optional<CoWalletSettings> optionalWalletSettings =
                    walletSettingsRepository.findById(
                            requestDto.getWalletSettingsId()
                    );

            if (optionalWalletSettings.isEmpty()) {

                log.error(
                        "Wallet settings not found with ID: {}",
                        requestDto.getWalletSettingsId()
                );

                throw new IllegalArgumentException(
                        "Wallet settings not found with ID: "
                                + requestDto.getWalletSettingsId()
                );
            }

            walletSettings = optionalWalletSettings.get();

            walletSettings.setPointsType(
                    requestDto.getPointsType().trim()
            );

            walletSettings.setNumOfPoints(
                    requestDto.getNumOfPoints()
            );

            walletSettings.setUpdatedBy(
                    requestDto.getUpdatedBy()
            );

            walletSettings.setUpdatedAt(
                    LocalDateTime.now()
            );

        } else {

            /*
             * ====================================================
             * CREATE
             * ====================================================
             */

            log.info("Creating new wallet settings");

            walletSettings =
                    CoWalletSettingsMapper.mapToEntity(requestDto);

            walletSettings.setCreatedAt(
                    LocalDateTime.now()
            );
        }

        /*
         * ========================================================
         * SAVE ENTITY
         * ========================================================
         */

        CoWalletSettings savedEntity =
                walletSettingsRepository.save(walletSettings);

        log.info(
                "Wallet settings saved successfully. ID: {}",
                savedEntity.getWalletSettingsId()
        );

        return CoWalletSettingsMapper.mapToResponseDto(
                savedEntity
        );
    }


    // ============================================================
    // GET ALL WALLET SETTINGS
    // ============================================================

    /**
     * Get all Wallet Settings without pagination.
     */
    @Override
    public List<CoWalletSettings> getWalletSettings() {

        log.info("Entering getWalletSettings without pagination");

        List<CoWalletSettings> walletSettings =
                walletSettingsRepository.findAll();

        log.info(
                "Wallet settings fetched successfully. Total elements={}",
                walletSettings.size()
        );

        return walletSettings;
    }


    // ============================================================
    // GET WALLET SETTINGS WITH PAGINATION
    // ============================================================

    /**
     * Get Wallet Settings with Pagination.
     *
     * @param page page number starting from 0
     * @param size number of records per page
     */
    @Override
    public Page<CoWalletSettingsResponseDto> getWalletSettings(
            int page,
            int size) {

        log.info(
                "Entering getWalletSettings with pagination. page={}, size={}",
                page,
                size
        );

        /*
         * ========================================================
         * VALIDATE PAGINATION
         * ========================================================
         */

        validatePagination(page, size);

        /*
         * ========================================================
         * CREATE PAGEABLE
         *
         * Latest wallet settings first.
         * ========================================================
         */

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "walletSettingsId"
                        )
                );

        /*
         * ========================================================
         * FETCH FROM DATABASE
         * ========================================================
         */

        Page<CoWalletSettings> walletSettingsPage =
                walletSettingsRepository.findAll(pageable);

        log.info(
                "Wallet settings fetched successfully. " +
                        "Total elements={}, total pages={}, current page={}",
                walletSettingsPage.getTotalElements(),
                walletSettingsPage.getTotalPages(),
                page
        );

        /*
         * ========================================================
         * ENTITY -> RESPONSE DTO
         * ========================================================
         */

        return walletSettingsPage.map(
                CoWalletSettingsMapper::mapToResponseDto
        );
    }


    // ============================================================
    // VALIDATE SAVE REQUEST
    // ============================================================

    /**
     * Validate Create / Update Request.
     */
    private void validateSaveRequest(
            CoWalletSettingsRequestDto requestDto) {

        /*
         * Request cannot be null.
         */
        if (requestDto == null) {

            throw new IllegalArgumentException(
                    "Wallet settings request cannot be null"
            );
        }

        /*
         * ========================================================
         * VALIDATE POINTS TYPE
         * ========================================================
         */

        if (requestDto.getPointsType() == null
                || requestDto.getPointsType().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Points type is required"
            );
        }

        /*
         * ========================================================
         * VALIDATE NUMBER OF POINTS
         * ========================================================
         */

        if (requestDto.getNumOfPoints() == null) {

            throw new IllegalArgumentException(
                    "Number of points is required"
            );
        }

        if (requestDto.getNumOfPoints() <= 0) {

            throw new IllegalArgumentException(
                    "Number of points must be greater than 0"
            );
        }

        /*
         * ========================================================
         * CREATE VALIDATION
         * ========================================================
         */

        if (requestDto.getWalletSettingsId() == null) {

            if (requestDto.getCreatedBy() == null
                    || requestDto.getCreatedBy().trim().isEmpty()) {

                throw new IllegalArgumentException(
                        "Created by is required"
                );
            }
        }

        /*
         * ========================================================
         * UPDATE VALIDATION
         * ========================================================
         */

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

        /*
         * Page cannot be negative.
         */
        if (page < 0) {

            throw new IllegalArgumentException(
                    "Page number cannot be negative"
            );
        }

        /*
         * Page size must be greater than zero.
         */
        if (size <= 0) {

            throw new IllegalArgumentException(
                    "Page size must be greater than 0"
            );
        }

        /*
         * Maximum page size.
         */
        if (size > MAX_PAGE_SIZE) {

            throw new IllegalArgumentException(
                    "Page size cannot be greater than "
                            + MAX_PAGE_SIZE
            );
        }
    }
}