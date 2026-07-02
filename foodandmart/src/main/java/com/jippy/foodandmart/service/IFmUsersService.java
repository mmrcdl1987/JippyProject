package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmCreateEmployeeDto;
import com.jippy.foodandmart.dto.FmPasswordResetByAdminRequestDto;
import com.jippy.foodandmart.dto.FmUserDto;
import com.jippy.foodandmart.dto.FmUserResponseDto;
import com.jippy.foodandmart.entity.FmUser;

import java.util.List;

public interface IFmUsersService {
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
