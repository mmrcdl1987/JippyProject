package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.dto.FmFavoriteOutletRequestDto;
import com.jippy.foodandmart.dto.FmFavoriteOutletResponseDto;
import com.jippy.foodandmart.dto.FmFavoriteOutletWrapperDto;
import com.jippy.foodandmart.entity.FmFavoriteOutlet;
import com.jippy.foodandmart.exception.DuplicateResourceException;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.feignClients.CustomerAndOrderFeignClient;
import com.jippy.foodandmart.mapper.FmFavoriteOutletMapper;
import com.jippy.foodandmart.repository.FmFavoriteOutletRepository;
import com.jippy.foodandmart.service.FmFavoriteOutletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FmFavoriteOutletServiceImpl implements FmFavoriteOutletService {

    private static final Logger logger = LoggerFactory.getLogger(FmFavoriteOutletServiceImpl.class);

    private final FmFavoriteOutletRepository repository;

    @Autowired
    private CustomerAndOrderFeignClient feignClient;

    public FmFavoriteOutletServiceImpl(FmFavoriteOutletRepository repository) {
        this.repository = repository;
    }

    @Override
    public FmFavoriteOutletResponseDto addFavorite(FmFavoriteOutletRequestDto dto) {

        logger.info("Adding favorite outlet for customerId: {}", dto.getCustomerId());

        Optional<FmFavoriteOutlet> existing = repository.findByCustomerIdAndOutletId(dto.getCustomerId(), dto.getOutletId());

        if (existing.isPresent()) {
            throw new DuplicateResourceException("Your Outlet Already marked as favorite");
        }

        FmFavoriteOutlet entity = FmFavoriteOutletMapper.toFavOutletEntity(dto);
        FmFavoriteOutlet saved = repository.save(entity);

        return FmFavoriteOutletMapper.toFavOutletDto(saved);
    }

    @Override
    public void removeFavorite(Integer customerId, Integer outletId) {

        logger.info("Removing favorite outlet for customerId: {}", customerId);

        Optional<FmFavoriteOutlet> existing = repository.findByCustomerIdAndOutletId(customerId, outletId);

        if (!existing.isPresent()) {
            throw new ResourceNotFoundException("your Favorite Outlet not found");
        }

        repository.delete(existing.get());
    }

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

        for (FmFavoriteOutlet entity : list) {
            FmFavoriteOutletResponseDto dto = FmFavoriteOutletMapper.toFavOutletDto(entity);
            favorites.add(dto);
        }

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