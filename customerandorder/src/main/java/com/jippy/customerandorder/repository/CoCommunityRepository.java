package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoCommunity;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoCommunityRepository extends JpaRepository<CoCommunity,Integer> {

    Optional<CoCommunity> findByCommunityName(String communityName);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM jippy_customer_and_order.community " +
            "WHERE ST_Equals(CAST(community_boundary AS geometry), ST_SetSRID(CAST(:polygon AS geometry), " +
            "4326)))", nativeQuery = true)
    boolean existsBySpatialBoundary(Polygon polygon);
}
