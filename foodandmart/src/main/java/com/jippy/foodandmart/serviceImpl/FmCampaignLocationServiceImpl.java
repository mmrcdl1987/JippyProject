package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.dto.FmAreasDto;
import com.jippy.foodandmart.dto.FmCitysDto;
import com.jippy.foodandmart.dto.FmOutletsDto;
import com.jippy.foodandmart.dto.FmCampaignLocationResponse;
import com.jippy.foodandmart.projections.FmAreaProjection;
import com.jippy.foodandmart.projections.FmCityProjection;
import com.jippy.foodandmart.projections.FmOutletsProjection;
import com.jippy.foodandmart.repository.FmCampaignLocationRepository;
import com.jippy.foodandmart.service.FmCampaignLocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FmCampaignLocationServiceImpl implements FmCampaignLocationService {

    private final FmCampaignLocationRepository campaignLocationRepository;

    @Override
    public FmCampaignLocationResponse getCampaignLocation(Integer stateId, Integer cityId, Integer areaId) {

        log.info("========== Campaign Location API Started ==========");
        log.info("Request -> stateId={}, cityId={}, areaId={}", stateId, cityId, areaId);

        validateRequest(stateId);

        try {

            List<FmCitysDto> cities = Collections.emptyList();
            List<FmAreasDto> areas = Collections.emptyList();

            /*
             * ----------------------------------------------------
             * State Selected
             * Fetch Cities
             * ----------------------------------------------------
             */
            if (cityId == null) {

                log.info("Fetching Cities for State : {}", stateId);

                cities = campaignLocationRepository.getCities(stateId).stream().map(this::mapCity).toList();

            }
            /*
             * ----------------------------------------------------
             * City Selected
             * Fetch Areas
             * ----------------------------------------------------
             */
            else if (areaId == null) {

                log.info("Fetching Areas for City : {}", cityId);

                areas = campaignLocationRepository.getAreas(cityId).stream().map(this::mapArea).toList();
            }

            /*
             * ----------------------------------------------------
             * Fetch Outlets
             * ----------------------------------------------------
             */

            log.info("Fetching Campaign Outlets");

            List<FmOutletsDto> outlets = campaignLocationRepository.getCampaignOutlets(stateId, cityId, areaId).stream().map(this::mapOutlet).toList();

            log.info("Cities  : {}", cities.size());
            log.info("Areas   : {}", areas.size());
            log.info("Outlets : {}", outlets.size());

            log.info("========== Campaign Location API Completed ==========");

            return FmCampaignLocationResponse.builder().cities(cities).areas(areas).outlets(outlets).build();

        } catch (Exception ex) {

            log.error("Error while fetching campaign location.", ex);

            throw new RuntimeException("Unable to fetch campaign location.", ex);
        }
    }

    /**
     * Validate Request
     */
    private void validateRequest(Integer stateId) {

        if (stateId == null) {

            log.error("State Id is mandatory.");

            throw new IllegalArgumentException("State Id is mandatory.");
        }
    }

    /**
     * Map City Projection
     */
    private FmCitysDto mapCity(FmCityProjection city) {

        return FmCitysDto.builder().cityId(city.getCityId()).cityName(city.getCityName()).build();
    }

    /**
     * Map Area Projection
     */
    private FmAreasDto mapArea(FmAreaProjection area) {

        return FmAreasDto.builder().areaId(area.getAreaId()).areaName(area.getAreaName()).build();
    }

    /**
     * Map Outlet Projection
     */
    private FmOutletsDto mapOutlet(FmOutletsProjection outlet) {

        return FmOutletsDto.builder().outletId(outlet.getOutletId()).outletName(outlet.getOutletName()).build();
    }
}