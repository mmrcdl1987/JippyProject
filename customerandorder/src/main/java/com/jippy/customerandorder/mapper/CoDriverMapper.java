package com.jippy.customerandorder.mapper;


import com.jippy.customerandorder.dto.CoAddressRequestDto;
import com.jippy.customerandorder.dto.CoDriverDto;
import com.jippy.customerandorder.entity.CoDriver;
import com.jippy.customerandorder.entity.CoDriverKyc;
import com.jippy.customerandorder.exception.CoBadRequestException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CoDriverMapper {

    // Convert DTO to Driver entity
    // Also handles creating and linking KYC data if present
    public static CoDriver mapToDriverEntity(CoDriverDto dto) {

        // Check if input is null
        if (dto == null) {
            throw new CoBadRequestException("Driver DTO must not be null");
        }

        // Create Driver object and set basic details
        CoDriver driver = new CoDriver();
        driver.setFirstName(dto.getFirstName());
        driver.setLastName(dto.getLastName());
        driver.setPhoneNumber(dto.getPhoneNumber());
        driver.setEmail(dto.getEmail());
//        newly added feilds after changing requirement
        driver.setNomineeName(dto.getNomineeName());
        driver.setNomineePhoneNumber(dto.getNomineePhoneNumber());
        driver.setIsNomineeVerified(dto.getIsNomineeVerified());

        driver.setFamilyMemberName(dto.getFamilyMemberName());
        driver.setFamilyMemberPhoneNumber(dto.getFamilyMemberPhoneNumber());
        driver.setIsFamilyMemberVerified(dto.getIsFamilyMemberVerified());
        driver.setCreatedAt(LocalDateTime.now());
        driver.setCreatedBy(1);

        // Create KYC object from DTO
        CoDriverKyc driverKyc = mapToDriverKycEntity(dto);

        // Link Driver and KYC if KYC exists
        if (driverKyc != null) {
            driverKyc.setDriver(driver);     // owning side
            driver.setDriverKyc(driverKyc);  // reference side
        }

        return driver;
    }

    // Convert KYC fields from DTO to KYC entity
    // If no KYC data is provided, return null
    private static CoDriverKyc mapToDriverKycEntity(CoDriverDto dto) {

        // Safety check
        if (dto == null) {
            throw new CoBadRequestException("Driver DTO must not be null for KYC mapping");
        }

        // If all KYC fields are empty, skip creation
        if (dto.getAadharNumber() == null &&
                dto.getDrivingLicenseNumber() == null &&
                dto.getRcCopy() == null) {
            return null;
        }

        // Create KYC object and set values
        CoDriverKyc kyc = new CoDriverKyc();
        kyc.setAadharNumber(dto.getAadharNumber());
        kyc.setDrivingLicenseNumber(dto.getDrivingLicenseNumber());
        kyc.setRcCopy(dto.getRcCopy());
        kyc.setCreatedAt(LocalDateTime.now());

        return kyc;


    }

    // Convert Driver entity to DTO
    // Includes KYC details if available
    public static CoDriverDto mapToDriverDto(CoDriver driver, CoAddressRequestDto coAddressRequestDtoFeign) {

        // Check if entity is null
        if (driver == null) {
            throw new CoBadRequestException("Driver entity must not be null");
        }

        CoDriverDto dto = new CoDriverDto();

        // Set driver details
        dto.setDriverId(driver.getDriverId());
        dto.setFirstName(driver.getFirstName());
        dto.setLastName(driver.getLastName());
        dto.setPhoneNumber(driver.getPhoneNumber());
        dto.setEmail(driver.getEmail());
//        newly added feilds after changing requirement
        dto.setNomineeName(driver.getNomineeName());
        dto.setNomineePhoneNumber(driver.getNomineePhoneNumber());
        dto.setIsNomineeVerified(driver.getIsNomineeVerified());

        dto.setFamilyMemberName(driver.getFamilyMemberName());
        dto.setFamilyMemberPhoneNumber(driver.getFamilyMemberPhoneNumber());
        dto.setIsFamilyMemberVerified(driver.getIsFamilyMemberVerified());

        // Set KYC details if present
        if (driver.getDriverKyc() != null) {
            dto.setDriverKycId(driver.getDriverKyc().getDriverKycId());
            dto.setAadharNumber(driver.getDriverKyc().getAadharNumber());
            dto.setDrivingLicenseNumber(driver.getDriverKyc().getDrivingLicenseNumber());
            dto.setRcCopy(driver.getDriverKyc().getRcCopy());
        }

//        set address through feign client response
        if(coAddressRequestDtoFeign != null) {
            dto.setBuildingNumber(coAddressRequestDtoFeign.getBuildingNumber());
            dto.setRoad(coAddressRequestDtoFeign.getRoad());
            dto.setLandmark(coAddressRequestDtoFeign.getLandmark());
            dto.setCityId(coAddressRequestDtoFeign.getCityId());
            dto.setStateId(coAddressRequestDtoFeign.getStateId());
            dto.setAreaId(coAddressRequestDtoFeign.getAreaId());

        }

        return dto;


    }
}