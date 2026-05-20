package com.jippy.driver.repositary;


import com.jippy.driver.entity.DriverZone;
import org.locationtech.jts.geom.Polygon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DriverZoneRepository extends JpaRepository<DriverZone, Integer> {

    @Query(value = "SELECT EXISTS(SELECT 1 FROM jippy_customer_and_order.zones WHERE ST_Equals(CAST(boundary AS geometry), ST_SetSRID(CAST(:polygon AS geometry), 4326)))", nativeQuery = true)
    boolean existsBySpatialBoundary(@Param("polygon") Polygon polygon);

    Optional<DriverZone> findByZoneName(String zoneName);

    // Find zone using latitude and longitude and
    // return the zone which contains the point defined by latitude and longitude
    @Query(value = """
            SELECT *
            FROM jippy_customer_and_order.zones
            WHERE ST_Intersects(
                boundary,
                ST_SetSRID(
                    ST_MakePoint(:longitude, :latitude),
                    4326
                )::geography
            )
            LIMIT 1
            """, nativeQuery = true)
    DriverZone findZoneByCoordinates(@Param("latitude") Double latitude, @Param("longitude") Double longitude);
}
