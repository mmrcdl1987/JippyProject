package com.jippy.foodandmart.controller;


import com.jippy.foodandmart.dto.FmPasswordResetByAdminRequestDto;
import com.jippy.foodandmart.dto.FmUserDto;
import com.jippy.foodandmart.service.IFmUsersService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/fm/users")
public class FmUsersController {

    @Autowired
    private IFmUsersService usersService;

    // -------------------------------
    // API For: DEACTIVATE DRIVER
    // -------------------------------
    @PostMapping("/deactivateDriver")
    public String deactivateDriver(@RequestParam Integer userId) {

        usersService.deactivateDriver(userId);

        return "Driver deactivated successfully in FM microservice Users table .";
    }

//    for creating user in FM microservice, we will receive the user details from CO microservice
//    and then we will save the user details in FM microservice users table
    @PostMapping("/createUser")
    public ResponseEntity<FmUserDto> createUser(@RequestBody FmUserDto dto) {
        return ResponseEntity.ok(usersService.createUser(dto));
    }

    @PostMapping("/passwordResetByAdminForRoles")
    @Operation(summary = "Reset Password By Admin", description =
                    "Allows admin to reset password for an existing user" +
                            " by username and user type:1)DRIVER,2)MERCHANT,3)OUTLET s")
    public ResponseEntity<String> passwordResetByAdminForRoles(
            @RequestBody FmPasswordResetByAdminRequestDto dto) {

        log.info("Password reset request received for username: {}", dto.getUsername());

        return ResponseEntity.ok(usersService.passwordResetByAdminForRoles(dto));

    }


    @GetMapping("/findByUserIdAndUserType")
    public ResponseEntity<FmUserDto> findByUserIdAndUserType(@RequestParam Integer userId,@RequestParam String userType) {
        return ResponseEntity.ok(usersService.findByUserIdAndUserType(userId,userType));
    }




}