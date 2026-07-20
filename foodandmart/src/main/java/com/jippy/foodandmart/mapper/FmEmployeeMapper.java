package com.jippy.foodandmart.mapper;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.FmCreateEmployeeRequestDTO;
import com.jippy.foodandmart.dto.FmCreateEmployeeResponseDTO;
import com.jippy.foodandmart.entity.FmEmployee;
import com.jippy.foodandmart.entity.FmOutletAddress;
import com.jippy.foodandmart.entity.FmUser;

import java.time.LocalDateTime;

/**
 * Static utility class for creating {@link FmEmployee} entities from raw fields.
 *
 * <p>Why not accept a DTO: employees in this system are always created as a
 * side effect of merchant or outlet onboarding. There is no dedicated
 * "Create Employee" request DTO — the values come directly from the
 * merchant/outlet registration data.</p>
 */
public final class FmEmployeeMapper {

    /**
     * Private constructor — static utility class, must not be instantiated.
     */
    private FmEmployeeMapper() {}

    /**
     * Creates a new {@link FmEmployee} entity from individual field values.
     *
     * <p>Why trim name and email: user-supplied strings from registration forms
     * or file uploads may have extra whitespace. Trimming prevents duplicate
     * records that differ only by whitespace.</p>
     *
     * <p>Why lowercase email: the email column does not have a case-insensitive
     * unique index. Storing always-lowercase ensures lookups won't miss
     * records due to case differences.</p>
     *
     * <p>isActive defaults to "Y" because a newly created employee record
     * is always active — it is only deactivated via a separate admin action.</p>
     *
     * @param employeeName the employee's full name (will be trimmed)
     * @param email        the employee's email address (will be lowercased and trimmed)
     * @param mobileNumber the employee's mobile number (will be trimmed)
     * @param createdBy    the ID of the admin/user who created this record (audit trail)
     * @return a transient {@link FmEmployee} entity ready to persist
     */
    public static FmEmployee toEntity(String employeeName, String email, String mobileNumber, Integer createdBy) {
        FmEmployee entity = new FmEmployee();
        entity.setEmployeeName(employeeName != null ? employeeName.trim() : null);
        // Normalise email to lowercase for consistent storage
        entity.setEmail(email != null ? email.toLowerCase().trim() : null);
        entity.setMobileNumber(mobileNumber != null ? mobileNumber.trim() : null);
        // New employees are always active — deactivation is a separate action
        entity.setIsActive("Y");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setCreatedBy(createdBy);
        return entity;
    }

//    -----------------------------CREATE EMPLOYEE ----------------------------------------------------
    public static FmEmployee toEmployeeEntity(FmCreateEmployeeRequestDTO dto) {

        FmEmployee entity = new FmEmployee();

        entity.setEmployeeName(safe(dto.getEmployeeName()));

        entity.setEmail(safe(dto.getEmail()).toLowerCase());

        entity.setMobileNumber(safe(dto.getMobileNumber()));

        entity.setIsActive("Y");

        entity.setCreatedAt(LocalDateTime.now());

        entity.setCreatedBy(dto.getCreatedBy());

        return entity;
    }
    public static FmUser toEmployeeUserEntity(FmCreateEmployeeRequestDTO dto,
                                              Integer employeeId) {

        FmUser user = new FmUser();

        user.setUsername(safe(dto.getUsername()));

        user.setUserId(employeeId);

        user.setUserType(FmAppConstants.TYPE_EMPLOYEE);

        user.setIsActive("Y");

        return user;
    }

    public static FmOutletAddress toEmployeeAddressEntity(FmCreateEmployeeRequestDTO dto,
                                                          Integer employeeId) {

        FmOutletAddress address = new FmOutletAddress();

        address.setJippyAddressId(employeeId);

        address.setBuildingNumber(safe(dto.getBuildingNumber()));

        address.setRoad(safe(dto.getRoad()));

        address.setLandmark(safe(dto.getLandmark()));

        address.setStateId(dto.getStateId());

        address.setCityId(dto.getCityId());

        address.setAreaId(dto.getAreaId());

        address.setAddressType(FmAppConstants.TYPE_EMPLOYEE);

        return address;
    }
    public static FmCreateEmployeeResponseDTO toCreateEmployeeResponseDto
            (FmCreateEmployeeRequestDTO request,
            FmEmployee employee) {

        FmCreateEmployeeResponseDTO response = new FmCreateEmployeeResponseDTO();

        response.setEmployeeId(employee.getEmployeeId());

        response.setEmployeeName(employee.getEmployeeName());

        response.setEmail(employee.getEmail());

        response.setMobileNumber(employee.getMobileNumber());

        response.setUsername(safe(request.getUsername()));

//        password is masked
        response.setPassword("********");

        response.setBuildingNumber(safe(request.getBuildingNumber()));

        response.setRoad(request.getRoad());

        response.setLandmark(request.getLandmark());

        response.setStateId(request.getStateId());

        response.setCityId(request.getCityId());

        response.setAreaId(request.getAreaId());

        response.setCreatedBy(request.getCreatedBy());

        response.setIsActive(employee.getIsActive());

        return response;
    }

    //    Helper method for trim() trimming the Strings
    private static String safe(String value) {

        return value != null ? value.trim() : "";
    }


}
