/*
package com.jippy.customerandorder.mapper;


import com.jippy.customerandorder.dto.*;
import com.jippy.customerandorder.entity.*;
import com.jippy.customerandorder.exception.CoBadRequestException;
import com.jippy.customerandorder.projection.CoDriverOrderHistoryProjection;
import com.jippy.customerandorder.projection.CoDriverTotalEarningsProjection;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
        if (dto.getAadharNumber() == null && dto.getDrivingLicenseNumber() == null && dto.getRcCopy() == null) {
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
//    For post and update driver details API, we will use this method to convert entity
//    to dto and set address details through feign client response
// used for updating driver details and getting
// driver details with address details through feign client response
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
//        new feild // encrypted
        dto.setPassword("************");
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
        if (coAddressRequestDtoFeign != null) {
            dto.setBuildingNumber(coAddressRequestDtoFeign.getBuildingNumber());
            dto.setRoad(coAddressRequestDtoFeign.getRoad());
            dto.setLandmark(coAddressRequestDtoFeign.getLandmark());
            dto.setCityId(coAddressRequestDtoFeign.getCityId());
            dto.setStateId(coAddressRequestDtoFeign.getStateId());
            dto.setAreaId(coAddressRequestDtoFeign.getAreaId());

        }

        return dto;


    }

    public static CoZone mapToZoneEntity(CoZoneDto zoneDto, Polygon polygon) {
        CoZone zone = new CoZone();
        zone.setZoneName(zoneDto.getZoneName());
        zone.setBoundary(polygon);
        zone.setCreatedAt(LocalDateTime.now());
        zone.setCreatedBy(zoneDto.getCreatedBy());
        return zone;
    }

    // Update editable driver fields only
    public static void updateDriverEntity(CoDriver existingDriver, CoDriverDto dto) {

        // Update allowed only this fields
        existingDriver.setFirstName(dto.getFirstName());
        existingDriver.setLastName(dto.getLastName());

        existingDriver.setNomineeName(dto.getNomineeName());
        existingDriver.setNomineePhoneNumber(dto.getNomineePhoneNumber());
        existingDriver.setIsNomineeVerified(dto.getIsNomineeVerified());

        existingDriver.setFamilyMemberName(dto.getFamilyMemberName());
        existingDriver.setFamilyMemberPhoneNumber(dto.getFamilyMemberPhoneNumber());
        existingDriver.setIsFamilyMemberVerified(dto.getIsFamilyMemberVerified());

        existingDriver.setUpdatedAt(LocalDateTime.now());
        existingDriver.setUpdatedBy(1);

        // restricting updation of 5 feilds as per requirement
        // phoneNumber,email,kyc details(more 3 feilds namely aadharNumber , drivingLicenseNumber, rcCopy)
        // should not be updated through this method

    }

    //    for api to fetchOrderEarningsHistory mapper
// Convert projection data to DTO
    public static CoDriverOrderHistoryDto mapToDriverOrderHistoryDto(CoDriverOrderHistoryProjection projection, String outletName) {

        // Create DTO object
        CoDriverOrderHistoryDto dto = new CoDriverOrderHistoryDto();

        // Set driver id
        dto.setDriverId(projection.getDriverId());

        // Set order id
        dto.setOrderId(projection.getOrderId());

        // Set pick up distance
        dto.setPickUpDistanceInKms(projection.getPickUpDistanceInKms());

        // Set delivery distance
        dto.setDeliveryDistanceInKms(projection.getDeliveryDistanceInKms());

        // Set pick up charges
        dto.setPickUpCharges(projection.getPickUpCharges());

        // Set delivery charges
        dto.setDeliverCharges(projection.getDeliverCharges());

        // Set total delivery fee
        dto.setTotalDeliveryFee(projection.getTotalDeliveryFee());

        // Set surge fee
        dto.setSurgeFee(projection.getSurgeFee());

        // Set tips
        dto.setTips(projection.getTips());

        // Set order status
        dto.setOrderStatus(projection.getOrderStatus());

        // Set outlet name
        dto.setOutletName(outletName);

        return dto;
    }

    // Convert projection to DTO for api fetchTotalEarnings
    public static CoDriverTotalEarningsDto mapToTotalEarningsDto(Integer driverId, CoDriverTotalEarningsProjection projection, Long rejectedOrders) {

        CoDriverTotalEarningsDto dto = new CoDriverTotalEarningsDto();

        dto.setDriverId(driverId);

        dto.setTotalPickUpCharges(projection.getTotalPickUpCharges());

        dto.setTotalDeliveryCharges(projection.getTotalDeliveryCharges());

        dto.setTotalTips(projection.getTotalTips());

        dto.setTotalSurgeFee(projection.getTotalSurgeFee());

        dto.setTotalEarnings(projection.getTotalEarnings());

        dto.setCompletedOrders(projection.getCompletedOrders());

        dto.setRejectedOrders(rejectedOrders);

//    total_orders = completed orders(from driver_orders table) + rejected orders (rejection_order table)
        dto.setTotalOrders(projection.getCompletedOrders() + rejectedOrders);

        return dto;
    }

    //    this method will calculate incentive bonus for driver based on slabs defined in
//    CoDriverIncentiveSettings table and total orders count for the day
// ex :9 ≤ 10 < 15 → TRUE --> bonus = 120 assigned from table incentive amount
//Find correct slab → we just assign its final value not adding Find all slabs → sum values
    public static BigDecimal calculateIncentiveBonus(List<CoDriverIncentiveSettings> slabs, Integer orders) {

//       initialize bonus to zero
        BigDecimal bonus = BigDecimal.ZERO;

        for (int i = 0; i < slabs.size(); i++) {

            CoDriverIncentiveSettings current = slabs.get(i);

            // Case 1: Last slab (no upper bound)
            // comparing 2 slabs
            if (i == slabs.size() - 1) {

                if (orders >= current.getOrdersCount()) {
//                    Pick ONE slab → assign its value
                    bonus = current.getIncentiveAmount();
                    break; // no need to check further as this is the last slab
                }

            } else {

                CoDriverIncentiveSettings next = slabs.get(i + 1);

                // Case 2: Range check (current ≤ orders < next)
//                 As 9(current slab) ≤ 10 < 15(next slab)
                if (orders >= current.getOrdersCount() && orders < next.getOrdersCount()) {

                    bonus = current.getIncentiveAmount();
                    break; // slab found, exit loop
                }
            }
        }

        return bonus;
    }
    public static CoDriverOrder mapToDriverOrderEntity(CoDriverOrderDto driverOrderDto, CoDriver driver) {

        CoDriverOrder driverOrder = new CoDriverOrder();
        driverOrder.setOrderId(driverOrderDto.getOrderId());
        driverOrder.setDriver(driver);
        driverOrder.setDeliverCharges(driverOrderDto.getDeliverCharges());
        driverOrder.setTips(driverOrderDto.getTips());
        driverOrder.setDeliveryDistanceInKms(driverOrderDto.getDeliveryDistanceInKms());
        driverOrder.setPickUpCharges(driverOrderDto.getPickUpCharges());
        driverOrder.setPickUpDistanceInKms(driverOrderDto.getPickUpDistanceInKms());
        BigDecimal totalDeliveryFee = driverOrderDto.getDeliverCharges().add(driverOrderDto.getPickUpCharges()).
                add( driverOrderDto.getTips()).add(driverOrderDto.getSurgeFee());
        driverOrder.setTotalDeliveryFee(totalDeliveryFee);
        driverOrder.setSurgeFee(driverOrderDto.getSurgeFee());
        driverOrder.setCreatedAt(java.time.LocalDateTime.now());
        driverOrder.setCreatedBy(driverOrderDto.getDriverId());

        return driverOrder;
    }

    public static CoDriverWalletTransactions mapToTransaction(Integer driverWalletId, String orderId, double orderAmount) {
        CoDriverWalletTransactions txn = new CoDriverWalletTransactions();

        // Wallet reference
        txn.setDriverWalletId(driverWalletId);

        // Order reference
        txn.setOrderId(orderId);

        // COD deducted amount
        txn.setCodAmount(BigDecimal.valueOf(orderAmount));

        // Audit field
        txn.setCreatedAt(LocalDateTime.now());

        return txn;
    }

}
*/
