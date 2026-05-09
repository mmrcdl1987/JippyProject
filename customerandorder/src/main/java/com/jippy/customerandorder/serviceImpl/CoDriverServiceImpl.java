package com.jippy.customerandorder.serviceImpl;


import com.jippy.customerandorder.constants.COConstants;
import com.jippy.customerandorder.dto.CoAddressRequestDto;
import com.jippy.customerandorder.dto.CoDriverDto;
import com.jippy.customerandorder.entity.CoDriver;
import com.jippy.customerandorder.feignClients.AddressFeignClient;
import com.jippy.customerandorder.iservice.ICoDriverService;
import com.jippy.customerandorder.mapper.CoDriverMapper;
import com.jippy.customerandorder.repository.CoDriverRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoDriverServiceImpl implements ICoDriverService {

    private final CoDriverRepository driverRepository;

    @Autowired
    private AddressFeignClient addressFeignClient;

    @Override
    @Transactional
    public CoDriverDto postDriverDetails(CoDriverDto dto) {

        log.info("Creating driver for phone: {}", dto.getPhoneNumber());

        // Convert DTO → Entity
        CoDriver driver = CoDriverMapper.mapToDriverEntity(dto);

        // Save driver
        CoDriver savedDriver = driverRepository.save(driver);

        log.info("Driver saved with id: {}", savedDriver.getDriverId());

        CoAddressRequestDto coAddressRequestDto = new CoAddressRequestDto();
        coAddressRequestDto.setJippyAddressId(savedDriver.getDriverId());
        coAddressRequestDto.setBuildingNumber(dto.getBuildingNumber());
        coAddressRequestDto.setRoad(dto.getRoad());
        coAddressRequestDto.setLandmark(dto.getLandmark());
        coAddressRequestDto.setCityId(dto.getCityId());
        coAddressRequestDto.setStateId(dto.getStateId());
        coAddressRequestDto.setAreaId(dto.getAreaId());
        coAddressRequestDto.setAddressType(COConstants.TYPE_DRIVER);
       CoAddressRequestDto coAddressRequestDtoFeign = addressFeignClient.saveAddressDetails(coAddressRequestDto).getBody();

        // Convert Entity → DTO
        CoDriverDto mapToDriverDto = CoDriverMapper.mapToDriverDto(savedDriver,coAddressRequestDtoFeign);

        return mapToDriverDto;
    }

    @Override
    @Transactional
    public CoDriverDto getDriverDetails(Integer driverId) {

        log.info("Fetching driver with id: {}", driverId);

        CoDriver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> {
                    log.error("Driver not found with id: {}", driverId);
                    return new ResourceNotFoundException("Driver not found with id: " + driverId);
                });
        CoAddressRequestDto coGetAddressRequestDtoFeign =addressFeignClient.getAddressDetails(driverId).getBody();
        CoDriverDto driverDto = CoDriverMapper.mapToDriverDto(driver, coGetAddressRequestDtoFeign);

        return driverDto;
    }


}