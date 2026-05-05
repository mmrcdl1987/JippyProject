package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmOutletCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FmOutletCategoryRepository extends JpaRepository<FmOutletCategory, Integer> {
    List<FmOutletCategory> findByOutletId(Integer outletId);
    Optional<FmOutletCategory> findByOutletIdAndCategoryId(Integer outletId, Integer categoryId);
    boolean existsByOutletIdAndCategoryId(Integer outletId, Integer categoryId);
}
