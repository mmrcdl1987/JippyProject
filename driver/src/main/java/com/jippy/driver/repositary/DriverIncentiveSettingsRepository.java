package com.jippy.driver.repositary;

import com.jippy.driver.entity.DriverIncentiveSettings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DriverIncentiveSettingsRepository
        extends JpaRepository<DriverIncentiveSettings, Integer> {

    /**
     * Check whether an incentive slab already exists
     * for the given orders count.
     *
     * Used while creating a new incentive.
     */
    boolean existsByOrdersCount(Integer ordersCount);

    /**
     * Check duplicate orders count while updating.
     *
     * Excludes the current record ID so that
     * updating the same record with the same
     * orders count is allowed.
     */
    boolean existsByOrdersCountAndDriverIncentiveSettingsIdNot(
            Integer ordersCount,
            Integer driverIncentiveSettingsId);
    @Query(value = """
        SELECT *
        FROM jippy_driver.driver_incentive_settings
        ORDER BY orders_count ASC
        """, nativeQuery = true)
    List<DriverIncentiveSettings> findAllSlabs();

    Page<DriverIncentiveSettings> findAllByOrderByOrdersCountAsc(
            Pageable pageable
    );

}