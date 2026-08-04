package com.jippy.foodandmart.controller;


import com.jippy.foodandmart.dto.FmAssignRoleToUserDto;
import com.jippy.foodandmart.dto.FmCreateEmployeeDto;
import com.jippy.foodandmart.dto.FmPasswordResetByAdminRequestDto;
import com.jippy.foodandmart.dto.FmUserDto;
import com.jippy.foodandmart.service.IFmUsersService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@Validated
@RequestMapping("/api/fm/users")
public class FmUsersController {

    @Autowired
    private IFmUsersService usersService;

    // -------------------------------
    // API For: DEACTIVATE DRIVER
    // -------------------------------
    @PostMapping("/deactivateDriver")
    public String deactivateDriver( @Positive(message = "User Id must be greater than zero")
                                               @RequestParam Integer userId) {

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
           @Valid @RequestBody FmPasswordResetByAdminRequestDto dto) {

        log.info("Password reset request received for username: {}", dto.getUsername());

        return ResponseEntity.ok(usersService.passwordResetByAdminForRoles(dto));

    }


    @GetMapping("/findByUserIdAndUserType")
    public ResponseEntity<FmUserDto> findByUserIdAndUserType(@RequestParam Integer userId,
                                                             @RequestParam String userType) {
        return ResponseEntity.ok(usersService.findByUserIdAndUserType(userId,userType));
    }
    @PostMapping("/assignRole")
    public String assignRoleToUser(@RequestBody FmAssignRoleToUserDto dto) {

        log.info("Received Assign Role Request");

        log.info("User Id : {}", dto.getUserId());

        log.info("Role Id : {}", dto.getRoleIds());

        usersService.assignRolesToUser(dto.getUserId(), dto.getRoleIds());

        log.info("Role assigned successfully to User Id : {}", dto.getUserId());

        return "Role Assigned Successfully";
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(usersService.getAllUsers());

    }

    @GetMapping("/{userId}/roles")
    public ResponseEntity<List<Integer>> getUserRoles(@PathVariable Integer userId) {
        return ResponseEntity.ok(usersService.getUserRoleIds(userId));
    }

    @PostMapping("/createEmployee")
    public ResponseEntity<?> createEmployee(@Valid @RequestBody FmCreateEmployeeDto dto) {

        log.info("Create Employee Request Received | username={} | email={}", dto.getUsername(), dto.getEmail());

        usersService.createEmployee(dto);

        log.info("Create Employee Request Completed | username={}", dto.getUsername());

        return ResponseEntity.ok("Employee Created Successfully");
    }



}