package com.jippy.foodandmart.service;

import com.jippy.foodandmart.dto.FmPasswordResetByAdminRequestDto;
import com.jippy.foodandmart.dto.FmUserDto;
import com.jippy.foodandmart.entity.FmUser;

public interface IFmUsersService {
    // -------------------------------
    // DEACTIVATE DRIVER
    // -------------------------------
    void deactivateDriver(Integer userId);

    // for creating user in FM microservice
    FmUserDto createUser(FmUserDto dto);

//     for api -passwordResetByAdminForRoles
    String passwordResetByAdminForRoles( FmPasswordResetByAdminRequestDto dto);
}
