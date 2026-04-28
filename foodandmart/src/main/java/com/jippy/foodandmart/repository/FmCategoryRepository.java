package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FmCategoryRepository extends JpaRepository<FmCategory, Integer> {
    Optional<FmCategory> findByCategoryNameIgnoreCase(String categoryName);
    boolean existsByCategoryNameIgnoreCase(String categoryName);
}
