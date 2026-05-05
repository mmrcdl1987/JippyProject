package com.jippy.foodandmart.repository;


import com.jippy.foodandmart.entity.FmCity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FmCityRepository extends JpaRepository<FmCity, Integer> {

    List<FmCity> findByStateId(Integer stateId);
}