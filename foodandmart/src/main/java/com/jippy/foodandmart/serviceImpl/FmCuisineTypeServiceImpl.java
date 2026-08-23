package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.dto.FmCuisineTypeRequestDTO;
import com.jippy.foodandmart.dto.FmCuisineTypeResponseDTO;
import com.jippy.foodandmart.entity.FmCuisineType;
import com.jippy.foodandmart.exception.DuplicateResourceException;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.mapper.FmCuisineTypeMapper;
import com.jippy.foodandmart.repository.FmCuisineTypeRepository;
import com.jippy.foodandmart.service.FmCuisineTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FmCuisineTypeServiceImpl
        implements FmCuisineTypeService {

    private final FmCuisineTypeRepository cuisineTypeRepository;
    // CREATE
    @Override
    @Transactional
    public FmCuisineTypeResponseDTO createCuisineType(
            FmCuisineTypeRequestDTO dto) {

        log.info(
                "Creating cuisine type. name={}",
                dto.getCuisineTypesName()
        );

        String cuisineName =
                dto.getCuisineTypesName()
                        .trim()
                        .toUpperCase();

        if (cuisineTypeRepository
                .existsByCuisineTypesNameIgnoreCase(cuisineName)) {

            log.warn(
                    "Cuisine type already exists. name={}",
                    cuisineName
            );

            throw new DuplicateResourceException(
                    "Cuisine type already exists."
            );
        }

        FmCuisineType entity =
                FmCuisineTypeMapper.toEntity(dto);

        FmCuisineType savedEntity =
                cuisineTypeRepository.save(entity);

        log.info(
                "Cuisine type created successfully. id={}",
                savedEntity.getCuisineTypesId()
        );

        return FmCuisineTypeMapper.toResponseDTO(
                savedEntity
        );
    }


    // GET BY ID
    @Override
    @Transactional(readOnly = true)
    public FmCuisineTypeResponseDTO getCuisineTypeById(
            Integer cuisineTypesId) {

        log.info(
                "Fetching cuisine type. id={}",
                cuisineTypesId
        );

        FmCuisineType entity =
                cuisineTypeRepository.findById(cuisineTypesId)
                        .orElseThrow(() -> {

                            log.warn(
                                    "Cuisine type not found. id={}",
                                    cuisineTypesId
                            );

                            return new ResourceNotFoundException(
                                    "Cuisine type not found with id: "
                                            + cuisineTypesId
                            );
                        });

        return FmCuisineTypeMapper.toResponseDTO(
                entity
        );
    }

    // GET ALL
    @Override
    @Transactional(readOnly = true)
    public List<FmCuisineTypeResponseDTO> getAllCuisineTypes() {

        log.info("Fetching all cuisine types.");

        return cuisineTypeRepository.findAll()
                .stream()
                .map(FmCuisineTypeMapper::toResponseDTO)
                .toList();
    }


    // UPDATE


    @Override
    @Transactional
    public FmCuisineTypeResponseDTO updateCuisineType(
            Integer cuisineTypesId,
            FmCuisineTypeRequestDTO dto) {

        log.info(
                "Updating cuisine type. id={}",
                cuisineTypesId
        );

        FmCuisineType entity =
                cuisineTypeRepository.findById(cuisineTypesId)
                        .orElseThrow(() -> {

                            log.warn(
                                    "Cuisine type not found. id={}",
                                    cuisineTypesId
                            );

                            return new ResourceNotFoundException(
                                    "Cuisine type not found with id: "
                                            + cuisineTypesId
                            );
                        });

        String cuisineName =
                dto.getCuisineTypesName()
                        .trim()
                        .toUpperCase();

        if (cuisineTypeRepository
                .existsByCuisineTypesNameIgnoreCaseAndCuisineTypesIdNot(
                        cuisineName,
                        cuisineTypesId
                )) {

            log.warn(
                    "Cuisine type already exists. name={}, id={}",
                    cuisineName,
                    cuisineTypesId
            );

            throw new DuplicateResourceException(
                    "Cuisine type already exists."
            );
        }

        FmCuisineTypeMapper.updateEntity(
                entity,
                dto
        );

        FmCuisineType updatedEntity =
                cuisineTypeRepository.save(entity);

        log.info(
                "Cuisine type updated successfully. id={}",
                cuisineTypesId
        );

        return FmCuisineTypeMapper.toResponseDTO(
                updatedEntity
        );
    }
}