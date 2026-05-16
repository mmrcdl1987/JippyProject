package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmOutletCategory;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FmOutletCategoryRepository extends JpaRepository<FmOutletCategory, Integer> {
    List<FmOutletCategory> findByOutletId(Integer outletId);
    Optional<FmOutletCategory> findByOutletIdAndCategoryId(Integer outletId, Integer categoryId);
    boolean existsByOutletIdAndCategoryId(Integer outletId, Integer categoryId);
    boolean existsByOutletCategoryId(Integer outletCategoryId);

    @Modifying
    @Transactional
    @Query("""
            UPDATE FmOutletCategory o
            SET o.isToggle = false
            WHERE o.outletCategoryId = :outletCategoryId
            """)
    void disableOutletCategory(Integer outletCategoryId);

    @Modifying
    @Transactional
    @Query("""
            UPDATE FmOutletCategory o
            SET o.isToggle = true
            WHERE o.outletCategoryId = :outletCategoryId
            """)
    void enableOutletCategory(
            Integer outletCategoryId);

    @Modifying
    @Query("""
           UPDATE FmOutletCategory o
           SET o.isActive = :status,
               o.isToggle = false
           WHERE o.outletCategoryId = :outletCategoryId
           """)
    void permanentlyCloseOutletCategory(
            @Param("outletCategoryId")
            Integer outletCategoryId,
            @Param("status")
            String status);
}
