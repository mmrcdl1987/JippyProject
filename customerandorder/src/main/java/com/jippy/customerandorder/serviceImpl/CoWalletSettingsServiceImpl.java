package com.jippy.customerandorder.serviceImpl;

import com.jippy.customerandorder.dto.CoWalletSettingsRequestDto;
import com.jippy.customerandorder.dto.CoWalletSettingsResponseDto;
import com.jippy.customerandorder.entity.CoWalletSettings;
import com.jippy.customerandorder.exception.CoBusinessException;
import com.jippy.customerandorder.iservice.CoWalletSettingsService;
import com.jippy.customerandorder.mapper.CoWalletSettingsMapper;
import com.jippy.customerandorder.repository.CoWalletSettingsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CoWalletSettingsServiceImpl implements CoWalletSettingsService {

    @Autowired
    private CoWalletSettingsRepository walletSettingsRepository;

    @Override
    public List<CoWalletSettings> getWalletSettings() {
        return walletSettingsRepository.findAll();
    }

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
                walletSettings.setSettingType(requestDto.getSettingType());
                walletSettings.setSettingValue(requestDto.getSettingValue());
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