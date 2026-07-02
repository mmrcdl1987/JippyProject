package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.FmBannerDesignerResponseDto;
import com.jippy.foodandmart.service.IFmBannerDesignerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/fm/banner-designer")
@RequiredArgsConstructor
@Slf4j
public class FmBannerDesignerController {

    private final IFmBannerDesignerService bannerDesignerService;

    @GetMapping
    public ResponseEntity<List<FmBannerDesignerResponseDto>>
    getAllBannerDesigners() {

        log.info("GET API : Fetching Banner Designer List");

        List<FmBannerDesignerResponseDto> response =
                bannerDesignerService.getAllBannerDesigners();

        log.info("Returning {} Banner Records", response.size());

        return new ResponseEntity<>(
                response,
                HttpStatus.OK);

    }

}