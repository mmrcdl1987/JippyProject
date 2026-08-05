package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmAreaDto;
import com.jippy.foodandmart.dto.FmCityDto;
import com.jippy.foodandmart.dto.FmStateDto;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IFmLocationService {

    List<FmStateDto> fetchStates();

    List<FmCityDto> fetchCityInState(Integer stateId);

    List<FmAreaDto> fetchAreaInCity(Integer cityId);

    String findAreaById(Integer areaId);
}