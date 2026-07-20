package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.FmDriverApprovalResponseDTO;
import com.jippy.foodandmart.dto.FmLevel1PendingApprovalResponseDTO;
import com.jippy.foodandmart.entity.*;
import com.jippy.foodandmart.projections.FmDriverAddressProjection;
import com.jippy.foodandmart.projections.FmDriverLevel1PendingApprovalProjection;
import com.jippy.foodandmart.projections.FmMerchantLevel1PendingApprovalProjection;
import com.jippy.foodandmart.projections.FmOutletLevel1PendingApprovalProjection;

import java.time.LocalDateTime;

/**
 * Mapper class for Approval Requests.
 *
 * Converts Approval Request data into
 * Approval Request Entity.
 */
public class FmApprovalRequestMapper {

    /**
     * Converts the given details into an
     * Approval Request entity.
     *
     * Business Rules:
     * 1. Current Level is always Level 1.
     * 2. Status is always PENDING.
     * 3. Created Time is the current system time.
     *
     * @param entityType MERCHANT / OUTLET / DRIVER
     * @param entityId Merchant Id / Outlet Id / Driver Id
     * @param createdBy Logged-in User Id
     * @return Approval Request Entity
     */
    public static FmApprovalRequest toEntity(String entityType, Integer entityId,
            Integer createdBy) {

        FmApprovalRequest entity = new FmApprovalRequest();

        entity.setEntityType(entityType);
        entity.setEntityId(entityId);

        entity.setCurrentLevel(FmAppConstants.APPROVAL_LEVEL_1);

        entity.setStatus(FmAppConstants.STATUS_PENDING);

        entity.setCreatedBy(createdBy);

        entity.setCreatedAt(LocalDateTime.now());

        return entity;
    }

//    -------------------------------------------------------------------------------------------------
public static FmLevel1PendingApprovalResponseDTO
toOutletResponse(FmOutletLevel1PendingApprovalProjection outlet) {

    FmLevel1PendingApprovalResponseDTO dto = new FmLevel1PendingApprovalResponseDTO();

    dto.setApprovalRequestId(outlet.getApprovalRequestId());
    dto.setEntityType(outlet.getEntityType());
    dto.setEntityId(outlet.getEntityId());
    dto.setCurrentLevel(outlet.getCurrentLevel());
    dto.setStatus(outlet.getStatus());

    dto.setOutletId(outlet.getOutletId());
    dto.setOutletName(outlet.getOutletName());
    dto.setMerchantName(outlet.getMerchantName());
    dto.setCuisineType(outlet.getCuisineType());
    dto.setOutletPhone(outlet.getOutletPhone());
    dto.setOutletEmail(outlet.getOutletEmail());
    dto.setOutletApproved(outlet.getOutletApproved());
//converting from location to lattitude and longitude
    dto.setLatitude(outlet.getLatitude());
    dto.setLongitude(outlet.getLongitude());

//    from user_kyc
    dto.setFssaiNumber(outlet.getFssaiNumber());
    dto.setGstNumber(outlet.getGstNumber());

//    from address table
    dto.setAddressId(outlet.getAddressId());
    dto.setBuildingNumber(outlet.getBuildingNumber());
    dto.setRoad(outlet.getRoad());
    dto.setLandmark(outlet.getLandmark());
    dto.setStateName(outlet.getStateName());
    dto.setCityName(outlet.getCityName());
    dto.setAreaName(outlet.getAreaName());


    return dto;
}

    public static FmLevel1PendingApprovalResponseDTO toMerchantResponse
            (FmMerchantLevel1PendingApprovalProjection merchant) {

        FmLevel1PendingApprovalResponseDTO dto = new FmLevel1PendingApprovalResponseDTO();

        dto.setApprovalRequestId(merchant.getApprovalRequestId());
        dto.setEntityType(merchant.getEntityType());
        dto.setEntityId(merchant.getEntityId());
        dto.setCurrentLevel(merchant.getCurrentLevel());
        dto.setStatus(merchant.getStatus());
        dto.setMerchantId(merchant.getMerchantId());
        dto.setMerchantName(merchant.getMerchantName());
        dto.setMerchantEmail(merchant.getMerchantEmail());
        dto.setMerchantPhone(merchant.getMerchantPhone());
        dto.setMerchantBusinessType(merchant.getMerchantBusinessType());
        dto.setMerchantApproved(merchant.getMerchantApproved());
        dto.setMerchantProfilePicUrl(merchant.getMerchantProfilePicUrl());

//        user_kyc
        dto.setAadhaarNumber(merchant.getAadhaarNumber());
        dto.setPanNumber(merchant.getPanNumber());
//        address Feilds
        dto.setAddressId(merchant.getAddressId());
        dto.setBuildingNumber(merchant.getBuildingNumber());
        dto.setRoad(merchant.getRoad());
        dto.setLandmark(merchant.getLandmark());
        dto.setStateName(merchant.getStateName());
        dto.setCityName(merchant.getCityName());
        dto.setAreaName(merchant.getAreaName());
        return dto;
    }

    /**
     * ===========================================================
     * Convert Driver Approval Projection + Driver Feign Response +
     * Driver Address to Final Approval Response DTO
     * ===========================================================
     */
    public static FmLevel1PendingApprovalResponseDTO toDriverResponse(
            FmDriverLevel1PendingApprovalProjection projection,
            FmDriverApprovalResponseDTO driverResponse,
            FmDriverAddressProjection address){
        FmLevel1PendingApprovalResponseDTO dto = new FmLevel1PendingApprovalResponseDTO();

    /*==========================================================
     = Approval Request Details
     ==========================================================*/

        dto.setApprovalRequestId(projection.getApprovalRequestId());
        dto.setEntityType(projection.getEntityType());
        dto.setEntityId(projection.getEntityId());
        dto.setCurrentLevel(projection.getCurrentLevel());
        dto.setStatus(projection.getStatus());

    /*==========================================================
     = Driver Details (Feign Response)
     ==========================================================*/

        if (driverResponse != null) {

            dto.setDriverId(driverResponse.getDriverId());
            dto.setFirstName(driverResponse.getFirstName());
            dto.setLastName(driverResponse.getLastName());
            dto.setPhoneNumber(driverResponse.getPhoneNumber());
            dto.setEmail(driverResponse.getEmail());
            dto.setProfilePicUrl(driverResponse.getProfilePicUrl());

            dto.setNomineeName(driverResponse.getNomineeName());
            dto.setNomineePhoneNumber(driverResponse.getNomineePhoneNumber());
            dto.setNomineeVerified(driverResponse.getNomineeVerified());

            dto.setFamilyMemberName(driverResponse.getFamilyMemberName());
            dto.setFamilyMemberPhoneNumber(driverResponse.getFamilyMemberPhoneNumber());
            dto.setFamilyMemberVerified(driverResponse.getFamilyMemberVerified());

            dto.setDriverKycId(driverResponse.getDriverKycId());
            dto.setAadhaarNumber(driverResponse.getAadhaarNumber());
            dto.setDrivingLicenseNumber(driverResponse.getDrivingLicenseNumber());
            dto.setRcCopy(driverResponse.getRcCopy());
        }

        /*==========================================================
         = Driver Address Details (FM Database)
         ==========================================================*/
        if (address != null) {

            dto.setAddressId(address.getAddressId());
            dto.setBuildingNumber(address.getBuildingNumber());
            dto.setRoad(address.getRoad());
            dto.setLandmark(address.getLandmark());

            dto.setStateName(address.getStateName());
            dto.setCityName(address.getCityName());
            dto.setAreaName(address.getAreaName());
        }
        return  dto;
}}