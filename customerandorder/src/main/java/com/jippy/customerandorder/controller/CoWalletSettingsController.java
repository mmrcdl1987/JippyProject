package com.jippy.customerandorder.controller;

import com.jippy.customerandorder.dto.CoWalletSettingsRequestDto;
import com.jippy.customerandorder.dto.CoWalletSettingsResponseDto;
import com.jippy.customerandorder.iservice.CoWalletSettingsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wallet-settings")
@Slf4j
public class CoWalletSettingsController {

    @Autowired
    private CoWalletSettingsService walletSettingsService;

    @PostMapping("/save")
    public ResponseEntity<CoWalletSettingsResponseDto>
    saveWalletSettings(
            @RequestBody CoWalletSettingsRequestDto requestDto) {

        log.info("Received request for save wallet settings");

        CoWalletSettingsResponseDto responseDto =
                walletSettingsService
                        .saveWalletSettings(requestDto);

        return ResponseEntity.ok(responseDto);
    }
}