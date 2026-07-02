package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.FmAreaDto;
import com.jippy.foodandmart.service.IFmAreaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/fm")
@RequiredArgsConstructor
@Slf4j
public class FmAreaController {

    private final IFmAreaService areaService;

    @GetMapping("/areas")
    public List<FmAreaDto> getAllAreas() {

        log.info("GET_ALL_AREAS_API_START");

        List<FmAreaDto> response = areaService.getAllAreas();

        log.info("GET_ALL_AREAS_API_SUCCESS | count={}", response.size());

        return response;
    }
}