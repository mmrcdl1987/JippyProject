package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.GroupOrderInvitation;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface GroupOrderInvitationRepository extends JpaRepository<GroupOrderInvitation, Integer> {

    Optional<GroupOrderInvitation> findByInvitationCode(String invitationCode);

    @Query(value = "SELECT * FROM jippy_customer_and_order.group_orders_invitation WHERE STATUS =:groupOrderStatus  " +
            " AND host_customer_id =:hostCustomerId ", nativeQuery = true)
    Optional<GroupOrderInvitation> getActiveGroupOrderByCustomerId(@Param("hostCustomerId") Integer hostCustomerId,
            String groupOrderStatus);

    @Modifying
    @Transactional
    @Query(value = "UPDATE jippy_customer_and_order.group_orders_invitation SET status = 'ACTIVE', " +
            "updated_at = now() WHERE group_orders_invitation_id = :invitationId" +
            " AND status = 'CREATED'",nativeQuery = true)
    void updateStatusToActive(@Param("invitationId") Integer invitationId);


    @Modifying
    @Query(value = "UPDATE jippy_customer_and_order.group_orders_invitation  SET status = 'ACTIVE', updated_at = now() "+
        "WHERE status = 'CREATED' "+
          "AND order_type = 'COMMUNITY_ORDER' "+
          "AND EXISTS ("+
             "SELECT e FROM jippy_customer_and_order.community_events e "+
               " WHERE e.booking_start_date <= now()"+
          ")",nativeQuery = true)
    int activatePendingCommunityGroupOrders();

    Optional<GroupOrderInvitation> findByGroupOrdersInvitationId(Integer groupOrdersInvitationId);
}
