package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoCommunity;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoCommunityRepository extends JpaRepository<CoCommunity,Integer> {

    Optional<CoCommunity> findByCommunityName(String communityName);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM jippy_customer_and_order.community " +
            "WHERE ST_Equals(CAST(community_boundary AS geometry), ST_SetSRID(CAST(:polygon AS geometry), " +
            "4326)))", nativeQuery = true)
    boolean existsBySpatialBoundary(Polygon polygon);

    @Query(value = "SELECT * FROM jippy_customer_and_order.community c WHERE " +
            "ST_Covers(c.community_boundary, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)) " +
            "LIMIT 1",
            nativeQuery = true)
    Optional<CoCommunity> findCustomerInCommunity(@Param("latitude") Double latitude, @Param("longitude") Double longitude);

    @Query(value = "SELECT * FROM jippy_customer_and_order.community c WHERE ST_Covers(c.community_boundary," +
            " ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)) AND c.community_id =:communityId LIMIT 1",
            nativeQuery = true)
    Optional<CoCommunity> checkCustomerAddressWithCommunity(
            @Param("latitude") Double latitude, @Param("longitude") Double longitude,
            @Param("communityId") Integer communityId);

    Optional<CoCommunity> findByCommunityId(Integer communityId);
}
