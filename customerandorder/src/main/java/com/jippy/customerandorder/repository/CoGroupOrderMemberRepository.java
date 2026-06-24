package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoCustomer;
import com.jippy.customerandorder.entity.GroupOrderInvitation;
import com.jippy.customerandorder.entity.GroupOrderMembers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoGroupOrderMemberRepository extends JpaRepository<GroupOrderMembers, Integer> {

    @Query(value = "SELECT COUNT(gom) FROM jippy_customer_and_order.group_order_members gom WHERE " +
            "gom.group_orders_invitation_id = :groupOrdersInvitationId",nativeQuery = true)
    long getMaxMembersCount(Integer groupOrdersInvitationId);


    @Query(value = "SELECT * FROM jippy_customer_and_order.group_order_members gom WHERE " +
            "gom.group_orders_invitation_id = :groupOrdersInvitationId AND gom.customer_id = :customerId",nativeQuery = true)
    GroupOrderMembers validateGroupMemberAlreadyExists(Integer groupOrdersInvitationId, Integer customerId);

    boolean existsByGroupOrdersInvitationAndCustomer(GroupOrderInvitation groupOrderInvitation, CoCustomer customer);

    @Query(value = "SELECT DISTINCT ON (delivery_address_id) * " +
            " FROM jippy_customer_and_order.group_order_members "+
            " WHERE group_orders_invitation_id = :groupOrdersInvitationId " +
            " AND is_dropped = false", nativeQuery = true)
    List<GroupOrderMembers> getDeliveryAddressesByGroupOrderInvitationId(Integer groupOrdersInvitationId);

    GroupOrderMembers findByCustomer(CoCustomer customer);

    @Query(value = "SELECT * FROM jippy_customer_and_order.group_order_members  WHERE " +
            "group_orders_invitation_id = :goInvitationId AND order_placed = :orderPlaced " +
            "AND is_dropped=:isDropped",nativeQuery = true)
    List<GroupOrderMembers> findByGOInvitationIdAndOrderPlaced(@Param("goInvitationId") Integer goInvitationId,
            @Param("orderPlaced") Boolean orderPlaced, @Param("isDropped") Boolean isDropped);
}
