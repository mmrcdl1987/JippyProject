package com.jippy.customerandorder.controller;

import com.jippy.customerandorder.dto.CoWalletSettingsRequestDto;
import com.jippy.customerandorder.dto.CoWalletSettingsResponseDto;
import com.jippy.customerandorder.entity.CoWalletSettings;
import com.jippy.customerandorder.iservice.CoWalletSettingsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/co/wallet-settings")
@Slf4j
public class CoWalletSettingsController {

    @Autowired
    private CoWalletSettingsService walletSettingsService;

    /**
     * Create / Update Wallet Settings
     *
     * POST /api/co/wallet-settings/save
     */
    @PostMapping("/save")
    public ResponseEntity<CoWalletSettingsResponseDto> saveWalletSettings(
            @RequestBody CoWalletSettingsRequestDto requestDto) {

        log.info("Received request to save wallet settings");

        CoWalletSettingsResponseDto responseDto =
                walletSettingsService.saveWalletSettings(requestDto);

        return ResponseEntity.ok(responseDto);
    }

    /**
     * Get all Wallet Settings without pagination.
     *
     * GET /api/co/wallet-settings
     */
    @GetMapping
    public ResponseEntity<List<CoWalletSettings>> getWalletSettings() {

        log.info("Received request to get all wallet settings");

        return ResponseEntity.ok(
                walletSettingsService.getWalletSettings()
        );
    }

    /**
     * Get Wallet Settings with Pagination.
     *
     * Example:
     * GET /api/co/wallet-settings/get?page=0&size=10
     */
    @GetMapping("/get")
    public ResponseEntity<Page<CoWalletSettingsResponseDto>> getWalletSettings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info(
                "Received request to get wallet settings. page={}, size={}",
                page,
                size
        );

        Page<CoWalletSettingsResponseDto> response =
                walletSettingsService.getWalletSettings(page, size);

        return ResponseEntity.ok(response);
    }
}