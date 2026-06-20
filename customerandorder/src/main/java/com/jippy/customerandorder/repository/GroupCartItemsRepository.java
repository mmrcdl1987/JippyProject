package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.CoCustomer;
import com.jippy.customerandorder.entity.GroupCartItems;
import com.jippy.customerandorder.entity.GroupOrderInvitation;
import com.jippy.customerandorder.projection.GroupOrderCartItemsProjection;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupCartItemsRepository extends JpaRepository<GroupCartItems, Integer> {

    @Query(value = "SELECT " +
            "  group_cart_item_id, " +
            "  customer_id, " +
            "  product_id, " +
            "  quantity, " +
            "  merchant_unit_price, " +
            "  online_unit_price, " +
            "  created_at, " +
            "  updated_at, " +
            "  created_by, " +
            "  updated_by, " +
            "  group_orders_invitation_id " +
            "FROM jippy_customer_and_order.group_cart_items " +
            "WHERE group_orders_invitation_id = :invitationId " +
            "AND customer_id = :customerId " +
            "AND product_id = :productId",
            nativeQuery = true)
    Optional<GroupCartItems> findByGroupOrdersAndCustomerAndProductId(
            @Param("invitationId") Integer invitationId,
            @Param("customerId") Integer customerId,
            @Param("productId") Integer productId
    );

    @Query(value = "SELECT gci.*,gom.customer_id,gom.delivery_address_id FROM jippy_customer_and_order.group_cart_items gci " +
            "inner join jippy_customer_and_order.group_order_members gom  " +
            "on gci.group_orders_invitation_id = gom.group_orders_invitation_id AND gci.customer_id = gom.customer_id " +
            "where gci.group_orders_invitation_id =:groupOrdersInvitationId " +
            "AND gom.is_dropped = FALSE ORDER BY " +
            "    gom.delivery_address_id, " +
            "    gci.customer_id ",
            nativeQuery = true)
    List<GroupOrderCartItemsProjection> findBygroupOrdersInvitationId(Integer groupOrdersInvitationId);


    @Query(value = "SELECT * FROM jippy_customer_and_order.group_cart_items  " +
            "where group_orders_invitation_id =:goInvitationId " +
            "AND customer_id IN (:customersAtAddress) " ,
            nativeQuery = true)
    List<GroupCartItems> findByGroupOrdersInvitationIdAndCustomerIdIn(Integer goInvitationId, List<Integer> customersAtAddress);
}
