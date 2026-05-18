package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmReviews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewsRepository extends JpaRepository<FmReviews,Integer> {

}
