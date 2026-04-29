package com.jippy.foodandmart.controller;
import com.jippy.foodandmart.dto.FmAreaDto;
import com.jippy.foodandmart.dto.FmCityDto;
import com.jippy.foodandmart.dto.FmStateDto;
import com.jippy.foodandmart.service.IFmLocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/fm/location")
@Tag(name = "Location API", description = "APIs for fetching State, City, Area data")
public class FmLocationController {
    private static final Logger logger = LoggerFactory.getLogger(FmLocationController.class);
    @Autowired
    private IFmLocationService locationService;

    // Fetch All States
    @Operation(summary = "Fetch all states")
    @GetMapping("/fetchStates")
    public ResponseEntity<List<FmStateDto>> fetchStates() {

        logger.info("API CALL: Fetch all states");

        List<FmStateDto> states = locationService.fetchStates();

        logger.info("Fetched {} states", states.size());

        return ResponseEntity.ok(states);
    }

    // Fetch Cities by state ID
    @Operation(summary = "Fetch cities based on stateId")
    @GetMapping("/fetchCityInState")
    public ResponseEntity<List<FmCityDto>> fetchCityInState(
            @RequestParam Integer stateId) {

        logger.info("API CALL: Fetch cities for stateId={}", stateId);

        List<FmCityDto> cities = locationService.fetchCityInState(stateId);

        logger.info("Fetched {} cities", cities.size());

        return ResponseEntity.ok(cities);
    }

    //  Fetch Areas by City ID
    @Operation(summary = "Fetch areas based on cityId")
    @GetMapping("/fetchAreaInCity")
    public ResponseEntity<List<FmAreaDto>> fetchAreaInCity(
            @RequestParam Integer cityId) {

        logger.info("API CALL: Fetch areas for cityId={}", cityId);

        List<FmAreaDto> areas = locationService.fetchAreaInCity(cityId);

        logger.info("Fetched {} areas", areas.size());

        return ResponseEntity.ok(areas);
    }
}