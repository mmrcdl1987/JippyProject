package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.GroupOrderPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupOrderPaymentRepository extends JpaRepository<GroupOrderPayment,Integer> {

    @Query(value = " SELECT * FROM jippy_customer_and_order.group_order_payments WHERE " +
            "group_orders_invitation_id =:groupOrdersInvitationId ", nativeQuery = true)
    List<GroupOrderPayment> findAllByGroupInvitationId(@Param("groupOrdersInvitationId") Integer groupOrdersInvitationId);


    @Query(value = "SELECT * FROM jippy_customer_and_order.group_order_payments WHERE " +
            "group_orders_invitation_id =:groupOrdersInvitationId AND customer_id =:customerId ",nativeQuery = true)
    Optional<GroupOrderPayment> findByGroupInvitationIdAndCustomerId(@Param("groupOrdersInvitationId") Integer
            groupOrdersInvitationId, @Param("customerId") Integer customerId);
}
