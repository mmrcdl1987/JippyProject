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


    //    returning list of favorite outlets for a customer based on the customerId
//   response + wrapper class to return list of favorite outlets for a customer
    @Override
    public FmFavoriteOutletWrapperDto getFavorites(Integer customerId) {

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

            FmOutlet outlet = outletRepository.findById(entity.getOutletId())
                    .orElse(null);

//            from Outlet table//FmOutlet Entity
            if (outlet != null) {
                dto.setOutletName(outlet.getOutletName());
                dto.setOutletPicUrl(outlet.getOutletPicUrl());
                dto.setReview(outlet.getReview());

            }

            favorites.add(dto);
        }
 // -------------------------------------------------


        // 2. Get frequent outlets from CO
        List<Integer> frequentOutlets = feignClient.getFrequentOutlets(customerId);

//        for the case when there are no frequent outlets,
//        we should return empty list [] instead of null in the response
        if (frequentOutlets == null) {
            frequentOutlets = new ArrayList<>();
        }

        //  3. Get recent outlet from CO
        // this handles the case when there are no orders at all for the customer.
//        if no order handled by global exception handler and
//        return recentOutlet as null in the response
        Integer recentOutlet = feignClient.getRecentOutlet(customerId);

            // 4. Prepare response
            FmFavoriteOutletWrapperDto response = new FmFavoriteOutletWrapperDto();

            response.setFavorites(favorites);
            response.setFrequentOutlets(frequentOutlets);
            response.setRecentOutlet(recentOutlet);

            return response;

        }
 }