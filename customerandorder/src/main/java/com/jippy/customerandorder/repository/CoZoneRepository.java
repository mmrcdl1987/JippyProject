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

    @Query(value = "SELECT EXISTS(SELECT 1 FROM jippy_customer_and_order.zones WHERE ST_Equals(CAST(boundary AS geometry), ST_SetSRID(CAST(:polygon AS geometry), 4326)))",
    nativeQuery = true)
    boolean existsBySpatialBoundary(@Param("polygon") Polygon polygon);

    Optional<CoZone> findByZoneName(String zoneName);
}
