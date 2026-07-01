package com.jippy.foodandmart.serviceImpl;

import com.jippy.foodandmart.constants.FmAppConstants;
import com.jippy.foodandmart.dto.*;
import com.jippy.foodandmart.entity.FmFavoriteOutlet;
import com.jippy.foodandmart.entity.FmOutlet;
import com.jippy.foodandmart.entity.FmOutletCategory;
import com.jippy.foodandmart.entity.FmProduct;
import com.jippy.foodandmart.exception.ResourceNotFoundException;
import com.jippy.foodandmart.feignClients.CustomerAndOrderFeignClient;
import com.jippy.foodandmart.mapper.FmFavoriteOutletMapper;
import com.jippy.foodandmart.repository.*;
import com.jippy.foodandmart.service.FmFavoriteOutletService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    private final FmProductRepository productRepository;

    private final FmPricingRepository pricingRepository;

    private final FmOutletCategoryRepository outletCategoryRepository;

    //    changed for production
    @Override
    @Transactional
    public FmFavoriteOutletResponseDto toggleFavorite(FmFavoriteOutletRequestDto dto) {

        logger.info("Favorite toggle request received for customerId={}, favouriteType={}, favoriteId={}",
                dto.getCustomerId(), dto.getFavouriteType(), dto.getFavoriteId());

//        outletRepository.findById(dto.getOutletId())
//                .orElseThrow(() ->
//                        new ResourceNotFoundException(
//                                "Outlet not found with id: " + dto.getOutletId()));

//        ------------------------------------------------------------------------------------------
        /*
         * Validate whether the favourite item exists.
         * OUTLET -> Validate in outlets table.
         * PRODUCT -> Validate in products table.
         */
        if (FmAppConstants.TYPE_OUTLET.equalsIgnoreCase(dto.getFavouriteType())) {

            outletRepository.findById(dto.getFavoriteId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                                    "Outlet not found with id: " + dto.getFavoriteId()));

        } else if (FmAppConstants.TYPE_PRODUCT.equalsIgnoreCase(dto.getFavouriteType())) {

            productRepository.findById(dto.getFavoriteId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                                    "Product not found with id: " + dto.getFavoriteId()));

        } else {
            throw new ResourceNotFoundException("Invalid favouriteType. " +
                    "Allowed values: OUTLET or PRODUCT");
        }
//        ------------------------------------------------------------------------------------------
//        always convert to uppercase to avoid case sensitivity issues in DB queries
        String favouriteType = dto.getFavouriteType().toUpperCase();

        // Check whether this OUTLET/PRODUCT is already marked as favorite by the customer
        Optional<FmFavoriteOutlet> existing =
                repository.findByCustomerIdAndFavoriteIdAndFavouriteType(
                        dto.getCustomerId(), dto.getFavoriteId(), favouriteType);
        logger.info("Existing = {}", existing);
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

         logger.info("Favorite already exists. Removing favourite for customerId={}, favouriteType={}, favoriteId={}",
                    dto.getCustomerId(), dto.getFavouriteType(), dto.getFavoriteId());

