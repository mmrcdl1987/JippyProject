package com.jippy.foodandmart.repository;

import com.jippy.foodandmart.entity.OutletUnavailability;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OutletUnavailabilityRepository extends JpaRepository<OutletUnavailability, Integer> {

    @Query("""
            SELECT o
            FROM OutletUnavailability o
            WHERE o.unavailabilityToDate <= :currentDateTime
            ORDER BY o.outletUnavailabilityId
            """)
    Page<OutletUnavailability> findExpiredUnavailability(@Param("currentDateTime") LocalDateTime currentDateTime, Pageable pageable);

    @Query("""
            SELECT o
            FROM OutletUnavailability o
            WHERE o.type = :type
            AND o.unavailabilityId = :unavailabilityId
            AND o.unavailabilityToDate >= :currentDateTime
            """)
    Optional<OutletUnavailability> findActiveUnavailability(@Param("type") String type,

                                                            @Param("unavailabilityId") Integer unavailabilityId,

                                                            @Param("currentDateTime") LocalDateTime currentDateTime);
}