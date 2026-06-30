package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.GroupOrderInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
