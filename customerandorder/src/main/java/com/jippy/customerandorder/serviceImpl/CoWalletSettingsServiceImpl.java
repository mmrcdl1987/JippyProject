package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.dto.CoWalletSettingsRequestDto;
import com.jippy.customerandorder.dto.CoWalletSettingsResponseDto;
import com.jippy.customerandorder.entity.CoWalletSettings;
import com.jippy.customerandorder.iservice.CoWalletSettingsService;
import com.jippy.customerandorder.mapper.CoWalletSettingsMapper;
import com.jippy.customerandorder.repository.CoWalletSettingsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class CoWalletSettingsServiceImpl implements CoWalletSettingsService {

    @Autowired
    private CoWalletSettingsRepository walletSettingsRepository;

    @Override
    public CoWalletSettingsResponseDto saveWalletSettings(CoWalletSettingsRequestDto requestDto) {

        log.info("Entering into saveWalletSettings");

        CoWalletSettings walletSettings;

        // UPDATE
        if (requestDto.getWalletSettingsId() != null) {
            Optional<CoWalletSettings> optionalWalletSettings = walletSettingsRepository.findById(requestDto.getWalletSettingsId());
            if (optionalWalletSettings.isPresent()) {
                log.info("Updating existing wallet settings");
                walletSettings = optionalWalletSettings.get();
                walletSettings.setPointsType(requestDto.getPointsType());
                walletSettings.setNumOfPoints(requestDto.getNumOfPoints());
                walletSettings.setUpdatedBy(requestDto.getUpdatedBy());
                walletSettings.setUpdatedAt(LocalDateTime.now());

            } else {

                log.info("Wallet settings ID not found, creating new");

                walletSettings = CoWalletSettingsMapper.mapToEntity(requestDto);

                walletSettings.setCreatedAt(LocalDateTime.now());
            }

        } else {

            // CREATE
            log.info("Creating new wallet settings");

            walletSettings = CoWalletSettingsMapper.mapToEntity(requestDto);

            walletSettings.setCreatedAt(LocalDateTime.now());
        }

        CoWalletSettings savedEntity = walletSettingsRepository.save(walletSettings);

        log.info("Wallet settings saved successfully");

        return CoWalletSettingsMapper.mapToResponseDto(savedEntity);
    }
}