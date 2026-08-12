package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmProductPriceSettings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FmProductPriceSettingsRepository extends JpaRepository<FmProductPriceSettings, Integer> {

    /**
     * Find all settings for a specific outlet/product/variant.
     * <p>
     * Used when determining previous/base price during restoration.
     */
    @Query("""
            SELECT p
            FROM FmProductPriceSettings p
            WHERE p.outletId = :outletId
              AND p.productId = :productId
              AND (
                    (:productVariantId IS NULL
                        AND p.productVariantId IS NULL)
                    OR p.productVariantId = :productVariantId
                  )
            ORDER BY p.startDateTime DESC
            """)
    List<FmProductPriceSettings> findByProductAndOutletAndVariant(@Param("outletId") Integer outletId, @Param("productId") Integer productId, @Param("productVariantId") Integer productVariantId);

    /**
     * Find price settings that are due for application.
     * <p>
     * A setting is eligible once its start time has been reached.
     * Duplicate APPLY execution is prevented by the service/history check.
     */
    @Query("""
            SELECT p
            FROM FmProductPriceSettings p
            WHERE p.startDateTime <= :currentDateTime
              AND p.endDateTime > :currentDateTime
            ORDER BY p.startDateTime ASC
            """)
    List<FmProductPriceSettings> findActivePriceSettings(@Param("currentDateTime") LocalDateTime currentDateTime);

    /**
     * Find expired price settings.
     * <p>
     * Scheduler uses this to restore previous/base prices.
     */
    @Query("""
            SELECT p
            FROM FmProductPriceSettings p
            WHERE p.endDateTime < :currentDateTime
            ORDER BY p.endDateTime ASC
            """)
    List<FmProductPriceSettings> findExpiredPriceSettings(@Param("currentDateTime") LocalDateTime currentDateTime);

    /**
     * Check whether an overlapping price setting exists.
     * <p>
     * Used during CREATE / UPDATE validation.
     */
    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                FROM jippy_fm.product_price_settings ps
                WHERE ps.outlet_id = :outletId
                  AND ps.product_id = :productId
                  AND (
                        (:productVariantId IS NULL AND ps.product_variant_id IS NULL)
                        OR ps.product_variant_id = :productVariantId
                      )
                  AND ps.start_date_time < :endDateTime
                  AND ps.end_date_time > :startDateTime
                  AND (
                        :currentSettingId IS NULL
                        OR ps.product_price_settings_id <> :currentSettingId
                      )
            )
            """, nativeQuery = true)
    boolean existsOverlappingPriceSetting(@Param("outletId") Integer outletId, @Param("productId") Integer productId, @Param("productVariantId") Integer productVariantId, @Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime, @Param("currentSettingId") Integer currentSettingId);

    /**
     * Find all product price settings with pagination.
     * <p>
     * Used by GET /api/fm/product-price-settings.
     */
    Page<FmProductPriceSettings> findAll(Pageable pageable);

}