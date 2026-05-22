package com.jippy.driver.repositary;

import com.jippy.driver.entity.DriverIncentiveSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DriverIncentiveSettingsRepository
        extends JpaRepository<DriverIncentiveSettings, Integer> {

    @Query(value = """
        SELECT *
        FROM jippy_driver.driver_incentive_settings
        ORDER BY orders_count ASC
        """, nativeQuery = true)
    List<DriverIncentiveSettings> findAllSlabs();
}