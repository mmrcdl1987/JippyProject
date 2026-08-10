package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmProductVariantOption;
import feign.Param;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FmProductVariantOptionRepository
        extends JpaRepository<FmProductVariantOption, Integer> {

    /**
     * Fetch all active variant options of a product.
     */
    List<FmProductVariantOption> findByProductIdAndIsActiveTrueOrderByProductVariantOptionsIdAsc(
            Integer productId);

    /**
     * Fetch one active variant option.
     */
    Optional<FmProductVariantOption>
    findByProductVariantOptionsIdAndProductIdAndIsActiveTrue(
            Integer productVariantOptionsId,
            Integer productId);

    /**
     * Duplicate check while creating.
     */
    boolean existsByProductIdAndProductVariantGroupValuesIdAndIsActiveTrue(
            Integer productId,
            Integer productVariantGroupValuesId);

    /**
     * Duplicate check while updating.
     */
    boolean existsByProductIdAndProductVariantGroupValuesIdAndProductVariantOptionsIdNotAndIsActiveTrue(
            Integer productId,
            Integer productVariantGroupValuesId,
            Integer productVariantOptionsId);

    @Modifying
    @Transactional
    void deleteByProductId(
            Integer productId);



    List<FmProductVariantOption> findByProductIdOrderByProductVariantOptionsIdAsc(
            Integer productId);


    @Query("""
    SELECT v
    FROM FmProductVariantOption v
    WHERE v.productId IN :productIds
      AND v.isActive = true
    ORDER BY v.productId, v.productVariantOptionsId
    """)
    List<FmProductVariantOption> findActiveVariantsForProducts(
            @Param("productIds") List<Integer> productIds
    );
}