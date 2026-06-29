package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.dto.FmFavoriteOutletRequestDto;
import com.jippy.foodandmart.dto.FmFavoriteOutletResponseDto;
import com.jippy.foodandmart.dto.FmFavoriteOutletWrapperDto;
import com.jippy.foodandmart.entity.FmFavoriteOutlet;
import com.jippy.foodandmart.entity.FmOutlet;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.feignClients.CustomerAndOrderFeignClient;
import com.jippy.foodandmart.mapper.FmFavoriteOutletMapper;
import com.jippy.foodandmart.repository.FmFavoriteOutletRepository;
import com.jippy.foodandmart.repository.FmOutletRepository;
import com.jippy.foodandmart.service.FmFavoriteOutletService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FmFavoriteOutletServiceImpl implements FmFavoriteOutletService {

    private static final Logger logger = LoggerFactory.getLogger(FmFavoriteOutletServiceImpl.class);

    private final FmFavoriteOutletRepository repository;

    private final CustomerAndOrderFeignClient feignClient;

    private final FmOutletRepository outletRepository;

    //    changed for production
    @Override
    @Transactional
    public FmFavoriteOutletResponseDto toggleFavorite(FmFavoriteOutletRequestDto dto) {

        logger.info("Favorite toggle request received for customerId: {} and outletId: {}",
                dto.getCustomerId(), dto.getOutletId());

//        outletRepository.findById(dto.getOutletId())
//                .orElseThrow(() ->
//                        new ResourceNotFoundException(
//                                "Outlet not found with id: " + dto.getOutletId()));

//
        // Check whether this outlet is already marked as favorite by the customer
        Optional<FmFavoriteOutlet> existing = repository.findByCustomerIdAndOutletId(
                        dto.getCustomerId(),
                        dto.getOutletId());

        /*
         * UI Behavior:
         *  First Click(CLICK HEART SYMBOL)  -> Add to Favorites
         *  Second Click(UN_CLICK HEART SYMBOL) -> Remove from Favorites
         *
         * If record exists in DB:
         * Customer is clicking the filled heart icon again,
         * so remove the favorite record and return isFavourite = false.
         */
        if (existing.isPresent()) {

            FmFavoriteOutlet entity = existing.get();

            logger.info("Favorite outlet already exists. Removing favorite for customerId: {} and outletId: {}",
                    dto.getCustomerId(), dto.getOutletId());

//            deleting the ROW in the DB but not in Java memory we use entity obj for mapper
            repository.delete(entity);

            FmFavoriteOutletResponseDto response =
                    FmFavoriteOutletMapper.toFavOutletDto(entity);

            // Indicates to UI that heart icon should be unfilled
            response.setIsFavourite(false);

            logger.info("Favorite outlet removed successfully for customerId: {} and outletId: {}",
                    dto.getCustomerId(), dto.getOutletId());

            return response;
        }

        /*
         * Record does not exist in DB.(if not present in DB)
         * Customer is clicking the empty heart icon,
         * so create a new favorite record and return isFavourite = true.
         */
        logger.info("Favorite outlet not found. Adding favorite for customerId: {} and outletId: {}",
                dto.getCustomerId(), dto.getOutletId());

//       for inserting record ->DTO setting to entity(DB)
        FmFavoriteOutlet entity =
                FmFavoriteOutletMapper.toFavOutletEntity(dto);

//        Inserted record in DB
        FmFavoriteOutlet saved = repository.save(entity);

//        for response --> entity(saved) setting to DTO
        FmFavoriteOutletResponseDto response =
                FmFavoriteOutletMapper.toFavOutletDto(saved);

        // Indicates to UI that heart icon should be filled
        response.setIsFavourite(true);

        logger.info("Favorite outlet added successfully for customerId: {} and outletId: {}",
                dto.getCustomerId(), dto.getOutletId());

        return response;
    }

//    changed for production
//    @Override
//    public void removeFavorite(Integer customerId, Integer outletId) {
//
//        logger.info("Removing favorite outlet for customerId: {}", customerId);
//
//        Optional<FmFavoriteOutlet> existing = repository.findByCustomerIdAndOutletId(customerId, outletId);
//
//        if (!existing.isPresent()) {
//            throw new ResourceNotFoundException("your Favorite Outlet not found");
//        }
//
//        repository.delete(existing.get());
//    }


    /**
     * Builds a complete outlet response for Favorites, Frequent and Recent sections.
     *
     * Scenarios Supported:
     * 1. Favorites + Frequent + Recent
     * 2. Favorites but No Order History
     * 3. Order History but No Favorites
     */
    //    HELPER METHOD- 1 /RE_USABLE Function
    private FmFavoriteOutletResponseDto buildOutletResponse(
            Integer customerId, Integer outletId) {

//        First it creates an empty DTO.
        FmFavoriteOutletResponseDto dto = new FmFavoriteOutletResponseDto();

        // Customer & Outlet Details
        dto.setCustomerId(customerId);
        dto.setOutletId(outletId);

        // Reuse common outlet details mapping
        setOutletDetails(dto, outletId);

        // Check whether this outlet is already marked as favourite from favourites table
        Optional<FmFavoriteOutlet> favourite =
                repository.findByCustomerIdAndOutletId(customerId, outletId);

        if (favourite.isPresent()) {

            dto.setFavoriteOutletId(favourite.get().getFavoriteOutletsId());
            dto.setCreatedAt(favourite.get().getCreatedAt());
            dto.setIsFavourite(true);
        } else {
            dto.setIsFavourite(false);

        }

        return dto;
    }
    //    HELPER METHOD- 2 /RE_USABLE Function
    private void setOutletDetails(FmFavoriteOutletResponseDto dto, Integer outletId) {

        FmOutlet outlet = outletRepository.findById(outletId).orElse(null);

        if (outlet != null) {
            dto.setOutletName(outlet.getOutletName());
            dto.setOutletPicUrl(outlet.getOutletPicUrl());
            dto.setReview(outlet.getReview());

        }
    }


    //    returning list of favorite outlets for a customer based on the customerId
//   response + wrapper class to return list of favorite outlets for a customer
    @Override
    public FmFavoriteOutletWrapperDto getFavorites(Integer customerId) {
        /*
         * UI Response Scenarios:
         *
         * 1. Favorites + Frequent + Recent:
         *    Returns complete details for favorite, frequent, and recent outlets.
         *
         * 2. Favorites but No Order History:
         *    Returns favorite outlets, an empty frequentOutlets list, and recentOutlet as null.
         *
         * 3. Order History but No Favorites:
         *    Returns an empty favorites list along with frequent and recent outlet details.
         */

        logger.info("Fetching favorites for customerId: {}", customerId);

//        ONE CUSTOMER CAN HAVE MULTIPLE FAVORITE OUTLETS, SO WE ARE USING LIST
        // 1. Get favorites from FM DB
        List<FmFavoriteOutlet> list = repository.findByCustomerId(customerId);

        if (list.isEmpty()) {
            throw new ResourceNotFoundException("No favorite outlets found for customerId: " + customerId);
        }

//        to store the favorite outlets in the response dto list, we are using arraylist
        List<FmFavoriteOutletResponseDto> favorites = new ArrayList<>();

// -------------------------------------------------
        for (FmFavoriteOutlet entity : list) {

            FmFavoriteOutletResponseDto dto =
                    FmFavoriteOutletMapper.toFavOutletDto(entity);

            // Reuse common outlet details mapping
            setOutletDetails(dto, entity.getOutletId());
            favorites.add(dto);
        }
        // -------------------------------------------------

        // 2. Get frequent outlets from CO
        //        List<Integer> frequentOutlets = feignClient.getFrequentOutlets(customerId);
        //        if (frequentOutlets == null) {
        //            frequentOutlets = new ArrayList<>();
        //        }
        // ------------------------------------------------------------
        // Fetch Frequent Outlets
        // If customer has no frequent orders,
        // return an empty list instead of failing the API.
        // ------------------------------------------------------------

        List<Integer> frequentOutletIds = new ArrayList<>();

        try {
            List<Integer> ids = feignClient.getFrequentOutlets(customerId);

            if (ids != null) {
                frequentOutletIds.addAll(ids);
            }
        } catch (Exception e) {

            logger.info("No frequent outlets found for customerId: {}", customerId);

        }

        List<FmFavoriteOutletResponseDto> frequentOutlets = new ArrayList<>();

        for (Integer outletId : frequentOutletIds) {

            // Build complete outlet response for this outlet
            FmFavoriteOutletResponseDto frequentOutlet = buildOutletResponse(customerId, outletId);

            // Add it to the frequent outlets list
            frequentOutlets.add(frequentOutlet);

        }
//  ---------------------------------------------------------------------------
        //  3. Get recent outlet from CO
//        Integer recentOutlet = feignClient.getRecentOutlet(customerId);
        // ------------------------------------------------------------
        // Fetch Recent Outlet
        // If customer has no recent orders,
        // return recentOutlet as null instead of throwing an exception.
        // ------------------------------------------------------------

        Integer recentOutletId = null;

        try {
            recentOutletId = feignClient.getRecentOutlet(customerId);

        } catch (Exception e) {

            logger.info("No recent outlet found for customerId: {}", customerId);

        }

        FmFavoriteOutletResponseDto recentOutlet = null;

        if (recentOutletId != null) {

            // Build complete response for recent outlet
            recentOutlet = buildOutletResponse(customerId, recentOutletId);

        }

        // 4. Prepare response
        FmFavoriteOutletWrapperDto response = new FmFavoriteOutletWrapperDto();

        response.setFavorites(favorites);

        response.setFrequentOutlets(frequentOutlets);

        response.setRecentOutlet(recentOutlet);

        return response;

    }
 }
