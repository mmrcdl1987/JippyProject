package com.jippy.foodandmart.controller;


import com.jippy.foodandmart.dto.FmUserDto;
import com.jippy.foodandmart.entity.FmUser;
import com.jippy.foodandmart.mapper.FmMerchantMapper;
import com.jippy.foodandmart.service.IFmUsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}