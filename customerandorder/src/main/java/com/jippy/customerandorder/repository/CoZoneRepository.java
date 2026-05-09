package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoZone;
import org.locationtech.jts.geom.Polygon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoZoneRepository extends JpaRepository<CoZone, Integer> {

    @Query(value = "SELECT EXISTS(SELECT 1 FROM jippy_customer_and_order.zones WHERE ST_Equals(CAST(boundary AS geometry), ST_SetSRID(CAST(:polygon AS geometry), 4326)))", nativeQuery = true)
    boolean existsBySpatialBoundary(@Param("polygon") Polygon polygon);

    Optional<CoZone> findByZoneName(String zoneName);

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
    CoZone findZoneByCoordinates(@Param("latitude") Double latitude, @Param("longitude") Double longitude);
}
