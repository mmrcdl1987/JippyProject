package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmFavoriteOutlet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FmFavoriteOutletRepository extends JpaRepository<FmFavoriteOutlet, Integer> {

    /*
     * Fetch all favourite records for a customer.
     * Returns both OUTLET and PRODUCT favourites.
     */
    List<FmFavoriteOutlet> findByCustomerId(Integer customerId);

    /*
     * Check whether a specific OUTLET or PRODUCT
     * is already marked as favourite by the customer.
     */
    Optional<FmFavoriteOutlet> findByCustomerIdAndFavoriteIdAndFavouriteType(
            Integer customerId,
            Integer favoriteId,
            String favouriteType);

    /*
     * Fetch all favourites of a specific type
     * (OUTLET / PRODUCT) for a customer.
     */
    List<FmFavoriteOutlet> findByCustomerIdAndFavouriteType(
            Integer customerId,
            String favouriteType);
}