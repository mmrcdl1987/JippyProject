package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoDriverIncentiveSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CoDriverIncentiveSettingsRepository extends JpaRepository<CoDriverIncentiveSettings, Integer> {

    @Query(value = """
        SELECT *
        FROM jippy_customer_and_order.driver_incentive_settings
        ORDER BY orders_count ASC
        """, nativeQuery = true)
    List<CoDriverIncentiveSettings> findAllSlabs();
}