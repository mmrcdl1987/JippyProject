package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.service.IFmUsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fm/users")
public class FmUsersController {

    @Autowired
    private IFmUsersService usersService;

    // -------------------------------
    // API: DEACTIVATE DRIVER
    // -------------------------------
    @PostMapping("/deactivateDriver")
    public String deactivateDriver(@RequestParam Integer userId) {

        usersService.deactivateDriver(userId);

        return "Driver deactivated successfully in FM microservice Users table .";
    }
}