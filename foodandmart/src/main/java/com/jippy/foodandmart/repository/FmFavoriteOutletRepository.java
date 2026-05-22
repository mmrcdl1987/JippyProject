package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmFavoriteOutlet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FmFavoriteOutletRepository extends JpaRepository<FmFavoriteOutlet, Integer> {

//    based on the customerId, find all the favorite outlets for that customer
    List<FmFavoriteOutlet> findByCustomerId(Integer customerId);

//    based on the customerId and outletId,
//    find if the outlet is already marked as favorite by the customer
    Optional<FmFavoriteOutlet> findByCustomerIdAndOutletId(Integer customerId, Integer outletId);
}