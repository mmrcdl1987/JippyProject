package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmCity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FmCityRepository
        extends JpaRepository<FmCity, Integer> {

    /**
     * Finds all cities belonging to a state.
     */
    List<FmCity> findByStateId(
            Integer stateId
    );

    /**
     * Finds a city by name.
     *
     * Used during bulk outlet upload:
     *
     * Excel/CSV:
     *     City Name = Hyderabad
     *
     *          ↓
     *
     * FmCityRepository
     *
     *          ↓
     *
     * cityId
     */
    Optional<FmCity> findByCityNameIgnoreCase(
            String cityName
    );
}