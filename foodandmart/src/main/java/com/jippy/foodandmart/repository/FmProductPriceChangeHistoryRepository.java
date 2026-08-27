package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.FmProductPriceChangeHistory;
import com.jippy.foodandmart.enums.FmPriceHistoryOperationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FmProductPriceChangeHistoryRepository extends JpaRepository<FmProductPriceChangeHistory, Integer> {

    /**
     * Finds the APPLY history for a scheduled price period.
     * <p>
     * APPLY:a
     * original price -> scheduled price
     */
    @Query("""
            SELECT h
            FROM FmProductPriceChangeHistory h
            WHERE h.outletId = :outletId
              AND h.productId = :productId
              AND (
                    (:productVariantId IS NULL AND h.productVariantId IS NULL)
                    OR h.productVariantId = :productVariantId
                  )
              AND h.startDateTime = :startDateTime
              AND h.endDateTime = :endDateTime
              AND h.operationType = :operationType
            ORDER BY h.productPriceChangeHistoryId ASC
            """)
    Optional<FmProductPriceChangeHistory> findByOperationType(@Param("outletId") Integer outletId, @Param("productId") Integer productId, @Param("productVariantId") Integer productVariantId, @Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime, @Param("operationType") FmPriceHistoryOperationType operationType);

    /**
     * Checks whether a specific operation already exists.
     * <p>
     * Used to prevent scheduler duplicate execution.
     */
    @Query("""
            SELECT COUNT(h) > 0
            FROM FmProductPriceChangeHistory h
            WHERE h.outletId = :outletId
              AND h.productId = :productId
              AND (
                    (:productVariantId IS NULL AND h.productVariantId IS NULL)
                    OR h.productVariantId = :productVariantId
                  )
              AND h.startDateTime = :startDateTime
              AND h.endDateTime = :endDateTime
              AND h.operationType = :operationType
            """)
    boolean existsByOperationType(@Param("outletId") Integer outletId, @Param("productId") Integer productId, @Param("productVariantId") Integer productVariantId, @Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime, @Param("operationType") FmPriceHistoryOperationType operationType);

    @Query("""
            SELECT h
            FROM FmProductPriceChangeHistory h
            WHERE h.operationType = :operationType
              AND h.startDateTime <= :maxStartDateTime
              AND h.endDateTime >= :minEndDateTime
            """)
    List<FmProductPriceChangeHistory> findOperationHistoriesForScheduler(@Param("operationType") FmPriceHistoryOperationType operationType,

                                                                         @Param("minEndDateTime") LocalDateTime minEndDateTime,

                                                                         @Param("maxStartDateTime") LocalDateTime maxStartDateTime);

    @Query("""
            SELECT h
            FROM FmProductPriceChangeHistory h
            WHERE h.operationType = :operationType
              AND h.endDateTime < :currentDateTime
            """)
    List<FmProductPriceChangeHistory> findExpiredOperationHistoriesForScheduler(@Param("operationType") FmPriceHistoryOperationType operationType, @Param("currentDateTime") LocalDateTime currentDateTime);
}