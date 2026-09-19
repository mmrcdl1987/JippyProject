package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmProductVariantOption;
import com.jippy.foodandmart.projections.FmProductMerchantPriceProjection;
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
    List<FmProductVariantOption>
    findByProductIdAndIsActiveOrderByProductVariantOptionsIdAsc(
            Integer productId,
            String isActive);

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
      AND v.isActive = 'Y'
    ORDER BY v.productId, v.productVariantOptionsId
    """)
    List<FmProductVariantOption> findActiveVariantsForProducts(
            @Param("productIds") List<Integer> productIds
    );

    Optional<FmProductVariantOption>
    findByProductIdAndProductVariantGroupValuesIdAndIsActiveTrue(
            Integer productId,
            Integer productVariantGroupValuesId
    );

    @Query(value = """
        SELECT product_variant_options_id as productOrProductVariantOptionId,variant_price as merchantPrice,
        price_type as priceType FROM "jippy_fm"."product_variant_options" where product_variant_options_id in (:productVariantOptionIds)
    """,nativeQuery = true)
    List<FmProductMerchantPriceProjection> findByProductVariantOptionIds(@Param("productVariantOptionIds") List<Integer> productVariantOptionIds);

    @Query(value = """
            SELECT pvo.product_variant_options_id,pvgv.variant_name FROM "jippy_fm"."product_variant_options" pvo\s
            join "jippy_fm"."product_variant_group_values" pvgv on pvo.product_variant_group_values_id = pvgv.product_variant_group_values_id
            where product_variant_options_id in(:productVariantsIds)
            """,nativeQuery = true)
    List<Object[]> findVariantNamesByProductVariantIds(@Param("productVariantsIds") List<Integer> productVariantsIds);
}