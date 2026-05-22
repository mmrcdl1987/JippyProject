package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.FmFavoriteOutletRequestDto;
import com.jippy.foodandmart.dto.FmFavoriteOutletResponseDto;
import com.jippy.foodandmart.dto.FmFavoriteOutletWrapperDto;
import com.jippy.foodandmart.service.FmFavoriteOutletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/fm/customer/favorites")
@Tag(name = "FM Customer Favorite Outlet API", description = "Manage favorite outlets")
public class FmFavoriteOutletController {

    private static final Logger logger = LoggerFactory.getLogger(FmFavoriteOutletController.class);

    private final FmFavoriteOutletService service;

    public FmFavoriteOutletController(FmFavoriteOutletService service) {
        this.service = service;
    }

    @Operation(summary = "Add favorite outlet")
    @PostMapping("/saveFavoriteOutlet")
    public FmFavoriteOutletResponseDto addFavorite(@RequestBody FmFavoriteOutletRequestDto dto) {

        logger.info("API call: Add favorite with customerId:" +
                " {} and outletId: {}", dto.getCustomerId(), dto.getOutletId());
        return service.addFavorite(dto);
    }
    @Operation(summary = "Get favorite outlets for a customer")
    @GetMapping("/getFavoriteRecentFrequentOutlets")
    public FmFavoriteOutletWrapperDto getFavorites(@RequestParam Integer customerId) {

        logger.info("API call: Favourites = Get favorites,Recent,Frequent with customerId: " +
                "{}", customerId);
        return service.getFavorites(customerId);
    }

    @Operation(summary = "Remove favorite outlet")
    @DeleteMapping("/removeFavoriteOutlet")
    public String removeFavorite(@RequestParam Integer customerId,
                                 @RequestParam Integer outletId) {

        logger.info("API call: Remove favorite with customerId: {} and outletId: {}", customerId, outletId);
        service.removeFavorite(customerId, outletId);
        return "Your Favourite Outlet Deleted successfully";
    }


}