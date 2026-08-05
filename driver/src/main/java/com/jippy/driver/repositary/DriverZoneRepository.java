        package com.jippy.driver.repositary;


        import com.jippy.driver.entity.DriverZone;
        import org.locationtech.jts.geom.MultiPolygon;
        import org.locationtech.jts.geom.Polygon;
        import org.springframework.data.jpa.repository.JpaRepository;
        import org.springframework.data.jpa.repository.Query;
        import org.springframework.data.repository.query.Param;
        import org.springframework.stereotype.Repository;

        import java.util.List;
        import java.util.Map;
        import java.util.Optional;

        @Repository
        public interface DriverZoneRepository extends JpaRepository<DriverZone, Integer> {

            @Query(value = "SELECT EXISTS(SELECT 1 FROM jippy_driver.zones " +
                    "WHERE ST_Equals(CAST(boundary AS geometry), ST_SetSRID(CAST(:multiPolygon AS geometry), " +
                    "4326)))", nativeQuery = true)
            boolean existsBySpatialBoundary(@Param("multiPolygon") MultiPolygon multiPolygon);

            Optional<DriverZone> findByZoneName(String zoneName);

            // Find zone using latitude and longitude and
            // return the zone which contains the point defined by latitude and longitude
            @Query(value = """
                    SELECT *
                    FROM jippy_driver.zones
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


            @Query(value = "SELECT zone_id, zone_name, " +
                    "ST_AsGeoJSON(boundary) as boundary_json " +
                    "FROM jippy_driver.zones " ,
                    nativeQuery = true)
            List<Map<String,Object>> findZones();

            //Optional<DriverZone> findByZoneIdAndZoneType(Integer communityId, String communityType);

//            @Query(value = "SELECT * FROM jippy_driver.zones z WHERE ST_Covers(z.boundary, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)) LIMIT 1",
//                    nativeQuery = true)
//            Optional<DriverZone> findCustomerInCommunity(@Param("latitude") Double latitude, @Param("longitude") Double longitude);
//
//            @Query(value = "SELECT * FROM jippy_driver.zones z WHERE ST_Covers(z.boundary, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)) AND zone_id =:communityId LIMIT 1",
//                    nativeQuery = true)
//            Optional<DriverZone> checkCustomerAddressWithCommunity(
//                    @Param("latitude") Double latitude, @Param("longitude") Double longitude,
//                    @Param("communityId") Integer communityId);
        }
