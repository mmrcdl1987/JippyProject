package com.jippy.foodandmart.serviceImpl;


import com.jippy.foodandmart.dto.FmDivPriceModelDto;
import com.jippy.foodandmart.dto.FmAreaDto;
import com.jippy.foodandmart.dto.FmCityDto;
import com.jippy.foodandmart.dto.FmStateDto;
import com.jippy.foodandmart.entity.FmArea;
import com.jippy.foodandmart.entity.FmCity;
import com.jippy.foodandmart.entity.FmState;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.feignClients.DivisionFeignClient;
import com.jippy.foodandmart.mapper.FmLocationMapper;
import com.jippy.foodandmart.repository.FmAreaRepository;
import com.jippy.foodandmart.repository.FmCityRepository;
import com.jippy.foodandmart.repository.FmStateRepository;
import com.jippy.foodandmart.service.IFmLocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FmLocationServiceImpl implements IFmLocationService {

    private static final Logger logger =
            LoggerFactory.getLogger(FmLocationServiceImpl.class);

    @Autowired
    private FmStateRepository stateRepository;

    @Autowired
    private FmCityRepository cityRepository;

    @Autowired
    private FmAreaRepository areaRepository;

//    @Autowired
//    private DivisionFeignClient priceModelFeignClient;

    //  Fetch all states
    @Override
    public List<FmStateDto> fetchStates() {

        logger.info("SERVICE: Fetching all states (Projection)");

        List<FmState> states = stateRepository.findAll();
        List<FmStateDto> stateDtoList = new ArrayList<>();


//        ResponseEntity<List<FmDivPriceModelDto>> priceModelDtoListResponse = priceModelFeignClient.getPriceModels();
//        List<FmDivPriceModelDto> priceModelDtoList = priceModelDtoListResponse.getBody();
//        for(FmDivPriceModelDto divPriceModelDto:priceModelDtoList){
//            logger.info("PriceModelId : {} ", divPriceModelDto.getPriceModelId());
//            logger.info("PriceModelName : {} ", divPriceModelDto.getPriceModelName());
//        }

        if (states != null && !states.isEmpty()) {
            for (FmState  state : states) {
                stateDtoList.add(FmLocationMapper.mapToStateDto(state));
            }
        } else {
            logger.warn("No states found in DB");
            throw new ResourceNotFoundException("No states found");
        }

        logger.info("SERVICE: Total states fetched = {}", stateDtoList.size());

        return stateDtoList;
    }

    // Fetch cities by stateId
    @Override
    public List<FmCityDto> fetchCityInState(Integer stateId) {

        if (stateId == null) {
            throw new ResourceNotFoundException("StateId cannot be null");
        }

        logger.info("SERVICE: Fetching cities for stateId={} (Projection)", stateId);

        List<FmCity> cities = cityRepository.findByStateId(stateId);
        List<FmCityDto> cityDtoList = new ArrayList<>();

        if (cities != null && !cities.isEmpty()) {
            for (FmCity city : cities) {
                cityDtoList.add(FmLocationMapper.mapToCityDto(city));
            }
        } else {
            logger.warn("No cities found for stateId={}", stateId);
            throw new ResourceNotFoundException("No cities found for stateId: " + stateId);

        }

        logger.info("SERVICE: Total cities fetched = {}", cityDtoList.size());

        return cityDtoList;
    }

    //  Fetch areas by cityId
    @Override
    public List<FmAreaDto> fetchAreaInCity(Integer cityId) {

        if (cityId == null) {
            throw new ResourceNotFoundException("CityId cannot be null");
        }

        logger.info("SERVICE: Fetching areas for cityId={} (Projection)", cityId);

        List<FmArea> areas = areaRepository.findByCityId(cityId);
        List<FmAreaDto> areaDtoList = new ArrayList<>();

        if (areas != null && !areas.isEmpty()) {
            for (FmArea area : areas) {
                areaDtoList.add(FmLocationMapper.mapToAreaDto(area));
            }
        } else {
            logger.warn("No areas found for cityId={}", cityId);
            throw new ResourceNotFoundException("No areas found for cityId: " + cityId);
        }

        logger.info("SERVICE: Total areas fetched = {}", areaDtoList.size());

        return areaDtoList;
    }

    @Override
    public String findAreaById(Integer areaId) {

       Optional<FmArea> optionalArea  = areaRepository.findByAreaId(areaId);
       if(optionalArea.isPresent()){

          String areaName =  optionalArea.get().getAreaName();

           return areaName;
       }else {
           logger.warn("No area found for areaId={}", areaId);
           return "No areas found for areaId: " + areaId;
       }

    }


}