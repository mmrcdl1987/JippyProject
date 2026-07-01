package com.jippy.foodandmart.controller;

import com.jippy.foodandmart.dto.FmFavoriteOutletRequestDto;
import com.jippy.foodandmart.dto.FmFavoriteOutletResponseDto;
import com.jippy.foodandmart.dto.FmFavoriteOutletWrapperDto;
import com.jippy.foodandmart.dto.FmFavoriteProductWrapperDto;
import com.jippy.foodandmart.service.FmFavoriteOutletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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


    @Operation(summary = "Toggle Favourite (Add/Remove Favourite Outlet/Product)")
    @PostMapping("/toggleFavouriteOutletOrProduct")
    public FmFavoriteOutletResponseDto toggleFavorite(
            @Valid @RequestBody FmFavoriteOutletRequestDto dto) {

        logger.info("Favourite toggle request received. customerId={}, favouriteType={}, favoriteId={}",
                dto.getCustomerId(), dto.getFavouriteType(), dto.getFavoriteId());

        return service.toggleFavorite(dto);
    }

    @Operation(summary = "Get favorite outlets for a customer")
    @GetMapping("/getFavoriteRecentFrequentOutlets")
    public FmFavoriteOutletWrapperDto getFavorites(@RequestParam Integer customerId) {

        logger.info("API call: Favourites = Get favorites,Recent,Frequent with customerId: " +
                "{}", customerId);
        return service.getFavorites(customerId);
    }

    @GetMapping("/getFavoriteProducts")
    @Operation(summary = "Get favourite products for a customer")
    public FmFavoriteProductWrapperDto getFavoriteProducts(
            @RequestParam Integer customerId){

        logger.info("Fetching favourite products for customerId={}", customerId);

        return service.getFavoriteProducts(customerId);
    }

    //changed for production
//    @Operation(summary = "Remove favorite outlet")
//    @DeleteMapping("/removeFavoriteOutlet")
//    public String removeFavorite(@RequestParam Integer customerId,
//                                 @RequestParam Integer outletId) {
//
//        logger.info("API call: Remove favorite with customerId: {} and outletId: {}", customerId, outletId);
//        service.removeFavorite(customerId, outletId);
//        return "Your Favourite Outlet Deleted successfully";
//    }


}