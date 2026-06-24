package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.GroupOrderPriceBreakup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupOrderPriceBreakupRepository extends JpaRepository<GroupOrderPriceBreakup,Integer> {

    @Query(value = "SELECT * FROM jippy_customer_and_order.group_order_price_breakup WHERE " +
            "group_orders_invitation_id =:groupOrdersInvitationId AND customer_id =:customerId ",nativeQuery = true)
    Optional<GroupOrderPriceBreakup> findByGroupOrderInvitationIdAndCustomerId(@Param("groupOrdersInvitationId")
    Integer groupOrdersInvitationId, @Param("customerId") Integer customerId);


    @Query(value = "SELECT * FROM jippy_customer_and_order.group_order_price_breakup WHERE " +
            "group_orders_invitation_id =:goInvitationId AND customer_id IN (:customersAtAddress) ",nativeQuery = true)
    List<GroupOrderPriceBreakup> findByGroupOrderInvitationIdAndCustomerIds(@Param("goInvitationId") Integer goInvitationId,
            @Param("customersAtAddress") List<Integer> customersAtAddress);
}
