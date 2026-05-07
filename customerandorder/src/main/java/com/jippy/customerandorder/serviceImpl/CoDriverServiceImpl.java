package com.jippy.customerandorder.serviceImpl;


import com.jippy.customerandorder.Constants.COConstants;
import com.jippy.customerandorder.dto.CoAddressRequestDto;
import com.jippy.customerandorder.dto.CoDriverDto;
import com.jippy.customerandorder.dto.CoZoneDto;
import com.jippy.customerandorder.entity.CoDriver;
import com.jippy.customerandorder.entity.CoZone;
import com.jippy.customerandorder.exception.CoZoneException;
import com.jippy.customerandorder.feignClients.FMFeignClient;
import com.jippy.customerandorder.iservice.ICoDriverService;
import com.jippy.customerandorder.mapper.CoDriverMapper;
import com.jippy.customerandorder.repository.CoDriverRepository;
import com.jippy.customerandorder.repository.CoZoneRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoDriverServiceImpl implements ICoDriverService {

    private final CoDriverRepository driverRepository;
    private final CoZoneRepository zoneRepository;

    @Autowired
    private FMFeignClient FMFeignClient;

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
       CoAddressRequestDto coAddressRequestDtoFeign = FMFeignClient.saveAddressDetails(coAddressRequestDto).getBody();

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
        CoAddressRequestDto coGetAddressRequestDtoFeign = FMFeignClient.getAddressDetails(driverId).getBody();
        CoDriverDto driverDto = CoDriverMapper.mapToDriverDto(driver, coGetAddressRequestDtoFeign);

        return driverDto;
    }

    @Transactional
    @Override
    public String createZones(CoZoneDto zoneDto) {

        Optional<CoZone> existingZone = zoneRepository.findByZoneName(zoneDto.getZoneName());
        Polygon polygon = convertToJtsPolygon(zoneDto.getBoundary());
        if (existingZone.isPresent()) {
            if (zoneRepository.existsBySpatialBoundary(polygon)) {
                throw new CoZoneException("A boundary with this exact shape already exists!");
            }else{
                log.info("Updating existing zone with id: {}", existingZone.get().getZoneId());
                CoZone zoneToUpdate = existingZone.get();
                zoneToUpdate.setBoundary(polygon);
                zoneToUpdate.setUpdatedAt(java.time.LocalDateTime.now());
                zoneToUpdate.setUpdatedBy(zoneDto.getCreatedBy());
                zoneRepository.save(zoneToUpdate);
                return "Zone:" +zoneToUpdate.getZoneName()+ " updated successfully!";
            }
        }
        CoZone zone = CoDriverMapper.mapToZoneEntity(zoneDto, polygon);
        CoZone savedZone =  zoneRepository.save(zone);
        log.info("New zone is created with id: {}", savedZone.getZoneId());

        return "Zone:" +zone.getZoneName()+ " created successfully!";
    }

    private Polygon convertToJtsPolygon(List<CoZoneDto.CoordinateDTO> boundary) {
        GeometryFactory factory = new GeometryFactory();

        Coordinate[] coords = boundary.stream()
                .map(p -> new Coordinate(p.getLongitude(), p.getLatitude()))
                .toArray(Coordinate[]::new);

        // JTS requires the linear ring to be closed (first == last)
        return factory.createPolygon(coords);
    }


}