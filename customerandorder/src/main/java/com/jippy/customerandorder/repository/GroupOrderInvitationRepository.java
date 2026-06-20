package com.jippy.customerandorder.repository;

import com.jippy.customerandorder.entity.GroupOrderInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupOrderInvitationRepository extends JpaRepository<GroupOrderInvitation, Integer> {

    Optional<GroupOrderInvitation> findByInvitationCode(String invitationCode);
}
