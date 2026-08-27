package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmProductVariantGroupValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FmProductVariantGroupValueRepository
        extends JpaRepository<FmProductVariantGroupValue, Integer> {

    /**
     * Get all active values for a group
     */
    List<FmProductVariantGroupValue> findByProductVariantGroupsIdAndIsActiveTrueOrderByVariantNameAsc(
            Integer productVariantGroupsId);

    /**
     * Get active value by id
     */
    Optional<FmProductVariantGroupValue> findByProductVariantGroupValuesIdAndIsActiveTrue(
            Integer productVariantGroupValuesId);

    /**
     * Get active value by id and group
     */
    Optional<FmProductVariantGroupValue> findByProductVariantGroupValuesIdAndProductVariantGroupsIdAndIsActiveTrue(
            Integer productVariantGroupValuesId,
            Integer productVariantGroupsId);

    /**
     * Duplicate validation while create
     */
    boolean existsByProductVariantGroupsIdAndVariantNameIgnoreCase(
            Integer productVariantGroupsId,
            String variantName);

    /**
     * Duplicate validation while update
     */
    boolean existsByProductVariantGroupsIdAndVariantNameIgnoreCaseAndProductVariantGroupValuesIdNot(
            Integer productVariantGroupsId,
            String variantName,
            Integer productVariantGroupValuesId);


    @Query("""
    SELECT v
    FROM FmProductVariantGroupValue v
    WHERE v.isActive = true
    ORDER BY v.productVariantGroupsId, v.variantName
    """)
    List<FmProductVariantGroupValue> findAllActiveValues();



}