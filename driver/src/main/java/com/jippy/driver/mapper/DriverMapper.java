package com.jippy.driver.mapper;



import com.jippy.driver.constants.DConstants;
import com.jippy.driver.dto.*;
import com.jippy.driver.entity.*;
import com.jippy.driver.exception.DriverBadRequestException;
import com.jippy.driver.projection.DriverOrderHistoryProjection;
import com.jippy.driver.projection.DriverTotalEarningsProjection;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DriverMapper {

    // Convert DTO to Driver entity
    // Also handles creating and linking KYC data if present
    public static Driver mapToDriverEntity(DriverDto dto) {

        // Check if input is null
        if (dto == null) {
            throw new DriverBadRequestException("Driver DTO must not be null");
        }

        // Create Driver object and set basic details
        Driver driver = new Driver();
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
        DriverKyc driverKyc = mapToDriverKycEntity(dto);

        // Link Driver and KYC if KYC exists
        if (driverKyc != null) {
            driverKyc.setDriver(driver);     // owning side
            driver.setDriverKyc(driverKyc);  // reference side
        }

        return driver;
    }

    // Convert KYC fields from DTO to KYC entity
    // If no KYC data is provided, return null
    private static DriverKyc mapToDriverKycEntity(DriverDto dto) {

        // Safety check
        if (dto == null) {
            throw new DriverBadRequestException("Driver DTO must not be null for KYC mapping");
        }

        // If all KYC fields are empty, skip creation
        if (dto.getAadharNumber() == null && dto.getDrivingLicenseNumber() == null && dto.getRcCopy() == null) {
            return null;
        }

        // Create KYC object and set values
        DriverKyc kyc = new DriverKyc();
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
    public static DriverDto mapToDriverDto(Driver driver, DriverAddressRequestDto coAddressRequestDtoFeign) {

        // Check if entity is null
        if (driver == null) {
            throw new DriverBadRequestException("Driver entity must not be null");
        }

        DriverDto dto = new DriverDto();

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

    public static DriverZone mapToZoneEntity(DriverZoneDto zoneDto, MultiPolygon multiPolygon) {
        DriverZone zone = new DriverZone();
        zone.setZoneName(zoneDto.getZoneName());
        zone.setBoundary(multiPolygon);
        zone.setCreatedAt(LocalDateTime.now());
        zone.setCreatedBy(zoneDto.getCreatedBy());
        zone.setZoneType(zoneDto.getZoneType());
        return zone;
    }

    // Update editable driver fields only
    public static void updateDriverEntity(Driver existingDriver, DriverDto dto) {

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
//    public static DriverOrderHistoryDto mapToDriverOrderHistoryDto(DriverOrderHistoryProjection projection, String outletName) {
//
//        // Create DTO object
//        DriverOrderHistoryDto dto = new DriverOrderHistoryDto();
//
//        // Set driver id
//        dto.setDriverId(projection.getDriverId());
//
//        // Set order id
//        dto.setOrderId(projection.getOrderId());
//
//        // Set pick up distance
//        dto.setPickUpDistanceInKms(projection.getPickUpDistanceInKms());
//
//        // Set delivery distance
//        dto.setDeliveryDistanceInKms(projection.getDeliveryDistanceInKms());
//
//        // Set pick up charges
//        dto.setPickUpCharges(projection.getPickUpCharges());
//
//        // Set delivery charges
//        dto.setDeliverCharges(projection.getDeliverCharges());
//
//        // Set total delivery fee
//        dto.setTotalDeliveryFee(projection.getTotalDeliveryFee());
//
//        // Set surge fee
//        dto.setSurgeFee(projection.getSurgeFee());
//
//        // Set tips
//        dto.setTips(projection.getTips());
//
//        // Set order status
//        dto.setOrderStatus(projection.getOrderStatus());
//
//        // Set outlet name
//        dto.setOutletName(outletName);
//
//        return dto;
//    }

    public static DriverOrderHistoryDto mapToDriverOrderHistoryDto(
            DriverOrderHistoryProjection projection,
            String orderStatus,
            String outletName
    ) {

        DriverOrderHistoryDto dto =
                new DriverOrderHistoryDto();

        // Set driver id
        dto.setDriverId(
                projection.getDriverId());

        // Set order id
        dto.setOrderId(
                projection.getOrderId());

        // Set pick up distance
        dto.setPickUpDistanceInKms(
                projection.getPickUpDistanceInKms());

        // Set delivery distance
        dto.setDeliveryDistanceInKms(
                projection.getDeliveryDistanceInKms());

        // Set pick up charges
        dto.setPickUpCharges(
                projection.getPickUpCharges());

        // Set delivery charges
        dto.setDeliverCharges(
                projection.getDeliverCharges());

        // Set total delivery fee
        dto.setTotalDeliveryFee(
                projection.getTotalDeliveryFee());

        // Set surge fee
        dto.setSurgeFee(
                projection.getSurgeFee());

        // Set tips
        dto.setTips(
                projection.getTips());

//        set created at timestamp
        dto.setCreatedAt(
                projection.getCreatedAt());

//--------------------------------------------------------------------
        // from Customer Ms
        dto.setOrderStatus(orderStatus);

        // from Fm Ms
        dto.setOutletName(outletName);

        return dto;
    }

    // Convert projection to DTO for api fetchTotalEarnings
    public static DriverTotalEarningsDto mapToTotalEarningsDto(Integer driverId, DriverTotalEarningsProjection projection, Long rejectedOrders) {

        DriverTotalEarningsDto dto = new DriverTotalEarningsDto();

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
    public static BigDecimal calculateIncentiveBonus(List<DriverIncentiveSettings> slabs, Integer orders) {

//       initialize bonus to zero
        BigDecimal bonus = BigDecimal.ZERO;

        for (int i = 0; i < slabs.size(); i++) {

            DriverIncentiveSettings current = slabs.get(i);

            // Case 1: Last slab (no upper bound)
            // comparing 2 slabs
            if (i == slabs.size() - 1) {

                if (orders >= current.getOrdersCount()) {
//                    Pick ONE slab → assign its value
                    bonus = current.getIncentiveAmount();
                    break; // no need to check further as this is the last slab
                }

            } else {

                DriverIncentiveSettings next = slabs.get(i + 1);

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
    public static DriverOrder mapToDriverOrderEntity(DriverOrderDto driverOrderDto, Driver driver) {

        DriverOrder driverOrder = new DriverOrder();
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
        driverOrder.setCreatedAt(LocalDateTime.now());
        driverOrder.setCreatedBy(driverOrderDto.getDriverId());

        return driverOrder;
    }

    public static DriverWalletTransactions mapToTransaction(Integer driverWalletId,
                        String orderId, double orderAmount,Integer driverId) {
        DriverWalletTransactions txn = new DriverWalletTransactions();

        // Wallet reference
        txn.setDriverWalletId(driverWalletId);

        // Order reference
        txn.setOrderId(orderId);

        // COD deducted amount
        txn.setCodAmount(BigDecimal.valueOf(orderAmount));

        // Audit field
        txn.setCreatedAt(LocalDateTime.now());
        txn.setCreatedBy(driverId);

//
        txn.setTransactionType(DConstants.TransactionType_debit);

        return txn;
    }

    public static DriverIncentiveHistory mapToDriverIncentiveHistory(
            DriverIncentiveHistory existingHistory,
            Integer driverId,
            LocalDate date,
            Integer orders,
            BigDecimal bonus) {

        DriverIncentiveHistory history;

        if (existingHistory != null) {

            history = existingHistory;

            history.setIncentiveAmount(bonus);
            history.setCompletedOrdersCount(orders);

            history.setUpdatedAt(LocalDateTime.now());
            history.setUpdatedBy(driverId);

        } else {

            history = new DriverIncentiveHistory();

            history.setDriverId(driverId);
            history.setCurrDate(date);
            history.setIncentiveAmount(bonus);
            history.setCompletedOrdersCount(orders);

            history.setCreatedAt(LocalDateTime.now());
            history.setCreatedBy(driverId);
        }

        return history;
    }

    /**
     * Convert Driver entity to FM Driver Approval Response DTO.
     * Used by the FM service during the Level-1 approval process.
     */
    public static FmDriverApprovalResponseDTO mapToDriverApprovalResponseDto(Driver driver) {

        if (driver == null) {
            throw new DriverBadRequestException("Driver entity must not be null");
        }

        FmDriverApprovalResponseDTO dto = new FmDriverApprovalResponseDTO();

        // Driver Details
        dto.setDriverId(driver.getDriverId());
        dto.setFirstName(driver.getFirstName());
        dto.setLastName(driver.getLastName());
        dto.setPhoneNumber(driver.getPhoneNumber());
        dto.setEmail(driver.getEmail());
        dto.setProfilePicUrl(driver.getProfilePicUrl());

        // Nominee Details
        dto.setNomineeName(driver.getNomineeName());
        dto.setNomineePhoneNumber(driver.getNomineePhoneNumber());
        dto.setNomineeVerified(driver.getIsNomineeVerified());

        // Family Member Details
        dto.setFamilyMemberName(driver.getFamilyMemberName());
        dto.setFamilyMemberPhoneNumber(driver.getFamilyMemberPhoneNumber());
        dto.setFamilyMemberVerified(driver.getIsFamilyMemberVerified());

        // Driver KYC Details
        if (driver.getDriverKyc() != null) {
            dto.setDriverKycId(driver.getDriverKyc().getDriverKycId());
            dto.setAadhaarNumber(driver.getDriverKyc().getAadharNumber());
            dto.setDrivingLicenseNumber(driver.getDriverKyc().getDrivingLicenseNumber());
            dto.setRcCopy(driver.getDriverKyc().getRcCopy());
        }

        return dto;
    }

}
