package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmCuisineType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FmCuisineTypeRepository
        extends JpaRepository<FmCuisineType, Integer> {

    boolean existsByCuisineTypesNameIgnoreCase(
            String cuisineTypesName
    );

    boolean existsByCuisineTypesNameIgnoreCaseAndCuisineTypesIdNot(
            String cuisineTypesName,
            Integer cuisineTypesId
    );

    Optional<FmCuisineType> findByCuisineTypesNameIgnoreCase(
            String cuisineTypesName
    );
}