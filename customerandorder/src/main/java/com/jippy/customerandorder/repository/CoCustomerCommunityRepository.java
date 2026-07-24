package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoCommunityEvents;
import com.jippy.customerandorder.entity.CoCustomerCommunities;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoCustomerCommunityRepository extends JpaRepository<CoCustomerCommunities,Integer> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO jippy_customer_and_order.customer_communities (" +
            "customer_id, community_id, created_at) " +
            "SELECT DISTINCT o.customer_id, :communityId, NOW() " +
            "FROM jippy_customer_and_order.orders o " +
            "JOIN jippy_customer_and_order.customer_delivery_addresses cda " +
            "  ON o.customer_delivery_address_id = cda.customer_address_id " +
            "WHERE o.order_status = 'DELIVERED' " +
            "  AND o.updated_at >= NOW() - INTERVAL '1 day' " +
            " AND ST_Covers(ST_GeomFromGeoJSON(:geoJsonBoundary)::geography, cda.location) " +
            "ON CONFLICT (customer_id, community_id) DO NOTHING",
            nativeQuery = true)
    void linkCustomersToCommunity(@Param("communityId") Integer communityId,
            @Param("geoJsonBoundary") String geoJsonBoundary);

    Optional<CoCustomerCommunities>  findByCustomerIdAndCommunityId(@Param("customerId") Integer customerId,
            @Param("communityId") Integer communityId);

    @Query(value = """
    SELECT ce.*  FROM jippy_customer_and_order.group_orders_invitation go
    JOIN jippy_customer_and_order.community_events ce
      ON ce.community_events_id = go.community_event_id
    JOIN jippy_customer_and_order.customer_communities cc
      ON cc.community_id = ce.community_id
    WHERE go.group_orders_invitation_id = :invitationId
      AND cc.customer_id = :customerId
""", nativeQuery = true)
    Optional<CoCommunityEvents> isCustomerInCommunityForInvitation(
            @Param("invitationId") Integer invitationId,
            @Param("customerId") Integer customerId
    );


}