//            deleting the ROW in the DB but not in Java memory we use entity obj for mapper
            repository.delete(entity);

            FmFavoriteOutletResponseDto response =
                    FmFavoriteOutletMapper.toFavOutletDto(entity);

            // Indicates to UI that heart icon should be unfilled
            response.setIsFavourite(false);

            logger.info("Favorite outlet removed successfully for customerId: {} and FavoriteId: {} FavoriteType: {}",
                    dto.getCustomerId(), dto.getFavoriteId(),dto.getFavouriteType());

            return response;
        }

        /*
         * Record does not exist in DB.(if not present in DB)
         * Customer is clicking the empty heart icon,
         * so create a new favorite record and return isFavourite = true.
         */
        logger.info("Favorite not found. Adding favourite for customerId={}, favouriteType={}, favoriteId={}",
                dto.getCustomerId(), dto.getFavouriteType(), dto.getFavoriteId());

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

        logger.info("Favourite added successfully for customerId={}, favouriteType={}, favoriteId={}",
                dto.getCustomerId(), dto.getFavouriteType(), dto.getFavoriteId());

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


    /** HELPER METHOD 1 FOR FAVOURITE OUTLETS API
     * Builds a complete favourite outlet response.
     *
     * Reused By:
     * 1. Favourite Outlets
     * 2. Frequent Outlets
     * 3. Recent Outlet
     *
     * Steps:
     * 1. Fetch outlet details from outlets table.
     * 2. Check whether this outlet exists in favourite_outlets table.
     * 3. Populate favourite information.
     */
    private FmFavoriteOutletResponseDto buildOutletResponse(
            Integer customerId, Integer outletId) {

        // Create response DTO
        FmFavoriteOutletResponseDto dto = new FmFavoriteOutletResponseDto();

        // Basic favourite information
        dto.setCustomerId(customerId);
        dto.setFavoriteId(outletId);
        dto.setFavouriteType(FmAppConstants.TYPE_OUTLET);

        /*
         * Fetch outlet details
         * from outlets table.
         */
        FmOutlet outlet = outletRepository.findById(outletId).orElse(null);

        if (outlet != null) {

            dto.setOutletName(outlet.getOutletName());
            dto.setOutletPicUrl(outlet.getOutletPicUrl());
            dto.setReview(outlet.getReview());

        }
        /*
         * Check whether this outlet
         * is already marked as favourite.
         */
        Optional<FmFavoriteOutlet> favourite =
                repository.findByCustomerIdAndFavoriteIdAndFavouriteType(
                        customerId, outletId, FmAppConstants.TYPE_OUTLET);
        /*
         * Populate favourite details.
         */
        if (favourite.isPresent()) {

            dto.setFavoriteOutletId(favourite.get().getFavoriteOutletsId());

//            dto.setCreatedAt(favourite.get().getCreatedAt());

            dto.setIsFavourite(true);
        } else {
            dto.setIsFavourite(false);
        }
        return dto;
    }

    /** HELPER METHOD 1 FOR FAVOURITE PRODUCTS API
     * Builds a complete favourite product response.
     *
     * Reused By:
     * 1. Favourite Products API
     *
     * Steps:
     * 1. Fetch product details.
     * 2. Fetch latest online price.
     * 3. Check favourite status.
     * 4. Populate response.
     */
    private FmFavoriteProductResponseDto buildProductResponse(
            Integer customerId,
            Integer productId) {

        // Create response DTO
        FmFavoriteProductResponseDto dto = new FmFavoriteProductResponseDto();

        // Basic favourite information
        dto.setCustomerId(customerId);
        dto.setFavoriteId(productId);
        dto.setFavouriteType(FmAppConstants.TYPE_PRODUCT);

        /*
         * Fetch product details
         * from products table.
         */
        FmProduct product = productRepository.findById(productId).orElse(null);

        if (product != null) {
            // Product Details
            dto.setProductName(product.getProductName());

            dto.setImageUrl(product.getImageLink());

            dto.setIsVeg(product.getIsVeg());

            dto.setRating(product.getRating());
            /*
             * Fetch outletId using outletCategoryId.
             */
            FmOutletCategory outletCategory =
               outletCategoryRepository.findById(product.getOutletCategoryId()).orElse(null);

            if (outletCategory != null) {
                dto.setOutletId(outletCategory.getOutletId());

            }

            /*
             * Fetch latest approved online price.
             */
            Optional<BigDecimal> onlinePrice =
                    pricingRepository.findOnlinePriceByProductIdAndOutletCategoryId(
                            product.getProductId(), product.getOutletCategoryId());

            if (onlinePrice.isPresent()) {
                dto.setOnlinePrice(onlinePrice.get());

            }
        }

        /*
         * Check whether product
         * is already marked as favourite.
         */
        Optional<FmFavoriteOutlet> favourite =
                repository.findByCustomerIdAndFavoriteIdAndFavouriteType(
                        customerId, productId, FmAppConstants.TYPE_PRODUCT);

        /*
         * Populate favourite details.
         */
        if (favourite.isPresent()) {

            dto.setFavoriteOutletId(favourite.get().getFavoriteOutletsId());

//            dto.setCreatedAt(favourite.get().getCreatedAt());

            dto.setIsFavourite(true);

        } else {
            dto.setIsFavourite(false);
        }
        return dto;
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
        List<FmFavoriteOutlet> list = repository.findByCustomerIdAndFavouriteType(
                        customerId, FmAppConstants.TYPE_OUTLET);
        if (list.isEmpty()) {
            throw new ResourceNotFoundException("No favorite outlets found for customerId: " + customerId);
        }

//        to store the favorite outlets in the response dto list, we are using arraylist
        List<FmFavoriteOutletResponseDto> favorites = new ArrayList<>();

// -------------------------------------------------
        for (FmFavoriteOutlet entity : list) {

            FmFavoriteOutletResponseDto dto = buildOutletResponse(
                            entity.getCustomerId(), entity.getFavoriteId());

            favorites.add(dto);
        }
//        ---------------------------------------------

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
            FmFavoriteOutletResponseDto frequentOutlet = buildOutletResponse(
                            customerId, outletId);

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

    @Override
    public FmFavoriteProductWrapperDto getFavoriteProducts(Integer customerId) {

        logger.info("Fetching favourite products for customerId={}", customerId);

        List<FmFavoriteOutlet> favouriteProducts =
                repository.findByCustomerIdAndFavouriteType(
                        customerId, FmAppConstants.TYPE_PRODUCT);

        if (favouriteProducts.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No favourite products found for customerId : " + customerId);
        }

        List<FmFavoriteProductResponseDto> products = new ArrayList<>();

        for (FmFavoriteOutlet favourite : favouriteProducts) {

            products.add(buildProductResponse(favourite.getCustomerId(),
                            favourite.getFavoriteId()));
        }

        FmFavoriteProductWrapperDto response = new FmFavoriteProductWrapperDto();

        response.setFavoriteProducts(products);

        return response;
    }
 }
