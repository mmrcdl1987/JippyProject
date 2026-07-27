package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmCreateEmployeeDto;
import com.jippy.foodandmart.dto.FmPasswordResetByAdminRequestDto;
import com.jippy.foodandmart.dto.FmUserDto;
import com.jippy.foodandmart.dto.FmUserResponseDto;
import com.jippy.foodandmart.entity.FmUser;

import java.util.List;

public interface IFmUsersService {

    /**
     * Activates the User after successful Entity Approval.
     *
     * <p>
     * Business Rules:
     *
     * 1. Supports OUTLET, MERCHANT and DRIVER.
     * 2. Finds the User using Entity Id and Entity Type.
     * 3. Changes User Status from N to Y.
     * 4. Stores the Approver Id in updated_by.
     *
     * @param entityType Entity Type
     * @param entityId   Entity Id
     * @param approverId Approver Id
     */
    void activateUser(
            String entityType,
            Integer entityId,
            Integer approverId);
    // -------------------------------
    // DEACTIVATE DRIVER
    // -------------------------------
    void deactivateDriver(Integer userId);

    // for creating user in FM microservice
    FmUserDto createUser(FmUserDto dto);

//     for api -passwordResetByAdminForRoles
    String passwordResetByAdminForRoles( FmPasswordResetByAdminRequestDto dto);

    FmUserDto findByUserIdAndUserType(Integer userId, String userType);

    void assignRolesToUser(Integer userId, List<Integer> roleIds);

    List<FmUserResponseDto> getAllUsers();

    List<Integer> getUserRoleIds(Integer userId);

    void createEmployee(FmCreateEmployeeDto dto);
}
